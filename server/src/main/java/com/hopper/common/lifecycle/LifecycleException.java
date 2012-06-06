package com.hopper.common.lifecycle;

/**
 * {@link LifecycleException} packaged some exception information occurred on
 * life cycle stage.
 * 
 * @author chenguoqing
 * 
 */
public class LifecycleException extends Exception {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1303044753352015798L;

	/**
	 * Default constructor
	 */
	public LifecycleException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 */
	public LifecycleException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 */
	public LifecycleException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message
	 * of <tt>(cause==null ? null : cause.toString())</tt> (which typically
	 * contains the class and detail message of <tt>cause</tt>).
	 */
	public LifecycleException(Throwable cause) {
		super(cause);
	}
}
