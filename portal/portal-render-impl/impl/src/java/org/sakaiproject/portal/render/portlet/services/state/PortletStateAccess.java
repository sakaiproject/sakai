/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.render.portlet.services.state;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.sakaiproject.tool.cover.SessionManager;

/**
 * Manages access to the current portlet states associated with the given
 * session.
 * 
 * @since Sakai 2.2.3
 * @version $Rev$
 */
public class PortletStateAccess
{

	private static final String PORTLET_STATE_PARAM = "org.sakaiproject.portal.pluto.PORTLET_STATE";

	/**
	 * Retrieve the PortletState for the given windowId.
	 * 
	 * @param request
	 * @param windowId
	 * @return
	 */
	public static PortletState getPortletState(HttpServletRequest request, String windowId)
	{
		// Force this to be the Sakai session - not "webapp" session
		HttpSession session = (HttpSession) SessionManager.getCurrentSession();
          
		PortletStateContainer theContainer = getContainer(session);
		PortletState theState = theContainer.get(windowId);
		if ( theState == null ) 
		{
			theState = new PortletState(windowId);
			theContainer.add(theState);
		}
		return theState;
	}

	/**
	 * Set the specified portlet state is the users session.
	 * 
	 * @param request
	 * @param state
	 */
	public static void setPortletState(HttpServletRequest request, PortletState state)
	{
		// Force this to be the Sakai session - not "webapp" session
		HttpSession session = (HttpSession) SessionManager.getCurrentSession();
		getContainer(session).add(state);
	}

	/**
	 * Retreive the portlet state container associated with the current session.
	 * 
	 * @param session
	 *        the current HttpSession.
	 * @return the PortletStateContainer associated with this session.
	 */
	private static PortletStateContainer getContainer(HttpSession session)
	{
		PortletStateContainer container = (PortletStateContainer) session
				.getAttribute(PORTLET_STATE_PARAM);

		if (container == null)
		{
			container = new PortletStateContainer();
			session.setAttribute(PORTLET_STATE_PARAM, container);
		}
		return container;
	}

	static class PortletStateContainer
	{
		private Map stateMap = new HashMap();

		public void add(PortletState state)
		{
			stateMap.put(state.getId(), state);
		}

		public PortletState get(String windowId)
		{
			return (PortletState) stateMap.get(windowId);
		}
	}
}
