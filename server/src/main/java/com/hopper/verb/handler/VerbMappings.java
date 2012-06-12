package com.hopper.verb.handler;

import com.hopper.quorum.PrepareVerbHandler;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import com.hopper.sync.*;
import com.hopper.session.Serializer;

import java.util.EnumMap;

public class VerbMappings {

    /**
     * Mapping the verb to class
     */
    private static final EnumMap<Verb, Class<? extends Serializer>> classMappings = new EnumMap<Verb,
            Class<? extends Serializer>>(Verb.class);

    private static final EnumMap<Verb, VerbHandler> handlerMappings = new EnumMap<Verb, VerbHandler>(Verb.class);

    static {
        classMappings.put(Verb.PAXOS_PREPARE, Prepare.class);
        classMappings.put(Verb.PAXOS_PROMISE, Promise.class);
        classMappings.put(Verb.PAXOS_ACCEPT, Accept.class);
        classMappings.put(Verb.REQUIRE_DIFF, RequireDiff.class);
        classMappings.put(Verb.DIFF_RESULT, DiffResult.class);
        classMappings.put(Verb.MUTATION, Mutation.class);

    }

    static {

        // register response verb handler
        handlerMappings.put(Verb.QUERY_LEADER, new QueryLeaderVerbHandler());
        handlerMappings.put(Verb.REPLY_QUERY_LEADER, new ReplyVerbHandler());

        handlerMappings.put(Verb.RES_BOUND_MULTIPLEXER_SESSION, new ReplyVerbHandler());
        handlerMappings.put(Verb.PAXOS_PREPARE, new PrepareVerbHandler());
        handlerMappings.put(Verb.PAXOS_ACCEPT, new AcceptVerbHandler());
        handlerMappings.put(Verb.REQUIRE_DIFF, new RequireDiffVerbHandler());
        handlerMappings.put(Verb.DIFF_RESULT, new ReplyVerbHandler());
        handlerMappings.put(Verb.REQUIRE_TREE, new RequireTreeVerbhandler());
        handlerMappings.put(Verb.TREE_RESULT, new ReplyVerbHandler());
        handlerMappings.put(Verb.APPLY_DIFF, new ApplyDiffVerbHandler());
        handlerMappings.put(Verb.APPLY_DIFF_RESULT, new ReplyVerbHandler());
        handlerMappings.put(Verb.MUTATION, new MutationVerbHandler());
    }

    public static Class<? extends Serializer> getVerClass(Verb verb) {
        return classMappings.get(verb);
    }

    public static VerbHandler getVerbHandler(Verb verb) {
        return handlerMappings.get(verb);
    }
}
