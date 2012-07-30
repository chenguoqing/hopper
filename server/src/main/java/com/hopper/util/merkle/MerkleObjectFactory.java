package com.hopper.util.merkle;

/**
 * The interface represents a object finder
 */
public interface MerkleObjectFactory<T extends MerkleObjectRef> {
    /**
     * Retrieve a object by a unique key
     */
    T find(String key);
}
