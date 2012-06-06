package com.hopper.storage;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-7
 * Time: 下午5:16
 * To change this template use File | Settings | File Templates.
 */
public class StateNodeSnapshot implements Serializer {
    /**
     * Unique key
     */
    public String key;

    /**
     * Node type
     */
    public int type;

    /**
     * Status
     */
    public int status;

    /**
     * state owner
     */
    public String owner;

    /**
     * State lease(seconds)
     */
    public int lease = -1;

    /**
     * State last modified
     */
    public long lastModified;

    /**
     * Version is a internal flag for data synchronization
     */
    public long version;

    public final List<String> stateChangeListeners = new ArrayList<String>();

    public StateNodeSnapshot() {
    }

    public StateNodeSnapshot(StateNode node) {
        this.key = node.key;
        this.type = node.type;
        this.status = node.getStatus();
        this.owner = node.getOwner();
        this.lease = node.getLease();
        this.lastModified = node.getLastModified();
        this.version = node.getVersion();
        this.stateChangeListeners.addAll(node.getStateChangeListeners());
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        writeUTF(key, out);
        out.writeInt(type);
        out.writeInt(status);
        writeUTF(owner, out);
        out.writeInt(lease);
        out.writeLong(lastModified);
        out.writeLong(version);
        out.writeInt(stateChangeListeners.size());
        for (String sessionId : stateChangeListeners) {
            writeUTF(sessionId, out);
        }
    }

    @Override
    public void deserialize(DataInput in) throws IOException {

        this.key = in.readUTF();
        this.type = in.readInt();
        this.status = in.readInt();
        this.owner = readUTF(in);
        this.lease = in.readInt();
        this.lastModified = in.readLong();
        this.version = in.readLong();

        int listenerSize = in.readInt();
        for (int i = 0; i < listenerSize; i++) {
            String sessionId = readUTF(in);
            this.stateChangeListeners.add(sessionId);
        }
    }

    private void writeUTF(String str, DataOutput out) throws IOException {
        out.writeUTF(str == null ? "null" : str);
    }

    private String readUTF(DataInput in) throws IOException {
        String str = in.readUTF();
        return "null".equals(str) ? null : str;
    }
}
