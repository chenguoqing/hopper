package com.hopper.common.lifecycle;

import java.util.EventObject;

/**
 * The {@link LifecycleEvent} representing a event data for establishing
 * contacts in event source and event processor(listener)
 * 
 * @author chenguoqing
 * 
 */
public class LifecycleEvent extends EventObject {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -8259385984006679569L;

	/**
	 * Life cycle event type
	 * 
	 * @author chenguoqing
	 * 
	 */
	public enum EventType {
		/**
		 * Before initializing
		 */
		INITIALIZE,
		/**
		 * After initialized
		 */
		INTIALIZED,
		/**
		 * Before start
		 */
		STARTING,
		/**
		 * After started
		 */
		STARTED,
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
		 * Before shutdown
		 */
		SHUTDOWNING,
		/**
		 * After shutdown
		 */
		SHUTDOWN
	}
	
	/**
	 * Event type
	 */
	public final EventType type;

	/**
	 * Attachment for extension
	 */
	private Object attachment;

	/**
	 * Constructor
	 * 
	 * @param source
	 *            event source(object)
	 * @param type
	 *            event type
	 */
	public LifecycleEvent(Object source, EventType type) {
		super(source);
		this.type = type;
	}

	/**
	 * Attach a extension object
	 */
	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	/**
	 * Retrieve the attachment object
	 */
	public Object getAttachment() {
		return attachment;
	}
}
