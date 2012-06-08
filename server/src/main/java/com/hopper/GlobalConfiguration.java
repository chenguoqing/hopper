package com.hopper;

import com.hopper.avro.ClientService;
import com.hopper.cache.CacheManager;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.quorum.LeaderElection;
import com.hopper.server.Endpoint;
import com.hopper.server.Server;
import com.hopper.session.ConnectionManager;
import com.hopper.session.SessionManager;
import com.hopper.sync.DataSyncService;
import com.hopper.thrift.HopperService;
import com.hopper.utils.ScheduleManager;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * {@link GlobalConfiguration} maintains all configuration items for hopper that will be loaded from YAML
 * configuration file on startup.
 */
public class GlobalConfiguration extends LifecycleProxy {
    /**
     * Server mode(single node or multiple nodes)
     */
    public static enum Mode {
        SINGLE, MULTI
    }

    /**
     * Resource prefix: classpath
     */
    private static final String CLASSPATH_PREFIX = "classpath:";
    /**
     * Resource prefix: absolute path
     */
    private static final String FILE_PREFIX = "file:";
    /**
     * Default system property
     */
    public static final String DEFAULT_SYSTEM_PROPERTY = "configFile";
    /**
     * Default configuration path
     */
    private static final String defaultYAML = FILE_PREFIX + "/conf/hopper.conf";
    /**
     * Singleton
     */
    private static volatile GlobalConfiguration instance = new GlobalConfiguration();
    /**
     * Configuration root object
     */
    private InnerConfig innerConfig;

    /**
     * Forbidden construct from outside
     */
    private GlobalConfiguration() {
    }

    /**
     * Singleton
     */
    public static GlobalConfiguration getInstance() {
        return instance;
    }

    @Override
    protected void doInit() {
        // Get the configuration file path
        String configPath = System.getProperty(DEFAULT_SYSTEM_PROPERTY, defaultYAML);

        try {
            InputStream in = read(configPath);
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(in);
            this.innerConfig = new InnerConfig(yamlMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load yaml configuration file:" + configPath, e);
        }
    }

    public Mode getMode() {
        String mode = innerConfig.getString("mode", "SINGLE");

        Mode _mode = Mode.valueOf(mode.toUpperCase());

        return _mode == null ? Mode.SINGLE : _mode;
    }

    /**
     * Return the server rpc timeout(milliseconds)
     */
    public long getRpcTimeout() {
        return innerConfig.getLong("rpc_timeout", 2000);
    }

    public long getPingPeriod() {
        return innerConfig.getLong("ping_period", 1000);
    }

    public Endpoint getEndpoint(SocketAddress address) {
        for (Endpoint endpoint : innerConfig.endpointMap.values()) {
            InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
            if (endpoint.address.equals(inetAddress)) {
                return endpoint;
            }
        }
        return null;
    }

    public boolean isLocalEndpoint(Endpoint endpoint) {
        return endpoint.serverId == innerConfig.localServerEndpoint.serverId;
    }

    public Endpoint getLocalEndpoint() {
        return innerConfig.localServerEndpoint;
    }

    public Endpoint getEndpoint(int serverId) {
        return innerConfig.endpointMap.get(serverId);
    }

    public CacheManager getCacheManager() {
        return null;
    }

    public ScheduleManager getScheduleManager() {
        return null;
    }

    public SessionManager getSessionManager() {
        return null;
    }

    public Server getDefaultServer() {
        return null;
    }

    public ConnectionManager getConnectionManager() {
        return null;
    }

    public LeaderElection getLeaderElection() {
        return null;
    }

    public Endpoint[] getConfigedEndpoints() {
        return innerConfig.endpointMap.values().toArray(new Endpoint[]{});
    }

    public int getQuorumSize() {
        return (getConfigedEndpoints().length / 2) + 1;
    }

    public int getLocalBallotServerId() {
        return 0;
    }

    public long getPeriodForJoin() {
        return innerConfig.getLong("period_for_waiting_join", 5000);
    }

    /**
     * The method will generate a random period between (1000,5000)
     */
    public long getRetryElectionPeriod() {
        long min = innerConfig.getLong("min_retry_election_period", 1000);
        long max = innerConfig.getLong("max_retry_election_period", 5000);

        Random random = new Random();
        int r = random.nextInt((int) (max - min));

        return r + min;
    }

    public long getWaitingPeriodForElectionComplete() {
        return innerConfig.getLong("period_for_waiting_election_complete", 5000);
    }

    public DataSyncService getDataSyncService() {
        return null;
    }

    public int getSyncThreadPoolCoreSize() {
        return innerConfig.getIntFromNestedMap("data_sync", "sync_threadpool_coresize", 0);
    }

    public int getSyncThreadPoolMaxSize() {
        return innerConfig.getIntFromNestedMap("data_sync", "sync_threadpool_maxsize", 2);
    }

    public long getSyncTimeout() {
        final long defaultValue = 10 * 1000L;
        return innerConfig.getLongFromNestedMap("data_sync", "sync_threadpool_maxsize", defaultValue);
    }

    public byte getMerkleTreeDepth() {
        return (byte) innerConfig.getIntFromNestedMap("storage", "merkle_tree_depth", 15);
    }

    public long getStateNodePurgeExpire() {
        return innerConfig.getLongFromNestedMap("data_sync", "state_node_purge_period", 30000);
    }

    public ClientService getStateService() {
        return null;
    }

    public long getRetryPeriod() {
        return innerConfig.getLong("service_retry_period", 3000);
    }

    public HopperService.Iface getHopperService() {
        return null;
    }

    /**
     * Read stream from path
     */
    private InputStream read(String path) throws IOException {

        if (path.startsWith(CLASSPATH_PREFIX)) {
            path = path.substring(CLASSPATH_PREFIX.length());
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            if (cl == null) {
                cl = this.getClass().getClassLoader();
            }
            return cl.getResourceAsStream(path);
        }

        if (path.startsWith(FILE_PREFIX)) {
            path = path.substring(FILE_PREFIX.length());
        }

        return new FileInputStream(path);
    }

    class InnerConfig {
        /**
         * Config root object
         */
        final Map<String, Object> yamlMap;

        final Map<Integer, Endpoint> endpointMap = new HashMap<Integer, Endpoint>();

        Endpoint localRpcEndpoint;
        Endpoint localServerEndpoint;

        InnerConfig(Map<String, Object> yamlMap) throws Exception {
            this.yamlMap = yamlMap;
            loadLocalEndpoint();
            loadGroup();
        }

        String getString(String key, String defaultValue) {
            Object v = yamlMap.get(key);

            if (v == null) {
                return defaultValue;
            }

            return (String) v;
        }

        long getLong(String key, long defaultValue) {
            Object v = yamlMap.get(key);

            if (v == null) {
                return defaultValue;
            }

            return Long.getLong(v.toString());
        }

        long getLongFromNestedMap(String mapKey, String itemKey, long defaultValue) {
            Map<String, Object> map = innerConfig.getMap(mapKey);

            if (map == null) {
                return defaultValue;
            }

            Object v = map.get(itemKey);

            if (v == null) {
                return defaultValue;
            }
            return Long.getLong(v.toString());
        }

        int getIntFromNestedMap(String mapKey, String itemKey, int defaultValue) {
            long v = getLongFromNestedMap(mapKey, itemKey, defaultValue);
            return (int) v;
        }

        Map<String, Object> getMap(String key) {
            Object v = yamlMap.get(key);
            if (v == null) {
                return null;
            }

            return (Map<String, Object>) v;
        }

        List<Object> getList(String key) {
            Object v = yamlMap.get(key);
            if (v == null) {
                return null;
            }

            return (List<Object>) v;
        }

        void loadGroup() throws Exception {
            List<Object> list = innerConfig.getList("group_nodes");
            if (list == null) {
                return;
            }

            // Load local endpoint configuration
            loadLocalEndpoint();

            for (Object o : list) {
                Map<String, String> map = (Map<String, String>) o;
                String serverId = map.get("serverId");
                String address = map.get("address");
                String port = map.get("port");

                Endpoint endpoint = new Endpoint(Integer.getInteger(serverId), InetAddress.getByName(address),
                        Integer.parseInt(port));

                endpointMap.put(endpoint.serverId, endpoint);
            }
        }

        private void loadLocalEndpoint() throws UnknownHostException {
            // Resolve rpc address
            String rpcAddress = (String) yamlMap.get("rpc_address");
            InetAddress _rpcAddress = null;
            if (rpcAddress == null || _rpcAddress.equals("")) {
                _rpcAddress = InetAddress.getLocalHost();
            } else {
                _rpcAddress = InetAddress.getByName(rpcAddress);
            }

            int rpcPort = (int) getLong("rpc_port", 7910);

            String s2sAddress = (String) yamlMap.get("s2s_address");
            InetAddress _s2sAddress = null;

            if (s2sAddress != null) {
                _s2sAddress = InetAddress.getByName(s2sAddress);
            }

            int s2sPort = (int) getLong("s2s_port", 7920);

            int localServerId = (int) getLong("serverId", 1);

            this.localRpcEndpoint = new Endpoint(localServerId, _rpcAddress, rpcPort);
            this.localServerEndpoint = new Endpoint(localServerId, _s2sAddress, s2sPort);
        }
    }
}
