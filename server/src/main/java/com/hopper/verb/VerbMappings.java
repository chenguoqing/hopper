package com.hopper.verb;

import com.hopper.quorum.*;
import com.hopper.session.Serializer;
import com.hopper.util.merkle.Difference;
import com.hopper.util.merkle.MerkleTree;
import com.hopper.sync.*;
import com.hopper.verb.handler.*;

import java.util.EnumMap;

public class VerbMappings {

    /**
     * Mapping the verb to class
     */
    private static final EnumMap<Verb, Class<? extends Serializer>> classMappings = new EnumMap<Verb,
            Class<? extends Serializer>>(Verb.class);

    private static final EnumMap<Verb, VerbHandler> handlerMappings = new EnumMap<Verb, VerbHandler>(Verb.class);

    static {
        initialize();
    }

    private static void initialize() {

        // register body class
        registerVerbBody(Verb.REPLY_QUERY_LEADER, QueryLeader.class);
        registerVerbBody(Verb.HEART_BEAT, HeartBeat.class);
        registerVerbBody(Verb.PAXOS_PREPARE, Prepare.class);
        registerVerbBody(Verb.PAXOS_PROMISE, Promise.class);
        registerVerbBody(Verb.PAXOS_ACCEPT, Accept.class);
        registerVerbBody(Verb.PAXOS_ACCEPTED, Accepted.class);
        registerVerbBody(Verb.PAXOS_LEARN, Learn.class);
        registerVerbBody(Verb.QUERY_MAX_XID_RESULT, QueryMaxXid.class);
        registerVerbBody(Verb.REQUIRE_DIFF, RequireDiff.class);
        registerVerbBody(Verb.DIFF_RESULT, DiffResult.class);
        registerVerbBody(Verb.TREE_RESULT, MerkleTree.class);
        registerVerbBody(Verb.APPLY_DIFF, Difference.class);
        registerVerbBody(Verb.MUTATION, Mutation.class);
        registerVerbBody(Verb.NOTIFY_STATUS_CHANGE, NotifyStatusChange.class);

        // register response verb handler
        registerVerbHandler(Verb.QUERY_LEADER, new QueryLeaderVerbHandler());
        registerVerbHandler(Verb.REPLY_QUERY_LEADER, new ReplyVerbHandler());

        registerVerbHandler(Verb.PAXOS_PREPARE, new PrepareVerbHandler());
        registerVerbHandler(Verb.PAXOS_PROMISE, new ReplyVerbHandler());

        registerVerbHandler(Verb.PAXOS_ACCEPT, new AcceptVerbHandler());
        registerVerbHandler(Verb.PAXOS_ACCEPTED, new ReplyVerbHandler());

        registerVerbHandler(Verb.PAXOS_LEARN, new LearnVerbHandler());

        registerVerbHandler(Verb.TEST_LEADER, new TestLeaderVerbHandler());
        registerVerbHandler(Verb.TEST_LEADER_RESULT, new ReplyVerbHandler());

        registerVerbHandler(Verb.QUERY_MAX_XID, new QueryMaxXidVerbHandler());
        registerVerbHandler(Verb.QUERY_MAX_XID_RESULT, new ReplyVerbHandler());

        registerVerbHandler(Verb.RES_BOUND_MULTIPLEXER_SESSION, new ReplyVerbHandler());

        registerVerbHandler(Verb.REQUIRE_DIFF, new RequireDiffVerbHandler());
        registerVerbHandler(Verb.DIFF_RESULT, new ReplyVerbHandler());

        registerVerbHandler(Verb.REQUIRE_TREE, new RequireTreeVerbhandler());
        registerVerbHandler(Verb.TREE_RESULT, new ReplyVerbHandler());

        registerVerbHandler(Verb.APPLY_DIFF, new ApplyDiffVerbHandler());
        registerVerbHandler(Verb.APPLY_DIFF_RESULT, new ReplyVerbHandler());

        registerVerbHandler(Verb.MUTATION, new MutationVerbHandler());
        registerVerbHandler(Verb.REPLY_MUTATION, new ReplyVerbHandler());

        registerVerbHandler(Verb.NOTIFY_STATUS_CHANGE, new NotifyStatusChangeVerbHandler());
    }

    /**
     * Register the verb and handler
     */
    public static void registerVerbHandler(Verb verb, VerbHandler handler) {
        handlerMappings.put(verb, handler);
    }

    public static void registerVerbBody(Verb verb, Class<? extends Serializer> cz) {
        classMappings.put(verb, cz);
    }

    public static Class<? extends Serializer> getVerClass(Verb verb) {
        return classMappings.get(verb);
    }

    public static VerbHandler getVerbHandler(Verb verb) {
        return handlerMappings.get(verb);
    }
}
