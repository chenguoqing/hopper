package com.hopper.util;

public class ByteUtils {

	/**
	 * Convert the int to byte array with high order
	 */
	public static byte[] int2Bytes(int v) {
		byte[] b = new byte[4];

		b[0] = (byte) ((v >>> 24) & 0xFF);
		b[1] = (byte) ((v >>> 16) & 0xFF);
		b[2] = (byte) ((v >>> 8) & 0xFF);
		b[3] = (byte) ((v >>> 0) & 0xFF);

		return b;
	}

	/**
	 * Convert the int to byte array with high order
	 */
	public static void int2Bytes(int v, byte[] buf, int offset) {
		if (buf == null) {
			throw new IllegalArgumentException();
		}

		if (offset < 0 || offset > buf.length - 1 || buf.length - offset < 4) {
			throw new IndexOutOfBoundsException("no enough space.");
		}

		buf[offset] = (byte) ((v >>> 24) & 0xFF);
		buf[offset + 1] = (byte) ((v >>> 16) & 0xFF);
		buf[offset + 2] = (byte) ((v >>> 8) & 0xFF);
		buf[offset + 3] = (byte) ((v >>> 0) & 0xFF);
	}

	public static final int bytes2Int(byte[] b) {
		return bytes2Int(b, 0);
	}

	public static final int bytes2Int(byte[] b, int offset) {
		return ((b[offset] & 0xff) << 24) | ((b[offset + 1] & 0xff) << 16) | ((b[offset + 2] & 0xff) << 8)
				| (b[offset + 3] & 0xff);
	}

	public static byte[] long2Bytes(long v) {
		byte[] b = new byte[8];

		b[0] = (byte) ((v >>> 56) & 0xFF);
		b[1] = (byte) ((v >>> 48) & 0xFF);
		b[2] = (byte) ((v >>> 40) & 0xFF);
		b[3] = (byte) ((v >>> 32) & 0xFF);
		b[4] = (byte) ((v >>> 24) & 0xFF);
		b[5] = (byte) ((v >>> 16) & 0xFF);
		b[6] = (byte) ((v >>> 8) & 0xFF);
		b[7] = (byte) ((v >>> 0) & 0xFF);

		return b;
	}

	public static long bytes2Long(byte[] b) {
		return bytes2Long(b, 0);
	}

	public static long bytes2Long(byte[] b, int offset) {
		return ((long) (b[offset] & 0xff) << 56) | ((long) (b[offset + 1] & 0xff) << 48)
				| ((long) (b[offset + 2] & 0xff) << 40) | ((long) (b[offset + 3] & 0xff) << 32)
				| ((long) (b[offset + 4] & 0xff) << 24) | ((long) (b[offset + 5] & 0xff) << 16)
				| ((long) (b[offset + 6] & 0xff) << 8) | (b[offset + 7] & 0xff);
	}
}
