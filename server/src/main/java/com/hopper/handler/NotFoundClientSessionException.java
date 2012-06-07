package com.hopper.handler;

public class NotFoundClientSessionException extends RuntimeException {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -3586008307643727927L;

	public final String sessionId;

	public NotFoundClientSessionException(String sessionId) {
		this.sessionId = sessionId;
	}
}
