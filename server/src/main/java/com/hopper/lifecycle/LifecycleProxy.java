package com.hopper.lifecycle;

import com.hopper.lifecycle.LifecycleEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The convince proxy class for Lifecycle implementation
 * 
 * @author chenguoqing
 * 
 */
public abstract class LifecycleProxy implements Lifecycle {
	/**
	 * Common logger
	 */
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Registered listener
	 */
	private final Queue<LifecycleListener> listeners = new ConcurrentLinkedQueue<LifecycleListener>();

	/**
	 * The lifecycle state
	 */
	private volatile LifecycleState state = LifecycleState.NEW;

	@Override
	public void initialize() throws LifecycleException {

		if (state != LifecycleState.NEW) {
			throw new LifecycleException("The object has been initialized.");
		}

		this.state = LifecycleState.INITIALIZING;

		LifecycleEvent event = new LifecycleEvent(this, EventType.INITIALIZE);
		fireLifecycleEvent(event);

		try {
			doInit();
		} catch (Exception e) {
			throw new LifecycleException("Failed to initialize...", e);
		}

		this.state = LifecycleState.INITIALIZED;

		event = new LifecycleEvent(this, EventType.INTIALIZED);
		fireLifecycleEvent(event);
	}

	/**
	 * Real work will delegate to implementations
	 */
	protected void doInit() {
	}

	@Override
	public void start() throws LifecycleException {

		if (this.state != LifecycleState.INITIALIZED && this.state != LifecycleState.SHUTDOWN) {
			throw new LifecycleException("Please initialize or shutdown first.");
		}

		this.state = LifecycleState.STARTING;

		LifecycleEvent event = new LifecycleEvent(this, EventType.STARTING);
		fireLifecycleEvent(event);

		try {
			doStart();
		} catch (Exception e) {
			throw new LifecycleException("Failed to starting...", e);
		}

		this.state = LifecycleState.RUNNING;

		event = new LifecycleEvent(this, EventType.STARTED);
		fireLifecycleEvent(event);
	}

	/**
	 * Subclass will do real starting works
	 */
	protected void doStart() {
	}

	@Override
	public void pause() throws LifecycleException {
		if (state != LifecycleState.RUNNING) {
			throw new LifecycleException("Invalidate state.");
		}

		this.state = LifecycleState.PAUSING;

		LifecycleEvent event = new LifecycleEvent(this, EventType.RESUMING);
		fireLifecycleEvent(event);

		try {
			doPause();
		} catch (Exception e) {
			throw new LifecycleException("Failed to pause.", e);
		}

		this.state = LifecycleState.PAUSED;

		event = new LifecycleEvent(this, EventType.PAUSED);
		fireLifecycleEvent(event);
	}

	protected void doPause() {
	}

	@Override
	public void resume() throws LifecycleException {
		if (state != LifecycleState.PAUSED) {
			throw new LifecycleException("Invalidate state.");
		}

		this.state = LifecycleState.RESUMING;

		LifecycleEvent event = new LifecycleEvent(this, EventType.RESUMING);
		fireLifecycleEvent(event);

		try {
			doResume();
		} catch (Exception e) {
			throw new LifecycleException("Failed to resume", e);
		}

		this.state = LifecycleState.RESUMED;

		event = new LifecycleEvent(this, EventType.RESUMED);
		fireLifecycleEvent(event);
	}

	protected void doResume() {
	}

	@Override
	public void shutdown() {

		this.state = LifecycleState.SHUTDOWNING;

		LifecycleEvent event = new LifecycleEvent(this, EventType.SHUTDOWNING);
		fireLifecycleEvent(event);

		try {
			doShutdown();
		} catch (Exception e) {
			logger.error("Exception has been occurred when shutdowning.", e);
		}

		this.state = LifecycleState.SHUTDOWN;

		event = new LifecycleEvent(this, EventType.SHUTDOWN);
		fireLifecycleEvent(event);
	}

	/**
	 * Subclass will do real shut down works
	 */
	protected void doShutdown() {
	}

	@Override
	public LifecycleState getState() {
		return state;
	}

	@Override
	public void addListener(LifecycleListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(LifecycleListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Fire the {@link LifecycleEvent}
	 */
	private void fireLifecycleEvent(LifecycleEvent event) {
		for (LifecycleListener listener : listeners) {
			if (listener.support(event.type)) {
				listener.lifecycle(event);
			}
		}
	}
}
