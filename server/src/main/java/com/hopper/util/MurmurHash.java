package com.hopper.util;

import com.hopper.session.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * This is a very fast, non-cryptographic hash suitable for general hash-based
 * find.  See http://murmurhash.googlepages.com/ for more details.
 * <p/>
 * <p>The C version of MurmurHash 2.0 found at that site was ported
 * to Java by Andrzej Bialecki (ab at getopt org).</p>
 * <p/>
 * <b>Notice:</b> The source code is coming from Cassandra
 */
public class MurmurHash {

    /**
     * Hash the string, takes the length as seed
     */
    public static int hash(String str) {
        if (str == null) {
            throw new NullPointerException();
        }

        return hash(str.getBytes(), 0, str.getBytes().length, str.length());
    }

    /**
     * Hash byte array, takes length as seed
     */
    public static int hash(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        return hash(data, 0, data.length, data.length);
    }

    /**
     * Hash object
     */
    public static int hash(Object o) throws IOException {
        if (o == null) {
            throw new NullPointerException();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (o instanceof Serializer) {
            ((Serializer) o).serialize(new DataOutputStream(out));
        } else {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
        }

        byte[] data = out.toByteArray();
        int seed = System.identityHashCode(o);

        return hash(data, 0, data.length, seed);
    }

    public static int hash(byte[] data, int offset, int length, int seed) {
        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ length;

        int len_4 = length >> 2;
        for (int i = 0; i < len_4; i++) {
            int i_4 = i << 2;
            int k = data[offset + i_4 + 3];
            k = k << 8;
            k = k | (data[offset + i_4 + 2] & 0xff);
            k = k << 8;
            k = k | (data[offset + i_4 + 1] & 0xff);
            k = k << 8;
            k = k | (data[offset + i_4 + 0] & 0xff);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // avoid calculating modulo
        int len_m = len_4 << 2;
        int left = length - len_m;

        if (left != 0) {
            if (left >= 3) {
                h ^= (int) data[offset + length - 3] << 16;
            }
            if (left >= 2) {
                h ^= (int) data[offset + length - 2] << 8;
            }
            if (left >= 1) {
                h ^= (int) data[offset + length - 1];
            }

            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }
}
