package com.hopper.stage;

import com.hopper.GlobalConfiguration;
import com.hopper.lifecycle.Lifecycle;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.sync.DataSyncThreadPool;

import java.util.EnumMap;
import java.util.concurrent.*;

/**
 * Manages all stage and thread pools
 */
public class StageManager extends LifecycleProxy {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    /**
     * Global configuration
     */
    private final GlobalConfiguration config = componentManager.getGlobalConfiguration();

    /**
     * Mappings between state and thread pool
     */
    private final EnumMap<Stage, ExecutorService> states = new EnumMap<Stage, ExecutorService>(Stage.class);

    /**
     * Register stage and pool
     */
    @Override
    protected void doInit() {
        states.put(Stage.S2S_BOSS, newCachedThreadPoolMBean(Stage.S2S_BOSS, null));
        states.put(Stage.S2S_WORKER, newCachedThreadPoolMBean(Stage.S2S_WORKER, null));
        states.put(Stage.RPC_BOSS, newCachedThreadPoolMBean(Stage.RPC_BOSS, null));
        states.put(Stage.RPC_WORKER, newCachedThreadPoolMBean(Stage.RPC_WORKER, null));
        states.put(Stage.SCHEDULE, newScheduledThreadPoolMBean(Stage.SCHEDULE, null));
        states.put(Stage.SYNC, new ThreadPoolMBean(newDataSyncThreadPool(), Stage.SYNC, null));
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        for (ExecutorService threadPool : states.values()) {
            if (threadPool instanceof Lifecycle) {
                ((Lifecycle) threadPool).initialize();
                ((Lifecycle) threadPool).start();
            }
        }
    }

    @Override
    public String getInfo() {
        return "Stage manager";
    }

    public ExecutorService getThreadPool(Stage stage) {
        return states.get(stage);
    }

    /**
     * Create thread pool for data synchronization
     */
    private ThreadPoolExecutor newDataSyncThreadPool() {
        ThreadPoolExecutor threadPool = new DataSyncThreadPool(config.getSyncThreadPoolCoreSize(),
                config.getSyncThreadPoolMaxSize(), new LinkedBlockingQueue<Runnable>());
        return threadPool;
    }

    public ExecutorService newClientBossExecutor(Stage stage, String name) {
        return newCachedThreadPoolMBean(stage, name);
    }

    public ExecutorService newClientWorkExecutor(Stage stage, String name) {
        return newCachedThreadPoolMBean(stage, name);
    }

    private ExecutorService newCachedThreadPoolMBean(Stage stage, String name) {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        return new ThreadPoolMBean(threadPool, stage, name);
    }

    private ExecutorService newScheduledThreadPoolMBean(Stage stage, String name) {
        int scheduleThreadCount = config.getScheduleThreadCount();

        // if the schedule count is not setting
        if (scheduleThreadCount <= 0) {
            scheduleThreadCount = 2;
        }

        ScheduledThreadPoolExecutor scheduleExecutor = new ScheduledThreadPoolExecutor(scheduleThreadCount, new RenamingThreadFactory());

        return new ScheduledThreadPoolMBean(scheduleExecutor, stage, name);
    }

    static class RenamingThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {

            Thread t = new Thread(r);
            t.setDaemon(true);

            try {
                t.setName("Schedule_Timer-" + t.getId());
            } catch (SecurityException e) {
                // no works
            }

            return t;
        }
    }
}
