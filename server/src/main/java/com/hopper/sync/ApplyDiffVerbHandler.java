package com.hopper.sync;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.session.OutgoingSession;
import com.hopper.storage.merkle.Difference;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * The handler for processing apply diff result
 */
public class ApplyDiffVerbHandler implements VerbHandler {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {
        Difference difference = (Difference) message.getBody();

        DiffResult result = new DiffResult();
        result.setMaxXid(componentManager.getStateStorage().getMaxXid());
        result.setDifference(difference);

        componentManager.getDataSyncService().applyDiff(result);

        Message reply = new Message();
        reply.setVerb(Verb.APPLY_DIFF_RESULT);
        reply.setId(message.getId());
        reply.setBody(new byte[]{0});

        OutgoingSession session = componentManager.getSessionManager().getOutgoingSession(message.getSessionId());
        if (session != null) {
            session.sendOneway(reply);
        }
    }
}