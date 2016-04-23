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

package org.sakaiproject.springframework.orm.hibernate;

import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.Timestamper;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Properties;

/**
 * This class attempts to get the Hibernate cache through Sakai locations.
 * Updated to no longer use ehcache directly
 */
public class SakaiCacheProvider implements CacheProvider, ApplicationContextAware
{
	private static final Logger LOG = LoggerFactory.getLogger(SakaiCacheProvider.class);

    private Cache defaultCache;

    private MemoryService memoryService;
    private String defaultCacheName = "org.sakaiproject.springframework.orm.hibernate.L2Cache";
    // We make the class aware it's in Spring so it doesn't need to use the component manager.
	private ApplicationContext applicationContext;

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public void setDefaultCacheName(String defaultCacheName) {
        this.defaultCacheName = defaultCacheName;
    }

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

    public void init() {
        LOG.info("INIT: hibernate cache: "+defaultCacheName);
        defaultCache = memoryService.getCache(defaultCacheName);
    }

    public void destroy() {
        try {
            defaultCache.close();
        } catch (Exception e) {
            // IGNORE
        }
    }

    // CacheProvider

    @Override
	public boolean isMinimalPutsEnabledByDefault()
	{
		return false;
	}

    @Override
    public org.hibernate.cache.Cache buildCache(String s, Properties properties) throws CacheException {
        try {
            net.sf.ehcache.Ehcache ehcache = (Ehcache) defaultCache.unwrap(Ehcache.class); // Ehcache required for now
            org.hibernate.cache.Cache rv = new EhCache(ehcache);
            return rv;
        } catch (Exception e) {
            // not an ehcache so we have to die for now
            LOG.error("Failed to build hibernate cache from ehcache: " + defaultCacheName + ":"+e, e);
            throw new CacheException("Unable to get net.sf.ehcache.Ehcache for hibernate secondary cache", e);
        }
    }

    @Override
	public long nextTimestamp()
	{
		return Timestamper.next();
	}

    @Override
	public void start(Properties arg0) throws CacheException
	{
		LOG.info("Starting Hibernate Cache "+defaultCacheName+" ++++++++++++++++++++++++++++++++ ");
	}

    @Override
	public void stop()
	{
		LOG.info("Stopping Hibernate Cache "+defaultCacheName+" ------------------------------- ");
	}

}
