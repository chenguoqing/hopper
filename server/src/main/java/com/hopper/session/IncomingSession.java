package com.hopper.session;

import java.util.List;

public interface IncomingSession extends Session {

    /**
     * Processing receiving message
     */
    void receive(Message message);

    /**
     * Add a multiplexer session id
     */
    void boundMultiplexerSession(String sessionId);

    /**
     * Remove a multiplexer session id
     */
    void unboundMultiplexerSession(String sessionId);

    /**
     * Contains?
     */
    boolean containsMultiplexerSession(String sessionId);

    /**
     * Retrieve all bound multiplexer sessions
     */
    List<String> getBoundMultiplexerSessions();
}
