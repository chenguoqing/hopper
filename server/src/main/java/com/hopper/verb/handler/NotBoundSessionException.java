package com.hopper.verb.handler;

import com.hopper.server.Endpoint;

public class NotBoundSessionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4886221293742580375L;

	public final String sessionId;
	public final Endpoint endpoint;

	public NotBoundSessionException(String sessionId, Endpoint endpoint) {
		this.sessionId = sessionId;
		this.endpoint = endpoint;
	}
}
