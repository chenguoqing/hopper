package com.hopper.client;

import com.hopper.thrift.RetryException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-9-18
 * Time: 下午5:33
 * To change this template use File | Settings | File Templates.
 */
public class RetryTaskExecutor {

    public static <T> T execute(int retryCount, Callable<T> callable) throws Exception {
        int count = 0;

        while (retryCount < 0 || count++ <= retryCount) {
            try {
                return callable.call();
            } catch (RetryException e) {
                Thread.sleep(e.getPeriod());
            }
        }

        throw new TimeoutException();
    }
}
