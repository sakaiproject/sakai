/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.tool.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * SessionManager is a static Cover for the {@link org.sakaiproject.tool.api.SessionManager SessionManager}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class SessionManager
{
	/** Possibly cached component instance. */
	private static org.sakaiproject.tool.api.SessionManager m_instance = null;

	/** Key in the ThreadLocalManager for the case where a session requested was invalid, and we started a new one. */
	public final static String CURRENT_INVALID_SESSION = org.sakaiproject.tool.api.SessionManager.CURRENT_INVALID_SESSION;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.tool.api.SessionManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
			{
				m_instance = (org.sakaiproject.tool.api.SessionManager) ComponentManager
						.get(org.sakaiproject.tool.api.SessionManager.class);
			}
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.tool.api.SessionManager) ComponentManager
					.get(org.sakaiproject.tool.api.SessionManager.class);
		}
	}

	public static org.sakaiproject.tool.api.Session startSession()
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;

		return manager.startSession();
	}

	public static String makeSessionId(javax.servlet.http.HttpServletRequest param0, java.security.Principal param1)
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;
		
		return manager.makeSessionId(param0, param1);
	}
	
	public static org.sakaiproject.tool.api.Session startSession(java.lang.String param0)
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;

		return manager.startSession(param0);
	}

	public static org.sakaiproject.tool.api.Session getSession(java.lang.String param0)
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;

		return manager.getSession(param0);
	}

	public static List<org.sakaiproject.tool.api.Session> getSessions()
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;

		return manager.getSessions();
	}

	public static org.sakaiproject.tool.api.Session getCurrentSession()
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;

		return manager.getCurrentSession();
	}

	public static java.lang.String getCurrentSessionUserId()
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;

		return manager.getCurrentSessionUserId();
	}

	public static org.sakaiproject.tool.api.ToolSession getCurrentToolSession()
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return null;

		return manager.getCurrentToolSession();
	}

	public static void setCurrentSession(org.sakaiproject.tool.api.Session param0)
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return;

		manager.setCurrentSession(param0);
	}

	public static void setCurrentToolSession(org.sakaiproject.tool.api.ToolSession param0)
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return;

		manager.setCurrentToolSession(param0);
	}

	public static int getActiveUserCount(int param0)
	{
		org.sakaiproject.tool.api.SessionManager manager = getInstance();
		if (manager == null) return 0;

		return manager.getActiveUserCount(param0);
	}
}
