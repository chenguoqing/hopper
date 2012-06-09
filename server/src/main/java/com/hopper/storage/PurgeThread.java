package com.hopper.storage;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;

/**
 * The PurgeThread will remove "unused" state nodes, if some nodes that are not be modified by some times,
 * and there no some state listeners, they should be purged.
 */
public class PurgeThread implements Runnable {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    final StateStorage storage = componentManager.getStateStorage();

    @Override
    public void run() {
        for (StateNode node : storage.getStateNodes()) {
            if (node.shouldPurge()) {
                storage.remove(node.key);
            }
        }
    }
}
