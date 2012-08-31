package com.hopper.server;

import com.hopper.GlobalConfiguration;
import com.hopper.cache.CacheManager;
import com.hopper.lifecycle.Lifecycle;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.quorum.DefaultLeaderElection;
import com.hopper.quorum.ElectionMonitor;
import com.hopper.quorum.LeaderElection;
import com.hopper.session.ConnectionManager;
import com.hopper.session.MessageService;
import com.hopper.session.SessionManager;
import com.hopper.session.SessionSnooper;
import com.hopper.stage.StageManager;
import com.hopper.storage.StateStorage;
import com.hopper.storage.TreeStorage;
import com.hopper.sync.DataSyncService;
import com.hopper.util.DefaultScheduleManager;
import com.hopper.util.ScheduleManager;
import com.hopper.util.merkle.MapStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ComponentManager} manages all life cycle components
 */
public class ComponentManager extends LifecycleProxy {
    /**
     * LOGGER
     */
    private final static Logger logger = LoggerFactory.getLogger(ComponentManager.class);
    /**
     * All components
     */
    private final List<Lifecycle> components = new ArrayList<Lifecycle>();

    private GlobalConfiguration globalConfiguration;
    private CacheManager cacheManager;
    private StateStorage stateStorage;
    private Server server;
    private ScheduleManager scheduleManager;
    private SessionManager sessionManager;
    private DataSyncService dataSyncService;
    private StageManager stageManager;
    private LeaderElection leaderElection;
    private ConnectionManager connectionManager;
    private MessageService messageService;
    private ElectionMonitor electionMonitor;
    private SessionSnooper sessionSnooper;

    public void registerComponent(Lifecycle component) {
        components.add(component);
    }

    @Override
    protected void doInit() throws Exception {

        this.globalConfiguration = createGlobalConfiguration();

        this.getGlobalConfiguration().initialize();
        logger.info("Initialize " + globalConfiguration.getInfo() + "...");

        this.globalConfiguration.start();
        logger.info("Starting " + globalConfiguration.getInfo() + "...");

        registerComponent(globalConfiguration);

        this.stageManager = createStageManager();
        registerComponent(stageManager);

        this.scheduleManager = createScheduleManager();
        registerComponent(scheduleManager);

        this.cacheManager = createCacheManager();
        registerComponent(cacheManager);

        this.stateStorage = createStateStorage();
        registerComponent(stateStorage);

        this.sessionManager = createSessionManager();
        registerComponent(sessionManager);

        this.dataSyncService = createDataSyncService();
        registerComponent(dataSyncService);

        this.leaderElection = createLeaderElection();
        registerComponent(leaderElection);

        this.connectionManager = createConnectionManager();

        this.messageService = createMessageService();

        this.server = createServer();
        registerComponent(server);

        this.sessionSnooper = createSessionSnooper();
        registerComponent(sessionSnooper);

        this.electionMonitor = createElectionMonitor();

        if (globalConfiguration.getServerMode() == GlobalConfiguration.ServerMode.MULTI) {
            registerComponent(electionMonitor);
        }
    }

    @Override
    protected void doStart() throws Exception {
        for (Lifecycle component : components) {

            if (component.getState() != LifecycleState.NEW) {
                continue;
            }

            logger.info("Initialize " + component.getInfo() + "...");
            component.initialize();
            logger.info("Starting " + component.getInfo() + "...");
            component.start();
        }

        logger.info("Hopper server started.");

        // start shutdown socket
        Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                startShutdownSocket();
            }
        };

        shutdownThread.start();
    }

    @Override
    protected void doShutdown() throws Exception {
        for (int i = components.size() - 1; i >= 0; i--) {
            Lifecycle component = components.get(i);
            component.shutdown();
            logger.debug("Shutdown the component:{} ", component.getInfo());
        }

        logger.info("Hopper has shutdown.");
    }

    @Override
    public String getInfo() {
        return "Component Manager";
    }

    public Server getDefaultServer() {
        return server;
    }

    private Server createServer() {
        DefaultServer server = new DefaultServer();
        server.setId(globalConfiguration.getServerId());
        server.setRpcEndpoint(globalConfiguration.getLocalRpcEndpoint());
        server.setServerEndpoint(globalConfiguration.getLocalServerEndpoint());

        logger.info("Create default server, rpc endpoint:{},s2s endpoint:{}", server.getRpcEndPoint(),
                server.getServerEndpoint());
        return server;
    }

    public StateStorage getStateStorage() {
        return stateStorage;
    }

    private StateStorage createStateStorage() {
        GlobalConfiguration.StorageMode mode = globalConfiguration.getStorageMode();
        StateStorage storage = mode == GlobalConfiguration.StorageMode.TREE ? new TreeStorage() : new MapStorage();
        return storage;
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    private GlobalConfiguration createGlobalConfiguration() {
        return new GlobalConfiguration();
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    private ScheduleManager createScheduleManager() {
        ScheduleManager scheduleManager = new DefaultScheduleManager();
        return scheduleManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    private SessionManager createSessionManager() {
        return new SessionManager();
    }

    public LeaderElection getLeaderElection() {
        return leaderElection;
    }

    private LeaderElection createLeaderElection() {
        return new DefaultLeaderElection();
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    private CacheManager createCacheManager() {
        CacheManager cacheManager = new CacheManager();
        cacheManager.setScheduleManager(getScheduleManager());
        cacheManager.setEvictPeriod(globalConfiguration.getCacheEvictPeriod());

        return cacheManager;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    private StageManager createStageManager() {
        return new StageManager();
    }

    public DataSyncService getDataSyncService() {
        return dataSyncService;
    }

    private DataSyncService createDataSyncService() {
        return new DataSyncService();
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private ConnectionManager createConnectionManager() {
        return new ConnectionManager();
    }

    public MessageService getMessageService() {
        return messageService;
    }

    private MessageService createMessageService() {
        return new MessageService();
    }

    public ElectionMonitor getElectionMonitor() {
        return electionMonitor;
    }

    private ElectionMonitor createElectionMonitor() {
        return new ElectionMonitor();
    }

    private SessionSnooper createSessionSnooper() {
        return new SessionSnooper();
    }

    /**
     * Start a shutdown hook on special port
     */
    private void startShutdownSocket() {
        final int shutdownPort = getGlobalConfiguration().getShutdownPort();
        final String shutdownCommand = getGlobalConfiguration().getShutdownCommand();

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(shutdownPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("Shutdown thread has stared on port:{}", shutdownPort);

        // Register hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        boolean isContinue = true;
        while (isContinue) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(2000);

                InputStream in = socket.getInputStream();

                byte[] buf = new byte[shutdownCommand.getBytes().length];

                int read = in.read(buf);

                if (read == buf.length && shutdownCommand.equals(new String(buf))) {
                    shutdown();
                    isContinue = false;
                }
                socket.close();
            } catch (IOException e) {
                isContinue = false;
                logger.error("Failed to process shutdown request.", e);
            }
        }
    }
}

