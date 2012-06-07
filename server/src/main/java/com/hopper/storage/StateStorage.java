package com.hopper.storage;

import com.hopper.lifecycle.Lifecycle;
import com.hopper.storage.merkle.MerkleTree;

import java.io.IOException;
import java.util.Collection;

/**
 * {@link StateStorage} represents a storage interface for StateNode object, all data will be saved only on memory
 * without persistent. There are two storage modes: hash and tree.
 * <p/>
 * <b>hash</b> mode:
 * Data is holden on hash map(ConcurrentHashMap), it provides significant performance for read/remove operations,
 * but will take up more space;
 * <p/>
 * <b>tree</b> mode:
 * Data is holden on tree(ConcurrentSkipListMap), it can provide logN performance but can take up less space.
 * <p/>
 * MerkleTree is the underlying component of StateStorage, that splits all date into some hash ranges by data key's
 * hash, it only maintains some key references not the data self.
 */
public interface StateStorage extends Lifecycle {

    /**
     * Add state node
     *
     * @param node state node
     * @return true:success,false:failed
     */
    StateNode put(StateNode node);

    /**
     * Retrieve the state node identified by <code>key</code>
     */
    StateNode get(String key);

    /**
     * Remove state node
     */
    StateNode remove(String key);

    /**
     * Dump the memory data to disk for debug purpose
     */
    void dump(String location) throws IOException;

    /**
     * Retrieve the root of hash tree
     */
    MerkleTree getHashTree();

    /**
     * Retrieve all saved state nodes
     */
    Collection<StateNode> getStateNodes();

    /**
     * Generate a snapshot for current {@link StateStorage} instance.
     */
    StateStorage snapshot();

    /**
     * Execute the invalidate task
     */
    void executeInvalidateTask();

    /**
     * Remove the invalidate task
     */
    void removeInvalidateTask();

    /**
     * Enable the purge thread
     */
    void enablePurgeThread();

    /**
     * Remove the purge thread
     */
    void removePurgeThread();

    /**
     * Return the maximum transaction id
     */
    long getMaxXid();
}
