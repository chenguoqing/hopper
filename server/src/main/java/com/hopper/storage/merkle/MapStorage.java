package com.hopper.storage.merkle;

import com.hopper.storage.AbstractStateStorage;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Holds all data on hash map for performance purpose.
 */
public class MapStorage extends AbstractStateStorage {
    private final ConcurrentMap<String, StateNode> map = new ConcurrentHashMap<String, StateNode>();

    @Override
    protected StateNode doPut(StateNode node) {
        return map.putIfAbsent(node.key, node);
    }

    @Override
    protected StateNode doRemove(String key) {
        return map.remove(key);
    }

    @Override
    public StateNode get(String key) {
        return map.get(key);
    }

    @Override
    public Collection<StateNode> getStateNodes() {
        return new ArrayList<StateNode>(map.values());
    }

    /**
     * Only shallow copy
     */
    @Override
    public StateStorage snapshot() {
        MapStorage snapshot = new MapStorage();
        // shallow copy
        snapshot.map.putAll(map);
        return snapshot;
    }
}
