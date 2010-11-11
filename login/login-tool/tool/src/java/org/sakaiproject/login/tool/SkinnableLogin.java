/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.io.PrintWriter;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.login.api.Login;
import org.sakaiproject.login.api.LoginCredentials;
import org.sakaiproject.login.api.LoginRenderContext;
import org.sakaiproject.login.api.LoginRenderEngine;
import org.sakaiproject.login.api.LoginService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class SkinnableLogin extends HttpServlet implements Login {

	private static final long serialVersionUID = 1L;

	/** Our log (commons). */
	private static Log log = LogFactory.getLog(SkinnableLogin.class);

	/** Session attribute used to store a message between steps. */
	protected static final String ATTR_MSG = "notify";

	/** Session attribute set and shared with ContainerLoginTool: URL for redirecting back here. */
	public static final String ATTR_RETURN_URL = "sakai.login.return.url";

	/** Session attribute set and shared with ContainerLoginTool: if set we have failed container and need to check internal. */
	public static final String ATTR_CONTAINER_CHECKED = "sakai.login.container.checked";

	/** Marker to indicate we are logging in the PDA Portal and should put out abbreviated HTML */
	public static final String PDA_PORTAL_SUFFIX = "/pda/";

	private static ResourceLoader rb = new ResourceLoader("auth");
	
	private transient LoginService loginService;
	
	private String loginContext;
	
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		loginContext = config.getInitParameter("login.context");
		if (loginContext == null || loginContext.length() == 0)
		{
			loginContext = DEFAULT_LOGIN_CONTEXT;
		}
		
		loginService = org.sakaiproject.login.cover.LoginService.getInstance();
		
		log.info("init()");
	}
	
	public void destroy()
	{
		log.info("destroy()");

		super.destroy();
	}
	
	/**
	 * Access the Servlet's information display.
	 *
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Login";
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException 
	{
		// get the session
		Session session = SessionManager.getCurrentSession();

		// get my tool registration
		Tool tool = (Tool) req.getAttribute(Tool.TOOL);
		
		// recognize what to do from the path
		String option = req.getPathInfo();

		// maybe we don't want to do the container this time
		boolean skipContainer = false;

		// if missing, set it to "/login"
		if ((option == null) || ("/".equals(option)))
		{
			option = "/login";
		}

		// look for the extreme login (i.e. to skip container checks)
		else if ("/xlogin".equals(option))
		{
			option = "/login";
			skipContainer = true;
		}

		// get the parts (the first will be "", second will be "login" or "logout")
		String[] parts = option.split("/");

		if (parts[1].equals("logout"))
		{
			// get the session info complete needs, since the logout will invalidate and clear the session
			String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);

			// logout the user
			UsageSessionService.logout();

			complete(returnUrl, null, tool, res);
			return;
		}

		// see if we need to check container
		// @SuppressWarnings(value = "HRS_REQUEST_PARAMETER_TO_HTTP_HEADER",
		// justification = "Looks like the data is already URL encoded")
		boolean checkContainer = ServerConfigurationService.getBoolean("container.login", false);
		if (checkContainer && !skipContainer)
		{
			// if we have not checked the container yet, check it now
			if (session.getAttribute(ATTR_CONTAINER_CHECKED) == null)
			{
				// save our return path
				session.setAttribute(ATTR_RETURN_URL, Web.returnUrl(req, null));

				String containerCheckPath = this.getServletConfig().getInitParameter("container");
				String containerCheckUrl = Web.serverUrl(req) + containerCheckPath;

				// support query parms in url for container auth
				String queryString = req.getQueryString();
				if (queryString != null) containerCheckUrl = containerCheckUrl + "?" + queryString;

				/*
				 * FindBugs: HRS_REQUEST_PARAMETER_TO_HTTP_HEADER Looks like the
				 * data is already URL encoded. Cannot get the @SuppressWarnings
				 * as expected.
				 */
				res.sendRedirect(res.encodeRedirectURL(containerCheckUrl));
				return;
			}
		}
		
		// PDA or not?
		String portalUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
		boolean isPDA = false;
		if ( portalUrl != null ) {
			isPDA = (portalUrl.indexOf (PDA_PORTAL_SUFFIX) > -1);
		}
		log.debug("isPDA: " + isPDA);

		// Present the xlogin template
		LoginRenderContext rcontext = startPageContext("", req, res);
		
		rcontext.put("isPDA", isPDA);

		// Decide whether or not to put up the Cancel
		String actualPortal = ServerConfigurationService.getPortalUrl();
                if ( portalUrl != null && portalUrl.indexOf("/site/") < 1 && portalUrl.startsWith(actualPortal) ) {
			rcontext.put("doCancel", Boolean.TRUE);
		}
		
		sendResponse(rcontext, res, "xlogin", null);
	}
	
	/**
	 * Respond to data posting requests.
	 *
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// Present the xlogin template
		LoginRenderContext rcontext = startPageContext(null, req, res);
		
		// Get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// Get my tool registration
		Tool tool = (Tool) req.getAttribute(Tool.TOOL);

		// Determine if the user canceled this request
		String cancel = req.getParameter("cancel");

		// cancel
		if (cancel != null)
		{
			rcontext.put(ATTR_MSG, rb.getString("log.canceled"));

			// get the session info complete needs, since the logout will invalidate and clear the session
			String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);

			// Trim off the force.login parameter from return URL if present
			if ( returnUrl != null )
			{
				int where = returnUrl.indexOf("?force.login");
				if ( where > 0 ) returnUrl = returnUrl.substring(0,where);
			}

			// TODO: send to the cancel URL, cleanup session
			complete(returnUrl, session, tool, res);
		}

		// submit
		else
		{
			LoginCredentials credentials = new LoginCredentials(req);
			credentials.setSessionId(session.getId());
			
			try {
				loginService.authenticate(credentials);
				String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
				complete(returnUrl, session, tool, res);
				
			} catch (LoginException le) {
				
				String message = le.getMessage();
				
				log.debug("LoginException: " + message);
				
				boolean showAdvice = false;
				
				if (message.equals(EXCEPTION_INVALID_CREDENTIALS)) {
					rcontext.put(ATTR_MSG, rb.getString("log.invalid.credentials"));
					showAdvice = true;
				} else if (message.equals(EXCEPTION_INVALID_WITH_PENALTY)) {
					rcontext.put(ATTR_MSG, rb.getString("log.invalid.with.penalty"));
					showAdvice = true;
				} else if (message.equals(EXCEPTION_MISSING_CREDENTIALS)) 
					rcontext.put(ATTR_MSG, rb.getString("log.tryagain"));
				else 
					rcontext.put(ATTR_MSG, rb.getString("log.invalid"));

				if (showAdvice) {
					String loginAdvice = loginService.getLoginAdvice(credentials);
					if (loginAdvice != null && !loginAdvice.equals("")) {
						log.debug("Returning login advice");
						rcontext.put("loginAdvice", loginAdvice);
					}
				}

				// Decide whether or not to put up the Cancel
				String portalUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
				String actualPortal = ServerConfigurationService.getPortalUrl();
                		if ( portalUrl != null && portalUrl.indexOf("/site/") < 1 && portalUrl.startsWith(actualPortal) ) {
					rcontext.put("doCancel", Boolean.TRUE);
				}
				
				sendResponse(rcontext, res, "xlogin", null);
			}
		}
	}
	
	public void sendResponse(LoginRenderContext rcontext, HttpServletResponse res,
			String template, String contentType) throws IOException
	{
		// headers
		if (contentType == null)
		{
			res.setContentType("text/html; charset=UTF-8");
		}
		else
		{
			res.setContentType(contentType);
		}
		res.addDateHeader("Expires", System.currentTimeMillis()
				- (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// get the writer
		PrintWriter out = res.getWriter();

		try
		{
			LoginRenderEngine rengine = rcontext.getRenderEngine();
			rengine.render(template, rcontext, out);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to render template ", e);
		}

	}
	
	public LoginRenderContext startPageContext(String skin, HttpServletRequest request, HttpServletResponse response)
	{
		LoginRenderEngine rengine = loginService.getRenderEngine(loginContext, request);
		LoginRenderContext rcontext = rengine.newRenderContext(request);

		if (skin == null || skin.trim().length() == 0)
		{
			skin = ServerConfigurationService.getString("skin.default");
		}
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String uiService = ServerConfigurationService.getString("ui.service");
		
		String eidWording = rb.getString("userid");
		String pwWording = rb.getString("log.pass");
		String loginRequired = rb.getString("log.logreq");
		String loginWording = rb.getString("log.login");
		String cancelWording = rb.getString("log.cancel");
		
		rcontext.put("action", response.encodeURL(Web.returnUrl(request, null)));
		rcontext.put("pageSkinRepo", skinRepo);
		rcontext.put("pageSkin", skin);
		rcontext.put("uiService", uiService);
		rcontext.put("pageScriptPath", getScriptPath());
		rcontext.put("loginEidWording", eidWording);
		rcontext.put("loginPwWording", pwWording);
		rcontext.put("loginRequired", loginRequired);
		rcontext.put("loginWording", loginWording);
		rcontext.put("cancelWording", cancelWording);
			
		String eid = StringEscapeUtils.escapeHtml(request.getParameter("eid"));
		String pw = StringEscapeUtils.escapeHtml(request.getParameter("pw"));
		
		if (eid == null)
			eid = "";
		if (pw == null)
			pw = "";
		
		rcontext.put("eid", eid);
		rcontext.put("password", pw);
		
		return rcontext;
	}

	public String getLoginContext() {
		return loginContext;
	}
	
	// Helper methods
	
	/**
	 * Cleanup and redirect when we have a successful login / logout
	 *
	 * @param session
	 * @param tool
	 * @param res
	 * @throws IOException
	 */
	protected void complete(String returnUrl, Session session, Tool tool, HttpServletResponse res) throws IOException
	{
		// cleanup session
		if (session != null)
		{
			session.removeAttribute(Tool.HELPER_MESSAGE);
			session.removeAttribute(Tool.HELPER_DONE_URL);
			session.removeAttribute(ATTR_MSG);
			session.removeAttribute(ATTR_RETURN_URL);
			session.removeAttribute(ATTR_CONTAINER_CHECKED);
		}

		// if we end up with nowhere to go, go to the portal
		if (returnUrl == null)
		{
			returnUrl = ServerConfigurationService.getPortalUrl();
			log.info("complete: nowhere set to go, going to portal");
		}

		// redirect to the done URL
		res.sendRedirect(res.encodeRedirectURL(returnUrl));
	}
	
	protected String getScriptPath()
	{
		return "/library/js/";
	}
}
