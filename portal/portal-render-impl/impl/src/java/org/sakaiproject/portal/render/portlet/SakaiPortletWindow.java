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

package org.sakaiproject.portal.render.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;

/**
 * @author ddwolf
 * @since Sakai 2.4
 * @version $Rev$
 */
public class SakaiPortletWindow implements PortletWindow
{

	private String contextPath;

	private String portletName;

	private PortletState state;

	public SakaiPortletWindow(String windowId, String contextPath, String portletName)
	{
		this.contextPath = contextPath;
		this.portletName = portletName;
		this.state = new PortletState(windowId);
	}

	public PortletState getState()
	{
		return state;
	}

	public void setState(PortletState state)
	{
		this.state = state;
	}

	public PortletWindowID getId()
	{
		return new SakaiPortletWindowId();
	}

	public String getContextPath()
	{
		return contextPath;
	}

	public String getPortletName()
	{
		return portletName;
	}

	public WindowState getWindowState()
	{
		return state.getWindowState();
	}

	public PortletMode getPortletMode()
	{
		return state.getPortletMode();
	}

	class SakaiPortletWindowId implements PortletWindowID
	{
		public String getStringId()
		{
			return state.getId();
		}
	}
}
