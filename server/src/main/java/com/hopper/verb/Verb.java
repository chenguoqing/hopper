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
    UNBOUND_MULTIPLEXER_SESSION(3),
    /**
     * (Request)Register multiplexer session
     */
    BOUND_MULTIPLEXER_SESSION(3),
    /**
     * (Response)Register multiplexer session
     */
    RES_BOUND_MULTIPLEXER_SESSION(4),
    /**
     * Leader duplicate data to follower
     */
    REQ_DUPLICATE(1), RES_DUPLICATE(2),
    /**
     * Acquire current leader
     */
    QUERY_LEADER(3),
    /**
     * Response the query leader
     */
    REPLY_QUERY_LEADER(4),

    TEST_LEADER(4),
    TEST_LEADER_RESULT(4),

    RES_TEST_LEADER(5),
    /**
     * Acquire max xid
     */
    QUERY_MAX_XID(4),
    /**
     * Compare hash tree
     */
    HASH_COMPARE(5),
    /**
     * Acquire follower
     */
    ACQUIRE_SYNCHRONIZED(6),
    /**
     * Paxos(Leader election) : Phase1a
     */
    PAXOS_PREPARE(7),
    /**
     * Paxos(leader election) : Phase1b
     */
    PAXOS_PROMISE(9),
    /**
     * Paxos(Leader election) : Phase2a
     */
    PAXOS_ACCEPT(8),

    PAXOS_ACCEPTED(8),
    /**
     * Paxos learn message
     */
    PAXOS_LEARN(9),
    /**
     * Ping request
     */
    HEART_BEAT(11),

    REQUIRE_DIFF(105),
    DIFF_RESULT(105),
    REQUIRE_TREE(109),
    TREE_RESULT(200),
    APPLY_DIFF(210),
    APPLY_DIFF_RESULT(211),
    REQUIRE_LEARN_DIFF(106),
    LEARN_DIFF_RESULT(106),
    STATE_MUTATION(222),
    STATE_MUTATION_REPLY(222),
    MUTATION(223),
    REPLY_MUTATION(224),
    SYNC(225),
    SYNC_RESULT(226),
    /**
     * Expand lease
     */
    NOTIFY_STATUS_CHANGE(104);

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
