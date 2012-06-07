package com.hopper.thrift.netty;

import com.hopper.session.ClientSession;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * The TTransport implementation only for sending notify to client, so it not allows for read operations.
 */
public class TNettyClientTransport extends TTransport {

    private final ClientSession session;

    public TNettyClientTransport(ClientSession session) {
        this.session = session;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void open() throws TTransportException {
    }

    @Override
    public void close() {
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws TTransportException {
        throw new TTransportException("The method has been forbidden.");
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        ChannelBuffer outputBuffer = ChannelBuffers.dynamicBuffer(len - off);
        outputBuffer.writeBytes(buf, off, len);
        session.getConnection().getChannel().write(outputBuffer);
    }
}
