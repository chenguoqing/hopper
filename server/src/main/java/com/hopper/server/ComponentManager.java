package com.hopper.server;

import com.hopper.GlobalConfiguration;
import com.hopper.cache.CacheManager;
import com.hopper.lifecycle.Lifecycle;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.quorum.DefaultLeaderElection;
import com.hopper.quorum.LeaderElection;
import com.hopper.session.ConnectionManager;
import com.hopper.session.MessageService;
import com.hopper.session.SessionManager;
import com.hopper.stage.StageManager;
import com.hopper.storage.StateStorage;
import com.hopper.storage.TreeStorage;
import com.hopper.storage.merkle.MapStorage;
import com.hopper.sync.DataSyncService;
import com.hopper.utils.ScheduleManager;

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

    public void registerComponent(Lifecycle component) {
        components.add(component);
    }

    @Override
    protected void doInit() {
        this.globalConfiguration = createGlobalConfiguration();
        try {
            this.getGlobalConfiguration().initialize();
            this.globalConfiguration.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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

        this.stageManager = createStageManager();
        registerComponent(stageManager);

        this.leaderElection = createLeaderElection();
        this.connectionManager = createConnectionManager();

        this.messageService = createMessageService();

        this.server = createServer();
        registerComponent(server);
    }

    @Override
    protected void doStart() {
        try {
            for (Lifecycle component : components) {
                component.initialize();
                component.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // start shutdown socket
        startShutdownSocket();
    }

    @Override
    protected void doShutdown() {
        for (int i = components.size() - 1; i >= 0; i--) {
            Lifecycle component = components.get(i);
            component.shutdown();
        }
        this.globalConfiguration.shutdown();
    }

    public Server getDefaultServer() {
        return server;
    }

    private Server createServer() {
        DefaultServer server = new DefaultServer();
        server.setRpcEndpoint(globalConfiguration.getLocalRpcEndpoint());
        server.setServerEndpoint(globalConfiguration.getLocalServerEndpoint());
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
        ScheduleManager scheduleManager = new ScheduleManager();
        scheduleManager.setScheduleThreadCount(globalConfiguration.getScheduleThreadCount());
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

    /**
     * Start a shutdown hook on special port
     */
    private void startShutdownSocket() {
        final int shutdownPort = getGlobalConfiguration().getShutdownPort();
        final String shutdownCommand = getGlobalConfiguration().getShutdownCommand();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(shutdownPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

