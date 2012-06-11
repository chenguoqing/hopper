package com.hopper.verb.handler;

import com.hopper.quorum.Paxos;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.VerbHandler;

public class AcceptVerbHandler implements VerbHandler {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private Paxos paxos = componentManager.getLeaderElection().getPaxos();

    @Override
    public void doVerb(Message message) {

        Accept accpet = (Accept) message.getBody();

        if (accpet.getBallot() < paxos.getRnd()) {
           //TODO:
        }
    }
}
