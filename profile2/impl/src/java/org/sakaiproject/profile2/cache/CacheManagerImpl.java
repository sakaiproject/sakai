package org.sakaiproject.profile2.cache;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

/**
 * Implementation of CacheManager for Profile2.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class CacheManagerImpl implements CacheManager {

	/**
 	* {@inheritDoc}
 	*/
	public Cache createCache(String cacheName) {
		return memoryService.newCache(cacheName);
	}

	private MemoryService memoryService;
	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
}
