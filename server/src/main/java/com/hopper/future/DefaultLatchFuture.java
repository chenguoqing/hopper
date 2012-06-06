package com.hopper.future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * The class provides a implementation of {@link Future}, with methods to get
 * the message communication result, query to see if the message communication
 * is complete. The result can only be retrieved when the message response has
 * arrived; the <tt>get</tt> method will block if the response has not yet
 * received.
 *
 * @author chenguoqing
 */
public class DefaultLatchFuture<T> implements LatchFuture<T> {
    /**
     * All registered listeners
     */
    private final List<LatchFutureListener> listeners = new ArrayList<LatchFutureListener>();

    /**
     * Synchronization control for MessageTask
     */
    private final Sync<T> sync = new Sync<T>();
    /**
     * Latch
     */
    private CountDownLatch latch;

    /**
     * The cancellation is disable
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    /**
     * Set the response result
     */
    public void set(T result) {
        sync.set(result);
    }

    public void setException(Throwable exception) {
        sync.setException(exception);
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return sync.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return sync.get(unit.toNanos(timeout));
    }

    @Override
    public void addListener(LatchFutureListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public boolean isSuccess() {
        return sync.isInnerSuccess();
    }

    /**
     * Synchronization control for MessageTask, for simplicity, we abandoned the
     * usage of {@link Lock},{@link Condition}, instead of using the shared mode
     * of {@link AbstractQueuedSynchronizer}
     */
    private final class Sync<T> extends AbstractQueuedSynchronizer {
        /**
         * Serial Version UID
         */
        private static final long serialVersionUID = 3330110975388515799L;
        /**
         * State : running
         */
        private static final int RUNNING = 1;
        /**
         * State: Done
         */
        private static final int DONE = 2;

        /**
         * The result to return from get()
         */
        private T result;
        /**
         * The exception to throw from get()
         */
        private Throwable exception;

        Sync() {
            setState(RUNNING);
        }

        @Override
        protected int tryAcquireShared(int arg) {
            return isDone() ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            return true;
        }

        boolean isDone() {
            return getState() == DONE;
        }

        void set(T result) {
            for (; ; ) {
                int s = getState();
                if (s == DONE) {
                    return;
                }

                if (compareAndSetState(s, DONE)) {
                    this.result = result;
                    releaseShared(0);
                    innerDone();
                    return;
                }
            }
        }

        void setException(Throwable exception) {
            for (; ; ) {
                int s = getState();
                if (s == DONE) {
                    return;
                }

                if (compareAndSetState(s, DONE)) {
                    this.exception = exception;
                    releaseShared(0);
                    innerDone();
                    return;
                }
            }
        }

        T get() throws InterruptedException, ExecutionException {
            acquireSharedInterruptibly(0);
            if (exception != null) {
                throw new ExecutionException(exception);
            }
            return result;
        }

        T get(long nanosTimeout) throws InterruptedException, ExecutionException, TimeoutException {
            // wait for timeout
            if (!tryAcquireSharedNanos(0, nanosTimeout)) {
                throw new TimeoutException();
            }

            if (exception != null) {
                throw new ExecutionException(exception);
            }

            return result;
        }

        boolean isInnerSuccess() {
            return isDone() && exception == null;
        }

        void innerDone() {
            if (isInnerSuccess() && latch != null) {
                latch.countDown();
            }

            for (LatchFutureListener listener : listeners) {
                listener.complete(DefaultLatchFuture.this);
            }
        }
    }

}
