package com.hopper.sync;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.session.OutgoingSession;
import com.hopper.storage.StateStorage;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-16
 * Time: 下午5:45
 * To change this template use File | Settings | File Templates.
 */
public class RequireTreeVerbhandler implements VerbHandler {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private final StateStorage storage = componentManager.getStateStorage();

    @Override
    public void doVerb(Message message) {

        Message reply = new Message();
        reply.setVerb(Verb.TREE_RESULT);
        reply.setId(message.getId());

        reply.setBody(storage.getHashTree());

        OutgoingSession session = componentManager.getSessionManager().getOutgoingSession(message.getSessionId());
        if (session != null) {
            session.sendOneway(reply);
        }
    }
}
