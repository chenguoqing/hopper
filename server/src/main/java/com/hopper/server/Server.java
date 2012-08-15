package com.hopper.server;

import com.hopper.lifecycle.Lifecycle;

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
     * Set the unique id
     */
    void setId(int id);

    /**
     * Return the unique server id
     */
    int getId();

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
     * Set the leader server id
     */
    void setLeader(int serverId);

    /**
     * Retrieve the local leader server id
     */
    int getLeader();

    /**
     * Test if the local node is leader
     */
    boolean isLeader();

    /**
     * If the special server id is leader
     */
    boolean isLeader(int serverId);

    /**
     * The if the node knows leader
     */
    boolean isKnownLeader();

    /**
     * Clear the associated leader
     */
    void clearLeader();

    /**
     * Waiting until the leader has benn set
     *
     * @param timeout wait milliseconds
     * @throws TimeoutException     If time out
     * @throws InterruptedException If thread has been interrupted
     */
    void awaitLeader(long timeout) throws TimeoutException, InterruptedException;

    /**
     * Abandon the leader identity
     */
    void abandonLeadership();

    /**
     * Take over the leader identity
     */
    void takeLeadership();

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
