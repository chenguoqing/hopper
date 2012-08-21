package com.hopper.sync;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.storage.StateStorage;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * The verb handler for making local merkle tree
 */
public class RequireTreeVerbhandler implements VerbHandler {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private final StateStorage storage = componentManager.getStateStorage();

    @Override
    public void doVerb(Message message) {

        Message reply = message.createResponse(Verb.TREE_RESULT);

        reply.setBody(storage.getMerkleTree());

        componentManager.getMessageService().responseOneway(reply);
    }
}
