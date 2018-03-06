/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.login.tool;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.SessionManager;

/**
 * This servlet is useful when you want to have another HTTP request made for logout as your container based
 * authentication needs to do some cleanup and you can't do that on a normal logout. We redirect to this servlet
 * so that we can be sure that we get the additional HTTP request on a known URL.
 */
@Slf4j
public class ContainerLogout extends HttpServlet {
	private ServerConfigurationService serverConfigurationService;
	private UsageSessionService usageSessionService;
	private SessionManager sessionManager;

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Container Logout";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		log.debug("init()");
		serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
		usageSessionService = (UsageSessionService) ComponentManager.get(UsageSessionService.class);
		sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		log.debug("destroy()");

		super.destroy();
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = sessionManager.getCurrentSession();
		String returnUrl = serverConfigurationService.getString("login.container.logout.url", null);
		
		// if we end up with nowhere to go, go to the portal
		if (returnUrl == null)
		{
			log.warn("login.container.logout.url isn't set, to use container logout it should be.");
			returnUrl = (String)session.getAttribute(Tool.HELPER_DONE_URL);
			if (returnUrl == null || "".equals(returnUrl))
			{
				log.debug("complete: nowhere set to go, going to portal");
				returnUrl = serverConfigurationService.getPortalUrl();
			}
		}

		usageSessionService.logout();

		// redirect to the done URL
		res.sendRedirect(res.encodeRedirectURL(returnUrl));

	}
}
