package com.hopper.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * LatchFuture is a control interface for mixing synchronous and asynchronous data duplicating.
 * It supports adds some {@link com.hopper.future.LatchFutureListener} which will be called when operation
 * complete.
 */
public interface LatchFuture<T> extends Future<T> {
    /**
     * Add one  LatchFutureListener instance which will be triggered when operation complete
     */
    void addListener(LatchFutureListener listener);

    /**
     * Set a CountDownLatch instance, when future is done successfully, the  <code>countDown</code> method will be
     * called.
     */
    void setLatch(CountDownLatch latch);

    /**
     * Whether the operation is done successfully?
     */
    boolean isSuccess();
}
