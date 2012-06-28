package com.hopper.session;

/**
 * {@link OutgoingSession} representing a outer connection
 *
 * @author chenguoqing
 *
 */
public interface OutgoingSession extends Session {
	/**
	 * Starting the background processing
	 */
	void background();
}
