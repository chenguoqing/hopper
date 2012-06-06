package com.hopper.future;

import java.util.EventListener;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-7
 * Time: 下午2:22
 * To change this template use File | Settings | File Templates.
 */
public interface LatchFutureListener<T> extends EventListener {

    void complete(LatchFuture<T> future);
}
