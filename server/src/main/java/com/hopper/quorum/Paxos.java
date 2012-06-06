package com.hopper.quorum;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link Paxos} holds all election information
 *
 * @author chenguoqing
 */
public class Paxos {
    /**
     * Inner read/werite lock
     */
    private final ReadWriteLock readWritelock = new ReentrantReadWriteLock();

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
        readWritelock.readLock().lock();
        try {
            return rnd;
        } finally {
            readWritelock.readLock().unlock();
        }
    }

    public void setRnd(int rnd) {
        readWritelock.writeLock().lock();
        try {
            this.rnd = rnd;
        } finally {
            readWritelock.writeLock().unlock();
        }
    }

    public int getVrnd() {
        readWritelock.readLock().lock();
        try {
            return vrnd;
        } finally {
            readWritelock.readLock().unlock();
        }
    }

    public void setVrnd(int vrnd) {
        readWritelock.writeLock().lock();
        try {
            this.vrnd = vrnd;
        } finally {
            readWritelock.writeLock().unlock();
        }
    }

    public int getVval() {
        readWritelock.readLock().lock();
        try {
            return vval;
        } finally {
            readWritelock.readLock().unlock();
        }
    }

    public void setVval(int vval) {
        readWritelock.writeLock().lock();
        try {
            this.vval = vval;
        } finally {
            readWritelock.writeLock().unlock();
        }
    }

    public boolean isVoted() {
        readWritelock.readLock().lock();
        try {
            return vval != -1;
        } finally {
            readWritelock.readLock().unlock();
        }
    }

    public int getEpoch() {
        readWritelock.readLock().lock();
        try {
            return epoch;
        } finally {
            readWritelock.readLock().unlock();
        }
    }

    public void setEpoch(int epoch) {
        readWritelock.writeLock().lock();
        try {
            this.epoch = epoch;
        } finally {
            readWritelock.writeLock().unlock();
        }
    }

    /**
     * Close current election
     */
    public void closeInstance() {
        readWritelock.writeLock().lock();
        try {
            this.epoch++;
            this.rnd = 0;
            this.vrnd = 0;
            this.vval = -1;
        } finally {
            readWritelock.writeLock().unlock();
        }
    }

    /**
     * Update the epoch to greater one and unbound other status.
     */
    public void updateInstance(int epoch) {
        readWritelock.writeLock().lock();
        try {
            this.epoch = epoch;
            this.rnd = 0;
            this.vrnd = 0;
            this.vval = -1;
        } finally {
            readWritelock.writeLock().unlock();
        }
    }
}
