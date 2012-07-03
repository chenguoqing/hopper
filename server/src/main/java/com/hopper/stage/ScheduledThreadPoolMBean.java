package com.hopper.stage;

import java.util.concurrent.*;

/**
 * Wrappers the {@link ScheduledExecutorService} to MBean
 */
public class ScheduledThreadPoolMBean extends ThreadPoolMBean implements ScheduledExecutorService {

    public ScheduledThreadPoolMBean(ThreadPoolExecutor threadPool, Stage stage, String name) {
        super(threadPool, stage, name);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {

        if (threadPool instanceof ScheduledExecutorService) {
            return ((ScheduledExecutorService) threadPool).schedule(command, delay, unit);
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {

        if (threadPool instanceof ScheduledExecutorService) {
            return ((ScheduledExecutorService) threadPool).schedule(callable, delay, unit);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {

        if (threadPool instanceof ScheduledExecutorService) {
            return ((ScheduledExecutorService) threadPool).scheduleAtFixedRate(command, initialDelay, period, unit);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {

        if (threadPool instanceof ScheduledExecutorService) {
            ((ScheduledExecutorService) threadPool).scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
        throw new UnsupportedOperationException();
    }
}
