package com.hopper.sync;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.session.OutgoingSession;
import com.hopper.storage.StateStorage;
import com.hopper.storage.merkle.Difference;
import com.hopper.storage.merkle.MerkleTree;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * The message handler for processing the REQUIRE_DIFF request, it differences the local merkle tree and remote tree,
 * and replies the comparison result to remote..
 */
public class RequireDiffVerbHandler implements VerbHandler {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private final StateStorage storage = componentManager.getStateStorage();

    @Override
    public void doVerb(Message message) {
        RequireDiff requireDiff = (RequireDiff) message.getBody();

        MerkleTree targetTree = requireDiff.getTree();
        storage.getHashTree().loadHash();
        Difference difference = storage.getHashTree().difference(targetTree);

        DiffResult result = new DiffResult();
        result.setMaxXid(storage.getMaxXid());
        result.setDifference(difference);

        Message reply = new Message();
        reply.setVerb(Verb.DIFF_RESULT);
        reply.setId(message.getId());
        reply.setBody(result);

        OutgoingSession session = componentManager.getSessionManager().getOutgoingSession(message.getSessionId());
        if (session != null) {
            session.sendOneway(reply);
        }
    }
}