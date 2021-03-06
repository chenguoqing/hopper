package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The handler for processing Prepare paxos message
 */
public class PrepareVerbHandler implements VerbHandler {

    private static final Logger logger = LoggerFactory.getLogger(PrepareVerbHandler.class);

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private Paxos paxos = componentManager.getLeaderElection().getPaxos();

    @Override
    public void doVerb(Message message) {

        Prepare prepare = (Prepare) message.getBody();

        // local epoch  is greater
        if (paxos.getEpoch() > prepare.getEpoch()) {
            logger.info("Reject the prepare because of lower epoch. Current Epoch:{},received Epoch:{}",
                    paxos.getEpoch(), prepare.getEpoch());
            sendPromise(message, Promise.REJECT_EPOCH);

            // local ballot is greater
        } else if (paxos.getRnd() >= prepare.getBallot()) {
            logger.info("Reject the prepare because of lower ballot. Current ballot:{},received ballot:{}",
                    paxos.getRnd(), prepare.getBallot());
            sendPromise(message, Promise.REJECT_BALLOT);

            // target's greater
        } else {
            if (paxos.getEpoch() < prepare.getEpoch()) {
                paxos.updateInstance(prepare.getEpoch());
            }

            if (paxos.getRnd() < prepare.getBallot()) {
                paxos.setRnd(prepare.getBallot());
            }

            sendPromise(message, Promise.PROMISE);
        }
    }

    private void sendPromise(Message message, int status) {

        Message reply = message.createResponse(Verb.PAXOS_PROMISE);

        Promise promise = new Promise();

        promise.setStatus(status);
        promise.setEpoch(paxos.getEpoch());
        promise.setRnd(paxos.getRnd());
        promise.setVrnd(paxos.getVrnd());
        promise.setVval(paxos.getVval());

        reply.setBody(promise);

        componentManager.getMessageService().responseOneway(reply);
    }
}
