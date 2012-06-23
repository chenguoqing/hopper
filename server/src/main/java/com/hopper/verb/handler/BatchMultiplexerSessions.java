package com.hopper.verb.handler;

import com.hopper.session.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The message body for multiplexer sessions operations
 */
public class BatchMultiplexerSessions implements Serializer {

    private List<String> multiplexerSessions = new ArrayList<String>();

    public void add(String sessionId) {
        multiplexerSessions.add(sessionId);
    }

    public void addAll(Collection<String> sessions) {
        if (sessions != null) {
            this.multiplexerSessions.addAll(sessions);
        }
    }

    public List<String> getSessions() {
        return multiplexerSessions;
    }

    public boolean containsSessions() {
        return !multiplexerSessions.isEmpty();
    }

    @Override
    public void serialize(DataOutput out) throws IOException {
        out.write(multiplexerSessions.size());
        for (String id : multiplexerSessions) {
            out.writeUTF(id);
        }
    }

    @Override
    public void deserialize(DataInput in) throws IOException {
        int size = in.readInt();
        int i = 0;
        while (i++ < size) {
            String id = in.readUTF();
            this.multiplexerSessions.add(id);
        }
    }
}
