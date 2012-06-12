package com.hopper.quorum;

/**
 * {@link PaxosResult} represents a election result
 */
public class PaxosResult {
    public final int instance;
    public final int leader;

    public PaxosResult(int instance, int leader) {
        this.instance = instance;
        this.leader = leader;
    }
}
