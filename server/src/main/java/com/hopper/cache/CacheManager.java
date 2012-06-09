package com.hopper.cache;

import com.hopper.lifecycle.Lifecycle;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.utils.ScheduleManager;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cache manager for all local caches. {@link CacheManager} provides a daemon
 * thread for evicting the expire entries.
 * 
 * @author chenguoqing
 * 
 */
public class CacheManager extends LifecycleProxy implements Lifecycle {
	/**
	 * Caches entries
	 */
	private final Map<Object, CacheEntry> caches = new ConcurrentHashMap<Object, CacheEntry>();

	/**
	 * Evict task
	 */
	private final Runnable evictTask = new EvictTask();
	/**
	 * Inject the {@link ScheduleManager} instance
	 */
	private ScheduleManager scheduleManager;
	/**
	 * Evict period
	 */
	private long evictPeriod;

	public ScheduleManager getScheduleManager() {
		return scheduleManager;
	}

	public void setScheduleManager(ScheduleManager scheduleManager) {
		this.scheduleManager = scheduleManager;
	}

	public void setEvictPeriod(long evictPeriod) {
		this.evictPeriod = evictPeriod;
	}

	/**
	 * Initialize
	 */
	@Override
	protected void doInit() {
		// Default value 1000(mills second)
		if (evictPeriod <= 0) {
			evictPeriod = 1000L;
		}
	}

	@Override
	protected void doStart()  {
		this.scheduleManager.schedule(evictTask, evictPeriod, evictPeriod);
	}

	@Override
	protected void doShutdown() {
		if (scheduleManager != null) {
			scheduleManager.removeTask(evictTask);
		}

		// unbound all cache entries
		caches.clear();
	}

	/**
	 * Put the entry with timeout
	 * 
	 * @param key
	 *            unique object key
	 * @param value
	 *            value
	 * @param period
	 *            timeout(milliseconds)
	 */
	public void put(Object key, Object value, long period) {
		CacheEntry entry = new CacheEntry(value, period, System.currentTimeMillis());
		caches.put(key, entry);
	}

	/**
	 * Remove object
	 */
	public <T> T remove(Object key) {
		CacheEntry entry = caches.remove(key);
		return entry != null && !entry.isExpire() ? (T) entry.value : null;
	}

	public <T> T get(Object key) {
		CacheEntry entry = caches.get(key);

		return entry != null && !entry.isExpire() ? (T) entry.value : null;
	}

	public int size() {
		return caches.size();
	}

	class EvictTask extends ScheduleManager.ThreadRenamingRunnable {

		EvictTask() {
			super("Cache-evict-timer");
		}

		@Override
		protected void executeTask() {
			List<Object> evictableKeys = new ArrayList<Object>();

			for (Object key : caches.keySet()) {
                CacheEntry entry = caches.get(key);

                if (entry == null || entry.isExpire()) {
                    evictableKeys.add(key);
                }
            }

			for (Object key : evictableKeys) {
				caches.remove(key);
			}
		}
	}
}
