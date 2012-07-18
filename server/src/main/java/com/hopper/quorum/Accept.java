package com.hopper.quorum;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Accept implements Serializer {

    private int epoch;
    private int ballot;
    private int vval;

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public int getBallot() {
        return ballot;
    }

    public void setBallot(int ballot) {
        this.ballot = ballot;
    }

    public int getVval() {
        return vval;
    }

    public void setVval(int vval) {
        this.vval = vval;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(epoch);
        out.writeInt(ballot);
        out.writeInt(vval);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.epoch = in.readInt();
        this.ballot = in.readInt();
        this.vval = in.readInt();
    }
}
