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

package org.sakaiproject.memory.impl;

import net.sf.ehcache.Ehcache;

import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.CacheRefresher;

/**
 * <p>
 * HardCache is a MemCache set to use hard, not soft references.
 * @deprecated
 * </p>
 */
public class HardCache extends MemCache
{
	/**
	 * Construct the Cache. No automatic refresh handling.
	 */
	public HardCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, Ehcache cache)
	{
		super(memoryService, eventTrackingService, cache);
	}

	/**
	 * Construct the Cache. Attempts to keep complete on Event notification by calling the refresher.
	 * 
	 * @param refresher
	 *        The object that will handle refreshing of event notified modified or added entries.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 */
	public HardCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService,
			CacheRefresher refresher, String pattern, Ehcache cache)
	{
		super(memoryService, eventTrackingService, refresher, pattern, cache);
	}

	/**
	 * Construct the Cache. Automatic refresh handling if refresher is not null.
	 * 
	 * @param refresher
	 *        The object that will handle refreshing of expired entries.
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @deprecated
	 */
	public HardCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService,
			CacheRefresher refresher, long sleep, Ehcache cache)
	{
		super(memoryService, eventTrackingService, refresher, sleep, cache);
	}

	/**
	 * Construct the Cache. No automatic refresh: expire only, from time and events.
	 * 
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 * @deprecated
	 */
	public HardCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, long sleep,
			String pattern, Ehcache cache)
	{
		super(memoryService, eventTrackingService, sleep, pattern, cache);
	}

	public HardCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, String pattern,
			Ehcache cache) 
	{
		super(memoryService, eventTrackingService, pattern, cache);
	}

}
