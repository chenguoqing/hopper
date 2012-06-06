package com.hopper.session;

import com.hopper.GlobalConfiguration;
import com.hopper.cache.CacheManager;
import com.hopper.common.lifecycle.LifecycleException;
import com.hopper.common.lifecycle.LifecycleProxy;
import com.hopper.future.DefaultLatchFuture;
import com.hopper.future.LatchFuture;
import com.hopper.server.DefaultServer.ServerPiplelineFactory;
import com.hopper.server.Endpoint;
import com.hopper.utils.ScheduleManager;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.TimeoutException;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * {@link NettyConnection} is used to identify the connection between client and
 * server(NIO connection) with Netty component.
 *
 * @author chenguoqing
 */
public class NettyConnection extends LifecycleProxy implements Connection {
    /**
     * The boss executor for {@link ChannelFactory},all connection will share
     * this executor
     */
    private static final Executor bossExecutor = Executors.newCachedThreadPool();
    /**
     * The worker executor for {@link ChannelFactory},all connection will share
     * this executor
     */
    private static final Executor workerExecutor = Executors.newCachedThreadPool();

    /**
     * Inject the {@link com.hopper.GlobalConfiguration} instance
     */
    private static final GlobalConfiguration config = GlobalConfiguration.getInstance();
    /**
     * Singleton CacheManager instance
     */
    private static final CacheManager cacheManager = config.getCacheManager();
    /**
     * Schedule manager
     */
    private static final ScheduleManager scheduleManager = config.getScheduleManager();
    /**
     * Source endpoint
     */
    private Endpoint source;
    /**
     * Destination endpoint
     */
    private Endpoint dest;
    /**
     * The connected channel future
     */
    private Channel channel;
    /**
     * Local associated session
     */
    private Session session;

    @Override
    protected void doStart() {
        connect();
    }

    @Override
    protected void doShutdown() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    public void setSourceEndpoint(Endpoint source) {
        this.source = source;
    }

    @Override
    public Endpoint getSourceEndpoint() {
        return source;
    }

    public void setDestEndpoint(Endpoint dest) {
        this.dest = dest;
    }

    @Override
    public Endpoint getDestEndpoint() {
        return dest;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void connect() {
        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(bossExecutor, workerExecutor));

        // set customs pipeline factory
        bootstrap.setPipelineFactory(new ServerPiplelineFactory());

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(dest.address, dest.port));

        // Wait complete for connect
        future.awaitUninterruptibly();

        Throwable t = future.getCause();

        if (t != null) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }

        this.channel = future.getChannel();

        // Wait until the connection is closed or the connection attempt fails.
        future.getChannel().getCloseFuture().awaitUninterruptibly();

        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();
    }

    @Override
    public void reconnect() {
        // if the connection is running, shutdown it
        if (getState() != LifecycleState.SHUTDOWN && getState() != LifecycleState.NEW) {
            // invoke the super shutdown and notifies all listener
            shutdown();
        }

        try {
            start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        shutdown();
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
    public void sendOnwayUntilComplete(Message message) {
        if (channel == null || !channel.isOpen()) {
            throw new IllegalStateException("Channel is not open or has been closed.");
        }

        ChannelFuture channelFuture = this.channel.write(message);

        // waiting for complete
        channelFuture.awaitUninterruptibly(config.getRpcTimeout(), TimeUnit.MILLISECONDS);

        if (!channelFuture.isDone()) {
            throw new TimeoutException();
        }

        Throwable t = channelFuture.getCause();

        if (t != null) {
            logger.error("Failed to send message.", t);

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }

            throw new RuntimeException(t);
        }
    }

    @Override
    public LatchFuture<Message> send(final Message message) {

        if (channel == null || !channel.isOpen()) {
            throw new IllegalStateException("Channel is not open or has been closed.");
        }

        ChannelFuture channelFuture = channel.write(message);

        DefaultLatchFuture future = new DefaultLatchFuture();

        cacheManager.put(message.getId(), future, config.getRpcTimeout());

        channelFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture callbackFuture) throws Exception {
                // if exception occurs when sending message,set the exception to
                // MesageFuture for quick unlocking the blocking threads on
                // MesageFuture
                if (callbackFuture.getCause() != null) {

                    // if sending message failure, remove the future from cache
                    DefaultLatchFuture cacheFuture = cacheManager.remove(message.getId());

                    if (cacheFuture != null) {
                        // set the exception for unlocking the threads waiting
                        cacheFuture.setException(callbackFuture.getCause());
                    }

                    logger.error("Failed to send message.", callbackFuture.getCause());
                }
            }
        });

        return future;
    }

    /**
     * Start ping with interval
     */
    @Override
    public void background() {
    }
}
