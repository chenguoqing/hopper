package com.hopper.session;

import org.jboss.netty.buffer.ChannelBuffer;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * The implementation of {@link DataInput} that will read data from {@link ChannelBuffer}
 */
public class BufferDataInput implements DataInput {

    private final ChannelBuffer buffer;

    private final int size;

    public BufferDataInput(ChannelBuffer buffer) {
        this.buffer = buffer;
        this.size = buffer.readInt();
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

    public int avaliable() {
        return buffer.readableBytes();
    }

    @Override
    public String readUTF() throws IOException {
        return _readUTF();
    }

    /**
     * Reads from the
     * stream <code>in</code> a representation
     * of a Unicode  character string encoded in
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a> format;
     * this string of characters is then returned as a <code>String</code>.
     * The details of the modified UTF-8 representation
     * are  exactly the same as for the <code>readUTF</code>
     * method of <code>DataInput</code>.
     *
     * @return a Unicode string.
     * @throws EOFException                   if the input stream reaches the end
     *                                        before all the bytes.
     * @throws IOException                    the stream has been closed and the contained
     *                                        input stream does not support reading after close, or
     *                                        another I/O error occurs.
     * @throws java.io.UTFDataFormatException if the bytes do not represent a
     *                                        valid modified UTF-8 encoding of a Unicode string.
     * @see java.io.DataInputStream#readUnsignedShort()
     */
    private String _readUTF() throws IOException {
        int utflen = readUnsignedShort();

        if (utflen == 0) {
            return null;
        }

        char[] chararr = new char[utflen];

        int char2, char3;
        int count = 0;
        int chararr_count = 0;

        while (count < utflen) {
            int c = (int) buffer.readByte() & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    if (count + 2 > utflen) {
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    }

                    char2 = (int) buffer.readByte();
                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException("malformed input around byte " + count);
                    }

                    chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    count += 2;
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */

                    if (count > utflen)
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    char2 = (int) buffer.readByte();
                    char3 = (int) buffer.readByte();

                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException("malformed input around byte " + (count - 1));
                    }

                    chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F)
                            << 0));

                    count += 3;
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException("malformed input around byte " + count);
            }
        }

        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }
}
