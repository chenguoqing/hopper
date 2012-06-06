package com.hopper.storage;

import com.hopper.GlobalConfiguration;

/**
 * The PurgeThread will remove "unused" state nodes, if some nodes that are not be modified by some times,
 * and there no some state listeners, they should be purged.
 */
public class PurgeThread implements Runnable {
    final GlobalConfiguration config = GlobalConfiguration.getInstance();
    final StateStorage storage = config.getDefaultServer().getStorage();

    @Override
    public void run() {
        for (StateNode node : storage.getStateNodes()) {
            if (node.shouldPurge()) {
                storage.remove(node.key);
            }
        }
    }
}
