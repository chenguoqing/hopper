package com.hopper.verb;

/**
 * List all server to server request type
 *
 * @author chenguoqing
 */
public enum Verb {

    /**
     * Close session
     */
    UNBOUND_MULTIPLEXER_SESSION(1),
    /**
     * (Request)Register multiplexer session
     */
    BOUND_MULTIPLEXER_SESSION(2),
    /**
     * (Response)Register multiplexer session
     */
    RES_BOUND_MULTIPLEXER_SESSION(3),
    /**
     * Acquire current leader
     */
    QUERY_LEADER(4),
    /**
     * Response the query leader
     */
    REPLY_QUERY_LEADER(5),

    TEST_LEADER(6),
    TEST_LEADER_RESULT(7),
    /**
     * Acquire max xid
     */
    QUERY_MAX_XID(8),
    QUERY_MAX_XID_RESULT(9),
    /**
     * Compare hash tree
     */
    HASH_COMPARE(10),
    /**
     * Acquire follower
     */
    ACQUIRE_SYNCHRONIZED(11),
    /**
     * Paxos(Leader election) : Phase1a
     */
    PAXOS_PREPARE(12),
    /**
     * Paxos(leader election) : Phase1b
     */
    PAXOS_PROMISE(13),
    /**
     * Paxos(Leader election) : Phase2a
     */
    PAXOS_ACCEPT(14),

    PAXOS_ACCEPTED(15),
    /**
     * Paxos learn message
     */
    PAXOS_LEARN(16),
    /**
     * Ping request
     */
    HEART_BEAT(17),

    REQUIRE_DIFF(18),
    DIFF_RESULT(19),
    REQUIRE_TREE(20),
    TREE_RESULT(21),
    APPLY_DIFF(22),
    APPLY_DIFF_RESULT(23),
    REQUIRE_LEARN_DIFF(24),
    LEARN_DIFF_RESULT(25),
    STATE_MUTATION(26),
    STATE_MUTATION_REPLY(27),
    MUTATION(28),
    REPLY_MUTATION(29),
    SYNC(30),
    SYNC_RESULT(31),
    /**
     * Expand lease
     */
    NOTIFY_STATUS_CHANGE(32);

    public final int type;

    Verb(int type) {
        this.type = type;
    }

    public static Verb getVerb(int type) {
        for (Verb v : Verb.values()) {
            if (v.type == type) {

                return v;
            }
        }
        return null;
    }
}
