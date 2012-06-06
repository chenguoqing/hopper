package com.hopper.storage;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-21
 * Time: 下午3:06
 * To change this template use File | Settings | File Templates.
 */
public class StateRegisterException extends RuntimeException {
    public final int expectStatus;
    public final int actualStatus;

    public StateRegisterException(int expectStatus, int actualStatus) {
        this.expectStatus = expectStatus;
        this.actualStatus = actualStatus;
    }
}
