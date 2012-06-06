package com.hopper.future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * The default implementation of {@link LatchFutureTask}
 *
 * @author chenguoqing
 */
public class LatchFutureTask<T> extends FutureTask<T> implements LatchFuture<T> {
    /**
     * All registered listeners
     */
    private final List<LatchFutureListener> listeners = new ArrayList<LatchFutureListener>();

    private CountDownLatch latch;

    public LatchFutureTask(Callable<T> tCallable) {
        super(tCallable);
    }

    public LatchFutureTask(Runnable runnable, T result) {
        super(runnable, result);
    }

    @Override
    public void addListener(LatchFutureListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean isSuccess() {
        if (!isDone() || isCancelled()) {
            return false;
        }

        try {
            get();
            return true;
        } catch (ExecutionException e) {
            // has exception
        } catch (InterruptedException e) {
            // swallow
        }
        return false;
    }

    @Override
    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    protected void done() {

        if (latch != null && isSuccess()) {
            latch.countDown();
        }

        for (LatchFutureListener listener : listeners) {
            listener.complete(this);
        }
    }
}
