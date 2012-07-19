package com.hopper.storage.merkle;

import com.hopper.session.Serializer;
import com.hopper.storage.StateNode;
import com.hopper.utils.MurmurHash;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * {@link MerkleTree} inteface represents a logical merkle-tree, it will construct the real tree node/leaf on runtime
 * (lazy-creation). About the storage design, see {@link com.hopper.storage.StateStorage}.
 */
public class MerkleTree implements Serializer {
    /**
     * The full range for current tree
     */
    private final HashRange range;
    /**
     * Tree height
     */
    private final byte hashDepth;
    /**
     * Tree root
     */
    private final MerkleNode root;

    public MerkleTree(byte hashDepth) {
        this(new HashRange(Integer.MIN_VALUE, Integer.MAX_VALUE), hashDepth);
    }

    public MerkleTree(HashRange range, byte hashDepth) {
        // Check range and hash depth
        checkRange(range, hashDepth);

        this.range = range;
        this.hashDepth = hashDepth;
        this.root = new InnerNode(range);
    }

    /**
     * Check whether the range size [left,right) can covers the hash depth size(2<sup>hashDepth</sup>). Or,
     * whether or not the range can be split to <code>hashDepth</code> levels.
     */
    private void checkRange(HashRange range, byte hashDepth) {
        if (hashDepth > 30) {
            throw new IllegalArgumentException("hashDepth muse be less than 31.");
        }
        long rangeSize = Math.abs((long) range.getRight() - (long) range.getLeft());

        int hashSize = 2 << hashDepth;

        if (rangeSize < hashSize) {
            throw new IllegalArgumentException("HashRange[" + range.getLeft() + "," + range.getRight() + "] is less " +
                    "than hash depth" + hashDepth);
        }
    }

    /**
     * Lazy-calculate tree's hash
     */
    public void loadHash() {
        this.root.hash();
    }

    public MerkleNode getRoot() {
        return root;
    }

    /**
     * Find a leaf range by hash, if it doesn't exists, create it first. The method will cause to lazy loading the
     * entire tree nodes. The mechanism can significant save space.
     */
    private HashRange findAndCreateLeafRange(int hash) {
        MerkleNode node = root;
        int depth = 0;

        while (!(node instanceof Leaf)) {

            if (node.getRange().contains(hash)) {
                node = createSubMerkleNode(node, node.getRange(), depth++, true);
            } else {
                node = createSubMerkleNode(node, node.getRange(), depth++, false);
            }
        }

        return node.getRange();
    }

    /**
     * Create a child node(left/right) for <code>parent</code> with thread-safety. For improving the concurrent
     * performance, it only locks the parent node.
     *
     * @param parent Parent node
     * @param range  Child node range
     * @param depth  Child node hash depth
     * @param isLeft Is left node?
     * @return Child node instance
     */
    private MerkleNode createSubMerkleNode(MerkleNode parent, HashRange range, int depth, boolean isLeft) {
        MerkleNode child = isLeft ? parent.getLeft() : parent.getRight();

        if (child == null) {
            synchronized (parent) {
                child = isLeft ? parent.getLeft() : parent.getRight();
                if (child == null) {
                    if (depth == hashDepth) {
                        child = new Leaf(range);
                    } else {
                        child = new InnerNode(range);
                    }

                    if (isLeft) {
                        ((InnerNode) parent).setLeft(child);
                    } else {
                        ((InnerNode) parent).setRight(child);
                    }
                }
            }
        }

        return child;
    }

    /**
     * The method should only be invoked by StateStorage.
     */
    public void put(StateNode stateNode) {
        if (stateNode == null) {
            throw new NullPointerException();
        }

        HashRange range = findAndCreateLeafRange(MurmurHash.hash(stateNode.key));
        range.put(stateNode);
    }

    /**
     * The method should only be invoked by StateStorage.
     */
    public void remove(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        HashRange leafRange = findAndCreateLeafRange(MurmurHash.hash(key));
        leafRange.remove(key);
    }

    /**
     * Difference the two merkle trees, local tree(this) will be taken as comparison standard
     *
     * @param target target merkle tree
     * @return Comparison result will be wrapped as Difference instance
     */
    public Difference difference(MerkleTree target) {
        Difference difference = new Difference();
        difference(root, target.root, difference);
        return difference;
    }

    /**
     * Difference two tree nodes with recursion, the comparison will be added to <code>difference</code>
     *
     * @param node1      node1 as the comparison standard
     * @param node2      node1 as the source
     * @param difference Difference instance that holds the comparison result
     */
    private void difference(MerkleNode node1, MerkleNode node2, final Difference difference) {

        if (node1 == null && node2 == null) {
            return;
        }

        if (node1 != null && node2 != null) {
            // two node are exactly equal
            if (node1.equals(node2)) {
                return;
            }

            if (node1 instanceof Leaf) {
                difference.updated((Leaf) node1, (Leaf) node2);
            } else {
                difference(node1.getLeft(), node2.getLeft(), difference);
                difference(node1.getRight(), node2.getRight(), difference);
            }
        } else if (node1 == null) {
            difference.removed(node2);
        } else if (node2 == null) {
            difference.added(node1);
        }
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        root.serialize(out);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        root.deserialize(in);
    }
}
