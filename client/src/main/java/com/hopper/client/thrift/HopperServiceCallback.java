package com.hopper.client.thrift;

import com.hopper.thrift.*;
import org.apache.thrift.TException;

/**
 * HopperServiceCallback receives the pushed message from server
 */
public class HopperServiceCallback implements HopperService.Iface {
    @Override
    public String login(String userName, String password) throws RetryException, AuthenticationException, TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reLogin(String sessionId) throws RetryException, TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout(String sessionId) throws TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ping() throws TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(String key, String owner, int initStatus, int invalidateStatus) throws RetryException, TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateStatus(String key, int expectStatus, int newStatus, String owner, int lease) throws RetryException, CASException, TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void expandLease(String key, int expectStatus, String owner, int lease) throws RetryException, CASException, NoStateNodeException, TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void watch(String key, int expectStatus) throws RetryException, CASException, NoStateNodeException, TException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void statusChange(int oldStatus, int newStatus) throws TException {
    }
}
