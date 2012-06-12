package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.session.OutgoingSession;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

public class PrepareVerbHandler implements VerbHandler {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private Paxos paxos = componentManager.getLeaderElection().getPaxos();

    @Override
    public void doVerb(Message message) {
        Prepare prepare = (Prepare) message.getBody();

        int localEpoch = paxos.getEpoch();
        int localRnd = paxos.getRnd();

        // local epoch or ballot is greater, reject the prepare
        if (localEpoch > prepare.getEpoch() || localRnd > prepare.getBallot()) {

            int status = 0;
            if (paxos.getEpoch() > prepare.getEpoch()) {
                status = (Promise.REJECT_EPOCH);
            } else {
                status = (Promise.REJECT_BALLOT);
            }

            sendPromise(message.getId(), message.getSessionId(), status);

        } else {

            // target's epoch is greaten than local
            if (localEpoch < prepare.getEpoch()) {
                paxos.closeInstance();
            } else if (localRnd < prepare.getBallot()) {
                paxos.setRnd(prepare.getBallot());
            }

            sendPromise(message.getId(), message.getSessionId(), Promise.PROMISE);
        }
    }

    private void sendPromise(int messageId, String sessionId, int status) {

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

        // Retrieve the OutgoingSession
        OutgoingSession session = componentManager.getSessionManager().getOutgoingSession(sessionId);

        if (session != null) {
            session.sendOneway(reply);
        }
    }
}
