/******************************************************************************
 * $URL: https://source.sakaiproject.org/svn/master/trunk/header.java $
 * $Id: header.java 307632 2014-03-31 15:29:37Z azeckoski@unicon.net $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.site.impl;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.*;
import org.sakaiproject.memory.impl.BasicCache;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * A safe and modern version of the site cache which is compatible with distributed caches
 *
 * Uses multiple caches instead of maps and uses the new Sakai MemoryService and Cache APIs which are JSR-107 compliant
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
@Slf4j
public class SiteCacheSafe extends BasicCache<String, Object> implements SiteCache, CacheEventListener<String, Object>
{
    private static final String CACHE_PREFIX = "org.sakaiproject.site.impl.SiteCacheImpl.";
    private static final String MAIN_CACHE_NAME = CACHE_PREFIX+"cache";

    /**
     * Cache of site ref -> Site object (or Boolean which indicates the site exists)
     */
    protected Cache m_cache = null;

    // the supporting caches - these only store keys, everything refers back to the site cache for actual data
    /**
     * Cache of tool id -> site ref
     */
    protected Cache m_cacheTools = null;
    /**
     * Cache of page id -> site ref
     */
    protected Cache m_cachePages = null;
    /**
     * Cache of group id -> site ref
     */
    protected Cache m_cacheGroups = null;

    BaseSiteService m_siteService;

    /**
     * Construct the Cache
     *
     * @param memoryService the memory service
     */
    public SiteCacheSafe(MemoryService memoryService, EventTrackingService eventTrackingService) {
        super(MAIN_CACHE_NAME);
        m_cache = memoryService.getCache(MAIN_CACHE_NAME);
        m_cache.registerCacheEventListener(this);
        if (!m_cache.isDistributed()) {
            // KNL_1229 use an Observer for cache cleanup when the cache is not distributed
            log.info("Creating SiteCacheImpl.cache observer for event based cache expiration (for local caches)");
            m_cacheObserver = new CacheObserver();
            eventTrackingService.addObserver(m_cacheObserver);
        }
        // create matching caches for the remaining elements (use an identical config so that expirations are aligned)
        Configuration cacheConfig = m_cache.getConfiguration();
        m_cacheTools = memoryService.createCache(CACHE_PREFIX+"cacheTools", cacheConfig);
        m_cachePages = memoryService.createCache(CACHE_PREFIX+"cachePages", cacheConfig);
        m_cacheGroups = memoryService.createCache(CACHE_PREFIX+"cacheGroups", cacheConfig);
    }

    /**
     * KNL-1229 Supports legacy event based cache expiration
     */
    CacheObserver m_cacheObserver;

    /**
     * KNL-1229 Allow for legacy event based cache expiration
     * Only used when distributed caches are not in use
     */
    class CacheObserver implements Observer {
        @Override
        public void update(Observable observable, Object o) {
            if (o instanceof Event) {
                Event event = (Event) o;
                if (event.getResource() != null && (
                        BaseSiteService.SECURE_ADD_SITE.equals(event.getEvent())
                                || BaseSiteService.SECURE_UPDATE_SITE.equals(event.getEvent())
                                || BaseSiteService.SECURE_REMOVE_SITE.equals(event.getEvent())
                )
            ) {
                    String siteRef = event.getResource();
                    m_cache.remove(siteRef);
                }
            }
        }
    }

    @Override
    public void put(String key, Object payload) {
        m_cache.put(key, payload);
    }

    @Override
    public boolean containsKey(String key) {
        return m_cache.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return m_cache.get(key);
    }

    @Override
    public void clear() {
        m_cache.clear();
        m_cacheGroups.clear();
        m_cachePages.clear();
        m_cacheTools.clear();
    }

    @Override
    public Configuration getConfiguration() {
        return m_cache.getConfiguration();
    }

    @Override
    public String getName() {
        return m_cache.getName();
    }

    @Override
    public void close() {
        m_cache.close();
        m_cacheGroups.close();
        m_cachePages.close();
        m_cacheTools.close();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return (T) m_cache.unwrap(clazz);
    }


    @Override
    public void registerCacheEventListener(CacheEventListener cacheEventListener) {
        m_cache.registerCacheEventListener(cacheEventListener);
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        return m_cache.getCacheStatistics();
    }

    @Override
    public Properties getProperties(boolean includeExpensiveDetails) {
        return m_cache.getProperties(includeExpensiveDetails);
    }

    @Override
    public String getDescription() {
        return m_cache.getDescription();
    }

    @Override
    public void attachLoader(CacheLoader cacheLoader) {
        m_cache.attachLoader(cacheLoader);
    }

    @Override
    public boolean isDistributed() {
        return m_cache.isDistributed();
    }

    @Override
    public boolean remove(String key) {
        return m_cache.remove(key);
    }

    @Override
    public void removeAll() {
        m_cache.removeAll();
    }

    @Override
    public ToolConfiguration getTool(String toolId) {
        ToolConfiguration toolConfiguration = null;
        String siteRef = (String) m_cacheTools.get(toolId);
        if (siteRef != null) {
            Object obj = m_cache.get(siteRef);
            if (obj != null && obj instanceof Site) {
                toolConfiguration = ((Site)obj).getTool(toolId);
            }
        }
        return toolConfiguration;
    }

    @Override
    public SitePage getPage(String pageId) {
        SitePage sitePage = null;
        String siteRef = (String) m_cachePages.get(pageId);
        if (siteRef != null) {
            Object obj = m_cache.get(siteRef);
            if (obj != null && obj instanceof Site) {
                sitePage = ((Site)obj).getPage(pageId);
            }
        }
        return sitePage;
    }

    @Override
    public Group getGroup(String groupId) {
        Group group = null;
        String siteRef = (String) m_cacheGroups.get(groupId);
        if (siteRef != null) {
            Object obj = m_cache.get(siteRef);
            if (obj != null && obj instanceof Site) {
                group = ((Site)obj).getGroup(groupId);
            }
        }
        return group;
    }

    private void notifyCachePut(String siteReference, Site site) {
        //noinspection ConstantConditions
        if (site != null && site instanceof Site) {
            Collection<SitePage> sitePages;
            Collection<Group> siteGroups;
            // TODO: If the boolean versions of getPages and getGroups are added to the Site interface, this check should be removed.
            if (site instanceof BaseSite) {
                //noinspection unchecked
                sitePages  = ((BaseSite) site).getPages(false);
                //noinspection unchecked
                siteGroups = ((BaseSite) site).getGroups(false);
            } else {
                sitePages  = site.getPages();
                siteGroups = site.getGroups();
            }
            // add the pages and tools to the cache
            for (SitePage page : sitePages) {
                m_cachePages.put(page.getId(), siteReference);
                for (ToolConfiguration tool : page.getTools()) {
                    m_cacheTools.put(tool.getId(), siteReference);
                }
            }
            // add the groups to the cache
            for (Group group : siteGroups) {
                m_cacheGroups.put(group.getId(), siteReference);
            }
        }
    }

    private void notifyCacheRemove(@SuppressWarnings("UnusedParameters") String key, Site site) {
        // clear the tool ids for this site
        //noinspection ConstantConditions
        if (site != null && site instanceof Site) {
            for (SitePage page : site.getPages()) {
                m_cachePages.remove(page.getId());
                for (ToolConfiguration tool : page.getTools()) {
                    m_cacheTools.remove(tool.getId());
                }
            }
            for (Group group : site.getGroups()) {
                m_cacheGroups.remove(group.getId());
            }
        }
    }

    // CACHE LISTENER METHODS

    @Override
    public boolean evaluate(CacheEntryEvent event) {
        return true;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ?>> cacheEntryEvents) {
        onUpdated(cacheEntryEvents);
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ?>> cacheEntryEvents) {
        for (CacheEntryEvent<? extends String, ?> cee : cacheEntryEvents) {
            // this ugly code is necessary because the cache holds Boolean and Site which JSR-107 does not like
            Object value = cee.isOldValueAvailable() ? cee.getOldValue() : cee.getValue();
            if (value instanceof Site) {
                notifyCachePut(cee.getKey(), (Site) value);
            }
        }
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ?>> cacheEntryEvents) {
        // should not be necessary unless the cache settings are out of sync - maybe remove this?
        onRemoved(cacheEntryEvents);
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ?>> cacheEntryEvents) {
        for (CacheEntryEvent<? extends String, ?> cee : cacheEntryEvents) {
            // this ugly code is necessary because the cache holds Boolean and Site which JSR-107 does not like
            Object value = cee.isOldValueAvailable() ? cee.getOldValue() : cee.getValue();
            if (value instanceof Site) {
                notifyCacheRemove(cee.getKey(), (Site) value);
            }
        }
    }

}
