package com.hopper.session;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Endpoint;
import com.hopper.session.Connection;
import com.hopper.session.NettyConnection;
import com.hopper.session.OutgoingSession;
import org.jboss.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConnectionManager manages all incoming or outgoing connections
 */
public class ConnectionManager {
    /**
     * Global configuration
     */
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();
    /**
     * Incoming connections
     */
    private final Map<Channel, Connection> incomingConnections = new ConcurrentHashMap<Channel, Connection>();
    /**
     * Outgoing connections
     */
    private final Map<Endpoint, Connection> outgoingConnections = new ConcurrentHashMap<Endpoint, Connection>();

    public void addIncomingConnection(Channel channel, Connection connection) {
        incomingConnections.put(channel, connection);
    }

    public Connection getIncomingConnection(Channel channel) {
        return incomingConnections.get(channel);
    }

    public void removeIncomingConnection(Channel channel) {
        incomingConnections.remove(channel);
    }

    public void addOutgoingConnection(Endpoint endpoint, Connection connection) {
        outgoingConnections.put(endpoint, connection);
    }

    public Connection getOutgoingConnection(Endpoint endpoint) {
        return outgoingConnections.get(endpoint);
    }

    public void removeOutgoingConnection(Endpoint endpoint) {
        outgoingConnections.remove(endpoint);
    }

    /**
     * Create a {@link Connection} instance, but doen't invoking any lifecycle
     * methods. The initial works will be delayed to caller.
     */
    public Connection createOutgoingServerConnection(OutgoingSession session, Endpoint endpoint) throws Exception {
        NettyConnection connection = (NettyConnection) config.getConnectionManager().getOutgoingConnection(endpoint);

        if (connection != null) {
            return connection;
        }

        connection = new NettyConnection();
        connection.setSourceEndpoint(GlobalConfiguration.getInstance().getLocalEndpoint());
        connection.setDestEndpoint(endpoint);
        connection.setSession(session);

        // start connection
        connection.start();

        // register connection
        addOutgoingConnection(endpoint, connection);

        return connection;
    }
}
