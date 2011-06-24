package org.sakaiproject.profile2.cache;

import lombok.Setter;

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

	@Setter
	private MemoryService memoryService;
}
