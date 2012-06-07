package com.hopper.verb.handler;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-4
 * Time: 下午2:59
 * To change this template use File | Settings | File Templates.
 */
public class BatchSessionCreator implements Serializer {

    private List<String> transferSessions = new ArrayList<String>();

    public void add(String sessionId) {
        transferSessions.add(sessionId);
    }

    public List<String> getSessions() {
        return transferSessions;
    }

    public boolean containsSessions() {
        return !transferSessions.isEmpty();
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.write(transferSessions.size());
        for (String id : transferSessions) {
            out.writeUTF(id);
        }
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        int size = in.readInt();
        int i = 0;
        while (i++ < size) {
            String id = in.readUTF();
            this.transferSessions.add(id);
        }
    }
}
