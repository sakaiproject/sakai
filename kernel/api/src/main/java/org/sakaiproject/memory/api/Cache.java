/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
	 * Cache an object
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 * @deprecated Since Sakai 2.5.0
	 * @see Cache#put(Object, Object)
	 */
	void put(Object key, Object payload, int duration);

	/**
	 * Cache an object - don't automatically exipire it.
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 */
	void put(Object key, Object payload);

	/**
	 * Test for an entry in the cache - expired or not.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a cache entry, false if not.
	 * @deprecated Since Sakai 2.5.0
	 * @see Cache#containsKey(Object)
	 */
	boolean containsKeyExpiredOrNot(Object key);

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
	 * Get the entry, or null if not there (expired entries are returned, too).
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found. (Note: use containsKey() to remove this ambiguity).
	 * @deprecated Since Sakai 2.5.0
	 * @see Cache#get(Object)
	 */
	Object getExpiredOrNot(Object key);

	/**
	 * Get the non expired entry, or null if not there (or expired)
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found, or the entry has expired (Note: use containsKey() to remove this ambiguity).
	 */
	Object get(Object key);

	/**
	 * Get all the non-expired non-null entries.
	 * 
	 * @return all the non-expired non-null entries, or an empty list if none.
	 * @deprecated Since Sakai 2.5.0
	 */
	List getAll();

	/**
	 * Get all the non-expired non-null entries that are in the specified reference path. Note: only works with String keys.
	 * 
	 * @param path
	 *        The reference path.
	 * @return all the non-expired non-null entries, or an empty list if none.
	 * @deprecated Since Sakai 2.5.0
	 */
	List getAll(String path);

	/**
	 * Get all the keys
	 * 
	 * @return The List of key values (Object).
	 */
	List getKeys();

	/**
	 * Get all the keys, modified from resource references to ids by removing the resource prefix. Note: only works with String keys.
	 * 
	 * @return The List of keys converted from references to ids (String).
	 * @deprecated Since Sakai 2.5.0
	 */
	List getIds();

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
