package com.hopper.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Holds all data on SkipList(tree) for space saved purpose.
 */
public class TreeStorage extends AbstractStateStorage {

    private final Map<String, StateNode> tree = new ConcurrentSkipListMap<String, StateNode>();

    public TreeStorage() {
        super();
    }

    @Override
    protected StateNode doPut(StateNode node) {
        return tree.put(node.key, node);
    }

    @Override
    protected StateNode doRemove(String key) {
        return tree.remove(key);
    }

    @Override
    public StateNode get(String key) {
        return tree.get(key);
    }

    @Override
    public Collection<StateNode> getStateNodes() {
        return new ArrayList<StateNode>(tree.values());
    }

    @Override
    public StateStorage snapshot() {
        try {
            // Deep copy
            return (StateStorage) this.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
