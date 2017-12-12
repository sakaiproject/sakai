/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.cache;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

/**
 * Implementation of CacheManager for Profile2.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class CacheManagerImpl implements CacheManager {

	@Override
	public <K, V>Cache<K, V> createCache(String cacheName) {
		return memoryService.getCache(cacheName);
	}
	
	@Override
	public <K, V>void evictFromCache(Cache<K, V> cache, K cacheKey) {
		cache.remove(cacheKey);
		log.debug("Evicted data in cache: " + cache.getName() + ", key: " + cacheKey);
	}
	
	@Setter
	private MemoryService memoryService;

}
