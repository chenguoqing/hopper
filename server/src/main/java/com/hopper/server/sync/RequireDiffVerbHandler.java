package com.hopper.server.sync;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Verb;
import com.hopper.server.VerbHandler;
import com.hopper.session.Message;
import com.hopper.session.OutgoingServerSession;
import com.hopper.storage.StateStorage;
import com.hopper.storage.merkle.Difference;
import com.hopper.storage.merkle.MerkleTree;

/**
 * The message handler for processing the REQUIRE_DIFF request, it differences the local merkle tree and remote tree,
 * and replies the comparison result to remote..
 */
public class RequireDiffVerbHandler implements VerbHandler {

    private final GlobalConfiguration config = GlobalConfiguration.getInstance();
    private final StateStorage storage = config.getDefaultServer().getStorage();

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

        OutgoingServerSession session = config.getSessionManager().getOutgoingServerSession(message.getSessionId());
        if (session != null) {
            session.sendOneway(reply);
        }
    }
}
