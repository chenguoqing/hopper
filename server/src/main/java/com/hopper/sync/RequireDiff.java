package com.hopper.sync;

import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Serializer;
import com.hopper.storage.StateNode;
import com.hopper.utils.merkle.MerkleTree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Requires to difference local merkle tree with remote(remote tree as the comparison standard)
 */
public class RequireDiff implements Serializer {
    /**
     * Local maximum xid
     */
    private long maxXid;
    /**
     * Merkle tree instance
     */
    private MerkleTree<StateNode> tree;

    public long getMaxXid() {
        return maxXid;
    }

    public void setMaxXid(long maxXid) {
        this.maxXid = maxXid;
    }

    public MerkleTree<StateNode> getTree() {
        return tree;
    }

    public void setTree(MerkleTree<StateNode> tree) {
        this.tree = tree;
    }


    @Override
    public void serialize(DataOutput out) throws IOException {
        if (tree == null) {
            throw new IllegalArgumentException("Please set the tree first.");
        }

        out.writeLong(maxXid);
        tree.serialize(out);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.maxXid = in.readLong();
        this.tree = new MerkleTree(ComponentManagerFactory.getComponentManager().getGlobalConfiguration()
                .getMerkleTreeDepth());
        tree.deserialize(in);
    }
}
