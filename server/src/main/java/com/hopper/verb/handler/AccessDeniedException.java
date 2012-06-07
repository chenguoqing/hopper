package com.hopper.verb.handler;

import java.net.SocketAddress;

/**
 * The exception representing a rejected remote connect. It can be used for
 * various risk controls.
 * 
 * @author chenguoqing
 * 
 */
public class AccessDeniedException extends RuntimeException {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -2099098300193117888L;
	/**
	 * Denied address
	 */
	public final SocketAddress address;

	public AccessDeniedException(SocketAddress address) {
		this.address = address;
	}
}
