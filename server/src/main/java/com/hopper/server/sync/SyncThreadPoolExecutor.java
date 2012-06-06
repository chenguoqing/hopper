package com.hopper.server.sync;

import com.hopper.future.LatchFutureTask;

import java.util.concurrent.*;

/**
 * The {@link ThreadFactory} implementation for data synchronization.
 * {@link SyncThreadPoolExecutor} supports the new future {@link com.hopper.future.LatchFutureTask}.
 */
public class SyncThreadPoolExecutor extends ThreadPoolExecutor {

    public SyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, 2 * 60 * 1000L, TimeUnit.MILLISECONDS, workQueue, new SyncThreadFactory());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new LatchFutureTask<T>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new LatchFutureTask<T>(callable);
    }

    private static class SyncThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("sync_" + t.getId());

            return t;
        }
    }
}
