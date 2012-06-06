package com.hopper.cache;

/**
 * cache entry
 * 
 * @author chenguoqing
 */
public class CacheEntry {
	/**
	 * cache value
	 */
	public final Object value;
	/**
	 * cache period(milliseconds)
	 */
	public final long period;
	/**
	 * Cache time
	 */
	public final long enterTime;

	CacheEntry(Object value, long period, long enterTime) {
		this.value = value;
		this.period = period;
		this.enterTime = enterTime;
	}

	public boolean isExpire() {
		return System.currentTimeMillis() - enterTime >= period;
	}
}
