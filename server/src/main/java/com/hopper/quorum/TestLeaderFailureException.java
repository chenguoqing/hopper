package com.hopper.quorum;

/**
 *
 */
public class TestLeaderFailureException extends RuntimeException {

    public final int leader;

    public TestLeaderFailureException(int leader) {
        this.leader = leader;
    }

    public TestLeaderFailureException(int leader, String message) {
        super(message);
        this.leader = leader;
    }

    public TestLeaderFailureException(int leader, String message, Throwable cause) {
        super(message, cause);
        this.leader = leader;
    }

    public TestLeaderFailureException(int leader, Throwable cause) {
        super(cause);
        this.leader = leader;
    }
}
