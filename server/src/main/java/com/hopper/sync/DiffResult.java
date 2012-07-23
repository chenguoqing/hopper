package com.hopper.sync;

import com.hopper.session.Serializer;
import com.hopper.utils.merkle.Difference;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The comparison result with remote(remote is comparison standard)
 */
public class DiffResult implements Serializer {
    /**
     * Remote max xid
     */
    private long maxXid;
    /**
     * Difference result
     */
    private Difference difference;

    public long getMaxXid() {
        return maxXid;
    }

    public void setMaxXid(long maxXid) {
        this.maxXid = maxXid;
    }

    public Difference getDifference() {
        return difference;
    }

    public void setDifference(Difference difference) {
        this.difference = difference;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        if (difference == null) {
            throw new IllegalArgumentException("Please set difference first.");
        }
        out.writeLong(maxXid);
        difference.serialize(out);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.maxXid = in.readLong();
        this.difference = new Difference();
        this.difference.deserialize(in);
    }
}
