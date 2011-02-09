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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.sakaiproject.component.api.ServerConfigurationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class SiteCacheImpl implements DerivedCache, CacheEventListener
{
	
	private static Log M_log = LogFactory.getLog(SiteCacheImpl.class);
	
	ServerConfigurationService serverConfigurationService = null;
	
	/** Map of a tool id to a cached site's tool configuration instance. */
	protected Map<String, ToolConfiguration> m_tools = new ConcurrentHashMap<String, ToolConfiguration>();

	/** Map of a page id to a cached site's SitePage instance. */
	protected Map<String, SitePage> m_pages = new ConcurrentHashMap<String, SitePage>();

	/** Map of a group id to a cached site's Group instance. */
	protected Map<String, Group> m_groups = new ConcurrentHashMap<String, Group>();

	/** The base cache. */
	protected Cache m_cache = null;
	
	/*** Variables to implement site cache specific metrics. The usual Ehcache metrics are not
	 * sufficient because we handle the page / tool / group caching outside of Ehcache. 
	 ***/
	/* Count number of cache event callbacks to the site cache implementation */
	private int cacheEventCount = 0;
	/* Set event interval at which to report the current status of the site cache */
	private int cacheEventReportInterval = 0;

	/**
	 * Construct the Cache. No automatic refresh: expire only, from time and events.
	 * 
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 */

	// Modify constructor to allow injecting the server configuration service.
	public SiteCacheImpl(MemoryService memoryService, long sleep, String pattern, ServerConfigurationService serverConfigurationService)
	{
		m_cache = memoryService.newCache(
				"org.sakaiproject.site.impl.SiteCacheImpl.cache", pattern);

		// setup as the derived cache
		m_cache.attachDerivedCache(this);

		// Provide an instance of the server configuration service.
		this.serverConfigurationService = serverConfigurationService;

		cacheEventReportInterval = serverConfigurationService.getInt("org.sakaiproject.site.impl.SiteCacheImpl.cache.cacheEventReportInterval",
				cacheEventReportInterval);
	}

	// Supply a default server configuration service if it is not supplied.
	public SiteCacheImpl(MemoryService memoryService, long sleep, String pattern) {
		this(memoryService,sleep,pattern,
				(ServerConfigurationService)org.sakaiproject.component.cover.ServerConfigurationService.getInstance());
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
		m_cache.put(key, payload);
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
			for (Iterator<SitePage> pages = site.getPages().iterator(); pages.hasNext();)
			{
				SitePage page = (SitePage) pages.next();
				m_pages.put(page.getId(), page);
				for (Iterator<ToolConfiguration> tools = page.getTools().iterator(); tools.hasNext();)
				{
					ToolConfiguration tool = (ToolConfiguration) tools.next();
					m_tools.put(tool.getId(), tool);
				}
			}

			// add the groups to the cache
			for (Iterator<Group> groups = site.getGroups().iterator(); groups.hasNext();)
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
			for (Iterator<SitePage> pages = site.getPages().iterator(); pages.hasNext();)
			{
				SitePage page = (SitePage) pages.next();
				m_pages.remove(page.getId());
				for (Iterator<ToolConfiguration> tools = page.getTools().iterator(); tools.hasNext();)
				{
					ToolConfiguration tool = (ToolConfiguration) tools.next();
					m_tools.remove(tool.getId());
				}
			}

			for (Iterator<Group> groups = site.getGroups().iterator(); groups.hasNext();)
			{
				Group group = (Group) groups.next();
				m_groups.remove(group.getId());
			}
		}
	}
	
	/***********
	 * Implement routines for Ehcache event notification.  This is to allow explicitly cleaning the 
	 * tool, page, group maps.
	 ***********/
	
	public int getCacheEventReportInterval() {
		return cacheEventReportInterval;
	}

	public void setCacheEventReportInterval(int cacheEventReportInterval) {
		this.cacheEventReportInterval = cacheEventReportInterval;
	}

	/* Note that events happen only when there is a change to the contents of the cache 
	 * so with an efficient cache configuration the tracking of the events will not be expensive.
	 * If the cache configuration is not efficient then you want to know about it.  
	 */
	protected void updateSiteCacheStatistics() {
	
		if (cacheEventReportInterval == 0) {
			return;
		}
		
		++cacheEventCount;
		if (cacheEventCount % cacheEventReportInterval != 0) {
			return;
		}
		
		M_log.info("SiteCache:"
				+" eventCount: "+cacheEventCount
				+" sites  "+m_cache.getSize()
				+" tools: "+m_tools.size()
				+" pages: "+m_pages.size()
				+" groups: "+m_groups.size()
				);
	}
	
	public void dispose() {
		M_log.debug("ehcache event: dispose");	
	}

	public void notifyElementEvicted(Ehcache cache, Element element) {
		
		if (M_log.isDebugEnabled()) {
			M_log.debug("ehcache event: notifyElementEvicted: "+element.getKey());
		}
		
		notifyCacheRemove(element.getObjectKey(), element.getObjectValue());		
		updateSiteCacheStatistics();
	}

	public void notifyElementExpired(Ehcache cache, Element element) {
		if (M_log.isDebugEnabled()) {
			M_log.debug("ehcache event: notifyElementExpired: "+element.getKey());
		}
		
		notifyCacheRemove(element.getObjectKey(), element.getObjectValue());
		updateSiteCacheStatistics();
	}

	public void notifyElementPut(Ehcache cache, Element element)
			throws CacheException {
		if (M_log.isDebugEnabled()) {
			M_log.debug("ehcache event: notifyElementPut: "+element.getKey());
		}
		updateSiteCacheStatistics();
	}

	public void notifyElementRemoved(Ehcache cache, Element element)
			throws CacheException {
		if (M_log.isDebugEnabled()) {
			M_log.debug("ehcache event: notifyElementRemoved: "+element.getKey());	
		}
		updateSiteCacheStatistics();
	}

	public void notifyElementUpdated(Ehcache cache, Element element)
			throws CacheException {
		if (M_log.isDebugEnabled()) {
			M_log.debug("ehcache event: notifyElementUpdated: "+element.getKey());
		}
		updateSiteCacheStatistics();
	}

	public void notifyRemoveAll(Ehcache cache) {
		if (M_log.isDebugEnabled()) {
			M_log.debug("ehcache event: notifyRemoveAll");
		}
		updateSiteCacheStatistics();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		M_log.debug("ehcache event: clone()");
		
		// Creates a clone of this listener. This method will only be called by ehcache before a cache is initialized.
		// This may not be possible for listeners after they have been initialized. Implementations should throw CloneNotSupportedException if they do not support clone.
		throw new CloneNotSupportedException(
				"CacheEventListener implementations should throw CloneNotSupportedException if they do not support clone");
	}

}
