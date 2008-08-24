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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

/**
 * @author ieb
 */
public class MemoryServiceJMXAgent
{

	private static final Log log = LogFactory.getLog(MemoryServiceJMXAgent.class);

	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

	private CacheManager cacheManager;

	private boolean registerCacheManager = true;

	private boolean registerCaches = true;

	private boolean registerCacheConfigurations = true;

	private boolean registerCacheStatistics = true;

	public MBeanServer getMBeanServer()
	{
		return mBeanServer;
	}

	public void init()
	{
		log.info("Registering Cache Provider with JMXBeanServer "+mBeanServer);
		ManagementService.registerMBeans(cacheManager, mBeanServer, registerCacheManager,
				registerCaches, registerCacheConfigurations, registerCacheStatistics);
	}

	/**
	 * @return the cacheManager
	 */
	public CacheManager getCacheManager()
	{
		return cacheManager;
	}

	/**
	 * @param cacheManager the cacheManager to set
	 */
	public void setCacheManager(CacheManager cacheManager)
	{
		this.cacheManager = cacheManager;
	}

	/**
	 * @return the registerCacheConfigurations
	 */
	public boolean isRegisterCacheConfigurations()
	{
		return registerCacheConfigurations;
	}

	/**
	 * @param registerCacheConfigurations the registerCacheConfigurations to set
	 */
	public void setRegisterCacheConfigurations(boolean registerCacheConfigurations)
	{
		this.registerCacheConfigurations = registerCacheConfigurations;
	}

	/**
	 * @return the registerCacheManager
	 */
	public boolean isRegisterCacheManager()
	{
		return registerCacheManager;
	}

	/**
	 * @param registerCacheManager the registerCacheManager to set
	 */
	public void setRegisterCacheManager(boolean registerCacheManager)
	{
		this.registerCacheManager = registerCacheManager;
	}

	/**
	 * @return the registerCaches
	 */
	public boolean isRegisterCaches()
	{
		return registerCaches;
	}

	/**
	 * @param registerCaches the registerCaches to set
	 */
	public void setRegisterCaches(boolean registerCaches)
	{
		this.registerCaches = registerCaches;
	}

	/**
	 * @return the registerCacheStatistics
	 */
	public boolean isRegisterCacheStatistics()
	{
		return registerCacheStatistics;
	}

	/**
	 * @param registerCacheStatistics the registerCacheStatistics to set
	 */
	public void setRegisterCacheStatistics(boolean registerCacheStatistics)
	{
		this.registerCacheStatistics = registerCacheStatistics;
	}
}
