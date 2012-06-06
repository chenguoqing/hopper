package com.hopper.common.lifecycle;

import com.hopper.common.lifecycle.LifecycleEvent.EventType;

import java.util.EventListener;

/**
 * {@link LifecycleListener} for monitoring the life cycle event. The
 * {@link LifecycleListener} should be registered to {@link Lifecycle} object by
 * {@link Lifecycle#addListener(LifecycleListener)} method.
 * 
 * @author chenguoqing
 * 
 */
public interface LifecycleListener extends EventListener {

	/**
	 * Whether the listener can monitor for the event type
	 */
	boolean support(EventType eventType);

	/**
	 * Fire the life cycle event
	 */
	void lifecycle(LifecycleEvent event);
}
