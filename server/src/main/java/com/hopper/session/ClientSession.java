package com.hopper.session;

import com.hopper.GlobalConfiguration;
import com.hopper.thrift.BidDirectNotify;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ClientSession represents a session between client and server
 */
public class ClientSession extends SessionProxy implements Session {
    /**
     * Last heart beat
     */
    private final AtomicLong lastHeartBeat = new AtomicLong(-1L);
    /**
     * Client notify
     */
    private BidDirectNotify notify;

    public BidDirectNotify getNotify() {
        return notify;
    }

    public void setNotify(BidDirectNotify notify) {
        this.notify = notify;
    }

    @Override
    public boolean isAlive() {
        if (lastHeartBeat.get() == -1L) {
            return getConnection() != null ? getConnection().validate() : false;
        }

        return System.currentTimeMillis() - lastHeartBeat.get() < GlobalConfiguration.getInstance().getRpcTimeout();
    }

    public void heartBeat() {
        lastHeartBeat.set(System.currentTimeMillis());
    }
}
