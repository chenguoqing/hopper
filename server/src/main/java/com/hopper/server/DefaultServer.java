package com.hopper.server;

import com.hopper.*;
import com.hopper.lifecycle.Lifecycle;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.quorum.Paxos;
import com.hopper.handler.ServerMessageDecoder;
import com.hopper.handler.ServerMessageHandler;
import com.hopper.stage.Stage;
import com.hopper.stage.StageThreadPools;
import com.hopper.thrift.HopperService;
import com.hopper.thrift.HopperServiceImpl;
import com.hopper.thrift.netty.ThriftPipelineFactory;
import com.hopper.thrift.netty.ThriftServerHandler;
import com.hopper.storage.StateStorage;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

public class DefaultServer extends LifecycleProxy implements Server {

    private static final GlobalConfiguration config = GlobalConfiguration.getInstance();

    /**
     * Outer connection endpoint
     */
    private Endpoint endpoint;
    /**
     * Internal communication endpoint
     */
    private Endpoint serverEndpoint;
    /**
     * Paxos node for election
     */
    private Paxos paxosNode;
    /**
     * State storage
     */
    private StateStorage storage;

    @Override
    protected void doInit() {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is null.");
        }

        if (serverEndpoint == null) {
            throw new IllegalArgumentException("server endpoint is null.");
        }

        if (paxosNode == null) {
            throw new IllegalArgumentException("paxos node is null.");
        }

        if (storage == null) {
            throw new IllegalArgumentException("storage instance is null.");
        }
    }

    @Override
    protected void doStart() {

        // start internal listen port
        startServerSocket();

        // start client listen port
        startClientSocket();
    }

    private void startServerSocket() {
        ServerBootstrap bootstrap = new ServerBootstrap(new OioServerSocketChannelFactory(StageThreadPools
                .getThreadPool(Stage.SERVER_BOSS), StageThreadPools.getThreadPool(Stage.SERVER_WORKER)));

        // set customs pipeline factory
        bootstrap.setPipelineFactory(new ServerPiplelineFactory());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(serverEndpoint.address, serverEndpoint.port));
    }

    /**
     * Start client-server socket with avro, all client requests will be processed by avro.
     */
    private void startClientSocket() {
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(StageThreadPools
                .getThreadPool(Stage.CLIENT_BOSS), StageThreadPools.getThreadPool(Stage.CLIENT_WORKER)));

        // set customs pipeline factory
        HopperService.Processor<HopperServiceImpl> processor = new HopperService.Processor<HopperServiceImpl>(new
                HopperServiceImpl());
        ThriftServerHandler handler = new ThriftServerHandler(processor);
        bootstrap.setPipelineFactory(new ThriftPipelineFactory(handler));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(endpoint.address, endpoint.port));
    }

    @Override
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Endpoint getEndPoint() {
        return endpoint;
    }

    @Override
    public void setServerEndpoint(Endpoint serverEndpoint) {
        this.serverEndpoint = serverEndpoint;
    }

    @Override
    public Endpoint getServerEndpoint() {
        return serverEndpoint;
    }

    @Override
    public void setPaxos(Paxos paxos) {
        this.paxosNode = paxos;
    }

    @Override
    public Paxos getPaxos() {
        return paxosNode;
    }

    @Override
    public void setStorage(StateStorage storage) {
        this.storage = storage;
    }

    @Override
    public StateStorage getStorage() {
        return storage;
    }

    @Override
    public void setLeader(int serverId) {

    }

    @Override
    public int getLeader() {
        return 0;
    }

    @Override
    public boolean isKnownLeader() {
        return false;
    }

    @Override
    public int getLeaderWithLock(long timeout) throws TimeoutException {
        return 0;
    }

    @Override
    public Object getLeaderLock() {
        return null;
    }

    @Override
    public void clearLeader() {

    }

    @Override
    public void anandonLeadeship() {
        storage.removeInvalidateTask();
        storage.removePurgeThread();
    }

    @Override
    public void takeLeadership() {
        storage.executeInvalidateTask();
        storage.enablePurgeThread();
    }

    @Override
    public boolean hasLeader() {
        return false;
    }

    @Override
    public boolean isLeader() {
        return false;
    }

    @Override
    public boolean isLeader(Endpoint endpoint) {
        return false;
    }

    @Override
    public boolean isFollower() {
        return false;
    }

    @Override
    public boolean isFollower(Endpoint endpoint) {
        return false;
    }

    @Override
    public void setElectionState(ElectionState state) {

    }

    @Override
    public ElectionState getElectionState() {
        return null;
    }

    /**
     * If the server is participating in election or not on running state, it will be unavailable
     */
    @Override
    public void assertServiceAvailable() throws ServiceUnavailableException {
        ElectionState state = getElectionState();

        if (state == Server.ElectionState.LOOKING || state == Server.ElectionState.SYNC || getState() != Lifecycle
                .LifecycleState.RUNNING) {
            throw new ServiceUnavailableException();
        }
    }

    /**
     * The {@link ChannelPipelineFactory} implementation for server
     * communication
     */
    public static class ServerPiplelineFactory implements ChannelPipelineFactory {

        @Override
        public ChannelPipeline getPipeline() throws Exception {
            // Create a default pipeline implementation.
            ChannelPipeline pipeline = Channels.pipeline();

            // put command decoder
            pipeline.addLast("decoder", new ServerMessageDecoder());

            // put command handler
            pipeline.addLast("commandHandler", new ServerMessageHandler());

            return pipeline;
        }
    }
}
