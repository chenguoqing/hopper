package com.hopper.storage;

import com.hopper.GlobalConfiguration;
import com.hopper.common.lifecycle.LifecycleProxy;
import com.hopper.storage.merkle.MerkleTree;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AbstractStateStorage provides some common operations for all storage implementations.
 */
public abstract class AbstractStateStorage extends LifecycleProxy implements StateStorage {
    /**
     * Singleton
     */
    protected final GlobalConfiguration config = GlobalConfiguration.getInstance();
    /**
     * merkle tree reference
     */
    protected final MerkleTree tree;
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
        this.tree = new MerkleTree(config.getMerkleTreeDepth());
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
            StateNodeSnapshot nodeSnapshot = new StateNodeSnapshot(node);
            nodeSnapshot.serialize(out);
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
            config.getScheduleManager().schedule(purgeThread, config.getStateNodePurgeThreadPeriod());
        } else {
            throw new IllegalStateException("Purge has already running.");
        }
    }

    @Override
    public void removePurgeThread() {
        if (purgeRunning.compareAndSet(true, false)) {
            config.getScheduleManager().removeTask(purgeThread);
        }
    }

    @Override
    public MerkleTree getHashTree() {
        return tree;
    }

    @Override
    public long getMaxXid() {
        return maxXid.longValue();
    }
}
