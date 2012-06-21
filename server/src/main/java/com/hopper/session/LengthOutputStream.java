package com.hopper.session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * The implementation of OutputStream, {@link LengthOutputStream} represents a stream with length at first 4 bytes
 * (int).
 * <p/>
 * The idea of {@link LengthOutputStream} is using same buffer for some operations(calculates data size,
 * wraps channel buffer e.g.) for reducing data duplicating between multiple buffers.
 */
public class LengthOutputStream extends ByteArrayOutputStream {
    /**
     * First 4 bytes for length
     */
    private static final int lenBytes = 4;

    /**
     * Constructor,default buffer is 32(bytes)
     */
    public LengthOutputStream() {
        this(32);
    }

    /**
     * Constructor with special buffer size, the first 4 bytes will be remained for length
     */
    public LengthOutputStream(int size) {
        super(size + lenBytes);
        count = lenBytes;
    }

    /**
     * Only write the content(excludes length) to OutputStream
     */
    @Override
    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(buf, lenBytes, count);
    }

    /**
     * Reset the stream
     */
    @Override
    public synchronized void reset() {
        count = lenBytes;
    }

    @Override
    public synchronized byte[] toByteArray() {
        byte[] copy = new byte[count - lenBytes];
        System.arraycopy(buf, lenBytes, copy, 0, copy.length);
        return copy;
    }

    /**
     * Return the origin buffer, the caller should not modify the buffer data.
     */
    public synchronized byte[] toFullByteArray() {
        return buf;
    }

    @Override
    public synchronized int size() {
        return count - lenBytes;
    }

    public synchronized int fullSize() {
        return count;
    }

    /**
     * Completes the stream, it causes the content size to be written in first 4 bytes,
     * the method can be called multiply times.
     */
    public synchronized void complete() {
        final int size = count - 4;

        buf[0] = (byte) ((size >>> 24) & 0xFF);
        buf[1] = (byte) ((size >>> 16) & 0xFF);
        buf[2] = (byte) ((size >>> 8) & 0xFF);
        buf[3] = (byte) ((size >>> 0) & 0xFF);
    }

    @Override
    public synchronized String toString() {
        return new String(buf, lenBytes, count);
    }

    @Override
    public synchronized String toString(String charsetName) throws UnsupportedEncodingException {
        return new String(buf, lenBytes, count, charsetName);
    }
}
