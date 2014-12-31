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
