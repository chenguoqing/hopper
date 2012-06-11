package com.hopper.utils;

import com.hopper.lifecycle.LifecycleProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * {@link ScheduleManager} provides the ability for managing all schedule tasks.
 * All tasks will be scheduled with multithreads by
 * {@link ScheduledThreadPoolExecutor}.
 * 
 * @author chenguoqing
 * 
 */
public class ScheduleManager extends LifecycleProxy {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

	/**
	 * Unique schedule executor
	 */
	private ScheduledThreadPoolExecutor scheduleExecutor;

	/**
	 * The unique timer instance
	 */
	private int scheduleThreadCount;

	public int getScheduleThreadCount() {
		return scheduleThreadCount;
	}

	public void setScheduleThreadCount(int scheduleThreadCount) {
		this.scheduleThreadCount = scheduleThreadCount;
	}

	@Override
	protected void doInit() {
		// if the schedule count is not setting
		if (scheduleThreadCount <= 0) {
			int adjust = (int) (Runtime.getRuntime().availableProcessors() * 0.75);
			this.scheduleThreadCount = adjust < 1 ? 1 : adjust;
		}
	}

	/**
	 * Start the Schedule service
	 */
	@Override
	protected void doStart() {
		this.scheduleExecutor = new ScheduledThreadPoolExecutor(scheduleThreadCount, new RenamingThreadFactory());
	}

	@Override
	protected void doShutdown() {
		this.scheduleExecutor.shutdown();
	}

    @Override
    public String getInfo() {
        return "Schedule Manager";
    }

	/**
	 * Execute the {@link Runnable} on-shot
	 * 
	 * @param command
	 *            The {@link Runnable} instance
	 * @param delay
	 *            Delay milli seconds for start
	 */
	public void schedule(Runnable command, long delay) {
		scheduleExecutor.schedule(command, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * Execute the task with period
	 * 
	 * @param command
	 *            The {@link Runnable} that will be executed
	 * @param initialDelay
	 *            The delay time for start
	 * @param period
	 *            The execution period
	 */
	public void schedule(Runnable command, long initialDelay, long period) {
		scheduleExecutor.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
	}

	/**
	 * Remove command from task queue
	 */
	public void removeTask(Runnable command) {
		if (command != null) {
			scheduleExecutor.remove(command);
		}
	}

	static class RenamingThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {

			Thread t = new Thread(r);
			t.setDaemon(true);

			try {
				t.setName("Hopper-Timer_" + t.getId());
			} catch (SecurityException e) {
				// no works
			}

			return t;
		}
	}

	/**
	 * Supper class for all timer task implementations. It rename the current
	 * thread to task name
	 */
	public static abstract class ThreadRenamingRunnable implements Runnable {

		private final String taskName;

		public ThreadRenamingRunnable(String taskName) {
			this.taskName = taskName;
		}

		@Override
		public final void run() {
			Thread thread = Thread.currentThread();
			String oldThreadName = thread.getName();
			String newTheradName = oldThreadName + "_" + taskName;

			boolean isRenamed = false;
			try {
				thread.setName(newTheradName);
				isRenamed = true;
			} catch (SecurityException e) {
				logger.debug("Failed to rename thread due to security excepiton", e);
			}

			try {
				executeTask();
			} finally {
				if (isRenamed) {
					// There is no security exception
					thread.setName(oldThreadName);
				}
			}
		}

		/**
		 * Concrete template method
		 */
		protected abstract void executeTask();
	}
}
