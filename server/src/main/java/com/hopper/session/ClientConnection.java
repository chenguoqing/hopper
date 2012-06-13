package com.hopper.session;

import com.hopper.future.LatchFuture;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Endpoint;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * Client connection
 */
public class ClientConnection extends LifecycleProxy implements Connection {
    /**
     * Client channel
     */
    private final Channel channel;
    /**
     * Bound client session
     */
    private ClientSession session;

    public ClientConnection(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void setSession(Session session) {
        assert session instanceof ClientSession;

        this.session = (ClientSession) session;
    }

    @Override
    public Endpoint getSourceEndpoint() {
        return null;
    }

    @Override
    public Endpoint getDestEndpoint() {
        return ComponentManagerFactory.getComponentManager().getGlobalConfiguration().getLocalServerEndpoint();
    }

    @Override
    public String getInfo() {
        return "Client connection";
    }

    @Override
    public void connect() {
    }

    @Override
    public void reconnect() {
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public boolean validate() {
        return channel.isConnected();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public void sendOneway(Message message) {
        if (channel == null || !channel.isOpen()) {
            throw new IllegalStateException("Channel is not open or has been closed.");
        }

        ChannelFuture channelFuture = this.channel.write(message);

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture callbackFuture) throws Exception {
                if (callbackFuture.getCause() != null) {
                    logger.error("Failed to send message.", callbackFuture.getCause());
                }
            }
        });
    }

    @Override
    public void sendOnewayUntilComplete(Message message) {
    }

    @Override
    public LatchFuture<Message> send(Message message) {
        return null;
    }
}
