package com.hopper.verb.handler;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class Accepted implements Serializer {
    /**
     * Promise
     */
    public static final int ACCEPTED = 0;
    /**
     * Reject with bigger ballot
     */
    public static final int REJECT_BALLOT = 1;
    /**
     * Reject with bigger epoch
     */
    public static final int REJECT_EPOCH = 2;
    /**
     * Status(see above)
     */
    private int status;
    /**
     * The bigger epoch node has begun
     */
    private int epoch;
    /**
     * The highest-numbered round in which node has participated
     */
    private int rnd;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRnd() {
        return rnd;
    }

    public void setRnd(int rnd) {
        this.rnd = rnd;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(status);
        out.writeInt(epoch);
        out.writeInt(rnd);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.status = in.readInt();
        this.epoch = in.readInt();
        this.rnd = in.readInt();
    }
}
