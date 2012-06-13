package com.hopper.quorum;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The server max id
 */
public class QueryMaxXid implements Serializer {
    private int serverId;
    private long maxXid;

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getMaxXid() {
        return maxXid;
    }

    public void setMaxXid(long maxXid) {
        this.maxXid = maxXid;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(serverId);
        out.writeLong(maxXid);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.serverId = in.readInt();
        this.maxXid = in.readLong();
    }
}
