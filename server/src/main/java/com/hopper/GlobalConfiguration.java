package com.hopper;

import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * {@link GlobalConfiguration} maintains all configuration items for hopper that will be loaded from YAML
 * configuration file on startup.
 */
@SuppressWarnings("unchecked")
public class GlobalConfiguration extends LifecycleProxy {
    /**
     * Server mode(single node or multiple nodes)
     */
    public static enum ServerMode {
        SINGLE, MULTI
    }

    public static enum StorageMode {
        HASH, TREE
    }

    public static enum ElectionMode {
        FAIR, FAST
    }

    private static final Logger logger = LoggerFactory.getLogger(GlobalConfiguration.class);

    /**
     * Default system property
     */
    public static final String DEFAULT_SYSTEM_PROPERTY = "configFile";
    /**
     * Resource prefix: classpath
     */
    private static final String CLASSPATH_PREFIX = "classpath:";
    /**
     * Resource prefix: absolute path
     */
    private static final String FILE_PREFIX = "file:";
    /**
     * Default configuration path
     */
    private static final String defaultYAML = CLASSPATH_PREFIX + "/conf/hopper.yaml";
    /**
     * Configuration root object
     */
    private InnerConfig innerConfig;

    @Override
    protected void doInit() {
        // Get the configuration file path
        String configPath = System.getProperty(DEFAULT_SYSTEM_PROPERTY, defaultYAML);

        try {
            InputStream in = read(configPath);
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(in);
            this.innerConfig = new InnerConfig(yamlMap);

            logger.info("Used the configuration file:{}", configPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load yaml configuration file:" + configPath, e);
        }
    }

    public ServerMode getServerMode() {
        String mode = innerConfig.getString("server_mode", ServerMode.SINGLE.name());

        ServerMode _mode = ServerMode.valueOf(mode.toUpperCase());

        return _mode == null ? ServerMode.SINGLE : _mode;
    }

    public StorageMode getStorageMode() {
        String mode = innerConfig.getString("storage_mode", StorageMode.HASH.name());
        StorageMode _mode = StorageMode.valueOf(mode.toUpperCase());

        return _mode == null ? StorageMode.HASH : _mode;
    }

    public ElectionMode getElectionMode() {
        String mode = innerConfig.getString("election_mode", ElectionMode.FAIR.name());
        ElectionMode _mode = ElectionMode.valueOf(mode.toUpperCase());

        return _mode == null ? ElectionMode.FAIR : _mode;
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

    public long getCacheEvictPeriod() {
        return innerConfig.getLong("cache_evict_period", 30000);
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

    public Endpoint getLocalRpcEndpoint() {
        return innerConfig.localRpcEndpoint;
    }

    public Endpoint getLocalServerEndpoint() {
        return innerConfig.localServerEndpoint;
    }

    public Endpoint getEndpoint(int serverId) {
        return innerConfig.endpointMap.get(serverId);
    }

    public int getScheduleThreadCount() {
        return (int) innerConfig.getLong("schedule_thread_count", 3);
    }

    public Endpoint[] getGroupEndpoints() {
        return innerConfig.endpointMap.values().toArray(new Endpoint[0]);
    }

    public int getQuorumSize() {
        return (getGroupEndpoints().length / 2) + 1;
    }

    /**
     * Return the base ballot id for server.
     * <p/>
     * It will map all server id to ballot id that from 0 to server count(exclude),
     * {@link com.hopper.quorum.BallotGenerator} will generate a ballot for server by the base ballot id.
     */
    public int getServerBallotId(int serverId) {
        return innerConfig.ballotIds.get(serverId);
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

    public long getRetryPeriod() {
        return innerConfig.getLong("service_retry_period", 3000);
    }

    public int getShutdownPort() {
        return innerConfig.getInt("shutdown_port", 7930);
    }

    public String getShutdownCommand() {
        return innerConfig.getString("shutdown_command", "shutdown");
    }

    public Map<String, Object> getRpcTcpSettings() {
        return getTcpSettings("rpc_tcp");
    }

    public Map<String, Object> getS2STcpSettings() {
        return getTcpSettings("s2s_tcp");
    }

    private Map<String, Object> getTcpSettings(String key) {
        List<Object> list = innerConfig.getList(key);

        Map<String, Object> tcpSettings = defaultTCPSettings();

        if (list != null) {
            for (Object o : list) {
                Map<String, Object> map = (Map<String, Object>) o;
                tcpSettings.putAll(map);
            }
        }

        return tcpSettings;
    }

    private Map<String, Object> defaultTCPSettings() {
        Map<String, Object> tcpSettings = new HashMap<String, Object>();
        tcpSettings.put("child.tcpNoDelay", true);
        tcpSettings.put("child.keepAlive", true);
        tcpSettings.put("reuseAddress", true);

        return tcpSettings;
    }

    /**
     * Read stream from path
     */
    private InputStream read(String path) throws IOException {

        if (path.startsWith(CLASSPATH_PREFIX)) {
            path = path.substring(CLASSPATH_PREFIX.length());
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            InputStream in = null;
            if (cl != null) {
                in = cl.getResourceAsStream(path);

            }

            if (in == null) {
                in = this.getClass().getResourceAsStream(path);
            }
            return in;
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

        /**
         * The map of server id and endpoint
         */
        final Map<Integer, Endpoint> endpointMap = new HashMap<Integer, Endpoint>();
        /**
         * The map of server id and base ballot id
         */
        final Map<Integer, Integer> ballotIds = new HashMap<Integer, Integer>();

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

            if (v instanceof Integer) {
                return ((Integer) v).intValue();
            }
            return (Long) v;
        }

        int getInt(String key, int defaultValue) {
            Object v = yamlMap.get(key);

            if (v == null) {
                return defaultValue;
            }

            return (Integer) v;
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

            if (v instanceof Integer) {
                return ((Integer) v).intValue();
            }
            return (Long) v;
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
            List<Object> list = getList("group_nodes");
            if (list == null) {
                return;
            }

            // Load local endpoint configuration
            loadLocalEndpoint();

            for (Object o : list) {
                Map<String, Object> map = (Map<String, Object>) o;
                Integer serverId = (Integer) map.get("serverId");
                String address = (String) map.get("address");
                Integer port = (Integer) map.get("port");

                Endpoint endpoint = new Endpoint(serverId, InetAddress.getByName(address), port);

                endpointMap.put(endpoint.serverId, endpoint);
            }

            List<Integer> serverIdList = new ArrayList<Integer>(endpointMap.keySet());
            Collections.sort(serverIdList);

            for (int i = 0; i < serverIdList.size(); i++) {
                ballotIds.put(serverIdList.get(i), i);
            }
        }

        private void loadLocalEndpoint() throws UnknownHostException {
            // Resolve rpc address
            String rpcAddress = (String) yamlMap.get("rpc_address");
            InetAddress _rpcAddress;
            if (rpcAddress == null || rpcAddress.equals("")) {
                _rpcAddress = InetAddress.getLocalHost();
            } else {
                _rpcAddress = InetAddress.getByName(rpcAddress);
            }

            int rpcPort = getInt("rpc_port", 7910);

            String s2sAddress = (String) yamlMap.get("s2s_address");
            InetAddress _s2sAddress = null;

            if (s2sAddress != null) {
                _s2sAddress = InetAddress.getByName(s2sAddress);
            }

            int s2sPort = getInt("s2s_port", 7920);

            int localServerId = getInt("serverId", 1);

            this.localRpcEndpoint = new Endpoint(localServerId, _rpcAddress, rpcPort);
            this.localServerEndpoint = new Endpoint(localServerId, _s2sAddress, s2sPort);
        }
    }

    @Override
    public String getInfo() {
        return "Global Configuration";
    }
}
