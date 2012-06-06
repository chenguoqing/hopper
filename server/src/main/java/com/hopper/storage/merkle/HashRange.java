package com.hopper.storage.merkle;

import com.hopper.GlobalConfiguration;
import com.hopper.session.Serializer;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateStorage;
import com.hopper.utils.ByteUtils;
import com.hopper.utils.MurmurHash;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link HashRange} represents a hash interval [left,right),it holds some StateNode(keys only) instances which key's
 * hash value belongs to this interval.
 */
public class HashRange implements Serializer {
    /**
     * HashRange left position
     */
    private int left;
    /**
     * Right position
     */
    private int right;
    /**
     * GlobalConfiguration instance
     */
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();
    /**
     * StateNode will be delegate to StateStorage
     */
    private final StateStorage storage = config.getDefaultServer().getStorage();
    /**
     * All associated StateNode keys
     */
    private final ConcurrentMap<String, AtomicLong> keyVersionMap = new ConcurrentHashMap<String, AtomicLong>();
    /**
     * Associated MerkleNode
     */
    private MerkleNode merkleNode;

    public HashRange() {
    }

    /**
     * Constructor
     */
    public HashRange(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public MerkleNode getMerkleNode() {
        return merkleNode;
    }

    public void setMerkleNode(MerkleNode merkleNode) {
        this.merkleNode = merkleNode;
    }

    public boolean contains(int hash) {
        return hash >= left && hash < right;
    }

    public int getMidPosition() {
        return (left + right) >>> 2;
    }

    public HashRange getLeftRange() {
        int midPos = getMidPosition();

        if (midPos == left || midPos == right) {
            return null;
        }
        return new HashRange(left, midPos);
    }

    public HashRange getRightRange() {
        int midPos = getMidPosition();

        if (midPos == left || midPos == right) {
            return null;
        }
        return new HashRange(midPos, right);
    }

    public void put(StateNode stateNode) {
        checkNode();
        keyVersionMap.putIfAbsent(stateNode.key, new AtomicLong(stateNode.getVersion()));
    }

    /**
     * Remove data by key
     */
    public void remove(String key) {
        checkNode();
        this.keyVersionMap.remove(key);
    }

    /**
     * Retrieve all StateNode
     */
    public List<StateNode> getStateNodes() {
        List<StateNode> nodes = new ArrayList<StateNode>(keyVersionMap.size());
        for (String key : keyVersionMap.keySet()) {
            nodes.add(storage.get(key));
        }
        return nodes;
    }

    public int keyHash() {
        checkNode();
        int hash = 0;

        for (String key : keyVersionMap.keySet()) {
            hash ^= MurmurHash.hash(key);
        }
        return hash;
    }

    public int valueHash() {
        checkNode();

        int hash = 0;

        for (AtomicLong version : keyVersionMap.values()) {
            byte[] b = ByteUtils.long2Bytes(version.get());
            hash ^= MurmurHash.hash(b);
        }
        return hash;
    }

    private void checkNode() {
        if (merkleNode == null || !(merkleNode instanceof Leaf)) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(left);
        out.writeInt(right);

        out.writeInt(keyVersionMap.size());

        for (String key : keyVersionMap.keySet()) {
            AtomicLong version = keyVersionMap.get(key);
            out.writeUTF(key);
            out.writeLong(version.get());
        }
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.left = in.readInt();
        this.right = in.readInt();

        int size = in.readInt();
        if (size <= 0) {
            return;
        }

        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            long v = in.readLong();
            AtomicLong version = new AtomicLong(v);
            keyVersionMap.put(key, version);
        }
    }

    /**
     * Lazy load real versions from storage
     */
    public void loadVersions() {
        for (String key : keyVersionMap.keySet()) {
            AtomicLong version = keyVersionMap.get(key);
            StateNode node = storage.get(key);

            version.set(node.getVersion());
        }
    }
}
