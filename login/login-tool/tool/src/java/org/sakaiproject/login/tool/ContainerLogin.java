/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.login.tool;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.util.ExternalTrustedEvidence;

/**
 * <p>
 * ContainerLogin ...
 * </p>
 */
@Slf4j
public class ContainerLogin extends HttpServlet
{
	private static final long serialVersionUID = -3589514330633190919L;

	private String defaultReturnUrl;

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Container Login";
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

		log.info("init()");
		defaultReturnUrl = ServerConfigurationService.getString("portalPath", "/portal"); 
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = SessionManager.getCurrentSession();

		// check the remote user for authentication
		String remoteUser = req.getRemoteUser();
		try
		{
			Evidence e = new ExternalTrustedEvidence(remoteUser);
			Authentication a = AuthenticationManager.authenticate(e);

			// login the user
			if (UsageSessionService.login(a.getUid(), a.getEid(), req.getRemoteAddr(), req.getHeader("user-agent"), UsageSessionService.EVENT_LOGIN_CONTAINER))
			{
				// get the return URL
				String url = getUrl(session, Tool.HELPER_DONE_URL);

				// cleanup session
				session.removeAttribute(Tool.HELPER_MESSAGE);
				session.removeAttribute(Tool.HELPER_DONE_URL);

				// Mark as successfully authenticated
				session.setAttribute(SkinnableLogin.ATTR_CONTAINER_SUCCESS, SkinnableLogin.ATTR_CONTAINER_SUCCESS);
				
				// redirect to the done URL
				res.sendRedirect(res.encodeRedirectURL(url));

				return;
			}
		}
		catch (AuthenticationException ex)
		{
			log.warn("Authentication Failed for: "+ remoteUser+ ". "+ ex.getMessage());
		}

		// mark the session and redirect (for login failure or authentication exception)
		session.setAttribute(SkinnableLogin.ATTR_CONTAINER_CHECKED, SkinnableLogin.ATTR_CONTAINER_CHECKED);
		res.sendRedirect(res.encodeRedirectURL(getUrl(session, SkinnableLogin.ATTR_RETURN_URL)));
	}

	/**
	 * Gets a URL from the session, if not found returns the portal URL.
	 * @param session The users HTTP session.
	 * @param sessionAttribute The attribute the URL is stored under.
	 * @return The URL.
	 */
	private String getUrl(Session session, String sessionAttribute) {
		String url = (String) session.getAttribute(sessionAttribute);
		if (url == null || url.length() == 0)
		{
			log.debug("No "+ sessionAttribute + " URL, redirecting to portal URL.");
			url = defaultReturnUrl;
		}
		return url;
	}
}
