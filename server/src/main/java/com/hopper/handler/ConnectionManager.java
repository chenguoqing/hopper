package com.hopper.handler;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Endpoint;
import com.hopper.session.Connection;
import com.hopper.session.LocalOutgoingSession;
import com.hopper.session.NettyConnection;
import org.jboss.netty.channel.Channel;

public class ConnectionManager {

    private GlobalConfiguration config = GlobalConfiguration.getInstance();

    public Connection getConnection(Channel channel) {
        return null;
    }

    public Connection getConnection(Endpoint endpoint) {
        return null;
    }

    public void addConnection(Channel channel, Connection connection) {

    }

    public void addOutgoingServerConnection(Endpoint endpoint, Connection connection) {

    }

    /**
     * Create a {@link Connection} instance, but doen't invoking any lifecycle
     * methods. The initial works will be delayed to caller.
     *
     * @param session
     * @return
     */
    public Connection createOutgoingServerConnection(LocalOutgoingSession session,
                                                     Endpoint endpoint) throws Exception {
        NettyConnection connection = (NettyConnection) config.getConnectionManager().getConnection(endpoint);

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
        addOutgoingServerConnection(endpoint, connection);

        return connection;
    }
}
