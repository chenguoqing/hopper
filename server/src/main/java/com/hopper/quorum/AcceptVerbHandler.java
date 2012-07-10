package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

public class AcceptVerbHandler implements VerbHandler {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private Paxos paxos = componentManager.getLeaderElection().getPaxos();

    @Override
    public void doVerb(Message message) {

        Accept accept = (Accept) message.getBody();

        if (accept.getEpoch() < paxos.getEpoch()) {
            sendResponse(message.getId(), Accepted.REJECT_EPOCH);
        } else if (accept.getBallot() < paxos.getRnd()) {
            sendResponse(message.getId(), Accepted.REJECT_BALLOT);
        } else {
            paxos.paxosLock.writeLock().lock();
            try {
                paxos.setRnd(accept.getBallot());
                paxos.setVrnd(accept.getBallot());
                paxos.setVval(accept.getVval());
            } finally {
                paxos.paxosLock.writeLock().unlock();
            }
            sendResponse(message.getId(), Accepted.ACCEPTED);
        }
    }

    private void sendResponse(int messageId, int status) {
        Message reply = new Message();
        reply.setId(messageId);
        reply.setVerb(Verb.PAXOS_ACCEPTED);

        Accepted accepted = new Accepted();
        accepted.setEpoch(paxos.getEpoch());
        accepted.setRnd(paxos.getRnd());
        accepted.setStatus(status);

        reply.setBody(accepted);

        componentManager.getMessageService().responseOneway(reply);
    }
}
