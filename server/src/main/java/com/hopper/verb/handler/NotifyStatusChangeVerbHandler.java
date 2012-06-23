package com.hopper.verb.handler;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.ClientSession;
import com.hopper.session.Message;
import com.hopper.verb.VerbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The verb handler for notifying client for status change
 */
public class NotifyStatusChangeVerbHandler implements VerbHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotifyStatusChangeVerbHandler.class);

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {
        NotifyStatusChange notifyStatusChange = (NotifyStatusChange) message.getBody();

        ClientSession clientSession = componentManager.getSessionManager().getClientSession(notifyStatusChange.
                getClientSessionId());

        if (clientSession == null) {
            logger.warn("Failed to notify the status change to client session:{}, the session is not found",
                    notifyStatusChange.getClientSessionId());
            return;
        }

        try {
            clientSession.getNotify().statusChange(notifyStatusChange.getOldStatus(), notifyStatusChange.getNewStatus());
        } catch (Exception e) {
            logger.error("Failed to notify the status change to client session:{}",
                    notifyStatusChange.getClientSessionId(), e);
        }

    }
}
