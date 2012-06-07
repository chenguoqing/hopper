package com.hopper.handler;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Mutation collects all state update operations
 */
public class Mutation implements Serializer {
    /**
     * Operation
     */
    public static enum OP {
        CREATE(0), UPDATE_STATUS(1), UPDATE_LEASE(2), WATCH(3);

        public final int value;

        OP(int value) {
            this.value = value;
        }

        public static OP get(byte value) {
            for (OP op : values()) {
                if (op.value == value) {
                    return op;
                }
            }
            return null;
        }
    }

    /**
     * See above
     */
    private OP op;

    private Serializer entity;

    public OP getOp() {
        return op;
    }

    public <T> T getEntity() {
        return (T) entity;
    }

    public void addCreate(String key, String owner, int initStatus, int invalidateStatus) {
        this.op = OP.CREATE;
        Create c = new Create();
        c.key = key;
        c.owner = owner;
        c.initStatus = initStatus;
        c.invalidateStatus = invalidateStatus;
        this.entity = c;
    }

    public void addUpdateStatus(String key, int expectStatus, int newStatus, String owner, int lease) {
        this.op = OP.UPDATE_STATUS;
        UpdateStatus us = new UpdateStatus();
        us.key = key;
        us.expectStatus = expectStatus;
        us.newStatus = newStatus;
        us.owner = owner;
        us.lease = lease;
        this.entity = us;
    }

    public void addUpdateLease(String key, int expectStatus, String owner, int lease) {
        this.op = OP.UPDATE_LEASE;
        UpdateLease ul = new UpdateLease();
        ul.key = key;
        ul.expectStatus = expectStatus;
        ul.owner = owner;
        ul.lease = lease;
        this.entity = ul;
    }

    public void addWatch(String key, int expectStatus) {
        this.op = OP.WATCH;
        Watch w = new Watch();
        w.key = key;
        w.expectStatus = expectStatus;
        this.entity = w;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {

        out.writeByte((byte) op.value);

        switch (op) {
            case CREATE:
                Create c = (Create) entity;
                c.serialize(out);
                break;
            case UPDATE_STATUS:
                UpdateStatus us = (UpdateStatus) entity;
                us.serialize(out);
                break;
            case UPDATE_LEASE:
                UpdateLease ul = (UpdateLease) entity;
                ul.serialize(out);
                break;
            case WATCH:
                Watch w = (Watch) entity;
                w.serialize(out);
        }
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.op = OP.get(in.readByte());

        switch (op) {
            case CREATE:
                Create c = new Create();
                c.deserialize(in);
                this.entity = c;
                break;
            case UPDATE_STATUS:
                UpdateStatus us = new UpdateStatus();
                us.deserialize(in);
                this.entity = us;
                break;
            case UPDATE_LEASE:
                UpdateLease ul = new UpdateLease();
                ul.deserialize(in);
                this.entity = ul;
                break;
            case WATCH:
                Watch w = new Watch();
                w.deserialize(in);
                this.entity = w;
        }
    }

    public static class Create implements Serializer {
        public String key;
        public String owner;
        public int initStatus;
        public int invalidateStatus;

        @Override
        public void serialize(DataOutput out) throws IOException {
            out.writeUTF(key);
            out.writeUTF(owner);
            out.writeInt(initStatus);
            out.writeInt(invalidateStatus);
        }

        @Override
        public void deserialize(DataInput in) throws IOException {
            this.key = in.readUTF();
            this.owner = in.readUTF();
            this.initStatus = in.readInt();
            this.invalidateStatus = in.readInt();
        }
    }

    public static class UpdateStatus implements Serializer {
        String key;
        int expectStatus;
        int newStatus;
        String owner;
        int lease;

        @Override
        public void serialize(DataOutput out) throws IOException {
            out.writeUTF(key);
            out.writeInt(expectStatus);
            out.writeInt(newStatus);
            out.writeUTF(owner);
            out.writeInt(lease);
        }

        @Override
        public void deserialize(DataInput in) throws IOException {
            this.key = in.readUTF();
            this.expectStatus = in.readInt();
            this.newStatus = in.readInt();
            this.owner = in.readUTF();
            this.lease = in.readInt();
        }
    }

    public static class UpdateLease implements Serializer {
        String key;
        int expectStatus;
        String owner;
        int lease;

        @Override
        public void serialize(DataOutput out) throws IOException {
            out.writeUTF(key);
            out.writeInt(expectStatus);
            out.writeUTF(owner);
            out.writeInt(lease);
        }

        @Override
        public void deserialize(DataInput in) throws IOException {
            this.key = in.readUTF();
            this.expectStatus = in.readInt();
            this.owner = in.readUTF();
            this.lease = in.readInt();
        }
    }

    public static class Watch implements Serializer {
        String key;
        int expectStatus;

        @Override
        public void serialize(DataOutput out) throws IOException {
            out.writeUTF(key);
            out.writeInt(expectStatus);
        }

        @Override
        public void deserialize(DataInput in) throws IOException {
            this.key = in.readUTF();
            this.expectStatus = in.readInt();
        }
    }
}
