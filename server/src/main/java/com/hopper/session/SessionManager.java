package com.hopper.session;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Endpoint;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    /**
     * {@link com.hopper.GlobalConfiguration} instance
     */
    private static final GlobalConfiguration config = GlobalConfiguration.getInstance();

    private final Map<Channel, IncomingSession> channelBoundIncomingSessions = new ConcurrentHashMap<Channel, IncomingSession>();

    /**
     * Create a {@link IncomingSession} implementation
     *
     * @param channel Netty communication channel
     * @return {@link LocalIncomingSession} instance for success; null for
     *         fail
     */
    public IncomingSession createIncomingSession(Channel channel) throws Exception {

        final Endpoint endpoint = config.getEndpoint(channel.getRemoteAddress());

        IncomingSession incommingSession = config.getSessionManager().getLocalIncomingSession(endpoint);

        if (incommingSession != null) {
            return incommingSession;
        }

        synchronized (endpoint) {

            incommingSession = config.getSessionManager().getLocalIncomingSession(endpoint);

            // dobule check
            if (incommingSession != null) {
                return incommingSession;
            }

            // Generate new session id
            String sessionId = SessionIdGenerator.generateSessionId();

            incommingSession = new LocalIncomingSession();
            ((LocalIncomingSession) incommingSession).setId(sessionId);
            incommingSession.setSessionManager(this);

            Connection conn = config.getConnectionManager().getConnection(channel);

            if (conn == null) {
                conn = new DummyConnection();
                ((DummyConnection) conn).setChannel(channel);
                conn.setSession(incommingSession);

                // Register the connection and channel to ConnectionManager
                config.getConnectionManager().addConnection(channel, conn);
            }

            ((LocalIncomingSession) incommingSession).setConnection(conn);

            // register session
            boundChannelToIncomingSession(channel, incommingSession);
        }

        return incommingSession;
    }

    /**
     * Create a {@link OutgoingSession} with endpoint
     */
    public LocalOutgoingSession createLocalOutgoingSession(Endpoint endpoint) throws Exception {

        LocalOutgoingSession session = config.getSessionManager().getLocalOutgoingServerSession(endpoint);

        if (session != null) {
            return session;
        }

        synchronized (endpoint) {
            session = config.getSessionManager().getLocalOutgoingServerSession(endpoint);
            if (session == null) {
                session = new LocalOutgoingSession();

                // local session id
                session.setId(SessionIdGenerator.generateSessionId());

                // create connection
                Connection connection = config.getConnectionManager().createOutgoingServerConnection(session, endpoint);

                // bound the session to connection
                session.setConnection(connection);

                // register session to SessionManager
                addOutgoingServerSession(session);
            }
        }
        return session;
    }

    /**
     * Add incomming session
     */
    public void addIncommingSession(IncomingSession session) {
    }

    private void boundChannelToIncomingSession(Channel channel, IncomingSession session) {
        channelBoundIncomingSessions.put(channel, session);
    }

    /**
     * Remove the incomming session
     */
    public void removeIncommingSession(IncomingSession session) {
    }

    /**
     * Retrieve {@link IncomingSession} instance by session id
     */
    public IncomingSession getIncommingSession(String sessionid) {
        return null;
    }

    /**
     * Retrieve all incomming sessions
     */
    public IncomingSession[] getAllIncommingSessions() {
        return null;
    }

    public OutgoingSession[] getAllOutgoingSessions() {
        return null;
    }

    /**
     * Retrieve the master {@link IncomingSession} by the associated
     * {@link Channel}
     */
    public IncomingSession getLocalIncomingSession(Channel channel) {
        return null;
    }

    public IncomingSession getLocalIncomingSession(Endpoint endpoint) {
        return null;
    }

    /**
     * Retrieve all master incomming sessions
     */
    public IncomingSession[] getAllLocalIncommingSessions() {
        return null;
    }

    public void addClientSession(ClientSession session) {

    }

    public ClientSession getClientSession(String sessionId) {
        return null;
    }

    public ClientSession getClientSession(Channel channel) {
        return null;
    }

    public OutgoingSession getOutgoingSessionByMultiplexerSessionId(String multiplexerSessionId) {
        for (IncomingSession session : getAllIncommingSessions()) {
            if (session.containsMultiplexerSession(multiplexerSessionId)) {
                return getOutgoingSession(session);
            }
        }
        return null;
    }

    public ClientSession[] getAllClientSessions() {
        return null;
    }

    public void removeClientSession(String sessionId) {

    }

    public void addOutgoingServerSession(OutgoingSession session) {
    }

    public void removeOutgoingServerSession(OutgoingSession session) {
    }

    public OutgoingSession getOutgoingServerSession(String sessionId) {
        return null;
    }

    public LocalOutgoingSession getLocalOutgoingServerSession(Endpoint endpoint) {
        return null;
    }

    public OutgoingSession getOutgoingSession(IncomingSession incommingSession) {
        return null;
    }

    /**
     * Close all {@link IncomingSession} and {@link OutgoingSession}
     * associated with <tt>endpoint</tt>.
     */
    public void closeServerSession(Endpoint endpoint) {

    }
}
