package com.hopper.quorum;

/**
 * Leader election interface
 */
public interface LeaderElection {
    /**
     * Retrieve the Paxos instance bound with LeaderElection instance
     */
    Paxos getPaxos();

    /**
     * Starting leader election
     */
    void startElecting();
}
