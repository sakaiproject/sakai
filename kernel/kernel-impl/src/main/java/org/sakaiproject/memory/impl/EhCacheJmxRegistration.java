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

package org.sakaiproject.memory.impl;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import lombok.extern.slf4j.Slf4j;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

/**
 * This class just registers a EhCache with a JMX MBean server.
 * By default it will use the platform MBean server.
 */
@Slf4j
public class EhCacheJmxRegistration
{
	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

	private boolean registerCacheManager = true;

	private boolean registerCaches = true;

	private boolean registerCacheConfigurations = true;

	private boolean registerCacheStatistics = true;

	public void register(CacheManager cacheManager)
	{
		log.info("Registering Cache Provider with JMXBeanServer "+mBeanServer);
		ManagementService.registerMBeans(cacheManager, mBeanServer, registerCacheManager,
				registerCaches, registerCacheConfigurations, registerCacheStatistics);
	}

	/**
	 * @param registerCacheConfigurations the registerCacheConfigurations to set
	 */
	public void setRegisterCacheConfigurations(boolean registerCacheConfigurations)
	{
		this.registerCacheConfigurations = registerCacheConfigurations;
	}

	/**
	 * @param registerCacheManager the registerCacheManager to set
	 */
	public void setRegisterCacheManager(boolean registerCacheManager)
	{
		this.registerCacheManager = registerCacheManager;
	}

	/**
	 * @param registerCaches the registerCaches to set
	 */
	public void setRegisterCaches(boolean registerCaches)
	{
		this.registerCaches = registerCaches;
	}

	/**
	 * @param registerCacheStatistics the registerCacheStatistics to set
	 */
	public void setRegisterCacheStatistics(boolean registerCacheStatistics)
	{
		this.registerCacheStatistics = registerCacheStatistics;
	}
}
