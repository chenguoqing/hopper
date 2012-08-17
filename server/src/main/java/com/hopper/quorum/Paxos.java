package com.hopper.quorum;

import com.hopper.lifecycle.LifecycleMBeanProxy;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link Paxos} representing a running election progress, if the election progress has completed,
 * all status except <code>epoch</code> will be cleared. "epoch" indicates the instance of next election.
 * <p/>
 * "leader" as the election result will be hold on {@link com.hopper.server.Server#setLeader(int)}
 *
 * @author chenguoqing
 */
public class Paxos extends LifecycleMBeanProxy {
    /**
     * Inner read/werite lock
     */
    public final ReadWriteLock paxosLock = new ReentrantReadWriteLock();

    /**
     * Epoch(increase for each election)
     */
    private int epoch;
    /**
     * The highest-numbered round in which node has participated
     */
    private int rnd;
    /**
     * The highest-numbered round in which node has cast a vote
     */
    private int vrnd;
    /**
     * The value a voted to accept in round vrnd(server id)
     */
    private int vval = -1;

    public int getRnd() {
        paxosLock.readLock().lock();
        try {
            return rnd;
        } finally {
            paxosLock.readLock().unlock();
        }
    }

    public void setRnd(int rnd) {
        paxosLock.writeLock().lock();
        try {
            this.rnd = rnd;
        } finally {
            paxosLock.writeLock().unlock();
        }
    }

    public int getVrnd() {
        paxosLock.readLock().lock();
        try {
            return vrnd;
        } finally {
            paxosLock.readLock().unlock();
        }
    }

    public void setVrnd(int vrnd) {
        paxosLock.writeLock().lock();
        try {
            this.vrnd = vrnd;
        } finally {
            paxosLock.writeLock().unlock();
        }
    }

    public int getVval() {
        paxosLock.readLock().lock();
        try {
            return vval;
        } finally {
            paxosLock.readLock().unlock();
        }
    }

    public void setVval(int vval) {
        paxosLock.writeLock().lock();
        try {
            this.vval = vval;
        } finally {
            paxosLock.writeLock().unlock();
        }
    }

    public boolean isVoted() {
        paxosLock.readLock().lock();
        try {
            return vval != -1;
        } finally {
            paxosLock.readLock().unlock();
        }
    }

    public int getEpoch() {
        paxosLock.readLock().lock();
        try {
            return epoch;
        } finally {
            paxosLock.readLock().unlock();
        }
    }

    public void setEpoch(int epoch) {
        paxosLock.writeLock().lock();
        try {
            this.epoch = epoch;
        } finally {
            paxosLock.writeLock().unlock();
        }
    }

    /**
     * Close current election
     */
    public void closeInstance() {
        paxosLock.writeLock().lock();
        try {
            this.epoch++;
            this.rnd = 0;
            this.vrnd = 0;
            this.vval = -1;
        } finally {
            paxosLock.writeLock().unlock();
        }
    }

    /**
     * Update the epoch to greater one and unbound other status.
     */
    public void updateInstance(int epoch) {
        paxosLock.writeLock().lock();
        try {
            this.epoch = epoch;
            this.rnd = 0;
            this.vrnd = 0;
            this.vval = -1;
        } finally {
            paxosLock.writeLock().unlock();
        }
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return "type=paxos";
    }

    @Override
    public String getInfo() {
        return "paxos";
    }
}
