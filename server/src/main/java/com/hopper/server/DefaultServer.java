package com.hopper.server;

import com.hopper.GlobalConfiguration;
import com.hopper.lifecycle.Lifecycle;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.quorum.Paxos;
import com.hopper.stage.Stage;
import com.hopper.thrift.HopperService;
import com.hopper.thrift.HopperServiceImpl;
import com.hopper.thrift.netty.ThriftPipelineFactory;
import com.hopper.thrift.netty.ThriftServerHandler;
import com.hopper.verb.handler.ServerMessageDecoder;
import com.hopper.verb.handler.ServerMessageHandler;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

public class DefaultServer extends LifecycleProxy implements Server {

    private static Logger logger = LoggerFactory.getLogger(DefaultServer.class);

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

    private int leader;

    private ElectionState electionState;

    private ComponentManager componentManager;

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

        componentManager = ComponentManagerFactory.getComponentManager();
    }

    @Override
    protected void doStart() {

        // start internal listen port
        startServerSocket();

        // join group
        joinGroup();

        // start client listen port
        startClientSocket();
    }

    private void startServerSocket() {
        ServerBootstrap bootstrap = new ServerBootstrap(new OioServerSocketChannelFactory(componentManager
                .getStageManager().getThreadPool(Stage.SERVER_BOSS), componentManager.getStageManager().getThreadPool
                (Stage.SERVER_WORKER)));

        // set customs pipeline factory
        bootstrap.setPipelineFactory(new ServerPiplelineFactory());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(serverEndpoint.address, serverEndpoint.port));
    }

    /**
     * Start client-server socket with avro, all client requests will be processed by avro.
     */
    private void startClientSocket() {
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(componentManager
                .getStageManager().getThreadPool(Stage.CLIENT_BOSS), componentManager.getStageManager().getThreadPool
                (Stage.CLIENT_WORKER)));

        // set customs pipeline factory
        HopperService.Processor<HopperServiceImpl> processor = new HopperService.Processor<HopperServiceImpl>(new
                HopperServiceImpl());
        ThriftServerHandler handler = new ThriftServerHandler(processor);
        bootstrap.setPipelineFactory(new ThriftPipelineFactory(handler));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(endpoint.address, endpoint.port));
    }

    /**
     * Join group
     */
    private void joinGroup() {
        GlobalConfiguration.ServerMode mode = componentManager.getGlobalConfiguration().getServerMode();
        if (mode == GlobalConfiguration.ServerMode.MULTI) {
            logger.info("Start server with multiple nodes mode...");
            componentManager.getLeaderElection().startElecting();
        } else {
            logger.info("Start server with single nodes mode...");
            componentManager.getDefaultServer().setLeader(componentManager.getGlobalConfiguration()
                    .getLocalServerEndpoint().serverId);
        }
    }

    @Override
    public void setRpcEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Endpoint getRpcEndPoint() {
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
    public void setLeader(int serverId) {
        this.leader = serverId;
    }

    @Override
    public int getLeader() {
        return leader;
    }

    @Override
    public boolean isKnownLeader() {
        return leader != -1;
    }

    @Override
    public int getLeaderWithLock(long timeout) throws TimeoutException {
        //TODO:
        return 0;
    }

    @Override
    public void clearLeader() {
        this.leader = -1;
    }

    @Override
    public void abandonLeadership() {
        clearLeader();
        componentManager.getStateStorage().removeInvalidateTask();
        componentManager.getStateStorage().removePurgeThread();
    }

    @Override
    public void takeLeadership() {
        this.leader = serverEndpoint.serverId;
        componentManager.getStateStorage().executeInvalidateTask();
        componentManager.getStateStorage().enablePurgeThread();
    }

    @Override
    public boolean hasLeader() {
        return leader != -1;
    }

    @Override
    public boolean isLeader() {
        return leader != -1 && serverEndpoint.serverId == leader;
    }

    @Override
    public boolean isLeader(Endpoint endpoint) {
        return leader != -1 && endpoint.serverId == leader;
    }

    @Override
    public boolean isFollower() {
        return leader != -1 && serverEndpoint.serverId != -leader;
    }

    @Override
    public boolean isFollower(Endpoint endpoint) {
        return leader != -1 && endpoint.serverId != -leader;
    }

    @Override
    public void setElectionState(ElectionState state) {
        this.electionState = state;
    }

    @Override
    public ElectionState getElectionState() {
        return electionState;
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
