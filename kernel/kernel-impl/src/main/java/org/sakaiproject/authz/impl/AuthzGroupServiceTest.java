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

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.messaging.api.MicrosoftMessagingService;
import org.sakaiproject.scheduling.api.SchedulingService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.tool.api.SessionManager;

import java.util.Set;

/**
 * <p>
 * AuthzGroupServiceTest extends the db authz group service providing the dependency injectors for testing.
 * </p>
 */
public class AuthzGroupServiceTest extends DbAuthzGroupService
{
	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected SqlService sqlService()
	{
		return null;
	}

	/**
	 * @return the SchedulingService collaborator.
	 */
	protected SchedulingService schedulingService()
	{
		return null;
	}

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected UserDirectoryService userDirectoryService()
	{
		return null;
	}

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected ServerConfigurationService serverConfigurationService()
	{
		return null;
	}

	/**
	 * @return the EntityManager collaborator.
	 */
	protected EntityManager entityManager()
	{
		return null;
	}

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected FunctionManager functionManager()
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
	 * @return the TimeService collaborator.
	 */
	protected TimeService timeService()
	{
		return null;
	}

	/**
	 * @return the SessionManager collaborator.
	 */
	protected SessionManager sessionManager()
	{
		return null;
	}

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected EventTrackingService eventTrackingService()
	{
		return null;
	}

    @Override
    public Set<String> getMaintainRoles() {
        return null;
    }

    @Override
    public String getGroupReference(String siteId, String groupId) {
        return null;
    }

	@Override
	protected MicrosoftMessagingService microsoftMessagingService() {
		return null;
	}
}
