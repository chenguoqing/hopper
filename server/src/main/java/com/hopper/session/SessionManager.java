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

    private final Map<Channel, IncomingServerSession> channelBoundIncomingSessions = new ConcurrentHashMap<Channel,
            IncomingServerSession>();

    /**
     * Create a {@link IncomingServerSession} implementation
     *
     * @param channel Netty communication channel
     * @return {@link LocalIncomingServerSession} instance for success; null for
     *         fail
     */
    public IncomingServerSession createIncomingSession(Channel channel) throws Exception {

        final Endpoint endpoint = config.getEndpoint(channel.getRemoteAddress());

        IncomingServerSession incommingSession = config.getSessionManager().getLocalIncommingSession(endpoint);

        if (incommingSession != null) {
            return incommingSession;
        }

        synchronized (endpoint) {

            incommingSession = config.getSessionManager().getLocalIncommingSession(endpoint);

            // dobule check
            if (incommingSession != null) {
                return incommingSession;
            }

            // Generate new session id
            String sessionId = SessionIdGenerator.generateSessionId();

            incommingSession = new LocalIncomingServerSession();
            ((LocalIncomingServerSession) incommingSession).setId(sessionId);
            incommingSession.setSessionManager(this);

            Connection conn = config.getConnectionManager().getConnection(channel);

            if (conn == null) {
                conn = new DummyConnection();
                ((DummyConnection) conn).setChannel(channel);
                conn.setSession(incommingSession);

                // Register the connection and channel to ConnectionManager
                config.getConnectionManager().addConnection(channel, conn);
            }

            ((LocalIncomingServerSession) incommingSession).setConnection(conn);

            // register session
            boundChannelToIncomingSession(channel, incommingSession);
        }

        return incommingSession;
    }

    /**
     * Create a {@link OutgoingServerSession} with endpoint
     */
    public LocalOutgoingServerSession createLocalOutgoingSession(Endpoint endpoint) throws Exception {

        LocalOutgoingServerSession session = config.getSessionManager().getLocalOutgoingServerSession(endpoint);

        if (session != null) {
            return session;
        }

        synchronized (endpoint) {
            session = config.getSessionManager().getLocalOutgoingServerSession(endpoint);
            if (session == null) {
                session = new LocalOutgoingServerSession();

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
    public void addIncommingSession(IncomingServerSession session) {
    }

    private void boundChannelToIncomingSession(Channel channel, IncomingServerSession session) {
        channelBoundIncomingSessions.put(channel, session);
    }

    /**
     * Remove the incomming session
     */
    public void removeIncommingSession(IncomingServerSession session) {
    }

    /**
     * Retrieve {@link IncomingServerSession} instance by session id
     */
    public IncomingServerSession getIncommingSession(String sessionid) {
        return null;
    }

    /**
     * Retrieve all incomming sessions
     */
    public IncomingServerSession[] getAllIncommingSessions() {
        return null;
    }

    public OutgoingServerSession[] getAllOutgoingSessions() {
        return null;
    }

    /**
     * Retrieve the master {@link IncomingServerSession} by the associated
     * {@link Channel}
     */
    public IncomingServerSession getLocalIncommingSession(Channel channel) {
        return null;
    }

    public IncomingServerSession getLocalIncommingSession(Endpoint endpoint) {
        return null;
    }

    /**
     * Retrieve all master incomming sessions
     */
    public IncomingServerSession[] getAllLocalIncommingSessions() {
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

    public ClientSession[] getAllClientSessions() {
        return null;
    }

    public void removeClientSession(String sessionId) {

    }

    public void addOutgoingServerSession(OutgoingServerSession session) {
    }

    public void removeOutgoingServerSession(OutgoingServerSession session) {
    }

    public OutgoingServerSession getOutgoingServerSession(String sessionId) {
        return null;
    }

    public LocalOutgoingServerSession getLocalOutgoingServerSession(Endpoint endpoint) {
        return null;
    }

    public OutgoingServerSession getOutgoingSession(IncomingServerSession incommingSession){
        return null;
    }
    public boolean isServerSession(String sessionId) {
        return false;
    }

    public boolean isIncomingServerSession(String sessionId) {
        return false;
    }

    public boolean isOutgoingServerSession(String sessionId) {
        return false;
    }

    public boolean isClientSession(String sessionId) {
        return false;
    }

    /**
     * Close all {@link IncomingServerSession} and {@link OutgoingServerSession}
     * associated with <tt>endpoint</tt>.
     */
    public void closeServerSession(Endpoint endpoint) {

    }
}
