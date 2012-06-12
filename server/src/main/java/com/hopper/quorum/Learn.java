package com.hopper.quorum;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The {@link Learn} representing a paxos "LEARN" message
 */
public class Learn implements Serializer {
    /**
     * Proposer server id
     */
    private int proposer;
    /**
     * Election instance
     */
    private int epoch;
    /**
     * The accepted value
     */
    private int vval;

    public int getProposer() {
        return proposer;
    }

    public void setProposer(int proposer) {
        this.proposer = proposer;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public int getVval() {
        return vval;
    }

    public void setVval(int vval) {
        this.vval = vval;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(proposer);
        out.writeInt(epoch);
        out.writeInt(vval);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.proposer = in.readInt();
        this.epoch = in.readInt();
        this.vval = in.readInt();
    }
}
