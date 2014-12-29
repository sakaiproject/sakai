/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.cheftool;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.util.ParameterParser;

public class JetspeedRunData extends RunData
{
	protected SessionState state = null;

	protected String pid = null;

	public JetspeedRunData(HttpServletRequest req, SessionState state, String pid, ParameterParser params)
	{
		super(req, params);
		this.state = state;
		this.pid = pid;
	}

	// support the return of the SessionState by:
	// SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

	/**
	 * Access the current request's PortletSession state object.
	 * 
	 * @param id
	 *        The Portlet's unique id.
	 * @return the current request's PortletSession state object. (may be null).
	 */
	public SessionState getPortletSessionState(String id)
	{
		return state;
	}

	/**
	 * Returns the portlet id (PEID) referenced in this request
	 * 
	 * @return the portlet id (PEID) referenced or null
	 */
	public String getJs_peid()
	{
		return pid;
	}
}
