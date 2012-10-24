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

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.Timestamper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class attempts to get the Hibernate cache through Sakai locations.
 */
public class SakaiCacheProvider implements CacheProvider, ApplicationContextAware
{
	private static final Log LOG = LogFactory.getLog(SakaiCacheProvider.class);

	private CacheManager sakaiCacheManager;

	private net.sf.ehcache.Cache defaultCache;

	// We make the class aware it's in Spring so it doesn't need to use the component manager.
	private ApplicationContext applicationContext;

	public void setSakaiCacheManager(CacheManager sakaiCacheManager) {
		this.sakaiCacheManager = sakaiCacheManager;
	}
	
	public void setDefaultCache(net.sf.ehcache.Cache defaultCache) {
		this.defaultCache = defaultCache;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

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

			if (applicationContext.containsBean(cacheName))
			{
				try
				{
					cache = (net.sf.ehcache.Cache) applicationContext.getBean(cacheName);
					LOG.info("Loaded cache from component manager: "+ cacheName);
				}
				catch (ClassCastException e)
				{
					LOG.warn("Illegal class type (must be net.sf.ehcache.Cache) for cache bean: "
							+ cacheName);
				}
			}
			

			// try to get cache directly from ehcache next
			if (cache == null)
			{
				CacheManager cacheManager = sakaiCacheManager;
				if (cacheManager != null && cacheManager.cacheExists(cacheName))
				{
					cache = cacheManager.getCache(cacheName);
					LOG.info("Loaded cache from cache manager: " + cacheName);
				}
			}

			// load up the default cache bean next
			if (cache == null)
			{
				cache = defaultCache;
				if (cache != null)
				{
					LOG.info("Loaded Default Cache bean for "+ cacheName);
				}
			}

			// finally just get a default cache from the cache manager
			if (cache == null)
			{
				cache = sakaiCacheManager.getCache(cacheName);
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
