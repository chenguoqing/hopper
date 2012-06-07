package com.hopper.server.sync;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Verb;
import com.hopper.server.VerbHandler;
import com.hopper.session.Message;
import com.hopper.session.OutgoingSession;
import com.hopper.storage.merkle.Difference;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-16
 * Time: 下午5:49
 * To change this template use File | Settings | File Templates.
 */
public class ApplyDiffVerbHandler implements VerbHandler {
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();

    @Override
    public void doVerb(Message message) {
        Difference difference = (Difference) message.getBody();

        DiffResult result = new DiffResult();
        result.setMaxXid(config.getDefaultServer().getStorage().getMaxXid());
        result.setDifference(difference);

        config.getDataSyncService().applyDiff(result);

        Message reply = new Message();
        reply.setVerb(Verb.APPLY_DIFF_RESULT);
        reply.setId(message.getId());
        reply.setBody(new byte[]{0});

        OutgoingSession session = config.getSessionManager().getOutgoingServerSession(message.getSessionId());
        if (session != null) {
            session.sendOneway(reply);
        }
    }
}
