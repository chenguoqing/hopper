package com.hopper.storage;

import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.util.merkle.MerkleTree;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AbstractStateStorage provides some common operations for all storage implementations.
 */
public abstract class AbstractStateStorage extends LifecycleProxy implements StateStorage {
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    /**
     * merkle tree reference
     */
    protected final MerkleTree<StateNode> tree;
    /**
     * Monotonically increasing xid
     */
    private final AtomicLong maxXid = new AtomicLong();
    /**
     * Purge thread
     */
    private final PurgeThread purgeThread = new PurgeThread();

    private final AtomicBoolean purgeRunning = new AtomicBoolean();

    /**
     * Constructor for initializing the merkle tree uniquely
     */
    public AbstractStateStorage() {
        this.tree = new MerkleTree<StateNode>(componentManager.getGlobalConfiguration().getMerkleTreeDepth(),
                StateNode.class);
        this.tree.setObjectFactory(componentManager.getStateStorage());
    }

    @Override
    public StateNode put(StateNode node) {
        maxXid.incrementAndGet();
        StateNode r = doPut(node);

        // update the merkle tree range
        tree.put(node);

        return r;
    }

    protected abstract StateNode doPut(StateNode node);

    @Override
    public StateNode remove(String key) {
        maxXid.incrementAndGet();
        StateNode old = doRemove(key);

        // update the merkle tree range
        tree.remove(key);

        return old;
    }

    @Override
    public StateNode find(String key) {
        return get(key);
    }

    protected abstract StateNode doRemove(String key);

    /**
     * Duping all state nodes to location
     */
    @Override
    public void dump(String location) throws IOException {
        StateStorage snapshot = this.snapshot();

        Collection<StateNode> nodes = snapshot.getStateNodes();

        File file = new File(location);
        if (file.exists()) {
            throw new IOException("File already exists.");
        }

        FileOutputStream fout = new FileOutputStream(file);
        DataOutput out = new DataOutputStream(fout);

        for (StateNode node : nodes) {
            node.serialize(out);
        }

        fout.flush();
        fout.close();
    }

    @Override
    public void executeInvalidateTask() {
        for (StateNode node : getStateNodes()) {
            node.executeInvalidateTask();
        }
    }

    @Override
    public void removeInvalidateTask() {
        for (StateNode node : getStateNodes()) {
            node.removeInvalidateTask();
        }
    }

    @Override
    public void enablePurgeThread() {
        if (purgeRunning.compareAndSet(false, true)) {
            componentManager.getScheduleManager().schedule(purgeThread, componentManager.getGlobalConfiguration()
                    .getStateNodePurgeExpire());
        } else {
            throw new IllegalStateException("Purge has already running.");
        }
    }

    @Override
    public void removePurgeThread() {
        if (purgeRunning.compareAndSet(true, false)) {
            componentManager.getScheduleManager().removeTask(purgeThread);
        }
    }

    @Override
    public MerkleTree<StateNode> getHashTree() {
        return tree;
    }

    @Override
    public long getMaxXid() {
        return maxXid.longValue();
    }
}
