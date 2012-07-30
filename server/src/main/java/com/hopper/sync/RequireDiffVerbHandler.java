package com.hopper.sync;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateStorage;
import com.hopper.util.merkle.Difference;
import com.hopper.util.merkle.MerkleTree;
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

        MerkleTree<StateNode> targetTree = requireDiff.getTree();
        storage.getMerkleTree().loadHash();
        Difference<StateNode> difference = storage.getMerkleTree().difference(targetTree);
        difference.setClazz(StateNode.class);

        DiffResult result = new DiffResult();
        result.setMaxXid(storage.getMaxXid());
        result.setDifference(difference);

        Message reply = new Message();
        reply.setVerb(Verb.DIFF_RESULT);
        reply.setId(message.getId());
        reply.setBody(result);

        componentManager.getMessageService().responseOneway(reply);
    }
}
