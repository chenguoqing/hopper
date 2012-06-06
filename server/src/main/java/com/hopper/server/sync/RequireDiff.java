package com.hopper.server.sync;

import com.hopper.GlobalConfiguration;
import com.hopper.session.Serializer;
import com.hopper.storage.merkle.MerkleTree;

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
    private MerkleTree tree;

    public long getMaxXid() {
        return maxXid;
    }

    public void setMaxXid(long maxXid) {
        this.maxXid = maxXid;
    }

    public MerkleTree getTree() {
        return tree;
    }

    public void setTree(MerkleTree tree) {
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
        this.tree = new MerkleTree(GlobalConfiguration.getInstance().getMerkleTreeDepth());
        tree.deserialize(in);
        tree.loadHash();
    }
}
