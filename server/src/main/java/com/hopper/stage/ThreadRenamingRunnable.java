package com.hopper.stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrappers the {@link Runnable} for renaming current thread name
 */
public abstract class ThreadRenamingRunnable implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String taskName;

    public ThreadRenamingRunnable(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public final void run() {
        Thread thread = Thread.currentThread();
        String oldThreadName = thread.getName();
        String newTheradName = oldThreadName + "_" + taskName;

        boolean isRenamed = false;
        try {
            thread.setName(newTheradName);
            isRenamed = true;
        } catch (SecurityException e) {
            logger.debug("Failed to rename thread due to security exception", e);
        }

        try {
            executeTask();
        } finally {
            if (isRenamed) {
                // There is no security exception
                thread.setName(oldThreadName);
            }
        }
    }

    /**
     * Concrete template method
     */
    protected abstract void executeTask();
}
