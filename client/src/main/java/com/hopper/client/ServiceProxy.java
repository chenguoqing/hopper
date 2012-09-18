package com.hopper.client;

import com.hopper.thrift.*;
import org.apache.thrift.TException;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-9-13
 * Time: 下午5:19
 * To change this template use File | Settings | File Templates.
 */
public class ServiceProxy implements HopperService.Iface {
    @Override
    public String login(String userName, String password) throws RetryException, AuthenticationException, TException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reLogin(String sessionId) throws RetryException, TException {
    }

    @Override
    public void logout(String sessionId) throws TException {
    }

    @Override
    public void ping() throws TException {
    }

    @Override
    public void create(String key, String owner, int initStatus, int invalidateStatus) throws RetryException, TException {
    }

    @Override
    public void updateStatus(String key, int expectStatus, int newStatus, String owner, int lease) throws RetryException, CASException, TException {
    }

    @Override
    public void expandLease(String key, int expectStatus, String owner, int lease) throws RetryException, CASException, NoStateNodeException, TException {
    }

    @Override
    public void watch(String key, int expectStatus) throws RetryException, CASException, NoStateNodeException, TException {
    }

    @Override
    public void statusChange(int oldStatus, int newStatus) throws TException {
    }
}
