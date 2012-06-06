package com.hopper.storage;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-6-1
 * Time: 下午5:59
 * To change this template use File | Settings | File Templates.
 */
public class OwnerCASException extends RuntimeException {
    public final String expectOwner;
    public final String actualOwner;

    public OwnerCASException(String expectOwner, String actualOwner) {
        this.expectOwner = expectOwner;
        this.actualOwner = actualOwner;
    }
}
