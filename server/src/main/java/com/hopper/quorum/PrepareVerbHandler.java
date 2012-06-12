package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * The handler for processing Prepare paxos message
 */
public class PrepareVerbHandler implements VerbHandler {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private Paxos paxos = componentManager.getLeaderElection().getPaxos();

    @Override
    public void doVerb(Message message) {
        Prepare prepare = (Prepare) message.getBody();

        final int localEpoch = paxos.getEpoch();
        final int localRnd = paxos.getRnd();

        // local epoch or ballot is greater, reject the prepare
        if (localEpoch > prepare.getEpoch()) {
            sendPromise(message.getId(), Promise.REJECT_EPOCH);
        } else if (localRnd > prepare.getBallot()) {
            sendPromise(message.getId(), Promise.REJECT_BALLOT);
        } else {
            // target's epoch is greater than local
            if (localEpoch < prepare.getEpoch()) {
                paxos.closeInstance();
            } else if (localRnd < prepare.getBallot()) {
                paxos.setRnd(prepare.getBallot());
            }
            sendPromise(message.getId(), Promise.PROMISE);
        }
    }

    private void sendPromise(int messageId, int status) {

        Message reply = new Message();
        reply.setId(messageId);
        reply.setVerb(Verb.PAXOS_PROMISE);

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
