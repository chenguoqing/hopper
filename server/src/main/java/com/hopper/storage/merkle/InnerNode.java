package com.hopper.storage.merkle;

import com.hopper.storage.StateNode;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of MerkleNode, it represents a inner node(not leaf)
 */
public class InnerNode implements MerkleNode {
    /**
     * Left sub tree
     */
    private MerkleNode left;
    /**
     * Right sub tree
     */
    private MerkleNode right;
    /**
     * Associated range
     */
    private HashRange range;

    public InnerNode(HashRange range) {
        this.range = range;
        range.setMerkleNode(this);
    }

    @Override
    public int getKeyHash() {
        if (left == null) {
            return right.getKeyHash();
        } else if (right == null) {
            return left.getKeyHash();
        }
        return left.getKeyHash() ^ right.getKeyHash();
    }

    @Override
    public int getValueHash() {
        if (left == null) {
            return right.getValueHash();
        } else if (right == null) {
            return left.getValueHash();
        }
        return left.getValueHash() ^ right.getValueHash();
    }

    @Override
    public void hash() {
        if (left != null) {
            left.hash();
        }
        if (right != null) {
            right.hash();
        }
    }

    public void setLeft(MerkleNode left) {
        this.left = left;
    }

    @Override
    public MerkleNode getLeft() {
        return left;
    }

    public void setRight(MerkleNode right) {
        this.right = right;
    }

    @Override
    public MerkleNode getRight() {
        return right;
    }

    @Override
    public HashRange getRange() {
        return range;
    }

    @Override
    public List<StateNode> getStateNodes() {
        List<StateNode> nodeList = new ArrayList<StateNode>();

        getStateNodes(this, nodeList);
        return nodeList;
    }

    private void getStateNodes(MerkleNode node, final List<StateNode> nodeList) {
        if (node instanceof Leaf) {
            nodeList.addAll(range.getStateNodes());
        }

        if (left != null) {
            getStateNodes(left, nodeList);
        }

        if (right != null) {
            getStateNodes(right, nodeList);
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
