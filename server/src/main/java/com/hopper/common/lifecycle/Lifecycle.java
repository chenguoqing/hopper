package com.hopper.common.lifecycle;

/**
 * The common component life cycle interface for start,shutdown. The
 * implementation should provide the concrete semantic.
 * 
 * @author chenguoqing
 * 
 */
public interface Lifecycle {

	/**
	 * Initialize the component
	 */
	void initialize() throws LifecycleException;

	/**
	 * Start component.
	 */
	void start() throws LifecycleException;

	/**
	 * Pause the execution
	 */
	void pause() throws LifecycleException;

	/**
	 * Resume the execution
	 */
	void resume() throws LifecycleException;

	/**
	 * Shutdown component
	 */
	void shutdown();

	/**
	 * Return current life cycle state
	 */
	LifecycleState getState();

	/**
	 * Register {@link LifecycleListener} instance for monitoring life cycle
	 * event
	 */
	void addListener(LifecycleListener listener);

	/**
	 * Removing the listener
	 */
	void removeListener(LifecycleListener listener);

	/**
	 * Life cycle status
	 * 
	 */
	enum LifecycleState {
		/**
		 * Unstart
		 */
		NEW,
		/**
		 * Initializing
		 */
		INITIALIZING,
		/**
		 * Initialized
		 */
		INITIALIZED,
		/**
		 * Starting
		 */
		STARTING,
		/**
		 * Running
		 */
		RUNNING,
		/**
		 * Pausing
		 */
		PAUSING,
		/**
		 * Paused
		 */
		PAUSED,
		/**
		 * Resuming
		 */
		RESUMING,
		/**
		 * Resumed
		 */
		RESUMED,
		/**
		 * Shutting
		 */
		SHUTDOWNING,
		/**
		 * Shutdown
		 */
		SHUTDOWN
	}
}
