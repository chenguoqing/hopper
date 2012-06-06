package com.hopper.storage;

/**
 * Created with IntelliJ IDEA. User: chenguoqing Date: 12-5-24 Time: 下午5:20 To
 * change this template use File | Settings | File Templates.
 */
public class StatusCASException extends RuntimeException {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -2207052022676783113L;

    public final int expectStatus;
    public final int actualStatus;

    public StatusCASException(int expectStatus, int actualStatus) {
        this.expectStatus = expectStatus;
        this.actualStatus = actualStatus;
    }
}
