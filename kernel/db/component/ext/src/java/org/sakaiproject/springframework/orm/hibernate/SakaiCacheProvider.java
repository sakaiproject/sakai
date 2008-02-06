/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
 */
public class SakaiCacheProvider implements CacheProvider
{

	private static final Log LOG = LogFactory.getLog(SakaiCacheProvider.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.cache.CacheProvider#buildCache(java.lang.String,
	 *      java.util.Properties)
	 */
	public Cache buildCache(String name, Properties properties) throws CacheException
	{

		try
		{
			String defaultName = "org.sakaiproject.springframework.orm.hibernate.L2Cache";
			final String cacheName = defaultName+"."+name;
			net.sf.ehcache.Cache cache = null;
			try {
				cache = (net.sf.ehcache.Cache) ComponentManager.get(cacheName);
			} catch ( Exception ex ) {
			}
			if ( cache == null )  {
				cache = (net.sf.ehcache.Cache) ComponentManager.get(defaultName);
				LOG.info("Loaded Default Cache for "+cacheName);
			} else {
				LOG.info("Loaded Cache "+cacheName);
			}
			return new EhCache(cache) { 
			
				/* (non-Javadoc)
				 * @see org.hibernate.cache.EhCache#destroy()
				 */
				@Override
				public void destroy() throws CacheException
				{
					LOG.debug("Closing Cache, leaving cleanup to Spring :"+cacheName);
				}
			};
		}
		catch (Exception e)
		{
			LOG.error("Failed to build Cache ");
			throw new CacheException(e);
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
