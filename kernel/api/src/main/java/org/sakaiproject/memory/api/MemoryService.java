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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.api;

/**
 * <p>
 * MemoryService is the primary interface for the Sakai Memory service
 * </p>
 * <p>
 * This tracks memory users (cachers), runs a periodic garbage collection to keep memory available, and can be asked to report memory usage.
 * </p>
 */
public interface MemoryService
{
	/**
	 * Report the amount of unused and available memory for the JVM
	 *
	 * @return the amount of available memory.
	 */
   public long getAvailableMemory();

	/**
	 * Cause less memory to be used by clearing any optional caches.
	 *
	 * @throws SecurityException if the current user does not have permission to do this.
	 */
	void resetCachers();

	/**
	 * Evict all expired objects from the in-memory caches
	 *
	 * @throws SecurityException if the current user does not have permission to do this.
	 */
	void evictExpiredMembers();

   /**
    * Construct a Cache with the given name (often this is the fully qualified classpath of the api 
    * for the service that is being cached or the class if there is no api) or retrieve the one
    * that already exists with this name,
    * this will operate on system defaults (probably a distributed cache without replication)
    * @param cacheName Load a defined bean from the application context with this name or create a default cache with this name
    * @return a cache which can be used to store objects
    */
	public Cache newCache(String cacheName);

	/**
	 * Construct a Cache. Attempts to keep complete on Event notification by calling the refresher.
	 *
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 * @param refresher
	 *        The object that will handle refreshing of event notified modified or added entries.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
     *        If this is set then it enables automatic removal of the matching cache entry key (to the event reference value)
     *        when the event reference starts with this pattern string.
     * @deprecated pattern matching no longer needed or supported, 07/Oct/2007 -AZ
	 */
    @SuppressWarnings("deprecation")
    Cache newCache(String cacheName, CacheRefresher refresher, String pattern); // used in NotificationCache, AssignmentService(3), BaseContentService, BaseMessage(3)

	/**
	 * Construct a Cache. Attempts to keep complete on Event notification by calling the refresher.
	 *
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
     *        If this is set then it enables automatic removal of the matching cache entry key (to the event reference value)
     *        when the event reference starts with this pattern string.
     * @deprecated pattern matching no longer needed or supported, 07/Oct/2007 -AZ
	 */
	Cache newCache(String cacheName, String pattern); // used in BaseAliasService, SiteCacheImpl, BaseUserDirectoryService (2), BaseCalendarService(3), ShareUserCacheImpl

	/**
	 * Get a status report of memory cache usage
	 * @return A string representing the current status of all caches
	 */
	public String getStatus();

	/**
	 * Flushes and destroys the cache with this name<br/>
	 * @param cacheName unique name for this cache
	 */
	public void destroyCache(String cacheName);

	/**
	 * Construct a multi-ref Cache. No automatic refresh: expire only, from time and events.
	 * NOT Cluster safe
	 *
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 * @deprecated since Sakai 2.9, this should no longer be used, it is not cluster safe of JSR-107 compatible
	 */
	GenericMultiRefCache newGenericMultiRefCache(String cacheName);

}
