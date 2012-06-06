package com.hopper.server.handler;

import com.hopper.GlobalConfiguration;
import com.hopper.MessageService;
import com.hopper.quorum.NoQuorumException;
import com.hopper.server.Endpoint;
import com.hopper.server.Verb;
import com.hopper.server.VerbHandler;
import com.hopper.server.thrift.ChannelBound;
import com.hopper.session.Message;
import com.hopper.storage.OwnerCASException;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateStorage;
import com.hopper.storage.StatusCASException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-31
 * Time: 下午2:19
 * To change this template use File | Settings | File Templates.
 */
public class MutationVerbHandler implements VerbHandler {

    private final GlobalConfiguration config = GlobalConfiguration.getInstance();

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
        }
    }

    private void create(Mutation.Create create) {
        MutationReply reply = new MutationReply();
        try {
            create(create.key, create.owner, create.initStatus, create.invalidateStatus);
            reply.setStatus(MutationReply.SUCCESS);
        } catch (NoQuorumException e) {
            reply.setStatus(MutationReply.NO_QUORUM);
        }

        Message response = new Message();
        response.setVerb(Verb.REPLY_MUTATION);
        response.setId(Message.nextId());

        // send response
        Endpoint endpoint = config.getEndpoint(ChannelBound.get().getRemoteAddress());
        MessageService.sendOneway(response, endpoint.serverId);
    }

    /**
     * Create a state with initial value, if the key is existed, return with success
     */
    public void create(String key, String owner, int initStatus, int invalidateStatus) {
        // Local modification first
        StateNode node = new StateNode(key, StateNode.TYPE_TEMP, StateNode.DEFAULT_STATUS,
                StateNode.DEFAULT_INVALIDATE_STATUS, config.getDefaultServer().getPaxos().getEpoch());
        storage.put(node);

        // Synchronizes the modification to majority nodes
        if (config.getDefaultServer().isLeader()) {
            Mutation mutation = new Mutation();
            mutation.addCreate(key, owner, initStatus, invalidateStatus);

            synchronizeMutationToQuorum(mutation);
        }
    }

    private void updateStatus(Mutation.UpdateStatus us) {
        MutationReply reply = new MutationReply();
        try {
            updateStatus(us.key, us.expectStatus, us.newStatus, us.owner, us.lease);
            reply.setStatus(MutationReply.SUCCESS);
        } catch (NoQuorumException e) {
            reply.setStatus(MutationReply.NO_QUORUM);
        } catch (StatusCASException e) {
        } catch (OwnerCASException e) {
        }

        Message response = new Message();
        response.setVerb(Verb.REPLY_MUTATION);
        response.setId(Message.nextId());

        // send response
        Endpoint endpoint = config.getEndpoint(ChannelBound.get().getRemoteAddress());
        MessageService.sendOneway(response, endpoint.serverId);
    }

    /**
     * Update the status bound with key with CAS condition
     */
    public void updateStatus(String key, int expectStatus, int newStatus, String owner, int lease) {
        StateNode node = getAndCreateNode(key);
        node.setStatus(expectStatus, newStatus, owner, lease);

        // Synchronizes the modification to majority nodes
        if (config.getDefaultServer().isLeader()) {
            Mutation mutation = new Mutation();
            mutation.addUpdateStatus(key, expectStatus, newStatus, owner, lease);

            synchronizeMutationToQuorum(mutation);
        }
    }

    /**
     * Update the lease property bound with key with CAS condition
     *
     * @param key
     * @param expectStatus
     * @param owner
     * @param lease
     */
    public void updateLease(String key, int expectStatus, String owner, int lease) {

    }

    /**
     * Watch the special status(add a listener)
     *
     * @param key
     * @param expectStatus
     */
    public void watch(String key, int expectStatus) {

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
}
