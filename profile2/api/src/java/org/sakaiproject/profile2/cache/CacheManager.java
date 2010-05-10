package org.sakaiproject.profile2.cache;

import org.sakaiproject.memory.api.Cache;

/**
 * Lightweight interface for caching in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface CacheManager {

	/**
	 * Create a cache.
	 * @param cacheName
	 * @return
	 */
	public Cache createCache(String cacheName);
}
