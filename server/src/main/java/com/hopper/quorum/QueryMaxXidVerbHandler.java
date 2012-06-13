package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;

/**
 * The handler for querying max xid
 */
public class QueryMaxXidVerbHandler implements VerbHandler {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {

        Message reply = new Message();
        reply.setVerb(Verb.QUERY_MAX_XID);
        reply.setId(message.getId());

        QueryMaxXid queryMaxXid = new QueryMaxXid();
        queryMaxXid.setMaxXid(componentManager.getStateStorage().getMaxXid());
        queryMaxXid.setServerId(componentManager.getGlobalConfiguration().getLocalServerEndpoint().serverId);

        reply.setBody(queryMaxXid);

        componentManager.getMessageService().responseOneway(reply);
    }
}
