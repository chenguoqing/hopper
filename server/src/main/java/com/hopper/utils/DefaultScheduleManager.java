package com.hopper.utils;

import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.stage.Stage;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The default implementation of {@link ScheduleManager}
 */
public class DefaultScheduleManager extends LifecycleProxy implements ScheduleManager {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    /**
     * Unique schedule executor
     */
    private ScheduledExecutorService scheduleExecutor;

    /**
     * Start the Schedule service
     */
    @Override
    protected void doStart() {
        this.scheduleExecutor = (ScheduledExecutorService) componentManager.getStageManager().getThreadPool(Stage.SCHEDULE);
    }

    @Override
    protected void doShutdown() {
        if (scheduleExecutor != null) {
            this.scheduleExecutor.shutdown();
        }
    }

    @Override
    public String getInfo() {
        return "Schedule Manager";
    }

    /**
     * Execute the {@link Runnable} on-shot
     *
     * @param command The {@link Runnable} instance
     * @param delay   Delay milli seconds for start
     */
    @Override
    public void schedule(Runnable command, long delay) {
        scheduleExecutor.schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Execute the task with period
     *
     * @param command      The {@link Runnable} that will be executed
     * @param initialDelay The delay time for start
     * @param period       The execution period
     */
    @Override
    public void schedule(Runnable command, long initialDelay, long period) {
        scheduleExecutor.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Remove command from task queue
     */
    @Override
    public void removeTask(Runnable command) {
        if (command != null) {
            if (scheduleExecutor instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor) scheduleExecutor).remove(command);
            }
        }
    }
}

