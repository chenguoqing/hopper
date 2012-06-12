package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * Query the leader.
 */
public class QueryLeaderVerbHandler implements VerbHandler {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {

        Message reply = new Message();
        reply.setVerb(Verb.REPLY_QUERY_LEADER);
        reply.setId(message.getId());

        QueryLeader leader = new QueryLeader();
        leader.setEpoch(componentManager.getLeaderElection().getPaxos().getEpoch() - 1);
        leader.setLeader(componentManager.getDefaultServer().getLeader());

        reply.setBody(leader);

        componentManager.getMessageService().responseOneway(reply);
    }
}
