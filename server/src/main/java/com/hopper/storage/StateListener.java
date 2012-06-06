package com.hopper.storage;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-21
 * Time: 下午2:57
 * To change this template use File | Settings | File Templates.
 */
public interface StateListener {

    void stateChange(int newState, String sessionId);
}
