package com.hopper.server.handler;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class HeartBeat implements Serializer {
	/**
	 * Whether conform target is leader?
	 */
	private boolean isLeader;
	/**
	 * Local's max transaction id
	 */
	private long maxXid;

	public boolean isLeader() {
		return isLeader;
	}

	public void setLeader(boolean isLeader) {
		this.isLeader = isLeader;
	}

	public long getMaxXid() {
		return maxXid;
	}

	public void setMaxXid(long maxXid) {
		this.maxXid = maxXid;
	}

	@Override
	public void serialize(DataOutput out) throws IOException {
		byte b = isLeader ? (byte) 1 : 0;
		out.writeByte(b);
		out.writeLong(maxXid);
	}

	@Override
	public final void deserialize(DataInput in) throws IOException {

		int b = in.readByte();

		this.isLeader = b == 1;
		this.maxXid = in.readLong();
	}
}
