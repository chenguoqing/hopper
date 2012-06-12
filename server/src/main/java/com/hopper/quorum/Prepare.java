package com.hopper.quorum;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The paxos prepare request for phase1a
 * 
 * @author chenguoqing
 * 
 */
public class Prepare implements Serializer {
	/**
	 * Ballot
	 */
	private int ballot;
	/**
	 * Epoch
	 */
	private int epoch;

	public int getBallot() {
		return ballot;
	}

	public void setBallot(int ballot) {
		this.ballot = ballot;
	}

	public int getEpoch() {
		return epoch;
	}

	public void setEpoch(int epoch) {
		this.epoch = epoch;
	}

	@Override
	public void serialize(DataOutput out) throws IOException {
		out.writeInt(ballot);
		out.writeInt(epoch);
	}

	@Override
	public void deserialize(DataInput in) throws IOException {
		this.ballot = in.readInt();
		this.epoch = in.readInt();
	}
}
