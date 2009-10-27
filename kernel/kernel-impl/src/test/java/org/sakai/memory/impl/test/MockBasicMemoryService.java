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

package org.sakai.memory.impl.test;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.memory.impl.BasicMemoryService;

/**
 * @author ieb
 *
 */
public class MockBasicMemoryService extends BasicMemoryService
{

	private EventTrackingService eventTrackingService;
	private SecurityService securityService;
	private UsageSessionService usageSessionService;
	private AuthzGroupService authzGroupService;

	/**
	 * 
	 */
	public MockBasicMemoryService(EventTrackingService eventTrackingService, SecurityService securityService, UsageSessionService usageSessionService, AuthzGroupService authzGroupService)
	{
		this.eventTrackingService = eventTrackingService;
		this.securityService = securityService;
		this.usageSessionService = usageSessionService;
		this.authzGroupService = authzGroupService;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.memory.impl.BasicMemoryService#eventTrackingService()
	 */
	@Override
	protected EventTrackingService eventTrackingService()
	{
		return eventTrackingService;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.memory.impl.BasicMemoryService#securityService()
	 */
	@Override
	protected SecurityService securityService()
	{
	return securityService;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.memory.impl.BasicMemoryService#usageSessionService()
	 */
	@Override
	protected UsageSessionService usageSessionService()
	{
		return usageSessionService;
	}
	
	@Override
	protected AuthzGroupService authzGroupService()
	{
		return authzGroupService;
	}

}
