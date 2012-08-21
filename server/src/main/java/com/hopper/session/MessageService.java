package com.hopper.session;

import com.hopper.GlobalConfiguration;
import com.hopper.future.LatchFuture;
import com.hopper.future.LatchFutureListener;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Endpoint;
import com.hopper.thrift.ChannelBound;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbMappings;
import com.hopper.verb.handler.NotifyStatusChange;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * MessageService acts as a facade for sending message, it forwards all messages to Session
 */
public class MessageService {
    public static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    /**
     * Waiting mode: waiting for all nodes response
     */
    public static final int WAITING_MODE_QUORUM = 0;
    /**
     * Waiting mode: waiting for quorum nodes response
     */
    public static final int WAITING_MODE_ALL = 1;

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private final GlobalConfiguration config = componentManager.getGlobalConfiguration();

    /**
     * Send *LEARN* message to all endpoint asynchronously
     */
    public void sendLearnMessage(final Message message) {

        for (Endpoint endpoint : config.getGroupEndpoints()) {

            // If the endpoint is local, executes the message directly
            if (config.isLocalEndpoint(endpoint)) {
                VerbMappings.getVerbHandler(Verb.PAXOS_LEARN).doVerb(message);
                continue;
            }

            componentManager.getMessageService().sendOneway(message, endpoint.serverId);
        }
    }

    /**
     * Send the message to all endpoints and waiting the response.
     *
     * @param message     the message to send
     * @param waitingMode see above
     */
    public List<Message> sendMessageToQuorum(Message message, int waitingMode) {

        int waitCount = waitingMode == WAITING_MODE_QUORUM ? config.getQuorumSize() : config.getGroupEndpoints().length;

        final CountDownLatch latch = new CountDownLatch(waitCount);

        final List<Message> replies = new ArrayList<Message>();

        for (Endpoint endpoint : config.getGroupEndpoints()) {

            // ignoring the local endpoint
            if (config.isLocalEndpoint(endpoint)) {
                latch.countDown();
                continue;
            }

            try {
                LatchFuture<Message> future = send(message, endpoint.serverId);
                future.setLatch(latch);

                future.addListener(new LatchFutureListener<Message>() {
                    @Override
                    public void complete(LatchFuture<Message> future) {
                        if (future.isSuccess()) {
                            try {
                                replies.add(future.get());
                            } catch (Exception e) {
                                logger.error("Failed to get result ", e);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                logger.debug("Failed to send message to {} ", endpoint, e);
            }
        }

        try {
            latch.await(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.debug("Time out for waiting the return result.");
        }

        return replies;
    }

    /**
     * Send message to <code>destServerId</code> with ony-way mode
     */
    public void sendOneway(Message message, int destServerId) {
        Endpoint endpoint = config.getEndpoint(destServerId);

        int messageId = Message.nextId();
        message.setId(messageId);

        logger.debug("Send one-way message {} to {}", message, endpoint);

        try {
            OutgoingSession session = componentManager.getSessionManager().createOutgoingSession(endpoint);
            session.sendOneway(message);
        } catch (Exception e) {
            logger.debug("Failed to send one-way message {} to {}.", new Object[]{message, endpoint, e});
        }
    }

    /**
     * Response the message with the received message id to sender(the sender will be retrieved from {@link
     * ChannelBound)
     */
    public void responseOneway(Message message) {
        Channel channel = ChannelBound.get();

        if (channel == null) {
            throw new IllegalStateException("Not bound channel for current thread.");
        }

        // send response
        SocketAddress socketAddress = channel.getRemoteAddress();
        Endpoint endpoint = componentManager.getGlobalConfiguration().getEndpoint(socketAddress);

        if (endpoint == null) {
            throw new IllegalStateException("Not found endpoint for address:" + socketAddress);
        }

        logger.debug("Responses message {} to {}", message, endpoint);

        try {
            OutgoingSession session = componentManager.getSessionManager().createOutgoingSession(endpoint);
            session.sendOneway(message);
        } catch (Exception e) {
            logger.debug("Failed to response message {} to {}.", new Object[]{message, endpoint, e});
        }
    }

    /**
     * Send message to  <code>targetServerId</code> waits until the operation complete.
     */
    public void sendOnewayUntilComplete(Message message, int destServerId) throws Exception {
        Endpoint endpoint = config.getEndpoint(destServerId);

        int messageId = Message.nextId();
        message.setId(messageId);

        logger.debug("Send one-way until message {} to {}", message, endpoint);

        OutgoingSession session = componentManager.getSessionManager().createOutgoingSession(endpoint);
        session.sendOnewayUntilComplete(message);
    }

    /**
     * Send   message to  <code>destServerId</code> and return the future instance. The method will generate a new
     * message id for preventing duplication
     */
    public LatchFuture<Message> send(Message message, int destServerId) throws Exception {
        Endpoint endpoint = config.getEndpoint(destServerId);

        int messageId = Message.nextId();
        message.setId(messageId);

        logger.debug("Send message {} to {}", message, endpoint);

        OutgoingSession session = componentManager.getSessionManager().createOutgoingSession(endpoint);
        return session.send(message);
    }

    public void notifyStatusChange(String clientSessionId, int oldStatus, int newStatus) {
        ClientSession clientSession = componentManager.getSessionManager().getClientSession(clientSessionId);

        if (clientSession != null) {
            try {
                clientSession.getNotify().statusChange(oldStatus, newStatus);
            } catch (Exception e) {
                //nothing
            }
        } else {
            OutgoingSession outgoingSession = componentManager.getSessionManager()
                    .getOutgoingSessionByMultiplexerSessionId(clientSessionId);
            if (outgoingSession != null) {
                Message message = new Message();
                message.setVerb(Verb.NOTIFY_STATUS_CHANGE);
                message.setId(Message.nextId());

                NotifyStatusChange notifyStatusChange = new NotifyStatusChange();
                notifyStatusChange.setClientSessionId(clientSessionId);
                notifyStatusChange.setOldStatus(oldStatus);
                notifyStatusChange.setNewStatus(newStatus);

                message.setBody(notifyStatusChange);

                outgoingSession.sendOneway(message);
            }
        }
    }
}
