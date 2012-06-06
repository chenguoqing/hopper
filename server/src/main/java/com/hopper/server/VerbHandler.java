package com.hopper.server;

import com.hopper.session.Message;

/**
 * VerbHandler provides the method that all verb handlers need to implement. The
 * concrete implementation of this interface would provide the functionality for
 * a given verb.
 * 
 * @author chenguoqing
 * 
 */
public interface VerbHandler {

	/**
	 * Processing the received message according to the associated {@link Verb}.
	 * All same verbs will use the same instance,so the implementations should
	 * guarantee thread-safe.
	 */
	void doVerb(Message message);
}
