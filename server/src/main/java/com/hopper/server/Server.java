package com.hopper.server;

import com.hopper.lifecycle.Lifecycle;
import com.hopper.quorum.Paxos;
import com.hopper.storage.StateStorage;

import java.util.concurrent.TimeoutException;

/**
 * The {@link Server} representing a hopper socket server that can accept TCP/IP
 * connection form client or server. The server will start with two ports : 6000
 * for client request,and 6001 for server internal communication.
 *
 * @author chenguoqing
 */
public interface Server extends Lifecycle {

    /**
     * Server service state for election. Translation relation:
     * <p/>
     * (FOLLOWING|LEADING)---->LOOKING---->SYNC---->(FOLLOWING|LEADING)
     */
    public enum ElectionState {
        /**
         * Serving as a follower
         */
        FOLLOWING,
        /**
         * Serving as a leader
         */
        LEADING,
        /**
         * Out of service, leader election
         */
        LOOKING,
        /**
         * Leader out of service, data synchronization
         */
        SYNC,
        /**
         * Sync fail
         */
        SYNC_FAILED;
    }

    /**
     * Set the address and port for accepting outer request
     */
    void setRpcEndpoint(Endpoint endpoint);

    /**
     * Retrieve the {@link Endpoint} of outer communication
     */
    Endpoint getRpcEndPoint();

    /**
     * Set the address and port for internal communication(server-to-server)
     */
    void setServerEndpoint(Endpoint serverEndpoint);

    /**
     * Retrieve the {@link Endpoint} for internal communication
     */
    Endpoint getServerEndpoint();

    /**
     * Set {@link Paxos} for election
     */
    void setPaxos(Paxos paxos);

    /**
     * Retrieve the associated {@link Paxos}
     */
    Paxos getPaxos();

    /**
     * Set the leader server id
     */
    void setLeader(int serverId);

    /**
     * Retrieve the local leader server id
     */
    int getLeader();

    boolean isKnownLeader();

    void getLeaderWithLock(long timeout) throws TimeoutException, InterruptedException;

    /**
     * Clear the associated leader
     */
    void clearLeader();

    /**
     * Abandon the leader identity
     */
    void abandonLeadership();

    /**
     * Take over the leader identity
     */
    void takeLeadership();

    /**
     * Whether existing a local leader
     */
    boolean hasLeader();

    /**
     * Test if the local endpoint is test?
     */
    boolean isLeader();

    /**
     * If the special {@link Endpoint} is leader
     */
    boolean isLeader(Endpoint endpoint);

    /**
     * Test if the local endpoint is follower
     */
    boolean isFollower();

    /**
     * Whether or not the special endpoint is a follower?
     */
    boolean isFollower(Endpoint endpoint);

    /**
     * Set the {@link ElectionState}
     */
    void setElectionState(ElectionState state);

    /**
     * Retrieve current {@link ElectionState}
     */
    ElectionState getElectionState();

    /**
     * Check if the server can service normally,if not throws  ServiceUnavailableException
     */
    void assertServiceAvailable() throws ServiceUnavailableException;
}
