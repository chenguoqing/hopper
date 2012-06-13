package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Server;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * Processing test leader request
 */
public class TestLeaderVerbHandler implements VerbHandler {

    private ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {
        Server server = componentManager.getDefaultServer();
        int leader = -1;
        if (server.isKnownLeader()) {
            leader = server.getLeader();
        }

        Message reply = new Message();
        reply.setVerb(Verb.TEST_LEADER_RESULT);
        reply.setId(message.getId());

        reply.setBody(new byte[]{leader > 0 ? 0 : (byte) 1});

        componentManager.getMessageService().responseOneway(reply);
    }
}
