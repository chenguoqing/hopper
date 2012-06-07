package com.hopper.session;

import com.hopper.GlobalConfiguration;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.future.LatchFuture;
import com.hopper.server.Endpoint;
import org.jboss.netty.channel.Channel;

public class DummyConnection extends LifecycleProxy implements Connection {
    /**
     * Associated {@link Channel}
     */
    private Channel channel;
    /**
     * Associated {@link Session}
     */
    private Session session;

    private Endpoint sourceEndpoint;
    private final Endpoint destEndpoint = GlobalConfiguration.getInstance().getLocalEndpoint();

    public void setChannel(Channel channel) {
        if (channel != null) {
            this.channel = channel;
            this.sourceEndpoint = GlobalConfiguration.getInstance().getEndpoint(channel.getRemoteAddress());
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void setSession(Session session) {
        if (session != null) {
            this.session = session;
        }
    }

    @Override
    public Endpoint getSourceEndpoint() {
        return sourceEndpoint;
    }

    @Override
    public Endpoint getDestEndpoint() {
        return destEndpoint;
    }

    @Override
    public void connect() {
    }

    @Override
    public void reconnect() {
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    public boolean validate() {
        return channel != null && channel.isConnected();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public void sendOneway(Message message) {
    }

    @Override
    public void sendOnwayUntilComplete(Message message) {
    }

    @Override
    public LatchFuture<Message> send(Message message) {
        return null;
    }

    @Override
    public void background() {
    }

}
