/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MultiRefCache;

/**
 * <p>
 * MemoryServiceTest extends the basic memory service providing the dependency injectors for testing.
 * </p>
 */
public class MemoryServiceTest extends BasicMemoryService
{
	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected EventTrackingService eventTrackingService()
	{
		return null;
	}

	/**
	 * @return the SecurityService collaborator.
	 */
	protected SecurityService securityService()
	{
		return null;
	}

	/**
	 * @return the UsageSessionService collaborator.
	 */
	protected UsageSessionService usageSessionService()
	{
		return null;
	}
	
	/**
	 * @return the AuthzGroupService collaborator.
	 */
	protected AuthzGroupService authzGroupService()
	{
		return null;
	}

	public Cache newCache(String cacheName, CacheRefresher refresher,
			String pattern) {
		return null;
	}

	public Cache newCache(String cacheName, String pattern) {
		return null;
	}

	public Cache newCache(String cacheName, CacheRefresher refresher) {
		return null;
	}

	public Cache newCache(String cacheName) {
		return null;
	}

	public MultiRefCache newMultiRefCache(String cacheName) {
		return null;
	}
	
	@Override
	protected ServerConfigurationService serverConfigurationService() {
		// TODO Auto-generated method stub
		return null;
	}
}
