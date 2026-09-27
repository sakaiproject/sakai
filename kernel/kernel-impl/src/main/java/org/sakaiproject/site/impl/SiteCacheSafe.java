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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * A safe and modern version of the site cache which is compatible with distributed caches.
 *
 * Uses multiple caches instead of maps. The tool/page/group satellite caches (which map a
 * tool/page/group id back to its owning site reference) are maintained by explicit calls from
 * put()/remove() rather than a JSR-107 CacheEventListener, since Spring's Cache abstraction has
 * no equivalent listener concept.
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
@Slf4j
public class SiteCacheSafe implements SiteCache
{
    private static final String CACHE_PREFIX = "org.sakaiproject.site.impl.SiteCacheImpl.";
    private static final String MAIN_CACHE_NAME = CACHE_PREFIX+"cache";

    /**
     * Cache of site ref -> Site object (or Boolean which indicates the site exists)
     */
    protected Cache m_cache;

    // the supporting caches - these only store keys, everything refers back to the site cache for actual data
    /**
     * Cache of tool id -> site ref
     */
    protected Cache m_cacheTools;
    /**
     * Cache of page id -> site ref
     */
    protected Cache m_cachePages;
    /**
     * Cache of group id -> site ref
     */
    protected Cache m_cacheGroups;

    /**
     * The live service, needed to rehydrate a raw cache value's transient service references/
     * back-pointers (siteService, m_site, m_page, etc.) before navigating into it - a cache
     * read only deserializes a fresh, disconnected copy, it doesn't rebuild the object graph
     * the way the BaseSite/BaseSitePage/BaseToolConfiguration copy-constructor chain does.
     */
    protected BaseSiteService m_siteService;

    /**
     * Construct the Cache
     *
     * @param cacheManager the Spring/Ignite cache manager
     * @param siteService the live SiteService, used to rehydrate cached Site objects before
     *        navigating into them for a single tool/page/group
     */
    public SiteCacheSafe(CacheManager cacheManager, BaseSiteService siteService) {
        m_cache = cacheManager.getCache(MAIN_CACHE_NAME);
        m_cacheTools = cacheManager.getCache(CACHE_PREFIX+"cacheTools");
        m_cachePages = cacheManager.getCache(CACHE_PREFIX+"cachePages");
        m_cacheGroups = cacheManager.getCache(CACHE_PREFIX+"cacheGroups");
        m_siteService = siteService;
    }

    /**
     * Rehydrate a raw cache value into a fully-reconstructed Site (live services/back-pointers
     * re-attached throughout the page/tool/group graph), the same way getCachedSite() does.
     * A raw Object straight out of the cache only has its plain-data fields populated - its
     * transient fields (and everything nested under them) are null until this runs.
     */
    private Site rehydrate(Object obj) {
        return (obj instanceof Site) ? new BaseSite(m_siteService, (Site) obj, true) : null;
    }

    @Override
    public void put(String key, Object payload) {
        m_cache.put(key, payload);
        if (payload instanceof Site) {
            notifyCachePut(key, (Site) payload);
        }
    }

    @Override
    public boolean containsKey(String key) {
        return m_cache.get(key) != null;
    }

    @Override
    public Object get(String key) {
        Cache.ValueWrapper wrapper = m_cache.get(key);
        return wrapper != null ? wrapper.get() : null;
    }

    @Override
    public void clear() {
        m_cache.clear();
        m_cacheGroups.clear();
        m_cachePages.clear();
        m_cacheTools.clear();
    }

    @Override
    public boolean remove(String key) {
        Object old = get(key);
        m_cache.evict(key);
        if (old instanceof Site) {
            notifyCacheRemove((Site) old);
        }
        return old != null;
    }

    @Override
    public ToolConfiguration getTool(String toolId) {
        ToolConfiguration toolConfiguration = null;
        String siteRef = m_cacheTools.get(toolId, String.class);
        if (siteRef != null) {
            Site site = rehydrate(get(siteRef));
            if (site != null) {
                toolConfiguration = site.getTool(toolId);
            }
        }
        return toolConfiguration;
    }

    @Override
    public SitePage getPage(String pageId) {
        SitePage sitePage = null;
        String siteRef = m_cachePages.get(pageId, String.class);
        if (siteRef != null) {
            Site site = rehydrate(get(siteRef));
            if (site != null) {
                sitePage = site.getPage(pageId);
            }
        }
        return sitePage;
    }

    @Override
    public Group getGroup(String groupId) {
        Group group = null;
        String siteRef = m_cacheGroups.get(groupId, String.class);
        if (siteRef != null) {
            Site site = rehydrate(get(siteRef));
            if (site != null) {
                group = site.getGroup(groupId);
            }
        }
        return group;
    }

    /**
     * Populate the tool/page/group satellite caches for a site that was just put in the cache.
     */
    private void notifyCachePut(String siteReference, Site site) {
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

    /**
     * Clean up the tool/page/group satellite caches for a site that was just removed from the cache.
     */
    private void notifyCacheRemove(Site site) {
        for (SitePage page : site.getPages()) {
            m_cachePages.evict(page.getId());
            for (ToolConfiguration tool : page.getTools()) {
                m_cacheTools.evict(tool.getId());
            }
        }
        for (Group group : site.getGroups()) {
            m_cacheGroups.evict(group.getId());
        }
    }

}
