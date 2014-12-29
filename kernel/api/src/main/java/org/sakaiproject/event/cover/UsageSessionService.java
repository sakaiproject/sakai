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

package org.sakaiproject.event.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * UsageSessionService is a static Cover for the {@link org.sakaiproject.event.api.UsageSessionService UsageSessionService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class UsageSessionService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.event.api.UsageSessionService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.event.api.UsageSessionService) ComponentManager
						.get(org.sakaiproject.event.api.UsageSessionService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.event.api.UsageSessionService) ComponentManager
					.get(org.sakaiproject.event.api.UsageSessionService.class);
		}
	}

	private static org.sakaiproject.event.api.UsageSessionService m_instance = null;

	public static java.lang.String EVENT_LOGIN = org.sakaiproject.event.api.UsageSessionService.EVENT_LOGIN;
	public static java.lang.String EVENT_LOGIN_WS = org.sakaiproject.event.api.UsageSessionService.EVENT_LOGIN_WS;
	public static java.lang.String EVENT_LOGIN_DAV = org.sakaiproject.event.api.UsageSessionService.EVENT_LOGIN_DAV;
	public static java.lang.String EVENT_LOGIN_CONTAINER = org.sakaiproject.event.api.UsageSessionService.EVENT_LOGIN_CONTAINER;
	
	public static java.lang.String EVENT_LOGOUT = org.sakaiproject.event.api.UsageSessionService.EVENT_LOGOUT;

	public static java.lang.String SAKAI_CSRF_SESSION_ATTRIBUTE = org.sakaiproject.event.api.UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE;

	public static java.lang.String USAGE_SESSION_KEY = org.sakaiproject.event.api.UsageSessionService.USAGE_SESSION_KEY;

	public static org.sakaiproject.event.api.UsageSession startSession(java.lang.String param0, java.lang.String param1,
			java.lang.String param2)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.startSession(param0, param1, param2);
	}

	public static org.sakaiproject.event.api.UsageSession getSession(java.lang.String param0)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getSession(param0);
	}

	public static org.sakaiproject.event.api.UsageSession getSession()
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getSession();
	}

	public static java.lang.String getSessionId()
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getSessionId();
	}

	public static org.sakaiproject.event.api.SessionState getSessionState(java.lang.String param0)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getSessionState(param0);
	}

	public static java.util.List getSessions(java.lang.String joinTable, java.lang.String joinAlias, java.lang.String joinColumn, java.lang.String joinCriteria, java.lang.Object[] values)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getSessions(joinTable, joinAlias, joinColumn, joinCriteria, values);
	}

	public static java.util.List getSessions(java.util.List param0)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getSessions(param0);
	}

	public static int getSessionInactiveTimeout()
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return 0;

		return service.getSessionInactiveTimeout();
	}

	public static int getSessionLostTimeout()
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return 0;

		return service.getSessionLostTimeout();
	}

	public static java.util.List getOpenSessions()
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getOpenSessions();
	}

	public static java.util.Map getOpenSessionsByServer()
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return null;

		return service.getOpenSessionsByServer();
	}

	public static boolean login(org.sakaiproject.user.api.Authentication param0, javax.servlet.http.HttpServletRequest param1)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return false;

		return service.login(param0, param1);
	}

	public static boolean login(org.sakaiproject.user.api.Authentication param0, javax.servlet.http.HttpServletRequest param1, String param2)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return false;

		return service.login(param0, param1, param2);
	}

	public static boolean login(String param0, String param1, String param2, String param3, String param4)
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return false;

		return service.login(param0, param1, param2, param3, param4);
	}
	
	public static void logout()
	{
		org.sakaiproject.event.api.UsageSessionService service = getInstance();
		if (service == null) return;

		service.logout();
	}
}
