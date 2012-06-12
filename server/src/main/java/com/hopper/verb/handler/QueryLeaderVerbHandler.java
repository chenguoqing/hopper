package com.hopper.verb.handler;

import com.hopper.quorum.LeaderElection;
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
        reply.setId(Message.nextId());

    }
}
