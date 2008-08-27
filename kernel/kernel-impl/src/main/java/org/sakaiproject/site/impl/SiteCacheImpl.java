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

package org.sakaiproject.site.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.DerivedCache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * <p>
 * SiteCacheImpl is a cache tuned for Site (and page / tool) access.
 * </p>
 */
public class SiteCacheImpl implements DerivedCache
{
	/** Map of a tool id to a cached site's tool configuration instance. */
	protected Map m_tools = new ConcurrentHashMap();

	/** Map of a page id to a cached site's SitePage instance. */
	protected Map m_pages = new ConcurrentHashMap();

	/** Map of a group id to a cached site's Group instance. */
	protected Map m_groups = new ConcurrentHashMap();

	/** The base cache. */
	protected Cache m_cache = null;

	/**
	 * Construct the Cache. No automatic refresh: expire only, from time and events.
	 * 
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 */
	public SiteCacheImpl(MemoryService memoryService, long sleep, String pattern)
	{
		m_cache = memoryService.newCache(
				"org.sakaiproject.site.impl.SiteCacheImpl.cache", pattern);

		// setup as the derived cache
		m_cache.attachDerivedCache(this);
	}

	/**
	 * Cache an object
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 */
	public void put(Object key, Object payload, int duration)
	{
		m_cache.put(key, payload, duration);
	}

	/**
	 * Test for a non expired entry in the cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a non-expired cache entry, false if not.
	 */
	public boolean containsKey(Object key)
	{
		return m_cache.containsKey(key);
	}

	/**
	 * Get the non expired entry, or null if not there (or expired)
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found, or the entry has expired (Note: use containsKey() to remove this ambiguity).
	 */
	public Object get(Object key)
	{
		return m_cache.get(key);
	}

	/**
	 * Clear all entries.
	 */
	public void clear()
	{
		m_cache.clear();
	}

	/**
	 * Remove this entry from the cache.
	 * 
	 * @param key
	 *        The cache key.
	 */
	public void remove(Object key)
	{
		m_cache.remove(key);
	}

	/**
	 * Access the tool that is part of a cached site, by tool Id.
	 * 
	 * @param toolId
	 *        The tool's id.
	 * @return The ToolConfiguration that has this id, from a cached site.
	 */
	public ToolConfiguration getTool(String toolId)
	{
		return (ToolConfiguration) m_tools.get(toolId);
	}

	/**
	 * Access the page that is part of a cached site, by page Id.
	 * 
	 * @param pageId
	 *        The page's id.
	 * @return The SitePage that has this id, from a cached site.
	 */
	public SitePage getPage(String pageId)
	{
		return (SitePage) m_pages.get(pageId);
	}

	/**
	 * Access the group that is part of a cached site, by group Id.
	 * 
	 * @param id
	 *        The group id.
	 * @return The Group that has this id, from a cached site.
	 */
	public Group getGroup(String groupId)
	{
		return (Group) m_groups.get(groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyCacheClear()
	{
		// clear the tool ids
		m_tools.clear();

		// clear the pages
		m_pages.clear();

		// clear the groups
		m_groups.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyCachePut(Object key, Object payload)
	{
		// add the payload (Site) tool ids
		if (payload instanceof Site)
		{
			Site site = (Site) payload;

			// add the pages and tools to the cache
			for (Iterator pages = site.getPages().iterator(); pages.hasNext();)
			{
				SitePage page = (SitePage) pages.next();
				m_pages.put(page.getId(), page);
				for (Iterator tools = page.getTools().iterator(); tools.hasNext();)
				{
					ToolConfiguration tool = (ToolConfiguration) tools.next();
					m_tools.put(tool.getId(), tool);
				}
			}

			// add the groups to the cache
			for (Iterator groups = site.getGroups().iterator(); groups.hasNext();)
			{
				Group group = (Group) groups.next();
				m_groups.put(group.getId(), group);
			}
		}		
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyCacheRemove(Object key, Object payload)
	{
		// clear the tool ids for this site
		if ((payload != null) && (payload instanceof Site))
		{
			Site site = (Site) payload;
			for (Iterator pages = site.getPages().iterator(); pages.hasNext();)
			{
				SitePage page = (SitePage) pages.next();
				m_pages.remove(page.getId());
				for (Iterator tools = page.getTools().iterator(); tools.hasNext();)
				{
					ToolConfiguration tool = (ToolConfiguration) tools.next();
					m_tools.remove(tool.getId());
				}
			}

			for (Iterator groups = site.getGroups().iterator(); groups.hasNext();)
			{
				Group group = (Group) groups.next();
				m_groups.remove(group.getId());
			}
		}
	}
}
