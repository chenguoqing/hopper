package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Server;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processing test leader request
 */
public class TestLeaderVerbHandler implements VerbHandler {

    private static final Logger logger = LoggerFactory.getLogger(TestLeaderVerbHandler.class);
    private ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {
        logger.info("Received the test leader request {}", message);
        Server server = componentManager.getDefaultServer();
        int leader = -1;
        if (server.isKnownLeader()) {
            leader = server.getLeader();
        }

        Message reply = new Message();
        reply.setVerb(Verb.TEST_LEADER_RESULT);
        reply.setId(message.getId());

        reply.setBody(new byte[]{leader > 0 ? 0 : (byte) 1});

        logger.info("Response the test leader request {}", reply);
        componentManager.getMessageService().responseOneway(reply);
    }
}
