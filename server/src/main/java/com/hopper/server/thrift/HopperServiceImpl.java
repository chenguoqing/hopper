package com.hopper.server.thrift;

import com.hopper.GlobalConfiguration;
import com.hopper.MessageService;
import com.hopper.common.lifecycle.Lifecycle;
import com.hopper.quorum.NoQuorumException;
import com.hopper.server.Server;
import com.hopper.server.Verb;
import com.hopper.server.handler.Mutation;
import com.hopper.server.handler.MutationVerbHandler;
import com.hopper.server.handler.VerbMappings;
import com.hopper.session.ClientConnection;
import com.hopper.session.ClientSession;
import com.hopper.session.Message;
import com.hopper.session.SessionIdGenerator;
import com.hopper.storage.StateStorage;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-30
 * Time: 下午4:00
 * To change this template use File | Settings | File Templates.
 */
public class HopperServiceImpl implements HopperService.Iface {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HopperServiceImpl.class);

    /**
     * Singleton
     */
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();

    private final Server server = config.getDefaultServer();

    private int responseSize = 4096;

    /**
     * Singleton
     */
    private final StateStorage storage = config.getDefaultServer().getStorage();

    @Override
    public String login(String userName, String password) throws RetryException, AuthenticationException, TException {

        checkRunning();

        ClientSession session = config.getSessionManager().getClientSession(ChannelBound.get());

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
        checkRunning();

        // Register a client session with a existed session id
        registerClientSession(sessionId);
    }

    /**
     * Register a internal client session by id
     */
    private void registerClientSession(String sessionId) throws RetryException {
        ClientSession session = config.getSessionManager().getClientSession(sessionId);

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
            message.setBody(sessionId.getBytes());

            try {
                MessageService.sendOnwayUntilComplete(message, server.getLeader());
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
        ClientSession session = config.getSessionManager().getClientSession(sessionId);

        if (session != null) {
            session.close();
        }

        if (server.isFollower()) {
            Message message = new Message();
            message.setVerb(Verb.UNBOUND_MULTIPLEXER_SESSION);
            message.setId(Message.nextId());
            message.setSessionId(sessionId);

            MessageService.sendOneway(message, server.getLeader());
        }
    }

    @Override
    public void ping() throws TException {
        ClientSession clientSession = config.getSessionManager().getClientSession(ChannelBound.get());
        clientSession.heartBeat();
    }

    @Override
    public void create(String key, String owner, int initStatus, int invalidateStatus) throws RetryException,
            TException {

        checkRunning();

        Object[] logArgs = new Object[]{key, owner, initStatus, invalidateStatus, null};

        // Leader should process the request directly
        if (server.isLeader()) {

            MutationVerbHandler mutationVerbHandler = (MutationVerbHandler) VerbMappings.getVerbHandler(Verb.MUTATION);

            try {
                // delegate the operation to verb handler
                mutationVerbHandler.create(key, owner, initStatus, invalidateStatus);
            } catch (NoQuorumException e) {
                logger.error("Failed to create node[key:{0},owner:{1},initStatus:{2}," +
                        "invalidateStatus:{3}" + "] without enough nodes are live.", logArgs);

                throw new RetryException(config.getRetryPeriod());
            }
            // Follower node should transfer the request to leader
        } else {
            Mutation mutation = new Mutation();
            mutation.addCreate(key, owner, initStatus, invalidateStatus);

            Message message = makeMutationRequest(mutation);

            try {
                Future<Message> reply = MessageService.send(message, server.getLeader());
                reply.get(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logArgs[4] = e;
                logger.error("Failed to create node[key:{0},owner:{1},initStatus:{2}," +
                        "invalidateStatus:{3}" + "] without enough nodes are live.", logArgs);

                throw new RetryException(config.getRetryPeriod());
            }
        }
    }

    @Override
    public void updateStatus(String key, int expectStatus, int newStatus, String owner,
                             int lease) throws RetryException, StateCASException, TException {
        checkRunning();

        if (server.isLeader()) {
            MutationVerbHandler mutationVerbHandler = (MutationVerbHandler) VerbMappings.getVerbHandler(Verb.MUTATION);
            mutationVerbHandler.updateStatus(key, expectStatus, newStatus, owner, lease);
        } else {

        }
    }

    @Override
    public void updateLease(String key, int expectStatus, String owner, int lease) throws RetryException,
            StateCASException, TException {
        checkRunning();
    }

    @Override
    public void watch(String key, int expectStatus) throws RetryException, ExpectStatusException, TException {
        checkRunning();
    }

    @Override
    public void statusChange(int oldStatus, int newStatus) throws TException {
        throw new TException("The method should not be invoked by client.");
    }

    /**
     * Check server running state
     */
    private void checkRunning() throws RetryException {
        Server.ElectionState state = server.getElectionState();

        if (state == Server.ElectionState.LOOKING || state == Server.ElectionState.SYNC || server.getState() !=
                Lifecycle.LifecycleState.RUNNING) {
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
}
