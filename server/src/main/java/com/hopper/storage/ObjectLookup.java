package com.hopper.storage;

/**
 * The interface represents a object finder
 */
public interface ObjectLookup<T> {
    /**
     * Retrieve a object by a unique key
     */
    T lookup(String key);
}
