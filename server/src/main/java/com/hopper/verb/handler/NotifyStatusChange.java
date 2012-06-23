package com.hopper.verb.handler;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Message for notifying client the status change
 */
public class NotifyStatusChange implements Serializer {
    private String clientSessionId;
    private int oldStatus;
    private int newStatus;

    public String getClientSessionId() {
        return clientSessionId;
    }

    public void setClientSessionId(String clientSessionId) {
        this.clientSessionId = clientSessionId;
    }

    public int getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(int oldStatus) {
        this.oldStatus = oldStatus;
    }

    public int getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(int newStatus) {
        this.newStatus = newStatus;
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.writeUTF(clientSessionId);
        out.writeInt(oldStatus);
        out.writeInt(newStatus);
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        this.clientSessionId = in.readUTF();
        this.oldStatus = in.readInt();
        this.newStatus = in.readInt();
    }
}
