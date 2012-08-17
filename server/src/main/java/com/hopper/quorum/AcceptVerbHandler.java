package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcceptVerbHandler implements VerbHandler {
    private static final Logger logger = LoggerFactory.getLogger(AcceptVerbHandler.class);
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private Paxos paxos = componentManager.getLeaderElection().getPaxos();

    @Override
    public void doVerb(Message message) {
        logger.info("Received the accept request {}", message);
        Accept accept = (Accept) message.getBody();

        if (accept.getEpoch() < paxos.getEpoch()) {
            logger.info("Reject the accept request, because of lower epoch. Current Epoch:{},received Epoch:{}",
                    paxos.getEpoch(), accept.getEpoch());
            sendResponse(message.getId(), Accepted.REJECT_EPOCH);
        } else if (accept.getBallot() < paxos.getRnd()) {
            logger.info("Reject the accept request because of lower ballot. Current Ballot:{},received Ballot:{}",
                    paxos.getRnd(), accept.getBallot());
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
            logger.info("Accepted the accept request");
            sendResponse(message.getId(), Accepted.ACCEPTED);
        }
    }

    private void sendResponse(int messageId, int status) {
        Message reply = new Message();
        reply.setVerb(Verb.PAXOS_ACCEPTED);

        Accepted accepted = new Accepted();
        accepted.setEpoch(paxos.getEpoch());
        accepted.setRnd(paxos.getRnd());
        accepted.setStatus(status);

        reply.setBody(accepted);

        logger.info("Send accepted response {}", reply);
        componentManager.getMessageService().responseOneway(reply);
    }
}
