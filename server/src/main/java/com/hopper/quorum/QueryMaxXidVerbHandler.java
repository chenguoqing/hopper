package com.hopper.quorum;

import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The handler for querying max xid
 */
public class QueryMaxXidVerbHandler implements VerbHandler {
    private static final Logger logger = LoggerFactory.getLogger(QueryMaxXidVerbHandler.class);
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    @Override
    public void doVerb(Message message) {

        logger.info("Received the query max xid request {}", message);
        Message reply = new Message();
        reply.setVerb(Verb.QUERY_MAX_XID_RESULT);

        QueryMaxXid queryMaxXid = new QueryMaxXid();
        queryMaxXid.setMaxXid(componentManager.getStateStorage().getMaxXid());
        queryMaxXid.setServerId(componentManager.getGlobalConfiguration().getLocalServerEndpoint().serverId);

        reply.setBody(queryMaxXid);

        logger.info("Response the query max xid request {}", reply);
        componentManager.getMessageService().responseOneway(reply);
    }
}
