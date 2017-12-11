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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.login.api.Login;
import org.sakaiproject.login.api.LoginCredentials;
import org.sakaiproject.login.api.LoginRenderContext;
import org.sakaiproject.login.api.LoginRenderEngine;
import org.sakaiproject.login.api.LoginService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

@Slf4j
public class SkinnableLogin extends HttpServlet implements Login {

	private static final long serialVersionUID = 1L;

	// Service instance variables
	private AuthzGroupService authzGroupService = ComponentManager.get(AuthzGroupService.class);

	/** Session attribute used to store a message between steps. */
	protected static final String ATTR_MSG = "notify";

	/** Session attribute set and shared with ContainerLoginTool: URL for redirecting back here. */
	public static final String ATTR_RETURN_URL = "sakai.login.return.url";

	/** Session attribute set and shared with ContainerLoginTool: if set we have failed container and need to check internal. */
	public static final String ATTR_CONTAINER_CHECKED = "sakai.login.container.checked";

	/** Session attribute to show that session was successfully authenticated through the container. */
	public static final String ATTR_CONTAINER_SUCCESS = "sakai.login.container.success";

	// Services are transient because the class is serializable but the services aren't
	private transient ServerConfigurationService serverConfigurationService;

	private transient SiteService siteService;

	private transient LoginService loginService;

	private static ResourceLoader rb = new ResourceLoader("auth");
	
	// the list of login choices that could be supplied
	enum AuthChoices {
		CONTAINER,
		XLOGIN
	}


	private String loginContext;

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		loginContext = config.getInitParameter("login.context");
		if (loginContext == null || loginContext.length() == 0)
		{
			loginContext = DEFAULT_LOGIN_CONTEXT;
		}

		loginService = (LoginService)ComponentManager.get(LoginService.class);

		serverConfigurationService = (ServerConfigurationService) ComponentManager
				.get(ServerConfigurationService.class);

		siteService = (SiteService) ComponentManager.get(SiteService.class);

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

	@SuppressWarnings(value = "HRS_REQUEST_PARAMETER_TO_HTTP_HEADER", justification = "Looks like the data is already URL encoded")
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

		// flag for whether we should show the auth choice page
		boolean showAuthChoice = false;

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

			// if this is an impersonation, then reset the users old session and
			if (isImpersonating()) 
			{
				UsageSession oldSession = (UsageSession) session.getAttribute(UsageSessionService.USAGE_SESSION_KEY);
				String impersonatingEid = session.getUserEid();
				String userId = oldSession.getUserId();
				String userEid = oldSession.getUserEid();
				log.info("Exiting impersonation of " + impersonatingEid + " and returning to " + userEid);
				ArrayList<String> saveAttributes = new ArrayList<>();
				saveAttributes.add(UsageSessionService.USAGE_SESSION_KEY);
				saveAttributes.add(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
				session.clearExcept(saveAttributes);

				// login - set the user id and eid into session, and refresh this user's authz information
				session.setUserId(userId);
				session.setUserEid(userEid);
				authzGroupService.refreshUser(userId);

				try 
				{
					res.sendRedirect(serverConfigurationService.getString("portalPath", "/portal"));
					res.getWriter().close();
				} 
				catch (IOException e) 
				{
					log.error("failed to redirect after impersonating", e);
				}

				return;
			}

			// get the session info complete needs, since the logout will invalidate and clear the session
			String containerLogoutUrl = serverConfigurationService.getString("login.container.logout.url", null);
			String containerLogout = getServletConfig().getInitParameter("container-logout");
			if ( containerLogoutUrl != null && session.getAttribute(ATTR_CONTAINER_SUCCESS) != null &&
					containerLogout != null)
			{
				res.sendRedirect(res.encodeRedirectURL(containerLogout));
			}
			else
			{
				String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
				// logout the user
				UsageSessionService.logout();
				complete(returnUrl, null, tool, res);
			}
			return;
		}
		
		//SAK-29092 if an auth is specified in the URL, skip any other checks and go straight to it
		String authPreferred = req.getParameter("auth");
		log.debug("authPreferred: " + authPreferred);

		if(StringUtils.equalsIgnoreCase(authPreferred, AuthChoices.XLOGIN.toString())) {
			log.debug("Going straight to xlogin");
			skipContainer = true;
		}
		
		// see if we need to check container
		boolean checkContainer = serverConfigurationService.getBoolean("container.login", false);
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
				 * data is already URL encoded. Had to @SuppressWarnings
				 * the entire method.
				 */

				//SAK-21498 choice page for selecting auth sources
				showAuthChoice = serverConfigurationService.getBoolean("login.auth.choice", false);
				URL helperUrl = null;
				// /portal/relogin doesn't explicitly set a HELPER_DONE_URL so we can't be sure it's there.
				if (session.getAttribute(Tool.HELPER_DONE_URL) != null) {
					helperUrl = new URL((String) session.getAttribute(Tool.HELPER_DONE_URL));
				}
				String helperPath = helperUrl == null ? null : helperUrl.getPath();

				if(StringUtils.equalsIgnoreCase(authPreferred, AuthChoices.CONTAINER.toString())) {
					log.debug("Going straight to container login");
					showAuthChoice = false;
				}
				
				if (showAuthChoice && !(StringUtils.isEmpty(helperPath) || helperPath.equals("/portal") || 
						helperPath.equals("/portal/") )) {
					String xloginUrl = serverConfigurationService.getPortalUrl() + "/xlogin";

					// Present the choice template
					LoginRenderContext rcontext = startChoiceContext("", req, res);
					rcontext.put("containerLoginUrl", containerCheckUrl);
					rcontext.put("xloginUrl", xloginUrl);

					sendResponse(rcontext, res, "choice", null);

				} else {
					//go straight to container check
					res.sendRedirect(res.encodeRedirectURL(containerCheckUrl));
				}
				return;
			}
		}

		String portalUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);

		// Present the xlogin template
		LoginRenderContext rcontext = startPageContext("", req, res);

		// Decide whether or not to put up the Cancel
		String actualPortal = serverConfigurationService.getPortalUrl();
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
					logFailedAttempt(credentials);
				} else if (message.equals(EXCEPTION_DISABLED)) {
					rcontext.put(ATTR_MSG, rb.getString("log.disabled.user"));
					logFailedAttempt(credentials);
					String disabledUrl = serverConfigurationService.getString("disabledSiteUrl");
					if(disabledUrl != null && !"".equals(disabledUrl)){
						res.sendRedirect(disabledUrl);
					}
				} else if (message.equals(EXCEPTION_INVALID_WITH_PENALTY)) {
					rcontext.put(ATTR_MSG, rb.getString("log.invalid.with.penalty"));
					showAdvice = true;
					logFailedAttempt(credentials);
				} else if (message.equals(EXCEPTION_MISSING_CREDENTIALS)) {
					rcontext.put(ATTR_MSG, rb.getString("log.tryagain"));
					//Do we need to log this one? You can't really brute force with empty credentials...
				} else {
					rcontext.put(ATTR_MSG, rb.getString("log.invalid"));
					logFailedAttempt(credentials);
				}

				if (showAdvice) {
					String loginAdvice = loginService.getLoginAdvice(credentials);
					if (loginAdvice != null && !loginAdvice.equals("")) {
						log.debug("Returning login advice");
						rcontext.put("loginAdvice", loginAdvice);
					}
				}

				// Decide whether or not to put up the Cancel
				String portalUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
				String actualPortal = serverConfigurationService.getPortalUrl();
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

		if (StringUtils.isEmpty(skin))
		{
			skin = serverConfigurationService.getString("skin.default", "default");
		}

		String skinRepo = serverConfigurationService.getString("skin.repo");
		String uiService = serverConfigurationService.getString("ui.service", "Sakai");
		String passwordResetUrl = getPasswordResetUrl();

		String xloginChoice = serverConfigurationService.getString("xlogin.choice", null);
		String containerText = serverConfigurationService.getString("login.text.title", "Container Login");
		String loginContainerUrl = serverConfigurationService.getString("login.container.url");

		String eidWording = rb.getString("userid");
		String pwWording = rb.getString("log.pass");
		String loginRequired = rb.getString("log.logreq");
		String loginWording = rb.getString("log.login");
		String cancelWording = rb.getString("log.cancel");
		String passwordResetWording = rb.getString("log.password.reset");

		rcontext.put("action", response.encodeURL(Web.returnUrl(request, null)));
		rcontext.put("pageSkinRepo", skinRepo);
		rcontext.put("pageSkin", skin);
		rcontext.put("uiService", uiService);
		rcontext.put("pageScriptPath", getScriptPath());
		rcontext.put("pageWebjarsPath", getWebjarsPath());
		rcontext.put("loginEidWording", eidWording);
		rcontext.put("loginPwWording", pwWording);
		rcontext.put("loginRequired", loginRequired);
		rcontext.put("loginWording", loginWording);
		rcontext.put("cancelWording", cancelWording);
		rcontext.put("passwordResetUrl", passwordResetUrl);
		rcontext.put("passwordResetWording", passwordResetWording);
		rcontext.put("xloginChoice", xloginChoice);
		rcontext.put("containerText", containerText);
		rcontext.put("loginContainerUrl", loginContainerUrl);

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

	public LoginRenderContext startChoiceContext(String skin, HttpServletRequest request, HttpServletResponse response)
	{
		LoginRenderEngine rengine = loginService.getRenderEngine(loginContext, request);
		LoginRenderContext rcontext = rengine.newRenderContext(request);

		if (skin == null || skin.trim().length() == 0)
		{
			skin = serverConfigurationService.getString("skin.default");
		}
		String skinRepo = serverConfigurationService.getString("skin.repo");
		String uiService = serverConfigurationService.getString("ui.service","Sakai");

		rcontext.put("pageSkinRepo", skinRepo);
		rcontext.put("pageSkin", skin);
		rcontext.put("uiService", uiService);
		rcontext.put("pageScriptPath", getScriptPath());
		rcontext.put("pageWebjarsPath", getWebjarsPath());

		rcontext.put("choiceRequired", rb.getString("log.choicereq"));
		rcontext.put("loginRequired", rb.getString("log.logreq"));
		rcontext.put("loginTitle", serverConfigurationService.getString("login.text.title"));
		rcontext.put("loginTitle2", serverConfigurationService.getString("xlogin.text.title"));

		rcontext.put("containerLoginChoiceIcon", serverConfigurationService.getString("container.login.choice.icon"));
		rcontext.put("xloginChoiceIcon", serverConfigurationService.getString("xlogin.choice.icon"));
		//the URLs for these are set above, as containerLoginUrl and xloginUrl

		rcontext.put("containerLoginChoiceText", serverConfigurationService.getString("container.login.choice.text",
				serverConfigurationService.getString("login.text")));
		rcontext.put("xloginChoiceText", serverConfigurationService.getString("xlogin.choice.text",
				serverConfigurationService.getString("xlogin.text")));

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
			returnUrl = serverConfigurationService.getPortalUrl();
			log.info("complete: nowhere set to go, going to portal");
		}

		// redirect to the done URL
		res.sendRedirect(res.encodeRedirectURL(returnUrl));
	}

	protected String getScriptPath()
	{
		return "/library/js/";
	}
	protected String getWebjarsPath()
	{
		return "/library/webjars/";
	}

	/**
	 * Gets the password reset URL. If looks for a configured URL, otherwise it looks
	 * for the password reset tool in the gateway site and builds a link to that.
	 * @return The password reset URL or <code>null</code> if there isn't one or we
	 * can't find the password reset tool.
	 */
	protected String getPasswordResetUrl()
	{
		// Has a password reset url been specified in sakai.properties? If so, it rules.
		String passwordResetUrl = serverConfigurationService.getString("login.password.reset.url", null);

		if(passwordResetUrl == null) {
			// No explicit password reset url. Try and locate the tool on the gateway page.
			// If it has been  installed we'll use it.
			String gatewaySiteId = serverConfigurationService.getGatewaySiteId();
			try {
				Site gatewaySite = siteService.getSite(gatewaySiteId);
				ToolConfiguration resetTC = gatewaySite.getToolForCommonId("sakai.resetpass");
				if(resetTC != null) {
					passwordResetUrl = resetTC.getContainingPage().getUrl();
				}
			} catch(IdUnusedException iue) {
				log.warn("No " + gatewaySiteId + " site found whilst building password reset url, set password.reset.url" +
						" or create " + gatewaySiteId + " and add password reset tool.");
			}
		}
		return passwordResetUrl;
	}

	/**
	 * Helper to log failed login attempts (SAK-22430)
	 * @param credentials the credentials supplied
	 * 
	 * Note that this could easily be extedned to track login attempts per session and report on it here
	 */
	private void logFailedAttempt(LoginCredentials credentials) {
		if(serverConfigurationService.getBoolean("login.log-failed", true)) {
			// SAK-23672 Safe login string before log
			log.warn("Login attempt failed. ID=" + StringUtils.abbreviate(credentials.getIdentifier().replaceAll("(\\r|\\n)", ""),255) + ", IP Address=" + credentials.getRemoteAddr());
		}
	}

	/**
	 * Helper to see if this session has used SuTool to become another user
	 * 
	 * Returns true if the user is currently impersonating.
	 */
	private boolean isImpersonating() 
	{
		Session s = SessionManager.getCurrentSession();
		String  userId = s.getUserId();
		UsageSession session = (UsageSession) s.getAttribute(UsageSessionService.USAGE_SESSION_KEY);

		if (session != null) 
		{
			// If we have a session for this user, simply reuse
			if (userId != null)
			{
				if (userId.equals(session.getUserId()))
				{
					return false;
				} 
				else 
				{
					return true;
				}
			}
			else 
			{
				log.error("null userId in check isImpersonating");
			}
		}
		return false;
	}

}
