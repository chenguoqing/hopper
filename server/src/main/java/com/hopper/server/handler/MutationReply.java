package com.hopper.server.handler;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * MutationReply encapsulates the result of Mutation request
 */
public class MutationReply implements Serializer {

    public static final int SUCCESS = 0;
    public static final int WAITING = 1;
    public static final int NO_QUORUM = 2;

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {

    }

    @Override
    public void deserialize(DataInput in) throws IOException {
    }
}
