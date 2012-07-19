package com.hopper.utils;

import com.hopper.lifecycle.Lifecycle;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * {@link ScheduleManager} provides the ability for managing all schedule tasks.
 * All tasks will be scheduled with multithreads by {@link ScheduledThreadPoolExecutor}.
 *
 * @author chenguoqing
 */
public interface ScheduleManager extends Lifecycle {

    /**
     * Execute the {@link Runnable} on-shot
     *
     * @param command The {@link Runnable} instance
     * @param delay   Delay milli seconds for start
     */
    void schedule(Runnable command, long delay);

    /**
     * Execute the task with period
     *
     * @param command      The {@link Runnable} that will be executed
     * @param initialDelay The delay time for start
     * @param period       The execution period
     */
    void schedule(Runnable command, long initialDelay, long period);

    /**
     * Remove command from task queue
     */
    void removeTask(Runnable command);
}
