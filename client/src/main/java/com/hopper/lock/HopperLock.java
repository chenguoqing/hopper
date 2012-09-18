package com.hopper.lock;

import com.hopper.NodeRing;
import com.hopper.client.Client;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-9-13
 * Time: 下午6:18
 * To change this template use File | Settings | File Templates.
 */
public class HopperLock implements Lock {

    private final String key;
    private final NodeRing ring;
    private final Map<InetSocketAddress, Client> clients = new ConcurrentHashMap<>();

    /**
     * Constructor
     *
     * @param key the unique lock key
     */
    public HopperLock(String key, NodeRing ring) {
        this.key = key;
        this.ring = ring;
    }

    @Override
    public void lock() {
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    private Client getClient(InetSocketAddress address) throws Exception {
        Client client = clients.get(address);

        if (client == null) {
            synchronized (address) {
                client = clients.get(address);
                if (client == null) {
                    client = new Client(address.getHostName(), address.getPort(), null);

                    // start client
                    client.start();

                    clients.put(address, client);
                }
            }
        }
        return client;
    }
}
