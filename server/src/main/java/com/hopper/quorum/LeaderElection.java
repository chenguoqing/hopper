package com.hopper.quorum;

import com.hopper.lifecycle.Lifecycle;

/**
 * Leader election interface
 */
public interface LeaderElection extends Lifecycle {
    /**
     * Retrieve the Paxos instance bound with LeaderElection instance
     */
    Paxos getPaxos();

    /**
     * Starting leader election
     */
    void startElecting();
}
