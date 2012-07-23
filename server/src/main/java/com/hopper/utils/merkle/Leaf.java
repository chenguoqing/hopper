package com.hopper.utils.merkle;

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
 * {@link Leaf} represents a merkle tree leaf
 */
public class Leaf<T extends MerkleObjectRef> extends InnerNode<T> {

    private final ConcurrentMap<String, AtomicLong> keyVersionMap = new ConcurrentHashMap<String, AtomicLong>();
    private final List<T> objRefs = new ArrayList<T>();
    private MerkleObjectReferenceable objectReferenceable;
    private Class<T> clazz;
    /**
     * Associated range
     */
    private int keyHash;
    private int versionHash;

    private boolean readonly;

    public Leaf(Range range) {
        super(range);
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public void setObjectReferenceable(MerkleObjectReferenceable objectReferenceable) {
        this.objectReferenceable = objectReferenceable;
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
    public int getVersionHash() {
        return versionHash;
    }

    @Override
    public void hash() {
        if (isReadonly()) {
            return;
        }

        this.objRefs.addAll(_getObjectRefs());

        loadVersions();
        this.keyHash = keyHash();
        this.versionHash = versionHash();
    }

    /**
     * Lazy load real versions from storage
     */
    private void loadVersions() {
        for (String key : keyVersionMap.keySet()) {
            AtomicLong version = keyVersionMap.get(key);
            T node = (T) objectReferenceable.find(key);

            version.set(node.getVersion());
        }
    }

    public int keyHash() {
        int hash = 0;

        for (String key : keyVersionMap.keySet()) {
            hash ^= MurmurHash.hash(key);
        }
        return hash;
    }

    public int versionHash() {
        int hash = 0;

        for (AtomicLong version : keyVersionMap.values()) {
            byte[] b = ByteUtils.long2Bytes(version.get());
            hash ^= MurmurHash.hash(b);
        }
        return hash;
    }

    @Override
    public List<T> getObjectRefs() {
        return objRefs;
    }

    private List<T> _getObjectRefs() {
        List<T> nodes = new ArrayList<T>(keyVersionMap.size());
        for (String key : keyVersionMap.keySet()) {
            nodes.add((T) objectReferenceable.find(key));
        }
        return nodes;
    }

    public void put(T stateNode) {
        keyVersionMap.putIfAbsent(stateNode.getKey(), new AtomicLong(stateNode.getVersion()));
    }

    /**
     * Remove data by key
     */
    public void remove(String key) {
        this.keyVersionMap.remove(key);
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(keyHash);
        out.writeInt(versionHash);

        out.writeInt(objRefs.size());
        for (T obj : objRefs) {
            obj.serialize(out);
        }
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.keyHash = in.readInt();
        this.versionHash = in.readInt();

        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            try {
                T instance = clazz.newInstance();
                instance.deserialize(in);
                this.objRefs.add(instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
