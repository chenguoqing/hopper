package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query the leader.
 */
public class QueryLeaderVerbHandler implements VerbHandler {
    private static final Logger logger = LoggerFactory.getLogger(QueryLeaderVerbHandler.class);
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {

        logger.info("Received query leader request {}", message);

        Message reply = new Message();
        reply.setVerb(Verb.REPLY_QUERY_LEADER);
        reply.setId(message.getId());

        QueryLeader leader = new QueryLeader();
        leader.setEpoch(componentManager.getLeaderElection().getPaxos().getEpoch());
        leader.setLeader(componentManager.getDefaultServer().getLeader());

        reply.setBody(leader);

        logger.info("Response query leader request {}", reply);
        componentManager.getMessageService().responseOneway(reply);
    }
}
