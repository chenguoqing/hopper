package com.hopper.server.handler;

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
     * Target's server id
     */
    private int serverId;
    /**
     * Election instance
     */
    private int epoch;
    /**
     * The alive leader
     */
    private int leader;

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

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
        return leader != -1;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeInt(serverId);
        out.writeInt(epoch);
        out.writeInt(leader);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.serverId = in.readInt();
        this.epoch = in.readInt();
        this.leader = in.readInt();
    }
}
