package com.hopper.session;

import com.hopper.future.LatchFuture;
import com.hopper.lifecycle.LifecycleEvent;
import com.hopper.lifecycle.LifecycleEvent.EventType;
import com.hopper.lifecycle.LifecycleListener;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import com.hopper.verb.VerbMappings;
import com.hopper.verb.handler.HeartBeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The default implementation of IncomingSession
 */
public class LocalIncomingSession extends SessionProxy implements IncomingSession, LifecycleListener {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(LocalIncomingSession.class);

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private final List<String> multiplexerSessions = Collections.synchronizedList(new ArrayList<String>());

    private final AtomicLong lastHeartBeat = new AtomicLong(-1L);

    @Override
    public boolean isAlive() {
        if (getConnection() == null) {
            throw new IllegalStateException("Not bound any connection.");
        }

        if (lastHeartBeat.get() == -1L) {
            return getConnection().validate();
        }

        return System.currentTimeMillis() - lastHeartBeat.get() < componentManager.getGlobalConfiguration()
                .getRpcTimeout();
    }

    /**
     * Delegate the processing to {@link VerbHandler}
     */
    @Override
    public void receive(Message message) {

        // processing heart beat request
        if (message.getVerb() == Verb.HEART_BEAT) {
            processHeartBeat(message);

        } else if (message.getVerb() == Verb.UNBOUND_MULTIPLEXER_SESSION) {
            String multiplexerSessionId = new String((byte[]) message.getBody());
            unboundMultiplexerSession(multiplexerSessionId);

            // processing the message on local
        } else {

            VerbHandler handler = VerbMappings.getVerbHandler(message.getVerb());

            // delegate processing to VerbHeandler
            if (handler != null) {
                handler.doVerb(message);
            } else {
                logger.warn("Not found ver handler for " + message.getVerb());
            }
        }
    }

    private void processHeartBeat(Message message) {

        HeartBeat beat = (HeartBeat) message.getBody();

        // ignore the heart beat from follower
        if (beat.isLeader()) {
            // records the last heart beat time
            lastHeartBeat.set(System.currentTimeMillis());

            // If the leader's fresh, starting data synchronous
            if (beat.getMaxXid() > componentManager.getStateStorage().getMaxXid()) {
                componentManager.getDataSyncService().syncDataFromRemote(componentManager.getDefaultServer()
                        .getLeader());
            }
        }
    }

    /**
     * Send message without response
     */
    @Override
    public void sendOneway(Message message) {
        throw new UnsupportedOperationException();
    }

    /**
     * Send message to endpoint and return the response
     */
    @Override
    public LatchFuture<Message> send(final Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean support(EventType eventType) {
        return eventType == EventType.SHUTDOWNING;
    }

    /**
     * When shutdowns the {@link IncomingSession}, will directly close the
     * associated {@link Connection} without sending any close message.
     * <p/>
     * Generally, the {@link #close()} method will be triggered from remote
     * request.
     */
    @Override
    public void lifecycle(LifecycleEvent event) {
        // remove self from session manager
        sessionManager.removeIncomingSession(this);
        multiplexerSessions.clear();
    }

    @Override
    public void boundMultiplexerSession(String sessionId) {
        if (!multiplexerSessions.contains(sessionId)) {
            multiplexerSessions.add(sessionId);
        }
    }

    @Override
    public void unboundMultiplexerSession(String sessionId) {
        multiplexerSessions.remove(sessionId);
    }

    @Override
    public boolean containsMultiplexerSession(String sessionId) {
        return multiplexerSessions.contains(sessionId);
    }

    @Override
    public List<String> getBoundMultiplexerSessions() {
        return Collections.unmodifiableList(multiplexerSessions);
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return "type=Session,direction=incoming,id=" + getId();
    }
}
