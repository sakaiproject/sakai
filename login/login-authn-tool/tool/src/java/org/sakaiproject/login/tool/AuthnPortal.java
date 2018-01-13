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

package org.sakaiproject.login.tool;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

/**
 * <p>
 * AuthnPortal is a public login/logout only Sakai portal - it provides a simple URL to login or logout and go somewhere if successful.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
@Slf4j
public class AuthnPortal extends HttpServlet
{
	/** messages. */
	private static ResourceLoader rb = new ResourceLoader("sitenav");

	/** Session attribute root for storing a site's last page visited - just append the site id. */
	protected static final String ATTR_SITE_PAGE = "sakai.portal.site.";

	/** ThreadLocal attribute set while we are processing an error. */
	protected static final String ATTR_ERROR = "org.sakaiproject.portal.error";

	/** Error response modes. */
	protected static final int ERROR_SITE = 0;

	protected static final int ERROR_GALLERY = 1;

	protected static final int ERROR_WORKSITE = 2;

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		log.info("destroy()");

		super.destroy();
	}

	protected void doError(HttpServletRequest req, HttpServletResponse res, Session session, int mode) throws IOException
	{
		// start the response
		PrintWriter out = startResponse(res, "Sakai Authn Portal", null);

		out.println("<H2>Unknown Request</H2>");
		Web.snoop(out, true, getServletConfig(), req);

		// end the response
		endResponse(out);
	}

	/**
	 * Respond to navigation / access requests.
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
		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// recognize what to do from the path
		String option = req.getPathInfo();

		// if missing, set it to login
		if ((option == null) || ("/".equals(option)))
		{
			option = "/login";
		}

		// get the parts (the first will be "")
		String[] parts = option.split("/");

		// recognize and dispatch the 'login' option
		if ((parts.length >= 2) && (parts[1].equals("login")))
		{
			doLogin(req, res, session, null);
		}

		// recognize and dispatch the 'logout' option
		if ((parts.length == 2) && (parts[1].equals("logout")))
		{
			doLogout(req, res, session, null);
		}

		// handle an unrecognized request
		else
		{
			doError(req, res, session, ERROR_SITE);
		}
	}

	protected void doLogin(HttpServletRequest req, HttpServletResponse res, Session session, String returnPath)
			throws ToolException
	{
		// setup for the helper if needed (Note: in session, not tool session, special for Login helper)
		// go to the parameter'ed place, if set, after
		String url = req.getParameter("url");
		if (url != null)
		{
			session.setAttribute(Tool.HELPER_DONE_URL, url);
		}

		// otherwise go to the configured place, after, unless already set
		else if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			session.setAttribute(Tool.HELPER_DONE_URL, ServerConfigurationService.getPortalUrl());
		}

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/login";
		tool.help(req, res, context, null);
	}

	protected void doLogout(HttpServletRequest req, HttpServletResponse res, Session session, String returnPath)
			throws ToolException
	{
		// setup for the helper if needed (Note: in session, not tool session, special for Login helper)
		// go to the parameter'ed place, if set, after
		String url = req.getParameter("url");
		if (url != null)
		{
			session.setAttribute(Tool.HELPER_DONE_URL, url);
		}
		
		// otherwise go to the configured place, after, unless already set
		else if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			session.setAttribute(Tool.HELPER_DONE_URL, ServerConfigurationService.getLoggedOutUrl());
		}

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/logout";
		tool.help(req, res, context, "/logout");
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
		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// recognize what to do from the path
		String option = req.getPathInfo();

		// if missing, we have a stray post
		if ((option == null) || ("/".equals(option)))
		{
			doError(req, res, session, ERROR_SITE);
			return;
		}

		// get the parts (the first will be "")
		String[] parts = option.split("/");

		// recognize and dispatch the 'login' option
		if ((parts.length == 2) && (parts[1].equals("login")))
		{
			doLogin(req, res, session, null);
		}

		// handle an unrecognized request
		else
		{
			doError(req, res, session, ERROR_SITE);
		}
	}

	/**
	 * Forward to the tool - but first setup JavaScript/CSS etc that the tool will render
	 */
	protected void forwardTool(ActiveTool tool, HttpServletRequest req, HttpServletResponse res, Placement p, String skin,
			String toolContextPath, String toolPathInfo) throws ToolException
	{
		// setup html information that the tool might need (skin, body on load, js includes, etc).
		if (skin == null || skin.length() == 0) skin = ServerConfigurationService.getString("skin.default");
		String skinRepo = ServerConfigurationService.getString("skin.repo");

		String headCssToolBase = "<link href=\"" + skinRepo
				+ "/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
		String headCssToolSkin = "<link href=\"" + skinRepo + "/" + skin
				+ "/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
		String headCss = headCssToolBase + headCssToolSkin;
		String headJs = "<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>\n";
		String head = headCss + headJs;
		StringBuilder bodyonload = new StringBuilder();
		if (p != null)
		{
			String element = Web.escapeJavascript("Main" + p.getId());
			bodyonload.append("setMainFrameHeight('" + element + "');");
		}
		bodyonload.append("setFocus(focus_path);");

		req.setAttribute("sakai.html.head", head);
		req.setAttribute("sakai.html.head.css", headCss);
		req.setAttribute("sakai.html.head.css.base", headCssToolBase);
		req.setAttribute("sakai.html.head.css.skin", headCssToolSkin);
		req.setAttribute("sakai.html.head.js", headJs);
		req.setAttribute("sakai.html.body.onload", bodyonload.toString());

		// let the tool do the the work (forward)
		tool.forward(req, res, p, toolContextPath, toolPathInfo);
	}

	protected void endResponse(PrintWriter out) throws IOException
	{
		out.println("</body></html>");
	}

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Login Portal";
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
	}

	protected PrintWriter startResponse(HttpServletResponse res, String title, String skin) throws IOException
	{
		// headers
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// get the writer
		PrintWriter out = res.getWriter();

		// form the head
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
				+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">" + "  <head>"
				+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");

		// pick the one full portal skin
		if (skin == null) skin = ServerConfigurationService.getString("skin.default");
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		out.println("    <link href=\"" + skinRepo + "/" + skin
				+ "/portal.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />");

		out.println("    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />" + "    <title>" + title + "</title>"
				+ "    <script type=\"text/javascript\" language=\"JavaScript\" src=\"" + getScriptPath()
				+ "headscripts.js\"></script>" + "  </head>");

		// start the body
		out.println("<body  marginwidth=\"0\" marginheight=\"0\" topmargin=\"0\" leftmargin=\"0\">");

		return out;
	}

	protected String getScriptPath()
	{
		String libPath = "/library";
		return libPath + "/js/";
	}
}
