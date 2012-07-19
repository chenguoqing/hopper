package com.hopper.storage;

import com.hopper.lifecycle.LifecycleException;
import com.hopper.lifecycle.LifecycleListener;
import com.hopper.utils.ScheduleManager;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-7-19
 * Time: 上午11:40
 * To change this template use File | Settings | File Templates.
 */
public class TestStateNode {
    @Test
    public void testSetStatus() {
        StateNode node = newStateNode("/key/123", 3);
        node.setScheduleManager(new TestScheduleManager());

        node.setStatus(0, 1, "test", 2);
        Assert.assertEquals(node.getStatus(), 1);

        try {
            node.setStatus(1, 2, "test1", -1);
            Assert.assertFalse(true);
        } catch (NotMatchOwnerException e) {
        }

        node.setStatus(1, 2, "test", -1);
        try {
            node.setStatus(1, 1, null, -1);
            Assert.assertFalse(true);
        } catch (NotMatchStatusException e) {
        }

        Assert.assertEquals(node.getLease(), -1);
    }

    private StateNode newStateNode(String key, long initialVersion) {
        StateNode node = new StateNode(key, initialVersion);
        node.setScheduleManager(new TestScheduleManager());
        ExecutorService notifyExecutorService = Executors.newCachedThreadPool();
        node.setNotifyExecutorService(notifyExecutorService);

        return node;
    }

    private class TestScheduleManager implements ScheduleManager {
        @Override
        public void schedule(Runnable command, long delay) {
        }

        @Override
        public void schedule(Runnable command, long initialDelay, long period) {
        }

        @Override
        public void removeTask(Runnable command) {
        }

        @Override
        public void initialize() throws LifecycleException {
        }

        @Override
        public void start() throws LifecycleException {
        }

        @Override
        public void pause() throws LifecycleException {
        }

        @Override
        public void resume() throws LifecycleException {
        }

        @Override
        public void shutdown() {
        }

        @Override
        public LifecycleState getState() {
            return null;
        }

        @Override
        public void addListener(LifecycleListener listener) {
        }

        @Override
        public void removeListener(LifecycleListener listener) {
        }

        @Override
        public String getInfo() {
            return "test";
        }
    }
}
