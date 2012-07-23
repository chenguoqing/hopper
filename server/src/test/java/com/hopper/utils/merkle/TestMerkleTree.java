package com.hopper.utils.merkle;

import com.hopper.lifecycle.LifecycleException;
import com.hopper.lifecycle.LifecycleListener;
import com.hopper.storage.StateNode;
import com.hopper.utils.ScheduleManager;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-7-19
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
public class TestMerkleTree {

    @Test
    public void testDiff() {

        // same
        StateNode node1 = newStateNode("/a/b/c", 10);
        StateNode node2 = newStateNode("/a/b/c", 10);
        StateNode node3 = newStateNode("/a/b/c", 10);

        node1.setStatus(0, 1, "test", 2);
        node2.setStatus(0, 1, "test", 2);
        node3.setStatus(0, 1, "test", 2);

        // change
        StateNode node4_1 = newStateNode("/a/b/c_1", 10);
        StateNode node4_2 = newStateNode("/a/b/c_1", 10);
        StateNode node5_1 = newStateNode("/a/b/c_2", 10);
        StateNode node5_2 = newStateNode("/a/b/c_2", 10);
        StateNode node6_1 = newStateNode("/a/b/c_3", 10);
        StateNode node6_2 = newStateNode("/a/b/c_3", 10);

        node4_1.setStatus(0, 1, "test", 2);
        node4_2.setStatus(0, 1, "test", 3);

        node5_1.expandLease(0, "test", 11);
        node5_2.expandLease(0, "test", 12);

        node6_1.watch("123", 0);
        node6_2.watch("1234", 0);

        StateNode node7 = newStateNode("/a/b/c_4", 10);
        StateNode node8 = newStateNode("/a/b/c_5", 10);

        StateNode node9 = newStateNode("/a/b/c_6", 10);
        StateNode node10 = newStateNode("/a/b/c_7", 10);

        MerkleTree tree1 = new MerkleTree((byte) 15);
        MerkleTree tree2 = new MerkleTree((byte) 15);

        tree1.put(node1);
        tree1.put(node2);
        tree1.put(node3);

        tree1.put(node4_1);
        tree1.put(node5_1);
        tree1.put(node6_1);

        tree1.put(node7);
        tree1.put(node8);

        tree2.put(node1);
        tree2.put(node2);
        tree2.put(node3);

        tree2.put(node4_2);
        tree2.put(node5_2);
        tree2.put(node6_2);

        tree2.put(node9);
        tree2.put(node10);

//        tree1.loadHash();
//        tree2.loadHash();

        Difference difference = tree1.difference(tree2);

        Assert.assertNotNull(difference);

        Assert.assertEquals(difference.updatedList.size(), 3);
        Assert.assertEquals(difference.addedList.size(), 2);
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
