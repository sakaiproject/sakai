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

package org.sakaiproject.springframework.orm.hibernate;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.Timestamper;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * @author ieb
 * @author azeckoski - fixed naming issues and now gets caches from the
 *         configuration as well
 */
public class SakaiCacheProvider implements CacheProvider
{

	private static final Log LOG = LogFactory.getLog(SakaiCacheProvider.class);

	private static final String DEFAULT = "org.sakaiproject.springframework.orm.hibernate.L2Cache";

	private static final String CACHE_MANAGER = "org.sakaiproject.memory.api.MemoryService.cacheManager";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.cache.CacheProvider#buildCache(java.lang.String,
	 *      java.util.Properties)
	 */
	public Cache buildCache(final String cacheName, Properties properties)
			throws CacheException
	{
		try
		{
			net.sf.ehcache.Cache cache = null;
			// try to get a bean which defines this cache first
			try
			{
				cache = (net.sf.ehcache.Cache) ComponentManager.get(cacheName);
			}
			catch (ClassCastException e)
			{
				LOG
						.warn("Illegal class type (must be net.sf.ehcache.Cache) for cache bean: "
								+ cacheName);
			}

			// try to get cache directly from ehcache next
			if (cache == null)
			{
				try
				{
					CacheManager cacheManager = (CacheManager) ComponentManager
							.get(CACHE_MANAGER);
					if (cacheManager != null && cacheManager.cacheExists(cacheName))
					{
						cache = cacheManager.getCache(cacheName);
						LOG.info("Loaded cache from cache manager: " + cacheName);
					}
				}
				catch (ClassCastException e)
				{
					LOG
							.warn("Illegal class type (must be net.sf.ehcache.CacheManager) for cache manager bean: "
									+ CACHE_MANAGER);
				}
			}

			// load up the default cache bean next
			if (cache == null)
			{
				cache = (net.sf.ehcache.Cache) ComponentManager.get(DEFAULT);
				if (cache != null)
				{
					LOG.info("Loaded Default Cache bean (" + DEFAULT + ") for "
							+ cacheName);
				}
			}

			// finally just get a default cache from the cache manager
			if (cache == null)
			{
				// this will throw exceptions if it fails at this point
				CacheManager cacheManager = (CacheManager) ComponentManager
						.get(CACHE_MANAGER);
				cache = cacheManager.getCache(cacheName);
				LOG.info("Loaded default cache from cache manager: " + cacheName);
			}

			return new EhCache(cache)
			{
				@Override
				public void destroy() throws CacheException
				{
					LOG.debug("Closing Cache, leaving cleanup to the context: "
							+ cacheName);
				}
			};
		}
		catch (Exception e)
		{
			LOG.error("Failed to build Cache: " + cacheName, e);
			throw new CacheException("Failed to build Cache: " + cacheName, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.cache.CacheProvider#isMinimalPutsEnabledByDefault()
	 */
	public boolean isMinimalPutsEnabledByDefault()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.cache.CacheProvider#nextTimestamp()
	 */
	public long nextTimestamp()
	{
		return Timestamper.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.cache.CacheProvider#start(java.util.Properties)
	 */
	public void start(Properties arg0) throws CacheException
	{
		LOG.info("Starting Hibernate Cache Cache ++++++++++++++++++++++++++++++++ ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.cache.CacheProvider#stop()
	 */
	public void stop()
	{
		LOG.info("Stopping Hibernate Cache Cache ------------------------------- ");
		// leave spring to perform the shutdown
	}

}
