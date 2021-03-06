package com.hopper.session;

import com.hopper.lifecycle.LifecycleEvent;
import com.hopper.lifecycle.LifecycleEvent.EventType;
import com.hopper.lifecycle.LifecycleListener;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Endpoint;
import com.hopper.server.Server;
import com.hopper.util.ScheduleManager;
import com.hopper.verb.Verb;
import com.hopper.verb.handler.BatchMultiplexerSessions;

public class LocalOutgoingSession extends SessionProxy implements OutgoingSession, LifecycleListener {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    /**
     * Schedule manager
     */
    private final ScheduleManager scheduleManager = componentManager.getScheduleManager();

    @Override
    public boolean isAlive() {
        if (getConnection() == null) {
            return false;
        }

        return getConnection().validate();
    }

    @Override
    public boolean support(EventType eventType) {
        return eventType == EventType.SHUTDOWNING;
    }

    /**
     * When shutdowns {@link OutgoingSession}, should send close message
     * to remote server, then release local resources.
     */
    @Override
    public void lifecycle(LifecycleEvent event) {

        final Server server = componentManager.getDefaultServer();

        ClientSession[] clientSessions = sessionManager.getAllClientSessions();

        Endpoint destEndpoint = getConnection().getDestEndpoint();

        if (clientSessions != null && clientSessions.length > 0 && server.isKnownLeader() && !server.isLeader() &&
                server.getLeader() != destEndpoint.serverId) {

            Message message = new Message();
            message.setVerb(Verb.UNBOUND_MULTIPLEXER_SESSION);

            BatchMultiplexerSessions batch = new BatchMultiplexerSessions();
            for (ClientSession clientSession : clientSessions) {
                batch.add(clientSession.getId());
            }

            message.setBody(batch);

            // notify endpoint
            componentManager.getMessageService().sendOneway(message, server.getLeader());
        }

        // remove self from session manager
        sessionManager.removeOutgoingServerSession(this);
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return "type=OutgoingSession,id=" + getId();
    }

    @Override
    public void background() {
    }
}
