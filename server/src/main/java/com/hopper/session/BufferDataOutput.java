package com.hopper.session;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.io.DataOutput;
import java.io.IOException;

/**
 * {@link BufferDataOutput} is the idea of supporting a {@link DataOutput} implementation base on {@link
 * ChannelBuffer}, it carries the data length at first 4 bytes, {@link BufferDataOutput} avoids data duplication
 * between multiple buffers.
 */
public class BufferDataOutput implements DataOutput {

    private final ChannelBuffer buffer;

    /**
     * Constructor,default buffer is 32(bytes)
     */
    public BufferDataOutput() {
        this(256);
    }

    /**
     * Constructor with special buffer size, the first 4 bytes will be remained for length
     */
    public BufferDataOutput(int size) {
        this.buffer = ChannelBuffers.dynamicBuffer(4 + size);
        buffer.writeInt(4);
    }

    @Override
    public void writeUTF(String str) {
        _writeUTF(str);
    }

    public byte[] toByteArray() {
        return buffer.array();
    }

    public int size() {
        return buffer.writerIndex();
    }

    public int dataSize() {
        return size() - 4;
    }

    public void reset() {
        buffer.setIndex(4, 0);
    }

    /**
     * Completes the stream, it causes the content size to be written in first 4 bytes,
     * the method can be called multiply times.
     */
    public void complete() {
        final int writeIndex = buffer.writerIndex();
        buffer.markWriterIndex();
        buffer.setIndex(0, 0);
        buffer.writeInt(writeIndex - 4);
        buffer.resetWriterIndex();
    }

    public ChannelBuffer buffer() {
        return buffer;
    }

    @Override
    public void write(int b) {
        buffer.writeInt((byte) b);
    }

    @Override
    public void write(byte[] b) {
        buffer.writeBytes(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.writeBytes(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) {
        buffer.writeByte(v ? 0 : 1);
    }

    @Override
    public void writeByte(int v) {
        buffer.writeByte(v);
    }

    @Override
    public void writeShort(int v) {
        buffer.writeShort(v);
    }

    @Override
    public void writeChar(int v) {
        buffer.writeChar(v);
    }

    @Override
    public void writeInt(int v) {
        buffer.writeInt(v);
    }

    @Override
    public void writeLong(long v) {
        buffer.writeLong(v);
    }

    @Override
    public void writeFloat(float v) {
        buffer.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) {
        buffer.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) {
        if (s == null) {
            throw new NullPointerException("string is null");
        }

        for (int i = 0; i < s.length(); i++) {
            writeByte((byte) s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int v = s.charAt(i);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }
    }

    /**
     * Writes a string to the specified DataOutput using
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
     * encoding in a machine-independent manner.
     * <p/>
     * First, two bytes are written to out as if by the <code>writeShort</code>
     * method giving the number of bytes to follow. This value is the number of
     * bytes actually written out, not the length of the string. Following the
     * length, each character of the string is output, in sequence, using the
     * modified UTF-8 encoding for the character. If no exception is thrown, the
     * counter <code>written</code> is incremented by the total number of
     * bytes written to the output stream. This will be at least two
     * plus the length of <code>str</code>, and at most two plus
     * thrice the length of <code>str</code>.
     *
     * @param str a string to be written.
     */
    private void _writeUTF(String str) {

        if (str == null) {
            writeShort(0);
            return;
        }
        int utflen = 0;

        final int writeIndex = buffer.writerIndex();

        buffer.ensureWritableBytes(3);

        // skip the length(short)
        buffer.writerIndex(writeIndex + 2);

        for (int i = 0; i < str.length(); i++) {
            int c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                writeByte(c);
                utflen++;

            } else if (c > 0x07FF) {
                writeByte((0xE0 | ((c >> 12) & 0x0F)));
                writeByte((0x80 | ((c >> 6) & 0x3F)));
                writeByte((0x80 | ((c >> 0) & 0x3F)));
                utflen += 3;
            } else {
                writeByte((0xC0 | ((c >> 6) & 0x1F)));
                writeByte((0x80 | ((c >> 0) & 0x3F)));
                utflen += 2;
            }
        }

        buffer.markWriterIndex();
        buffer.writeShort(utflen);
        buffer.resetWriterIndex();
    }
}
