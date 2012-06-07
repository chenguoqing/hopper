package com.hopper.stage;

import com.hopper.GlobalConfiguration;
import com.hopper.sync.DataSyncThreadPool;

import java.util.EnumMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Manages all stage and thread pools
 */
public class StageManager {
    /**
     * Global configuration
     */
    private final static GlobalConfiguration config = GlobalConfiguration.getInstance();

    /**
     * Mappings between state and thread pool
     */
    private static final EnumMap<Stage, ThreadPoolExecutor> states = new EnumMap<Stage,
            ThreadPoolExecutor>(Stage.class);

    /**
     * Register stage and pool
     */
    static {
        states.put(Stage.SERVER_BOSS, (ThreadPoolExecutor) Executors.newCachedThreadPool());
        states.put(Stage.SERVER_WORKER, (ThreadPoolExecutor) Executors.newCachedThreadPool());
        states.put(Stage.CLIENT_BOSS, (ThreadPoolExecutor) Executors.newCachedThreadPool());
        states.put(Stage.CLIENT_WORKER, (ThreadPoolExecutor) Executors.newCachedThreadPool());
        states.put(Stage.SYNC, newDataSyncThreadPool());
    }

    public static ThreadPoolExecutor getThreadPool(Stage stage) {
        return states.get(stage);
    }

    /**
     * Create thread pool for data synchronization
     */
    private static ThreadPoolExecutor newDataSyncThreadPool() {
        ThreadPoolExecutor threadPool = new DataSyncThreadPool(config.getSyncThreadPoolCoreSize(),
                config.getSyncThreadPoolMaxSize(), new LinkedBlockingQueue<Runnable>());
        return threadPool;
    }
}
