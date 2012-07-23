package com.hopper.utils.merkle;

/**
 * The interface represents a object finder
 */
public interface MerkleObjectReferenceable {
    /**
     * Retrieve a object by a unique key
     */
    MerkleObjectRef find(String key);
}
