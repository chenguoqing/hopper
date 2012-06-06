package com.hopper.storage.merkle;

import com.hopper.session.Serializer;
import com.hopper.storage.StateNode;

import java.util.List;

/**
 * {@link MerkleNode} represents a Merkle-Tree. The tradition Merkle-Tree makes the data hash as the node hash,
 * if data value changed, the hash position will change, so it doesn't find the differences about data modifications.
 * <p/>
 * {@link MerkleNode} eliminates the cases through maintaining two hashes-key hash and value hash-
 * .key hash monitors the keys modifications
 * (put/remove), and value hash monitors value modifications for same key.
 */
public interface MerkleNode extends Serializer {
    /**
     * Return the key hash
     */
    int getKeyHash();

    /**
     * Return the value hash
     */
    int getValueHash();

    /**
     * Lazy-calculate the node's hash value, this caused to recursion invocation.
     */
    void hash();

    /**
     * Return left node
     */
    MerkleNode getLeft();

    /**
     * Return right node
     */
    MerkleNode getRight();

    /**
     * Retrieve bound {@link HashRange}
     */
    HashRange getRange();

    /**
     * Retrieve all state nodes which hash has been mapped to range.
     */
    List<StateNode> getStateNodes();
}
