/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.api;

import java.util.List;

/**
 * <p>
 * A Cache holds objects with keys with a limited lifespan.
 * </p>
 * <p>
 * When the object expires, the cache may call upon a CacheRefresher to update the key's value. The update is done in a separate thread.
 * </p>
 */
public interface Cache extends Cacher
{

	/**
	 * Cache an object - don't automatically exipire it.
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 */
	void put(Object key, Object payload);


	/**
	 * Test for a non expired entry in the cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a non-expired cache entry, false if not.
	 */
	boolean containsKey(Object key);

	/**
	 * Expire this object.
	 * 
	 * @param key
	 *        The cache key.
	 */
	void expire(Object key);


	/**
	 * Get the non expired entry, or null if not there (or expired)
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found, or the entry has expired (Note: use containsKey() to remove this ambiguity).
	 */
	Object get(Object key);

	/**
	 * Get all the keys
	 * 
	 * @return The List of key values (Object).
	 */
	List getKeys();


	/**
	 * Clear all entries.
	 */
	void clear();

	/**
	 * Remove this entry from the cache.
	 * 
	 * @param key
	 *        The cache key.
	 */
	void remove(Object key);

	/**
	 * Disable the cache.
	 */
	void disable();

	/**
	 * Enable the cache.
	 */
	void enable();

	/**
	 * Is the cache disabled?
	 * 
	 * @return true if the cache is disabled, false if it is enabled.
	 */
	boolean disabled();

	/**
	 * Are we complete?
	 * 
	 * @return true if we have all the possible entries cached, false if not.
	 */
	boolean isComplete();

	/**
	 * Set the cache to be complete, containing all possible entries.
	 */
	void setComplete();

	/**
	 * Are we complete for one level of the reference hierarchy?
	 * 
	 * @param path
	 *        The reference to the completion level.
	 * @return true if we have all the possible entries cached, false if not.
	 */
	boolean isComplete(String path);

	/**
	 * Set the cache to be complete for one level of the reference hierarchy.
	 * 
	 * @param path
	 *        The reference to the completion level.
	 */
	void setComplete(String path);

	/**
	 * Set the cache to hold events for later processing to assure an atomic "complete" load.
	 */
	void holdEvents();

	/**
	 * Restore normal event processing in the cache, and process any held events now.
	 */
	void processEvents();

	/**
	 * Clear all entries and shudown the cache. Don't use after this call.
	 */
	void destroy();
	
	/**
	 * Attach this DerivedCache to the cache. The DerivedCache is then notified of the cache contents changing events.
	 * 
	 * @param cache
	 *        The DerivedCache to attach.
	 */
	void attachDerivedCache(DerivedCache cache);
}
