package com.hopper.server;

import java.net.InetAddress;

/**
 * {@link Endpoint} representing a communication node
 *
 * @author chenguoqing
 */
public class Endpoint {
    /**
     * Id(configuration)
     */
    public final int serverId;

    /**
     * Address
     */
    public final InetAddress address;

    /**
     * Port
     */
    public final int port;

    /**
     * Constructor with all fields
     */
    public Endpoint(int serverId, InetAddress address, int port) {
        if (serverId < 0 || port < 0) {
            throw new IllegalArgumentException();
        }

        this.serverId = serverId;
        this.address = address;
        this.port = port;
    }

    @Override
    public int hashCode() {
        int result = 31 + address.hashCode();
        result = 31 * result + port;
        result = 31 * result + serverId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Endpoint other = (Endpoint) obj;

        return serverId == other.serverId && address.equals(other.address) && port == other.port;
    }
}
