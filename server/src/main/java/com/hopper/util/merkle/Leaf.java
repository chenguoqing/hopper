package com.hopper.util.merkle;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hopper.util.ByteUtils;
import com.hopper.util.MurmurHash;

/**
 * {@link Leaf} represents a merkle tree leaf
 */
public class Leaf<T extends MerkleObjectRef> extends InnerNode<T> {

	private final ConcurrentMap<String, AtomicLong> keyVersionMap = new ConcurrentHashMap<String, AtomicLong>();
	private final List<T> objRefs = new ArrayList<T>();
	/**
	 * Associated range
	 */
	private int keyHash;
	private int versionHash;

	private boolean readonly;

	public Leaf(Range range) {
		super(range);
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	/**
	 * Leaf is the lowest level node, no left or right
	 */
	@Override
	public MerkleNode<T> getLeft() {
		return null;
	}

	/**
	 * Leaf is the lowest level node, no left or right
	 */
	@Override
	public MerkleNode<T> getRight() {
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
			T node = getObjectFactory().find(key);

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
			nodes.add(getObjectFactory().find(key));
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
				T instance = getClazz().newInstance();
				instance.deserialize(in);
				this.objRefs.add(instance);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
