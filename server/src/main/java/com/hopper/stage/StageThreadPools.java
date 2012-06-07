package com.hopper.stage;

import java.util.EnumMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-21
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class StageThreadPools {
    /**
     * Mappings between state and thread pool
     */
    private static final EnumMap<Stage, ThreadPoolExecutor> states = new EnumMap<Stage,
            ThreadPoolExecutor>(Stage.class);

    static {
        states.put(Stage.SERVER_BOSS, (ThreadPoolExecutor) Executors.newCachedThreadPool());
        states.put(Stage.SERVER_WORKER, (ThreadPoolExecutor) Executors.newCachedThreadPool());
        states.put(Stage.CLIENT_BOSS, (ThreadPoolExecutor) Executors.newCachedThreadPool());
        states.put(Stage.CLIENT_WORKER, (ThreadPoolExecutor) Executors.newCachedThreadPool());
    }

    public static ThreadPoolExecutor getThreadPool(Stage stage) {
        return states.get(stage);
    }
}
