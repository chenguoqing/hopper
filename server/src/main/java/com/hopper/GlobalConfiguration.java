package com.hopper;

import com.hopper.cache.CacheManager;
import com.hopper.quorum.LeaderElection;
import com.hopper.server.Endpoint;
import com.hopper.server.Server;
import com.hopper.server.avro.ClientService;
import com.hopper.server.handler.ConnectionManager;
import com.hopper.server.sync.DataSyncService;
import com.hopper.server.thrift.HopperService;
import com.hopper.session.SessionManager;
import com.hopper.utils.ScheduleManager;

import java.net.SocketAddress;

public class GlobalConfiguration {

    private static volatile GlobalConfiguration instance = new GlobalConfiguration();

    private GlobalConfiguration() {
    }

    public static GlobalConfiguration getInstance() {
        return instance;
    }

    /**
     * Return the server rpc timeout(milliseconds)
     */
    public long getRpcTimeout() {
        return 0;
    }

    public long getPingInterval() {
        return 0;
    }

    public boolean canConnect(SocketAddress address) {
        return true;
    }

    public Endpoint getEndpoint(SocketAddress address) {
        return null;
    }

    public boolean isLocalEndpoint(Endpoint endpoint) {
        return false;
    }

    public Endpoint getLocalEndpoint() {
        return null;
    }

    public Endpoint getEndpoint(int serverId) {
        return null;
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
        return null;
    }

    public int getQuorumSize() {
        return 0;
    }

    public int getBallotServerId(int serverId) {
        return serverId;
    }

    public int getLocalBallotServerId() {
        return 0;
    }

    public long getPeriodForJoin() {
        return 0;
    }

    public long getPeriodForPaxosRejected() {
        return 0;
    }

    public long getPeriodForRetryElection() {
        return 0;
    }

    public long getWaitingPeriodForElectionComplete() {
        return 0;
    }

    public DataSyncService getDataSyncService() {
        return null;
    }

    public int getSyncThreadPoolCoreSize() {
        return 0;
    }

    public int getSyncThreadPoolMaxSize() {
        return 0;
    }

    public long getSyncTimeout() {
        return 10 * 1000L;
    }

    public byte getMerkleTreeDepth() {
        return (byte) 15;
    }

    public long getStateNodePurgeExpire() {
        return 0;
    }

    public long getStateNodePurgeThreadPeriod() {
        return 0;
    }

    public ClientService getStateService() {
        return null;
    }

    public int getRetryPeriod() {
        return 0;
    }

    public HopperService.Iface getHopperService() {
        return null;
    }
}
