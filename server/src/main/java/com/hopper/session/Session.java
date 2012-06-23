package com.hopper.session;

import com.hopper.future.LatchFuture;

/**
 * The interface representing a session between client/server and server.
 * 
 * @author chenguoqing
 * 
 */
public interface Session {

	/**
	 * Return the session id, the id is unique through cluster
	 */
	String getId();

	/**
	 * Whether the session is alive.
	 */
	boolean isAlive();

	/**
	 * Send packet with no response
	 */
	void sendOneway(Message message);

	/**
	 * Send synchronous message( waiting until operation complete), if
	 * exceptions occurs, {@link RuntimeException} will be thrown.
	 */
	void sendOnewayUntilComplete(Message message);
	
	/**
	 * Send packet with future
	 */
	LatchFuture<Message> send(Message message);

	/**
	 * Close the session
	 */
	void close();

	/**
	 * Set {@link SessionManager}
	 */
	void setSessionManager(SessionManager manager);

	/**
	 * Return the associated {@link SessionManager}
	 */
	SessionManager getSessionManager();

	/**
	 * Retrieve the associated {@link Connection}
	 */
	Connection getConnection();
}
