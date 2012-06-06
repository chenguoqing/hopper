package com.hopper.storage.merkle;

/**
 * {@link Leaf} represents a merkle tree leaf
 */
public class Leaf extends InnerNode {

    private int keyHash;
    private int valueHash;

    public Leaf(HashRange range) {
        super(range);
    }

    /**
     * Leaf is the lowest level node, no left or right
     */
    @Override
    public MerkleNode getLeft() {
        return null;
    }

    /**
     * Leaf is the lowest level node, no left or right
     */
    @Override
    public MerkleNode getRight() {
        return null;
    }

    @Override
    public int getKeyHash() {
        return keyHash;
    }

    @Override
    public int getValueHash() {
        return valueHash;
    }

    @Override
    public void hash() {
        getRange().loadVersions();
        this.keyHash = getRange().keyHash();
        this.valueHash = getRange().valueHash();
    }
}
