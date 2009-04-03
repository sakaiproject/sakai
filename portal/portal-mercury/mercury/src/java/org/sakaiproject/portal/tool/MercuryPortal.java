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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.portal.util.ErrorReporter;
import org.sakaiproject.portal.util.ToolURLManagerImpl;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.ToolURL;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Mercury is the Sakai developers portal. It can be used while developing a new Sakai application to invoke the tool in the way that the tool will be
 * invoked by any of the Sakai navigation / portal technologies. Mercury is easier to use in development because you don't have to setup a Site or
 * build SuperStructure to test your application.
 * </p>
 * <p>
 * Mercury supports a few URL patterns, but those give you the ability to:
 * <ul>
 * <li>login to Sakai</li>
 * <li>see all registered tools</li>
 * <li>visit any tool complete with context and placement</li>
 * </ul>
 * </p>
 * <p>
 * Mercury is configured to be packaged into the "mercury.war" web application, so it can be visited with the <code>/mercury</code> URL.
 * </p>
 * <p>
 * To login, use the <code>/mercury/login</code> URL.
 * </p>
 * To visit any registered tool, use the URL pattern:
 * <ul>
 * <li><code>/mercury/TOOL-ID/CONTEXT/path-for-tool</code></li>
 * </ul>
 * where TOOL_ID is the tool's well known registered tool id (such as 'sakai.chat'), and CONTEXT is the tool context string.
 * </p>
 * <p>
 * A placement will be created for each tool in the context as needed, and will be tracked for the duration of the server run.
 * </p>
 */
public class MercuryPortal extends HttpServlet
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(MercuryPortal.class);

	/** Map of context+toolId -> Placement for keeping tool placements. */
	protected Map m_placements = new HashMap();

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Mercury Portal";
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

		M_log.info("init()");
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
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
		try
		{
			// make sure the portal is enabled
			if (!ServerConfigurationService.getString("mercury.enabled", "false").equalsIgnoreCase("true"))
			{
				doDisabled(req, res);
				return;
			}

			// get the Sakai session
			Session session = SessionManager.getCurrentSession();

			// recognize what to do from the path
			String option = req.getPathInfo();

			// if missing, set it to home
			if ((option == null) || ("/".equals(option)))
			{
				option = "/home";
			}

			// get the parts, [0] = "", [1] is /login, or [1] is /home, or [2] is context and [1] is known tool id and [3..n] are for the tool
			String[] parts = option.split("/");

			// recognize and dispatch the 'home' option
			if ((parts.length == 2) && (parts[1].equals("home")))
			{
				doHome(req, res, session);
			}

			// recognize and dispatch the 'login' option
			else if ((parts.length == 2) && (parts[1].equals("login")))
			{
				doLogin(req, res, session);
			}
			// recognize and dispatch the 'logout' option
			else if ((parts.length == 2) && (parts[1].equals("logout")))
			{
				doLogout(req, res, session);
			}
			else if ((parts.length == 3) && (parts[1].equals("loginx")))
			{
				doLoginx(req, res, parts[2], session);
			}

			// recognize and dispatch a tool request option: parts[2] is the context, parts[1] is a known tool id, parts[3..n] are for the tool
			else if (parts.length >= 3)
			{
				doTool(req, res, session, parts[1], parts[2], req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}

			// handle an unrecognized request
			else
			{
				doError(req, res, session);
			}
		}
		catch (Throwable t)
		{
			doThrowableError(req, res, t);
		}
	}

	protected void doHome(HttpServletRequest req, HttpServletResponse res, Session session) throws IOException
	{
		// get all tools
		Set tools = ToolManager.findTools(null, null);

		// get the test tools
		Set categories = new HashSet();
		categories.add("sakai.test");
		Set testTools = ToolManager.findTools(categories, null);

		// get the helper tools
		categories.clear();
		categories.add("sakai.helper");
		Set helperTools = ToolManager.findTools(categories, null);

		// get the sample tools
		categories.clear();
		categories.add("sakai.sample");
		Set sampleTools = ToolManager.findTools(categories, null);

		// remove the test, sample and helper tools from the main list
		tools.removeAll(testTools);
		tools.removeAll(helperTools);
		tools.removeAll(sampleTools);

		// start the response
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		PrintWriter out = res.getWriter();
		out.println("<html><head><title>Sakai Mercury Portal</title></head><body>");

		// fun
		out.println("<img src=\"/library/image/sakai.jpg\" width=\"110\" height=\"66\">");
		out.println("<img src=\"/library/image/Hg-TableImage.png\" width=\"250\" height=\"75\">");
		out.println("<img src=\"/library/image/mercury.gif\" width=\"78\" height=\"69\">");
		out.println("<img src=\"/library/image/mercsymb.gif\" width=\"94\" height=\"75\"><br />");

		// login
		if ((session.getUserId() == null) && (session.getUserEid() == null))
		{
			out.println("<p><a href=\"" + Web.returnUrl(req, "/login") + "\">login</a></p>");

			String[] loginIds = ServerConfigurationService.getStrings("mercury.login");
			int i = 0;
			if (loginIds != null)
			{
				for (String loginId : loginIds)
				{
					out.println("<p><a href=\"" + Web.returnUrl(req, "/loginx/" + Integer.toString(i++)) + "\">login: " + loginId + "</a></p>");
				}
			}
		}
		else
		{
			out.println("<p><a href=\"" + Web.returnUrl(req, "/logout") + "\">logout</a></p>");
		}
		// Show session information
		out.println("<H2>Session</H2>");
		showSession(out, true);

		// list the main tools
		out.println("<H2>Tools</H2>");
		out.println("<p>These are the tools registered with Sakai:</p>");
		out
				.println("<table border='1'><tr><th>id</th><th>title</th><th>description</th><th>final configuration</th><th>mutable configuration</th><th>categories</th><th>keywords</th></tr>");

		// sorted
		TreeSet sorted = new TreeSet(tools);
		for (Iterator i = sorted.iterator(); i.hasNext();)
		{
			Tool t = (Tool) i.next();
			String toolUrl = Web.returnUrl(req, "/" + t.getId() + "/mercury");
			out.println("<tr><td><a href=\"" + toolUrl + "\">" + t.getId() + "</a></td><td>" + t.getTitle() + "</td><td>" + t.getDescription()
					+ "</td><td>" + printConfiguration(t.getFinalConfig()) + "</td><td>" + printConfiguration(t.getMutableConfig()) + "</td><td>"
					+ printCategories(t.getCategories()) + "</td><td>" + printKeywords(t.getKeywords()) + "</td></tr>");
		}
		out.println("</table>");

		// list the helper tools
		out.println("<H2>Helper Tools</H2>");
		out.println("<p>These are the tools registered as helperswith Sakai. (Helper tools cannot be directly invoked): </p>");
		out
				.println("<table border='1'><tr><th>id</th><th>title</th><th>description</th><th>final configuration</th><th>mutable configuration</th><th>categories</th><th>keywords</th></tr>");

		// sorted
		sorted = new TreeSet(helperTools);
		for (Iterator i = sorted.iterator(); i.hasNext();)
		{
			Tool t = (Tool) i.next();
			String toolUrl = Web.returnUrl(req, "/" + t.getId() + "/mercury");
			out.println("<tr><td>" + t.getId() + "</td><td>" + t.getTitle() + "</td><td>" + t.getDescription() + "</td><td>"
					+ printConfiguration(t.getFinalConfig()) + "</td><td>" + printConfiguration(t.getMutableConfig()) + "</td><td>"
					+ printCategories(t.getCategories()) + "</td><td>" + printKeywords(t.getKeywords()) + "</td></tr>");
		}
		out.println("</table>");

		// list the sample tools
		out.println("<H2>Sample Tools</H2>");
		out.println("<p>These are the tools registered with Sakai, categorized as <i>sakai.sample</i>:</p>");
		out
				.println("<table border='1'><tr><th>id</th><th>title</th><th>description</th><th>final configuration</th><th>mutable configuration</th><th>categories</th><th>keywords</th></tr>");

		// sorted
		sorted = new TreeSet(sampleTools);
		for (Iterator i = sorted.iterator(); i.hasNext();)
		{
			Tool t = (Tool) i.next();
			String toolUrl = Web.returnUrl(req, "/" + t.getId() + "/mercury");
			out.println("<tr><td><a href=\"" + toolUrl + "\">" + t.getId() + "</a></td><td>" + t.getTitle() + "</td><td>" + t.getDescription()
					+ "</td><td>" + printConfiguration(t.getFinalConfig()) + "</td><td>" + printConfiguration(t.getMutableConfig()) + "</td><td>"
					+ printCategories(t.getCategories()) + "</td><td>" + printKeywords(t.getKeywords()) + "</td></tr>");
		}
		out.println("</table>");

		// list the test tools
		out.println("<H2>Test Tools</H2>");
		out.println("<p>These are the tools registered with Sakai, categorized as <i>sakai.test</i>:</p>");
		out
				.println("<table border='1'><tr><th>id</th><th>title</th><th>description</th><th>final configuration</th><th>mutable configuration</th><th>categories</th><th>keywords</th></tr>");

		// sorted
		sorted = new TreeSet(testTools);
		for (Iterator i = sorted.iterator(); i.hasNext();)
		{
			Tool t = (Tool) i.next();
			String toolUrl = Web.returnUrl(req, "/" + t.getId() + "/mercury");
			out.println("<tr><td><a href=\"" + toolUrl + "\">" + t.getId() + "</a></td><td>" + t.getTitle() + "</td><td>" + t.getDescription()
					+ "</td><td>" + printConfiguration(t.getFinalConfig()) + "</td><td>" + printConfiguration(t.getMutableConfig()) + "</td><td>"
					+ printCategories(t.getCategories()) + "</td><td>" + printKeywords(t.getKeywords()) + "</td></tr>");
		}
		out.println("</table>");

		// list placements
		out.println("<H2>Tool Placements</H2>");
		out.println("<p>These are the placements we are tracking so far:</p>");
		out.println("<table border='1'><tr><th>id</th><th>context</th><th>tool</th><th>configuration</th></tr>");
		for (Iterator i = m_placements.values().iterator(); i.hasNext();)
		{
			Placement p = (Placement) i.next();
			out.println("<tr><td>" + p.getId() + "</td><td>" + p.getContext() + "</td><td>" + p.getToolId() + "</td><td>"
					+ printConfiguration(p.getPlacementConfig()) + "</td></tr>");
		}
		out.println("</table>");

		out.println("<H2>Snoop</H2>");
		Web.snoop(out, true, getServletConfig(), req);

		// close the response
		out.println("</body></html>");
	}

	protected void doLogin(HttpServletRequest req, HttpServletResponse res, Session session) throws ToolException
	{
		// setup for the helper if needed (Note: in session, not tool session, special for Login helper)
		if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			// where to go after
			session.setAttribute(Tool.HELPER_DONE_URL, Web.returnUrl(req, null));
		}

		// map the request to the helper, leaving the path after ".../options" for the helper
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/login";
		tool.help(req, res, context, null);
	}

	protected void doLoginx(HttpServletRequest req, HttpServletResponse res, String which, Session session) throws ToolException, IOException
	{
		String[] loginIds = ServerConfigurationService.getStrings("mercury.login");
		String[] loginPws = ServerConfigurationService.getStrings("mercury.password");

		int i = Integer.parseInt(which);
		String eid = loginIds[i];
		String pw = loginPws[i];

		Evidence e = new IdPwEvidence(eid, pw);

		// authenticate
		try
		{
			if ((eid.length() == 0) || (pw.length() == 0))
			{
				throw new AuthenticationException("missing required fields");
			}

			Authentication a = AuthenticationManager.authenticate(e);

			// login the user
			UsageSessionService.login(a, req);
		}
		catch (AuthenticationException ex)
		{
		}

		doHome(req, res, session);
	}

	/**
	 * Process a logout - borrowed from CharonPortal.java
	 * 
	 * @param req
	 *        Request object
	 * @param res
	 *        Response object
	 * @param session
	 *        Current session
	 * @throws IOException
	 */
	protected void doLogout(HttpServletRequest req, HttpServletResponse res, Session session) throws ToolException
	{
		// setup for the helper if needed (Note: in session, not tool session, special for Login helper)
		if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			// where to go after
			session.setAttribute(Tool.HELPER_DONE_URL, Web.returnUrl(req, null));
		}

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/logout";
		tool.help(req, res, context, "/logout");
	}

	protected void doTool(HttpServletRequest req, HttpServletResponse res, Session session, String toolId, String context, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException
	{
		ActiveTool tool = ActiveToolManager.getActiveTool(toolId);
		if (tool == null)
		{
			doError(req, res, session);
		}

		else
		{
			// find / make the placement id for this tool - a Placement is stored keyed by context+toolId
			String key = context + "-" + toolId;
			Placement p = (Placement) m_placements.get(key);
			if (p == null)
			{
				p = new org.sakaiproject.util.Placement(IdManager.createUuid(), toolId, tool, null, context, null);
				m_placements.put(key, p);
			}

			forwardTool(tool, req, res, p, toolContextPath, toolPathInfo);
		}
	}

	protected void doError(HttpServletRequest req, HttpServletResponse res, Session session) throws IOException
	{
		// start the response
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		PrintWriter out = res.getWriter();
		out.println("<html><head><title>Sakai Mercury Portal</title></head><body>");

		// Show session information
		out.println("<H2>Session</H2>");
		showSession(out, true);

		out.println("<H2>Unknown Request</H2>");
		Web.snoop(out, true, getServletConfig(), req);

		// close the response
		out.println("</body></html>");
	}

	protected void doDisabled(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		// start the response
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		PrintWriter out = res.getWriter();
		out.println("<html><head><title>Sakai Mercury Portal</title></head><body>");

		// Show session information
		out.println("<H2>Disabled</H2>");
		out.println("<p>The Mercury Portal is currently disabled.</p>");
		out.println("<p>To enable this, set \"mercury.enabled=true\" in your sakai.properties file.</p>");

		// close the response
		out.println("</body></html>");
	}

	protected void doThrowableError(HttpServletRequest req, HttpServletResponse res, Throwable t)
	{
		ErrorReporter err = new ErrorReporter();
		err.report(req, res, t);
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
		try
		{
			// get the Sakai session
			Session session = SessionManager.getCurrentSession();

			// recognize what to do from the path
			String option = req.getPathInfo();

			// if missing, set it to home
			if ((option == null) || ("/".equals(option)))
			{
				option = "/home";
			}

			// get the parts, [0] = "", [1] is /login, or [1] is /home, or [2] is context and [1] is known tool id and [3..n] are for the tool
			String[] parts = option.split("/");

			// recognize and dispatch the 'home' option
			if ((parts.length == 2) && (parts[1].equals("home")))
			{
				postHome(req, res, session);
			}

			// recognize and dispatch the 'login' option
			else if ((parts.length == 2) && (parts[1].equals("login")))
			{
				postLogin(req, res, session);
			}

			// recognize and dispatch a tool request option: parts[2] is the context, parts[1] is a known tool id, parts[3..n] are for the tool
			else if (parts.length >= 3)
			{
				postTool(req, res, session, parts[1], parts[2], req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 3), Web
						.makePath(parts, 3, parts.length));
			}

			// handle an unrecognized request
			else
			{
				doError(req, res, session);
			}
		}
		catch (Throwable t)
		{
			doThrowableError(req, res, t);
		}
	}

	protected void postLogin(HttpServletRequest req, HttpServletResponse res, Session session) throws ToolException
	{
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/login";
		tool.help(req, res, context, null);
	}

	protected void postHome(HttpServletRequest req, HttpServletResponse res, Session session) throws IOException
	{
	}

	protected void postTool(HttpServletRequest req, HttpServletResponse res, Session session, String toolId, String context, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException
	{
		ActiveTool tool = ActiveToolManager.getActiveTool(toolId);
		if (tool == null)
		{
			doError(req, res, session);
		}

		else
		{
			// find / make the placement id for this tool - a Placement is stored keyed by context+toolId
			String key = context + "-" + toolId;
			Placement p = (Placement) m_placements.get(key);
			if (p == null)
			{
				p = new org.sakaiproject.util.Placement(IdManager.createUuid(), toolId, tool, null, context, null);
				m_placements.put(key, p);
			}

			forwardTool(tool, req, res, p, toolContextPath, toolPathInfo);
		}
	}

	/**
	 * Forward to the tool - setup JavaScript/CSS etc that the tool will render
	 */
	protected void forwardTool(ActiveTool tool, HttpServletRequest req, HttpServletResponse res, Placement p, String toolContextPath,
			String toolPathInfo) throws ToolException
	{
		String skin = ServerConfigurationService.getString("skin.default");
		String skinRepo = ServerConfigurationService.getString("skin.repo");

		// setup html information that the tool might need (skin, body on load, js includes, etc).
		String headCssToolBase = "<link href=\"" + skinRepo + "/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
		String headCssToolSkin = "<link href=\"" + skinRepo + "/" + skin + "/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
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
		req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));

		tool.forward(req, res, p, toolContextPath, toolPathInfo);
	}

	/**
	 * Output some session information
	 * 
	 * @param out
	 *        The print writer
	 * @param html
	 *        If true, output in HTML, else in text.
	 */
	protected void showSession(PrintWriter out, boolean html)
	{
		// get the current user session information
		Session s = SessionManager.getCurrentSession();
		if (s == null)
		{
			out.println("no session established");
			if (html) out.println("<br />");
		}
		else
		{
			out.println("session: " + s.getId() + " user id: " + s.getUserId() + " enterprise id: " + s.getUserEid() + " started: "
					+ DateFormat.getDateInstance().format(new Date(s.getCreationTime())) + " accessed: "
					+ DateFormat.getDateInstance().format(new Date(s.getLastAccessedTime())) + " inactive after: " + s.getMaxInactiveInterval());
			if (html) out.println("<br />");
		}

		ToolSession ts = SessionManager.getCurrentToolSession();
		if (ts == null)
		{
			out.println("no tool session established");
			if (html) out.println("<br />");
		}
		else
		{
			out.println("tool session: " + ts.getId() + " started: " + DateFormat.getDateInstance().format(new Date(ts.getCreationTime()))
					+ " accessed: " + DateFormat.getDateInstance().format(new Date(ts.getLastAccessedTime())));
			if (html) out.println("<br />");
		}
	}

	/**
	 * Format the tool's configuration parameters
	 * 
	 * @param t
	 *        The tool.
	 * @return The tool's configuration parameters, formatted for HTML display.
	 */
	protected String printConfiguration(Properties config)
	{
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Enumeration names = config.propertyNames(); names.hasMoreElements();)
		{
			if (!first)
			{
				buf.append("<br />");
			}
			first = false;

			String name = (String) names.nextElement();
			String value = config.getProperty(name);
			buf.append(name);
			buf.append("=");
			buf.append(value);
		}

		return buf.toString();
	}

	/**
	 * Format the tool's categories
	 * 
	 * @param categories
	 *        The categories set.
	 * @return The tool's categories, formatted for HTML display.
	 */
	protected String printCategories(Set categories)
	{
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Iterator i = categories.iterator(); i.hasNext();)
		{
			if (!first)
			{
				buf.append("<br />");
			}
			first = false;

			buf.append((String) i.next());
		}

		return buf.toString();
	}

	/**
	 * Format the tool's keywords
	 * 
	 * @param keywords
	 *        The keywords set.
	 * @return The tool's keywords, formatted for HTML display.
	 */
	protected String printKeywords(Set keywords)
	{
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Iterator i = keywords.iterator(); i.hasNext();)
		{
			if (!first)
			{
				buf.append("<br />");
			}
			first = false;

			buf.append((String) i.next());
		}

		return buf.toString();
	}
}
