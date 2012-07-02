package com.hopper.session;

import org.jboss.netty.buffer.ChannelBuffer;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-7-2
 * Time: 下午4:56
 * To change this template use File | Settings | File Templates.
 */
public class BufferDataInput implements DataInput {

    private final ChannelBuffer buffer;

    public BufferDataInput(ChannelBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void readFully(byte[] b) {
        buffer.readBytes(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        buffer.readBytes(b, off, len);
    }

    @Override
    public int skipBytes(int n) {
        int r = buffer.readableBytes();
        int s = Math.min(n, r);
        buffer.skipBytes(s);
        return s;
    }

    @Override
    public boolean readBoolean() {
        return readByte() == 0;
    }

    @Override
    public byte readByte() {
        return buffer.readByte();
    }

    @Override
    public int readUnsignedByte() {
        byte b = readByte();

        return b >= 0 ? b : b + 255;
    }

    @Override
    public short readShort() {
        return buffer.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        int ch1 = buffer.readByte();
        int ch2 = buffer.readByte();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    @Override
    public char readChar() throws IOException {
        int ch1 = buffer.readByte();
        int ch2 = buffer.readByte();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    @Override
    public int readInt() {
        return buffer.readInt();
    }

    @Override
    public long readLong() {
        return buffer.readLong();
    }

    @Override
    public float readFloat() {
        return buffer.readFloat();
    }

    @Override
    public double readDouble() {
        return buffer.readDouble();
    }

    @Override
    public String readLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTF() throws IOException {
        int utflen = readUnsignedShort();

        if (utflen == 0) {
            return null;
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
