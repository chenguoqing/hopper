package com.hopper.thrift;

import com.hopper.GlobalConfiguration;
import com.hopper.quorum.NoQuorumException;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Server;
import com.hopper.server.ServiceUnavailableException;
import com.hopper.session.ClientConnection;
import com.hopper.session.ClientSession;
import com.hopper.session.Message;
import com.hopper.session.SessionIdGenerator;
import com.hopper.storage.NotMatchOwnerException;
import com.hopper.storage.NotMatchStatusException;
import com.hopper.storage.StateStorage;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbMappings;
import com.hopper.verb.handler.BatchMultiplexerSessions;
import com.hopper.verb.handler.Mutation;
import com.hopper.verb.handler.MutationReply;
import com.hopper.verb.handler.MutationVerbHandler;
import org.apache.thrift.TException;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The implementation of HopperService.Iface,HopperService.Iface is a facade of client interface.
 */
public class HopperServiceImpl implements HopperService.Iface {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HopperServiceImpl.class);
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
    private final GlobalConfiguration config = componentManager.getGlobalConfiguration();
    /**
     * Singleton
     */
    private final Server server = componentManager.getDefaultServer();

    private int responseSize = 4096;

    /**
     * Singleton
     */
    private final StateStorage storage = componentManager.getStateStorage();

    @Override
    public String login(String userName, String password) throws RetryException, AuthenticationException, TException {

        assertServiceRunning();

        ClientSession session = componentManager.getSessionManager().getClientSession(ChannelBound.get());

        if (session != null) {
            return session.getId();
        }

        String sessionId = SessionIdGenerator.generateSessionId();

        // Register a new session by a new session id
        registerClientSession(sessionId);

        // build client notify(bidirectional)
        BidDirectNotify notify = new BidDirectNotify(session);
        session.setNotify(notify);

        return sessionId;
    }

    @Override
    public void reLogin(String sessionId) throws RetryException, TException {
        assertServiceRunning();

        // Register a client session with a existed session id
        registerClientSession(sessionId);
    }

    /**
     * Register a internal client session by id
     */
    private void registerClientSession(String sessionId) throws RetryException {
        ClientSession session = componentManager.getSessionManager().getClientSession(sessionId);

        boolean isNew = session == null;

        if (isNew) {
            ClientConnection conn = new ClientConnection(ChannelBound.get());
            session = new ClientSession();
            session.setId(sessionId);
            session.setConnection(conn);
            conn.setSession(session);
        }

        // register the user on leader
        if (server.isFollower()) {
            Message message = new Message();
            message.setVerb(Verb.BOUND_MULTIPLEXER_SESSION);
            message.setId(Message.nextId());

            BatchMultiplexerSessions batch = new BatchMultiplexerSessions();
            batch.add(sessionId);

            message.setBody(batch);

            try {
                componentManager.getMessageService().sendOnewayUntilComplete(message, server.getLeader());
            } catch (Exception e) {
                throw new RetryException(config.getRetryPeriod());
            }
        }

        if (isNew) {
            // Add session to session manager
            session.getSessionManager().addClientSession(session);
        }
    }

    @Override
    public void logout(String sessionId) throws TException {
        ClientSession session = componentManager.getSessionManager().getClientSession(sessionId);

        if (session != null) {
            session.close();
        }

        if (server.isFollower()) {
            Message message = new Message();
            message.setVerb(Verb.UNBOUND_MULTIPLEXER_SESSION);
            message.setId(Message.nextId());

            BatchMultiplexerSessions batch = new BatchMultiplexerSessions();
            batch.add(sessionId);
            message.setBody(batch);

            componentManager.getMessageService().sendOneway(message, server.getLeader());
        }
    }

    @Override
    public void ping() throws TException {
        ClientSession clientSession = componentManager.getSessionManager().getClientSession(ChannelBound.get());
        clientSession.heartBeat();
    }

    @Override
    public void create(final String key, final String owner, final int initStatus, final int invalidateStatus) throws
            RetryException, TException {

        assertServiceRunning();
        try {
            executeMutation(new MutationTask() {
                @Override
                public void mutation() {
                    MutationVerbHandler mutationVerbHandler = (MutationVerbHandler) VerbMappings.getVerbHandler(Verb
                            .MUTATION);
                    mutationVerbHandler.create(key, owner, initStatus, invalidateStatus);
                }

                @Override
                public Mutation getMutation() {
                    Mutation mutation = new Mutation();
                    mutation.addCreate(key, owner, initStatus, invalidateStatus);
                    return mutation;
                }
            });
        } catch (CASException e) {
            // nothing
        }
    }

    @Override
    public void updateStatus(final String key, final int expectStatus, final int newStatus, final String owner,
                             final int lease) throws RetryException, CASException, TException {
        assertServiceRunning();

        executeMutation(new MutationTask() {
            @Override
            public void mutation() {
                MutationVerbHandler mutationVerbHandler = (MutationVerbHandler) VerbMappings.getVerbHandler(Verb
                        .MUTATION);
                mutationVerbHandler.updateStatus(key, expectStatus, newStatus, owner, lease);
            }

            @Override
            public Mutation getMutation() {
                Mutation mutation = new Mutation();
                mutation.addUpdateStatus(key, expectStatus, newStatus, owner, lease);
                return mutation;
            }
        });
    }

    @Override
    public void expandLease(final String key, final int expectStatus, final String owner,
                            final int lease) throws RetryException, CASException, NoStateNodeException, TException {

        assertServiceRunning();

        if (storage.get(key) == null) {
            throw new NoStateNodeException(key);
        }

        final Mutation mutation = new Mutation();
        mutation.addUpdateLease(key, expectStatus, owner, lease);

        executeMutation(new MutationTask() {
            @Override
            public void mutation() {
                MutationVerbHandler mutationVerbHandler = (MutationVerbHandler) VerbMappings.getVerbHandler(Verb
                        .MUTATION);
                mutationVerbHandler.updateLease(key, expectStatus, owner, lease);
            }

            @Override
            public Mutation getMutation() {
                Mutation mutation = new Mutation();
                mutation.addUpdateLease(key, expectStatus, owner, lease);
                return mutation;
            }
        });
    }

    @Override
    public void watch(final String key, final int expectStatus) throws RetryException, CASException,
            NoStateNodeException, TException {
        assertServiceRunning();

        if (storage.get(key) == null) {
            throw new NoStateNodeException(key);
        }

        Channel channel = ChannelBound.get();
        final ClientSession session = componentManager.getSessionManager().getClientSession(channel);

        executeMutation(new MutationTask() {
            @Override
            public void mutation() {
                MutationVerbHandler mutationVerbHandler = (MutationVerbHandler) VerbMappings.getVerbHandler(Verb
                        .MUTATION);
                mutationVerbHandler.watch(session.getId(), key, expectStatus);
            }

            @Override
            public Mutation getMutation() {
                Mutation mutation = new Mutation();
                mutation.addWatch(session.getId(), key, expectStatus);
                return mutation;
            }
        });
    }

    @Override
    public void statusChange(int oldStatus, int newStatus) throws TException {
        throw new TException("The method should not be invoked by client.");
    }

    /**
     * Check server running state
     */
    private void assertServiceRunning() throws RetryException {
        try {
            server.assertServiceAvailable();
        } catch (ServiceUnavailableException e) {
            throw new RetryException(config.getRetryPeriod());
        }
    }

    private Message makeMutationRequest(Mutation mutation) {
        Message message = new Message();
        message.setVerb(Verb.MUTATION);
        message.setId(Message.nextId());
        message.setBody(mutation);
        return message;
    }

    /**
     * This method is a template method for executing all mutations, the concrete mutation execution will be delegate
     * to {@link MutationTask}, other common processing will be done on there.
     */
    private void executeMutation(MutationTask task) throws RetryException, CASException {
        if (server.isLeader()) {
            MutationVerbHandler mutationVerbHandler = (MutationVerbHandler) VerbMappings.getVerbHandler(Verb.MUTATION);
            try {
                task.mutation();
            } catch (NoQuorumException e) {
                throw new RetryException(config.getRetryPeriod());
            } catch (NotMatchStatusException e) {
                throw new CASException(1);
            } catch (NotMatchOwnerException e) {
                throw new CASException(2);
            }
        } else {
            Mutation mutation = task.getMutation();

            Message message = makeMutationRequest(mutation);
            MutationReply mutationReply = null;
            try {
                // If current node is follower, transfers the request to leader
                Future<Message> future = componentManager.getMessageService().send(message, server.getLeader());
                Message reply = future.get(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
                mutationReply = (MutationReply) reply.getBody();
            } catch (Exception e) {
                throw new RetryException(config.getRetryPeriod());
            }

            if (mutationReply.getStatus() == MutationReply.STATUS_CAS) {
                throw new CASException(1);
            }

            if (mutationReply.getStatus() == MutationReply.OWNER_CAS) {
                throw new CASException(2);
            }
        }
    }

    /**
     * MutationTask supports a abstract for all mutation executions.
     */
    private interface MutationTask {
        /**
         * Execute the concrete mutation
         */
        void mutation();

        /**
         * Retrieve the bound Mutation instance
         */
        Mutation getMutation();
    }
}
