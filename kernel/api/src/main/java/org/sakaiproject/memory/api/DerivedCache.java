/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

/**
 * <p>
 * A DerivedCache provides some additional caching derived from the primary data in a Cache. It is directly accessed by the client for this derived data. It is notified by the primary cache it is attached to when the cache contents change.
 * </p>
 */
public interface DerivedCache
{
	/**
	 * Notification that an object has been put into the primary cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @param payload
	 *        The cached object.
	 */
	void notifyCachePut(Object key, Object payload);

	/**
	 * Notification that the primary cache has been cleared of all entries.
	 */
	void notifyCacheClear();

	/**
	 * Notification that this object under this key has been removed from the cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @param payload
	 *        The cached object.
	 */
	void notifyCacheRemove(Object key, Object payload);
}
