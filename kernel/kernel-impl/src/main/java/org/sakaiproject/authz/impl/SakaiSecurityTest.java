/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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

package org.sakaiproject.authz.impl;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * SakaiSecurity extends the Sakai security service providing the dependency injectors for testing.
 * </p>
 */
public class SakaiSecurityTest extends SakaiSecurity
{
	@Override
	protected ThreadLocalManager threadLocalManager()
	{
		return null;
	}

	@Override
	protected AuthzGroupService authzGroupService()
	{
		return null;
	}

	@Override
	protected UserDirectoryService userDirectoryService()
	{
		return null;
	}

	@Override
	protected MemoryService memoryService()
	{
		return null;
	}

	@Override
	protected EntityManager entityManager()
	{
		return null;
	}

	@Override
	protected SessionManager sessionManager()
	{
		return null;
	}

	@Override
	protected EventTrackingService eventTrackingService()
	{
		return null;
	}

	@Override
    protected FunctionManager functionManager() {
        return null;
    }

	@Override
    protected SiteService siteService() {
    	return null;
    }

	@Override
	protected ServerConfigurationService serverConfigurationService() { return null; }
}
