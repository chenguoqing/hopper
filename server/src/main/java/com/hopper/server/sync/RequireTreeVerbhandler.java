package com.hopper.server.sync;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Verb;
import com.hopper.server.VerbHandler;
import com.hopper.session.Message;
import com.hopper.session.OutgoingServerSession;
import com.hopper.storage.StateStorage;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-16
 * Time: 下午5:45
 * To change this template use File | Settings | File Templates.
 */
public class RequireTreeVerbhandler implements VerbHandler {
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();
    private final StateStorage storage = config.getDefaultServer().getStorage();

    @Override
    public void doVerb(Message message) {

        Message reply = new Message();
        reply.setVerb(Verb.TREE_RESULT);
        reply.setId(message.getId());

        reply.setBody(storage.getHashTree());

        OutgoingServerSession session = config.getSessionManager().getOutgoingServerSession(message.getSessionId());
        if (session != null) {
            session.sendOneway(reply);
        }
    }
}
