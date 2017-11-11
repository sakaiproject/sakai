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

package org.sakaiproject.portal.render.portlet.services;

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.PortletURLProvider;
import org.apache.pluto.spi.ResourceURLProvider;
import org.sakaiproject.portal.render.portlet.services.state.EnhancedPortletStateEncoder;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;
import org.sakaiproject.portal.render.portlet.services.state.PortletStateAccess;
import org.sakaiproject.portal.render.portlet.services.state.PortletStateEncoder;

import org.sakaiproject.component.cover.ServerConfigurationService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author csev
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class SakaiPortalCallbackService implements PortalCallbackService
{
	public static final String PORTLET_STATE_QUERY_PARAM = "org.sakaiproject.portal.pluto.PORTLET_STATE";

	private PortletStateEncoder portletStateEncoder = new EnhancedPortletStateEncoder();

	public PortletStateEncoder getPortletStateEncoder()
	{
		return portletStateEncoder;
	}

	public void setPortletStateEncoder(PortletStateEncoder portletStateEncoder)
	{
		this.portletStateEncoder = portletStateEncoder;
	}

	public void setTitle(HttpServletRequest request, PortletWindow window, String title)
	{
		log.debug("Setting portlet title for window '" + window.getId() + "' to '"
				+ title + "'.");
		PortletStateAccess.getPortletState(request, window.getId().getStringId())
				.setTitle(title);
	}

	public PortletURLProvider getPortletURLProvider(HttpServletRequest request,
			PortletWindow window)
	{
		PortletState currentState = PortletStateAccess.getPortletState(request, window
				.getId().getStringId());
		PortletState state = null;
		if (currentState != null)
		{
			state = new PortletState(currentState);
		}
		else
		{
			state = new PortletState(window.getId().getStringId());
		}
		String baseUrl = request.getRequestURI();
		return new SakaiPortletURLProvider(baseUrl, state);
	}

	public ResourceURLProvider getResourceURLProvider(HttpServletRequest request,
			PortletWindow window)
	{
		return new SakaiResourceURLProvider("");
	}

	public Map getRequestProperties(HttpServletRequest request, PortletWindow window)
	{
		PortletState currentState = PortletStateAccess.getPortletState(request, window
				.getId().getStringId());
		return currentState.getRequestProperties();
	}

	public void setResponseProperty(HttpServletRequest request, PortletWindow window,
			String key, String value)
	{
		PortletState currentState = PortletStateAccess.getPortletState(request, window
				.getId().getStringId());
		currentState.getResponseProperties().put(key, value);
	}

	public void addResponseProperty(HttpServletRequest request, PortletWindow window,
			String key, String value)
	{
		PortletState currentState = PortletStateAccess.getPortletState(request, window
				.getId().getStringId());
		currentState.getResponseProperties().put(key, value);
	}

	/**
	 * PorltetURLProvider implementation. <p/> Implementation encodes the the
	 * url's state utilizing the encapsulating services portletStateEncoder.
	 * 
	 * @see SakaiPortalCallbackService#getPortletStateEncoder()
	 * @see SakaiPortalCallbackService#setPortletStateEncoder(PortletStateEncoder)
	 * @see PortletState
	 */
	class SakaiPortletURLProvider implements PortletURLProvider
	{

		private String baseUrl;

		private PortletState portletState;

		public SakaiPortletURLProvider(String baseUrl, PortletState portletState)
		{
			this.baseUrl = baseUrl;
			this.portletState = portletState;
		}

		public void setPortletMode(PortletMode portletMode)
		{
			portletState.setPortletMode(portletMode);
		}

		public void setWindowState(WindowState windowState)
		{
			portletState.setWindowState(windowState);
		}

		public void setAction(boolean b)
		{
			portletState.setAction(b);
		}

		public void setSecure()
		{
			portletState.setSecure(true);
		}

		public void clearParameters()
		{
			portletState.clearParameters();
		}

		public void setParameters(Map map)
		{
			portletState.setParameters(map);
		}

		@Override
		public String toString()
		{
			return new StringBuilder(baseUrl).append("?").append(
					PORTLET_STATE_QUERY_PARAM).append("=").append(
					portletStateEncoder.encode(portletState)).toString();
		}

		public boolean isSecureSupported()
		{
			String portalUrl = ServerConfigurationService.getPortalUrl();
			if ( portalUrl == null ) return false;
			if ( portalUrl.startsWith("https:") ) return true;
			return false;
		}
	}

	/**
	 * Resources URL Provider implementation used by this callback service.
	 */
	public class SakaiResourceURLProvider implements ResourceURLProvider
	{

		private String base;

		private String path;

		private String absolute;

		public SakaiResourceURLProvider(String serverUri)
		{
			this.base = serverUri;
		}

		public void setAbsoluteURL(String string)
		{
			this.absolute = string;

		}

		public void setFullPath(String string)
		{
			this.path = string;
		}

		@Override
		public String toString()
		{
			if (absolute != null)
			{
				return this.absolute;
			}
			return base + path;
		}

	}
}
