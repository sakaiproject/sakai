/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.user.impl;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.FormattedText;

/** Just checks we don't need any missing methods as the main implementation is abstract.*/
public class ConcreteUserDirectoryService extends BaseUserDirectoryService {

	@Override
	protected Storage newStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ServerConfigurationService serverConfigurationService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EntityManager entityManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SecurityService securityService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FunctionManager functionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SessionManager sessionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MemoryService memoryService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EventTrackingService eventTrackingService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AuthzGroupService authzGroupService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TimeService timeService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IdManager idManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FormattedText formattedText() {
		// TODO Auto-generated method stub
		return null;
	}

}
