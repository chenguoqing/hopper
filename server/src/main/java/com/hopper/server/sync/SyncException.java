package com.hopper.server.sync;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-8
 * Time: 下午1:54
 * To change this template use File | Settings | File Templates.
 */
public class SyncException extends RuntimeException {
    public SyncException() {
    }

    public SyncException(String message) {
        super(message);
    }

    public SyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyncException(Throwable cause) {
        super(cause);
    }
}
