package com.hopper.stage;

import com.hopper.lifecycle.LifecycleMBeanProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * The ThreadPoolExecutor is a delegation of {@link ThreadPoolExecutor}, and supports the MBean register
 */
public class ThreadPoolMBean extends LifecycleMBeanProxy implements ExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolMBean.class);

    /**
     * Referenced thread pool
     */
    protected final ThreadPoolExecutor threadPool;

    private final Stage stage;

    private final String name;

    public ThreadPoolMBean(ThreadPoolExecutor threadPool, Stage stage, String name) {
        this.threadPool = threadPool;
        this.stage = stage;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    protected String getObjectNameKeyProperties() {
        StringBuilder sb = new StringBuilder("type=ThreadPool");
        sb.append(",");
        sb.append("stage=").append(stage.name());

        if (name != null) {
            sb.append(",");
            sb.append("name=").append(name);
        }

        return sb.toString();
    }

    @Override
    public String getInfo() {
        return "Thread pool-" + name == null ? stage.name() : stage.name() + "-" + name;
    }

    public void setCorePoolSize(int corePoolSize) {
        threadPool.setCorePoolSize(corePoolSize);
    }

    public int getCorePoolSize() {
        return threadPool.getCorePoolSize();
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        threadPool.setMaximumPoolSize(maximumPoolSize);
    }

    public int getMaximumPoolSize() {
        return threadPool.getMaximumPoolSize();
    }

    public void setKeepAliveTime(long time) {
        threadPool.setKeepAliveTime(time, TimeUnit.MILLISECONDS);
    }

    public long getKeepAliveTime() {
        return threadPool.getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    public long getTaskCount() {
        return threadPool.getTaskCount();
    }

    public long getCompletedTaskCount() {
        return threadPool.getCompletedTaskCount();
    }

    public int getLargestPoolSize() {
        return threadPool.getLargestPoolSize();
    }

    public int getActiveCount() {
        return threadPool.getActiveCount();
    }

    @Override
    public boolean isShutdown() {
        return threadPool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPool.isTerminated();
    }

    public boolean isTerminating() {
        return threadPool.isTerminating();
    }

    @Override
    protected void doShutdown() throws Exception {
        super.doShutdown();
        threadPool.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        try {
            super.doShutdown();
        } catch (Exception e) {
            logger.warn("Failed to unregister mbean.", e);
        }
        return threadPool.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return threadPool.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return threadPool.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return threadPool.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return threadPool.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return threadPool.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return threadPool.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return threadPool.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return threadPool.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        threadPool.execute(command);
    }
}
