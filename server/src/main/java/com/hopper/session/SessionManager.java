package com.hopper.session;

import com.hopper.GlobalConfiguration;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Endpoint;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link SessionManager} packages all session operation
 *
 * @author chenguoqing
 */
public class SessionManager extends LifecycleProxy {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    /**
     * {@link com.hopper.GlobalConfiguration} instance
     */
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
    private final GlobalConfiguration config = componentManager.getGlobalConfiguration();
    /**
     * The mapping between channel and incoming session
     */
    private final Map<Channel, IncomingSession> incomingSessions = new ConcurrentHashMap<Channel, IncomingSession>();
    /**
     * Out going sessions
     */
    private final Map<Endpoint, OutgoingSession> outgoingSessions = new ConcurrentHashMap<Endpoint, OutgoingSession>();
    /**
     * Client sessions
     */
    private final Map<String, ClientSession> clientSessions = new ConcurrentHashMap<String, ClientSession>();

    /**
     * Create a {@link IncomingSession} implementation
     *
     * @param channel Netty communication channel
     * @return {@link LocalIncomingSession} instance for success; null for
     *         fail
     */
    public IncomingSession createIncomingSession(Channel channel) throws Exception {

        final Endpoint endpoint = config.getEndpoint(channel.getRemoteAddress());

        IncomingSession incomingSession = componentManager.getSessionManager().getIncomingSession(endpoint);

        if (incomingSession != null) {
            return incomingSession;
        }

        synchronized (endpoint) {

            incomingSession = componentManager.getSessionManager().getIncomingSession(endpoint);

            // double check
            if (incomingSession != null) {
                return incomingSession;
            }

            // Generate new session id
            String sessionId = SessionIdGenerator.generateSessionId();

            incomingSession = new LocalIncomingSession();
            ((LocalIncomingSession) incomingSession).setId(sessionId);
            incomingSession.setSessionManager(this);

            Connection conn = componentManager.getConnectionManager().getIncomingConnection(channel);

            if (conn == null) {
                conn = new DummyConnection();
                ((DummyConnection) conn).setChannel(channel);
                conn.setSession(incomingSession);

                // Register the connection and channel to ConnectionManager
                componentManager.getConnectionManager().addIncomingConnection(channel, conn);
            }

            ((LocalIncomingSession) incomingSession).setConnection(conn);

            // register session
            incomingSessions.put(channel, incomingSession);
        }

        return incomingSession;
    }

    /**
     * Create a {@link OutgoingSession} with endpoint
     */
    public OutgoingSession createOutgoingSession(Endpoint endpoint) throws Exception {

        OutgoingSession session = componentManager.getSessionManager().getOutgoingSession(endpoint);

        if (session != null) {
            return session;
        }

        synchronized (endpoint) {
            session = componentManager.getSessionManager().getOutgoingSession(endpoint);
            if (session == null) {
                session = new LocalOutgoingSession();

                // local session id
                ((LocalOutgoingSession) session).setId(SessionIdGenerator.generateSessionId());

                // create connection
                Connection connection = componentManager.getConnectionManager().createOutgoingConnection(session,
                        endpoint);

                // bound the session to connection
                ((LocalOutgoingSession) session).setConnection(connection);

                // register session to SessionManager
                addOutgoingServerSession(session);
            }
        }
        return session;
    }

    /**
     * Remove the incoming session
     */
    public void removeIncomingSession(IncomingSession session) {
        Channel boundChannel = null;

        for (Channel channel : incomingSessions.keySet()) {
            IncomingSession session1 = incomingSessions.get(channel);
            if (session == session1) {
                boundChannel = channel;
                break;
            }
        }

        if (boundChannel != null) {
            incomingSessions.remove(boundChannel);
        }
    }

    /**
     * Retrieve all incoming sessions
     */
    public IncomingSession[] getAllIncomingSessions() {
        return incomingSessions.values().toArray(new IncomingSession[]{});
    }

    /**
     * Retrieve the master {@link IncomingSession} by the associated
     * {@link Channel}
     */
    public IncomingSession getIncomingSession(Channel channel) {
        return incomingSessions.get(channel);
    }

    /**
     * Retrieve the IncomingSession bound with <code>endpoint</code>
     */
    public IncomingSession getIncomingSession(Endpoint endpoint) {
        for (IncomingSession session : getAllIncomingSessions()) {
            if (session.getConnection().getSourceEndpoint() == endpoint) {
                return session;
            }
        }
        return null;
    }

    /**
     * Add client session
     */
    public void addClientSession(ClientSession clientSession) {
        clientSessions.put(clientSession.getId(), clientSession);
    }

    public ClientSession getClientSession(String sessionId) {
        return clientSessions.get(sessionId);
    }

    public ClientSession getClientSession(Channel channel) {
        for (ClientSession session : clientSessions.values()) {
            if (session.getConnection().getChannel() == channel) {
                return session;
            }
        }
        return null;
    }

    /**
     * Retrieve the {@link OutgoingSession} by multiplexer session id
     */
    public OutgoingSession getOutgoingSessionByMultiplexerSessionId(String multiplexerSessionId) {
        for (IncomingSession session : getAllIncomingSessions()) {
            if (session.containsMultiplexerSession(multiplexerSessionId)) {
                return getOutgoingSession(session);
            }
        }
        return null;
    }

    private OutgoingSession getOutgoingSession(IncomingSession incomingSession) {
        Endpoint endpoint = incomingSession.getConnection().getSourceEndpoint();
        return getOutgoingSession(endpoint);
    }

    public ClientSession[] getAllClientSessions() {
        return clientSessions.values().toArray(new ClientSession[]{});
    }

    public void removeClientSession(String sessionId) {
        clientSessions.remove(sessionId);
    }

    public void addOutgoingServerSession(OutgoingSession session) {
        outgoingSessions.put(session.getConnection().getDestEndpoint(), session);
    }

    public void removeOutgoingServerSession(OutgoingSession session) {
        outgoingSessions.remove(session.getConnection().getDestEndpoint());
    }

    public OutgoingSession getOutgoingSession(String sessionId) {
        return null;
    }

    public OutgoingSession getOutgoingSession(Endpoint endpoint) {
        return outgoingSessions.get(endpoint);
    }

    /**
     * Close all {@link IncomingSession} and {@link OutgoingSession}
     * associated with <tt>endpoint</tt>.
     */
    public void closeServerSession(Endpoint endpoint) {
        IncomingSession incomingSession = getIncomingSession(endpoint);
        if (incomingSession != null) {
            incomingSession.close();
        }

        OutgoingSession outgoingSession = getOutgoingSession(endpoint);
        if (outgoingSession != null) {
            outgoingSession.close();
        }
    }

    @Override
    protected void doShutdown() {
        for (IncomingSession session : incomingSessions.values()) {
            session.close();
        }

        for (OutgoingSession session : outgoingSessions.values()) {
            session.close();
        }

        for (ClientSession session : clientSessions.values()) {
            session.close();
        }

        incomingSessions.clear();
        outgoingSessions.clear();
        clientSessions.clear();
    }

    @Override
    public String getInfo() {
        return "Session Manager";
    }
}
