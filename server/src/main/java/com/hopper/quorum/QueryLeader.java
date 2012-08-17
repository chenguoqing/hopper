package com.hopper.quorum;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The {@link QueryLeader} enscuplate all information for leader election.
 *
 * @author chenguoqing
 */
public class QueryLeader implements Serializer {
    /**
     * Election instance
     */
    private int epoch;
    /**
     * The alive leader
     */
    private int leader;

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public int getLeader() {
        return leader;
    }

    public void setLeader(int leader) {
        this.leader = leader;
    }

    public boolean hasLeader() {
        return leader > 0;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(epoch);
        out.writeInt(leader);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.epoch = in.readInt();
        this.leader = in.readInt();
    }

    @Override
    public String toString() {
        return String.format("(epoch=%d,leader=%d)", epoch, leader);
    }
}
