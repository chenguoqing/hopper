package com.hopper.storage;

import com.hopper.GlobalConfiguration;
import com.hopper.session.MessageService;
import com.hopper.stage.Stage;
import com.hopper.stage.StageManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StateNode {
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -3379513726443888571L;

    /**
     * Temporary node
     */
    public static final int TYPE_TEMP = 0;
    /**
     * Persist node
     */
    public static final int TYPE_PERSIST = 1;
    /**
     * Default initial state
     */
    public static final int DEFAULT_STATUS = 0;
    /**
     * Default invalidate state
     */
    public static final int DEFAULT_INVALIDATE_STATUS = -1;
    /**
     * Singleton
     */
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();
    /**
     * Invalidate task
     */
    private final Runnable task = new InvalidateTask();

    /**
     * Protected lock
     */
    public final transient ReadWriteLock nodeLock = new ReentrantReadWriteLock();

    /**
     * Unique key
     */
    public final String key;

    /**
     * Node type
     */
    public final int type;

    /**
     * Invalidate status
     */
    private final int invalidateStatus;
    /**
     * Status
     */
    private int status = DEFAULT_STATUS;

    /**
     * state owner
     */
    private String owner;

    /**
     * State lease(seconds)
     */
    private int lease = -1;

    /**
     * State last modified
     */
    private long lastModified;

    /**
     * Version is a internal flag for data synchronization
     */
    private long version;

    /**
     * Holds all listeners(session id) for state change
     */
    private final LinkedList<String> stateChangeListeners = new LinkedList<String>();

    /**
     * Constructor
     */
    public StateNode(String key, long initialVersion) {
        this(key, TYPE_TEMP, DEFAULT_STATUS, DEFAULT_INVALIDATE_STATUS, initialVersion);
    }

    public StateNode(String key, int type, int initStatus, int invalidateStatus, long initalVersion) {
        this.key = key;
        this.type = type;
        this.status = initStatus;
        this.invalidateStatus = invalidateStatus;
        this.version = initalVersion;
    }

    public void setStatus(int expectStatus, int newStatus, String owner, int lease) {
        nodeLock.writeLock().lock();
        try {
            if (expectStatus != this.status) {
                throw new StatusNoMatchException(expectStatus, status);
            }

            // Invalidate owner
            if (this.owner != null && owner != null && !this.owner.equals(owner)) {
                throw new OwnerNoMatchException(this.owner, owner);
            }
            final int oldStatus = this.status;
            this.status = newStatus;
            this.owner = owner;
            this.version++;
            this.lastModified = System.currentTimeMillis();

            // Remove the old task
            config.getScheduleManager().removeTask(task);

            if (lease > 0) {
                this.lease = lease;

                // add new task
                config.getScheduleManager().schedule(task, lease);
            }

            // fire state change
            fireStateChangeListeners(oldStatus, newStatus);

        } finally {
            nodeLock.writeLock().unlock();
        }
    }

    public int getStatus() {
        nodeLock.readLock().lock();
        try {
            return status;
        } finally {
            nodeLock.readLock().unlock();
        }
    }

    void setOwner(String owner) {
        nodeLock.writeLock().lock();
        this.owner = owner;
        this.version++;
        nodeLock.writeLock().unlock();
    }

    String getOwner() {
        nodeLock.readLock().lock();
        try {
            return owner;
        } finally {
            nodeLock.readLock().unlock();
        }
    }

    public void expandLease(int expectStatus, String owner, int lease) {
        nodeLock.writeLock().lock();
        try {

            if (lease < 0) {
                throw new IllegalArgumentException();
            }

            if (expectStatus != this.status) {
                throw new StatusNoMatchException(expectStatus, this.status);
            }

            // Invalidate owner
            if (this.owner != null && !this.owner.equals(owner)) {
                throw new OwnerNoMatchException(owner, this.owner);
            }

            // Remove the old task
            removeInvalidateTask();

            this.lease = lease;
            this.lastModified = System.currentTimeMillis();

            // add new task
            config.getScheduleManager().schedule(task, lease);

        } finally {
            nodeLock.writeLock().unlock();
        }
    }

    public void watch(int expectStatus) {
        nodeLock.writeLock().lock();
        try {
            if (this.status != expectStatus) {
                throw new StatusNoMatchException(expectStatus, status);
            }
        } finally {
            nodeLock.writeLock().unlock();
        }

    }

    void setLease(int lease) {
        nodeLock.writeLock().lock();
        this.lease = lease;
        version++;
        nodeLock.writeLock().unlock();
    }

    int getLease() {
        nodeLock.readLock().lock();
        try {
            return lease;
        } finally {
            nodeLock.readLock().unlock();
        }
    }

    void setLastModified(long lastModified) {
        nodeLock.writeLock().lock();
        this.lastModified = lastModified;
        nodeLock.writeLock().unlock();
    }

    long getLastModified() {
        nodeLock.readLock().lock();
        try {
            return lastModified;
        } finally {
            nodeLock.readLock().unlock();
        }
    }

    public long getVersion() {
        nodeLock.readLock().lock();
        try {
            return version;
        } finally {
            nodeLock.readLock().unlock();
        }
    }

    public void update(StateNodeSnapshot snapshot) {
        if (snapshot.version <= version) {
            return;
        }
        nodeLock.writeLock().lock();
        try {
            if (snapshot.version <= version) {
                return;
            }

            this.status = snapshot.status;
            this.owner = snapshot.owner;
            this.lease = snapshot.lease;
            this.lastModified = snapshot.lastModified;
            this.version = snapshot.version;
            this.stateChangeListeners.clear();
            this.stateChangeListeners.addAll(snapshot.stateChangeListeners);
        } finally {
            nodeLock.writeLock().unlock();
        }
    }

    private void invalidate() {
        nodeLock.writeLock().lock();
        this.lease = -1;
        this.owner = null;
        final int oldStatus = this.status;
        this.status = invalidateStatus;
        nodeLock.writeLock().unlock();
        fireStateChangeListeners(oldStatus, invalidateStatus);
    }

    /**
     * Register a state change listener with expect state
     *
     * @param expectState expect state
     * @param sessionId   listener session
     * @throws StateRegisterException if the except state is invalidate
     */
    void registerStateListener(int expectState, String sessionId) {
        nodeLock.writeLock().lock();

        try {
            if (status != expectState) {
                throw new StateRegisterException(expectState, status);
            }

            stateChangeListeners.add(sessionId);
        } finally {
            nodeLock.writeLock().unlock();
        }
    }

    List<String> getStateChangeListeners() {
        return new ArrayList<String>(stateChangeListeners);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StateNode)) {
            return false;
        }

        StateNode node = (StateNode) obj;
        return node.key.equals(key) && node.version == version;
    }

    /**
     * Executes the invalidate task, if the task has expired, running it immediately; otherwise,
     * running it after the remaining times.
     */
    void executeInvalidateTask() {
        if (lease < 0 || lastModified <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        // current state is expire
        if (now - lastModified >= lease * 1000L) {
            invalidate();

            // start tasks with remaining times
        } else {
            config.getScheduleManager().schedule(task, now - lastModified);
        }
    }

    /**
     * Remove the invalidate task from schedule manager immediately.
     */
    void removeInvalidateTask() {
        config.getScheduleManager().removeTask(task);
    }

    boolean shouldPurge() {
        return System.currentTimeMillis() - lastModified >= config.getStateNodePurgeExpire() && this
                .stateChangeListeners.isEmpty();
    }

    /**
     * Fire all state change listeners (asynchronous)
     */
    private void fireStateChangeListeners(int oldStatus, int newStatus) {
        ThreadPoolExecutor threadPool = StageManager.getThreadPool(Stage.STATE_CHANGE);

        String sessionId = stateChangeListeners.poll();

        while (sessionId != null) {
            Runnable task = new StateChangeNotifyTask(oldStatus, newStatus, sessionId);
            threadPool.execute(task);
            sessionId = stateChangeListeners.poll();
        }
    }

    private class StateChangeNotifyTask implements Runnable {
        final int oldStatus;
        final int newStatus;
        final String sessionId;

        StateChangeNotifyTask(int oldStatus, int newStatus, String sessionId) {
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.sessionId = sessionId;
        }

        @Override
        public void run() {
            MessageService.notifyStatusChange(sessionId, oldStatus, newStatus);
        }
    }

    class InvalidateTask implements Runnable {
        @Override
        public void run() {
            invalidate();
        }
    }
}
