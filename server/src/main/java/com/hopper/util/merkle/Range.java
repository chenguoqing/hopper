package com.hopper.util.merkle;

public class Range {
	/**
	 * HashRange left position
	 */
	private int left;
	/**
	 * Right position
	 */
	private int right;

	/**
	 * Constructor
	 */
	public Range(int left, int right) {
		this.left = left;
		this.right = right;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public boolean contains(int hash) {
		return hash >= left && hash < right;
	}

	public int getMidPosition() {
		return (left + right) / 2;
	}

	public Range getLeftRange() {
		int midPos = getMidPosition();

		if (midPos == left || midPos == right) {
			return null;
		}
		return new Range(left, midPos);
	}

	public Range getRightRange() {
		int midPos = getMidPosition();

		if (midPos == left || midPos == right) {
			return null;
		}
		return new Range(midPos, right);
	}
}
