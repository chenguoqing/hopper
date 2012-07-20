package com.hopper.storage.merkle;

import com.hopper.storage.KeyVersionObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of MerkleNode, it represents a inner node(not leaf)
 */
public class InnerNode<T extends KeyVersionObject> implements MerkleNode<T> {
    /**
     * Left sub tree
     */
    private MerkleNode<T> left;
    /**
     * Right sub tree
     */
    private MerkleNode<T> right;
    /**
     * Associated range
     */
    private HashRange<T> range;

    private int keyHash;
    private int valueHash;

    public InnerNode(HashRange<T> range) {
        this.range = range;
        range.setMerkleNode(this);
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
        Integer leftKeyHash = null;
        Integer leftValueHash = null;

        if (left != null) {
            left.hash();
            leftKeyHash = left.getKeyHash();
            leftValueHash = left.getValueHash();
        }

        Integer rightKeyHash = null;
        Integer rightValueHash = null;

        if (right != null) {
            right.hash();
            rightKeyHash = right.getKeyHash();
            rightValueHash = right.getValueHash();
        }

        if (leftKeyHash != null && rightKeyHash != null) {
            this.keyHash = leftKeyHash ^ rightKeyHash;
        } else if (leftKeyHash == null && rightKeyHash == null) {
            this.keyHash = 0;
        } else {
            this.keyHash = leftKeyHash != null ? leftKeyHash : rightKeyHash;
        }

        if (leftValueHash != null && rightValueHash != null) {
            this.valueHash = leftValueHash ^ rightValueHash;
        } else if (leftValueHash == null && rightValueHash == null) {
            this.valueHash = 0;
        } else {
            this.valueHash = leftValueHash != null ? leftValueHash : rightValueHash;
        }
    }

    public void setLeft(MerkleNode<T> left) {
        this.left = left;
    }

    @Override
    public MerkleNode<T> getLeft() {
        return left;
    }

    public void setRight(MerkleNode<T> right) {
        this.right = right;
    }

    @Override
    public MerkleNode<T> getRight() {
        return right;
    }

    @Override
    public HashRange<T> getRange() {
        return range;
    }

    @Override
    public List<T> getStateNodes() {
        List<T> nodeList = new ArrayList<T>();

        getBoundObjects(this, nodeList);
        return nodeList;
    }

    private void getBoundObjects(MerkleNode<T> node, final List<T> nodeList) {
        if (node instanceof Leaf) {
            nodeList.addAll(range.getBoundObjects());
        }

        if (left != null) {
            getBoundObjects(left, nodeList);
        }

        if (right != null) {
            getBoundObjects(right, nodeList);
        }
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        // Serialize the range
        range.serialize(out);

        out.writeByte(getChildFlag(left));
        out.writeByte(getChildFlag(right));

        if (left != null) {
            left.serialize(out);
        }

        if (right != null) {
            right.serialize(out);
        }
    }

    private byte getChildFlag(MerkleNode child) {
        return child == null ? 0 : (child instanceof InnerNode) ? (byte) 1 : (byte) 2;
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.range = new HashRange();
        range.deserialize(in);
        byte leftFlag = in.readByte();
        byte rightFlag = in.readByte();

        this.left = createNodeByFlag(leftFlag, range);
        this.right = createNodeByFlag(rightFlag, range);

        if (left != null) {
            left.deserialize(in);
        }

        if (right != null) {
            right.deserialize(in);
        }
    }

    private MerkleNode createNodeByFlag(byte flag, HashRange range) {

        if (flag == 1) {
            return new InnerNode(range);
        }

        if (flag == 2) {
            return new Leaf(range);
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MerkleNode)) {
            return false;
        }

        MerkleNode target = (MerkleNode) obj;

        return getKeyHash() == target.getKeyHash() && this.getValueHash() == target.getValueHash();
    }
}
