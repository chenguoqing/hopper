package com.hopper.util.merkle;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of MerkleNode, it represents a inner node(not leaf)
 */
public class InnerNode<T extends MerkleObjectRef> implements MerkleNode<T> {
	private Class<T> clazz;
	private MerkleObjectFactory<T> objectFactory;
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
	private final Range range;

	private int keyHash;
	private int valueHash;

	public InnerNode(Range range) {
		this.range = range;
	}

	@Override
	public Class<T> getClazz() {
		return clazz;
	}

	@Override
	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void setObjectFactory(MerkleObjectFactory<T> objectFactory) {
		this.objectFactory = objectFactory;
	}

	protected MerkleObjectFactory<T> getObjectFactory() {
		return objectFactory;
	}

	@Override
	public int getKeyHash() {
		return keyHash;
	}

	@Override
	public int getVersionHash() {
		return valueHash;
	}

	@Override
	public void hash() {
		Integer leftKeyHash = null;
		Integer leftValueHash = null;

		if (left != null) {
			left.hash();
			leftKeyHash = left.getKeyHash();
			leftValueHash = left.getVersionHash();
		}

		Integer rightKeyHash = null;
		Integer rightValueHash = null;

		if (right != null) {
			right.hash();
			rightKeyHash = right.getKeyHash();
			rightValueHash = right.getVersionHash();
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
	public Range getRange() {
		return range;
	}

	@Override
	public List<T> getObjectRefs() {
		List<T> nodeList = new ArrayList<T>();

		if (left != null) {
			nodeList.addAll(left.getObjectRefs());
		}

		if (right != null) {
			nodeList.addAll(right.getObjectRefs());
		}
		return nodeList;
	}

	@Override
	public void serialize(DataOutput out) throws IOException {

		out.writeByte(getChildFlag(left));
		out.writeByte(getChildFlag(right));

		if (left != null) {
			left.serialize(out);
		}

		if (right != null) {
			right.serialize(out);
		}
	}

	private byte getChildFlag(MerkleNode<T> child) {
		return child == null ? 0 : (child instanceof Leaf) ? (byte) 2 : (byte) 1;
	}

	@Override
	public void deserialize(DataInput in) throws IOException {
		byte leftFlag = in.readByte();
		byte rightFlag = in.readByte();

		this.left = createNodeByFlag(leftFlag, range.getLeftRange());
		this.right = createNodeByFlag(rightFlag, range.getRightRange());

		if (left != null) {
			left.setClazz(getClazz());
			left.deserialize(in);
		}

		if (right != null) {
			right.setClazz(getClazz());
			right.deserialize(in);
		}
	}

	private MerkleNode<T> createNodeByFlag(byte flag, Range range) {

		if (flag == 1) {
			return new InnerNode<T>(range);
		}

		if (flag == 2) {
			Leaf<T> leaf = new Leaf<T>(range);
			leaf.setReadonly(true);
			return leaf;
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

		MerkleNode<T> target = (MerkleNode<T>) obj;

		return getKeyHash() == target.getKeyHash() && this.getVersionHash() == target.getVersionHash();
	}
}
