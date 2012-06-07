package com.hopper.server.handler;

import com.hopper.GlobalConfiguration;
import com.hopper.MessageService;
import com.hopper.quorum.NoQuorumException;
import com.hopper.server.*;
import com.hopper.server.thrift.ChannelBound;
import com.hopper.session.Message;
import com.hopper.storage.OwnerNoMatchException;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateStorage;
import com.hopper.storage.StatusNoMatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MutationVerbHandler processes state related operations
 */
public class MutationVerbHandler implements VerbHandler {
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(MutationVerbHandler.class);

    private final GlobalConfiguration config = GlobalConfiguration.getInstance();

    private final Server server = config.getDefaultServer();

    private final StateStorage storage = config.getDefaultServer().getStorage();

    @Override
    public void doVerb(Message message) {

        Mutation mutation = (Mutation) message.getBody();

        if (mutation.getOp() == Mutation.OP.CREATE) {
            Mutation.Create create = (Mutation.Create) mutation.getEntity();
            create(create);
        } else if (mutation.getOp() == Mutation.OP.UPDATE_STATUS) {
            Mutation.UpdateStatus us = (Mutation.UpdateStatus) mutation.getEntity();
            updateStatus(us);
        } else if (mutation.getOp() == Mutation.OP.UPDATE_LEASE) {
            Mutation.UpdateLease ul = (Mutation.UpdateLease) mutation.getEntity();
            updateLease(ul);
        } else if (mutation.getOp() == Mutation.OP.WATCH) {
            Mutation.Watch watch = (Mutation.Watch) mutation.getEntity();
            watch(watch);
        }
    }

    private void create(Mutation.Create create) {
        MutationReply reply = new MutationReply();
        try {
            create(create.key, create.owner, create.initStatus, create.invalidateStatus);
            // reply mutation request only the operation success
            replyMutation(MutationReply.SUCCESS);
        } catch (NoQuorumException e) {
            logger.warn("No quorum nodes are alive, drops the create request.");
        } catch (ServiceUnavailableException e) {
            logger.warn("The server is unavailable, drops the create request.");
        }
    }

    /**
     * Create a state with initial value, if the key is existed, return with success
     */
    public void create(String key, String owner, int initStatus, int invalidateStatus) throws
            ServiceUnavailableException, NoQuorumException {
        // check server state
        server.assertServiceAvailable();

        // Local modification first
        StateNode node = new StateNode(key, StateNode.TYPE_TEMP, StateNode.DEFAULT_STATUS,
                StateNode.DEFAULT_INVALIDATE_STATUS, config.getDefaultServer().getPaxos().getEpoch());
        storage.put(node);

        if (config.getDefaultServer().isLeader()) {
            Mutation mutation = new Mutation();
            mutation.addCreate(key, owner, initStatus, invalidateStatus);

            // Synchronizes the modification to majority nodes
            synchronizeMutationToQuorum(mutation);
        }
    }

    private void updateStatus(Mutation.UpdateStatus us) {
        try {
            updateStatus(us.key, us.expectStatus, us.newStatus, us.owner, us.lease);
            replyMutation(MutationReply.SUCCESS);
        } catch (NoQuorumException e) {
            logger.warn("No quorum nodes are alive, drops the create request.");
        } catch (ServiceUnavailableException e) {
            logger.warn("The server is unavailable, drops the create request.");
        } catch (StatusNoMatchException e) {
            replyMutation(MutationReply.STATUS_CAS);
        } catch (OwnerNoMatchException e) {
            replyMutation(MutationReply.OWNER_CAS);
        }
    }

    /**
     * Update the status bound with key with CAS condition
     */
    public void updateStatus(String key, int expectStatus, int newStatus, String owner,
                             int lease) throws ServiceUnavailableException, StatusNoMatchException,
            OwnerNoMatchException {

        // check server state
        server.assertServiceAvailable();

        StateNode node = getAndCreateNode(key);
        node.setStatus(expectStatus, newStatus, owner, lease);

        // Synchronizes the modification to majority nodes
        if (config.getDefaultServer().isLeader()) {
            Mutation mutation = new Mutation();
            mutation.addUpdateStatus(key, expectStatus, newStatus, owner, lease);

            synchronizeMutationToQuorum(mutation);
        }
    }

    private void updateLease(Mutation.UpdateLease ul) {

        try {
            updateLease(ul.key, ul.expectStatus, ul.owner, ul.lease);
            replyMutation(MutationReply.SUCCESS);
        } catch (ServiceUnavailableException e) {
            logger.warn("No quorum nodes are alive, drops the updateLease request.");
        } catch (NoQuorumException e) {
            logger.warn("The server is unavailable, drops the updateLease request.");
        } catch (StatusNoMatchException e) {
            replyMutation(MutationReply.STATUS_CAS);
        } catch (OwnerNoMatchException e) {
            replyMutation(MutationReply.OWNER_CAS);
        }
    }

    /**
     * Update the lease property bound with key with CAS condition
     */
    public void updateLease(String key, int expectStatus, String owner,
                            int lease) throws ServiceUnavailableException, StatusNoMatchException,
            OwnerNoMatchException {
        // check server state
        server.assertServiceAvailable();

        StateNode node = getAndCreateNode(key);
        node.expandLease(expectStatus, owner, lease);

        // Synchronizes the modification to majority nodes
        if (config.getDefaultServer().isLeader()) {
            Mutation mutation = new Mutation();
            mutation.addUpdateLease(key, expectStatus, owner, lease);

            synchronizeMutationToQuorum(mutation);
        }
    }

    private void watch(Mutation.Watch watch) {

    }

    /**
     * Watch the special status(add a listener)
     *
     * @param key
     * @param expectStatus
     */
    public void watch(String key, int expectStatus) {
        // check server state
        server.assertServiceAvailable();


    }

    private StateNode getAndCreateNode(String key) {
        StateNode node = storage.get(key);

        if (node == null) {
            synchronized (key) {
                node = storage.get(key);
                node = new StateNode(key, config.getDefaultServer().getPaxos().getEpoch());
                storage.put(node);
            }
        }

        return node;
    }

    private void synchronizeMutationToQuorum(Mutation mutation) {
        Message message = new Message();
        message.setVerb(Verb.MUTATION);
        message.setId(Message.nextId());
        message.setBody(mutation);

        List<Message> replies = MessageService.sendMessageToQuorum(message, 0);

        // Failed to synchronize the modification to quorum
        if (replies.size() < config.getQuorumSize() - 1) {
            throw new NoQuorumException();
        }
    }

    /**
     * Reply the mutation result to sender
     */
    private void replyMutation(int replyStatus) {
        MutationReply reply = new MutationReply();
        reply.setStatus(replyStatus);
        Message response = new Message();
        response.setVerb(Verb.REPLY_MUTATION);
        response.setId(Message.nextId());
        response.setBody(reply);

        // send response
        Endpoint endpoint = config.getEndpoint(ChannelBound.get().getRemoteAddress());
        MessageService.sendOneway(response, endpoint.serverId);
    }
}
