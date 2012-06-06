package com.hopper.session;

/**
 * {@link OutgoingServerSession} representing a outer connection
 * 
 * @author chenguoqing
 * 
 */
public interface OutgoingServerSession extends ServerSession {
	/**
	 * Starting the background processing
	 */
	void background();
}
