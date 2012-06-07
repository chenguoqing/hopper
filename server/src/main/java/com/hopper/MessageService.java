package com.hopper;

import com.hopper.future.LatchFuture;
import com.hopper.future.LatchFutureListener;
import com.hopper.server.Endpoint;
import com.hopper.server.Verb;
import com.hopper.server.handler.VerbMappings;
import com.hopper.session.ClientSession;
import com.hopper.session.Message;
import com.hopper.session.OutgoingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * MessageService acts as a facade for sending message, it forwards all messages to Session
 */
public class MessageService {
    public static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final GlobalConfiguration config = GlobalConfiguration.getInstance();

    /**
     * Send *LEARN* message to all endpoint asynchronously
     */
    public static void sendLearnMessage(final Message message) {

        for (Endpoint endpoint : config.getConfigedEndpoints()) {

            // If the endpoint is local, executes the message directly
            if (config.isLocalEndpoint(endpoint)) {
                VerbMappings.getVerbHandler(Verb.PAXOS_LEARN).doVerb(message);
            }

            try {
                OutgoingSession session = config.getSessionManager().createLocalOutgoingSession(endpoint);
                session.sendOneway(message);
            } catch (Exception e) {
                logger.error("Failed to connect to " + endpoint, e);
            }
        }
    }

    /**
     * Send the message to all endpoints and waiting the response.
     *
     * @param message     the message to send
     * @param waitingMode waiting mode 0 - waiting for quorum nodes replies, 1 -  for all nodes
     */
    public static List<Message> sendMessageToQuorum(final Message message, int waitingMode) {

        List<Future<Message>> futures = new ArrayList<Future<Message>>();

        int waitCount = waitingMode == 0 ? config.getQuorumSize() : config.getConfigedEndpoints().length;

        final CountDownLatch latch = new CountDownLatch(waitCount);

        final List<Message> replies = new ArrayList<Message>();

        for (Endpoint endpoint : config.getConfigedEndpoints()) {

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
                                replies.add((Message) future.get());
                            } catch (Exception e) {
                                logger.error("Failed to get result ", e);
                            }
                        }
                    }
                });

                latch.await(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.error("Failed to connect to " + endpoint, e);
            }
        }
        return replies;
    }

    /**
     * Send message to <code>targetServerId</code> with ony-war mode
     */

    public static void sendOneway(Message message, int destServerId) {
        Endpoint endpoint = config.getEndpoint(destServerId);
        try {
            OutgoingSession session = config.getSessionManager().createLocalOutgoingSession(endpoint);
            session.sendOneway(message);
        } catch (Exception e) {
            logger.error("Failed to send message.", e);
        }
    }

    /**
     * Send message to  <code>targetServerId</code> waits until the operation complete.
     */
    public static void sendOnwayUntilComplete(Message message, int destServerId) throws Exception {
        Endpoint endpoint = config.getEndpoint(destServerId);
        OutgoingSession session = config.getSessionManager().createLocalOutgoingSession(endpoint);
        session.sendOnwayUntilComplete(message);
    }

    /**
     * Send   message to  <code>targetServerId</code> and return the future instance
     */
    public static LatchFuture<Message> send(Message message, int destServerId) throws Exception {
        Endpoint endpoint = config.getEndpoint(destServerId);
        OutgoingSession session = config.getSessionManager().createLocalOutgoingSession(endpoint);
        return session.send(message);
    }

    public static void notifyStatusChange(String clientSessionId, int oldStatus, int newStatus) {
        ClientSession clientSession = config.getSessionManager().getClientSession(clientSessionId);

        if (clientSession != null) {
            try {
                clientSession.getNotify().statusChange(oldStatus, newStatus);
            } catch (Exception e) {
                //nothing
            }
        } else {
            OutgoingSession outgoingSession = config.getSessionManager()
                    .getOutgoingSessionByMultiplexerSessionId(clientSessionId);
            if (outgoingSession != null) {
                Message message = new Message();
                message.setVerb(Verb.NOTIFY_STATUS_CHANGE);
                message.setId(Message.nextId());
                message.setSessionId(clientSessionId);

                outgoingSession.sendOneway(message);
            }
        }
    }
}
