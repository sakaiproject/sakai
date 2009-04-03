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

package org.sakaiproject.portal.charon;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.cover.ToolRenderService;
import org.sakaiproject.portal.util.ErrorReporter;
import org.sakaiproject.portal.util.ToolURLManagerImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
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
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Charon is the Sakai Site based portal.
 * </p>
 * @since Sakai 2.0
 * @version $Rev$
 * 
 */
public class CharonPortal extends HttpServlet 
{

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(CharonPortal.class);

	/** messages. */
	private static ResourceLoader rb = new ResourceLoader("sitenav");
	
	

	/**
	 * Session attribute root for storing a site's last page visited - just
	 * append the site id.
	 */
	protected static final String ATTR_SITE_PAGE = "sakai.portal.site.";

	/**
	 * Parameter value to allow anonymous users of gallery mode to be sent to
	 * the gateway site as anonymous user (like the /portal URL) instead of
	 * making them log in (like worksite, site, and tool URLs).
	 */
	protected static final String PARAM_FORCE_LOGIN = "force.login";

	/** Configuration option to enable/disable state reset on navigation change */
	protected static final String CONFIG_AUTO_RESET = "portal.experimental.auto.reset";

	/** Parameter value to indicate to look up a tool ID within a site */
	protected static final String PARAM_SAKAI_SITE = "sakai.site";

	/** ThreadLocal attribute set while we are processing an error. */
	protected static final String ATTR_ERROR = "org.sakaiproject.portal.error";

	/** Error response modes. */
	protected static final int ERROR_SITE = 0;

	protected static final int ERROR_GALLERY = 1;

	protected static final int ERROR_WORKSITE = 2;

	/**
	 * Names of tool config/registration attributes that control the rendering
	 * of the tool's titlebar
	 */
	private static final String TOOLCONFIG_SHOW_RESET_BUTTON = "reset.button";

	private static final String TOOLCONFIG_SHOW_HELP_BUTTON = "help.button";

	private static final String TOOLCONFIG_HELP_DOCUMENT_ID = "help.id";

	private static final String TOOLCONFIG_HELP_DOCUMENT_URL = "help.url";

	private BasicAuth basicAuth = null;

	private boolean enableDirect = false;

	private PortalService portalService;

	private ToolHelperImpl toolHelper = new ToolHelperImpl();


	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
	}

	protected void doError(HttpServletRequest req, HttpServletResponse res,
			Session session, int mode) throws ToolException, IOException
	{
		if (ThreadLocalManager.get(ATTR_ERROR) == null)
		{
			ThreadLocalManager.set(ATTR_ERROR, ATTR_ERROR);

			// send to the error site
			switch (mode)
			{
				case ERROR_SITE:
				{
					doSite(req, res, session, "!error", null, req.getContextPath()
							+ req.getServletPath());
					break;
				}
				case ERROR_GALLERY:
				{
					doGallery(req, res, session, "!error", null, req.getContextPath()
							+ req.getServletPath());
					break;
				}
				case ERROR_WORKSITE:
				{
					doWorksite(req, res, session, "!error", null, req.getContextPath()
							+ req.getServletPath());
					break;
				}
			}
			return;
		}

		// error and we cannot use the error site...

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : Portal";

		// start the response
		PrintWriter out = startResponse(res, title, null, false);

		// Show session information
		out.println("<h2>Session</h2>");
		showSession(out, true);

		out.println("<h2>Unknown Request</h2>");
		Web.snoop(out, true, getServletConfig(), req);

		// end the response
		endResponse(out);
	}

	protected void doThrowableError(HttpServletRequest req, HttpServletResponse res,
			Throwable t)
	{
		ErrorReporter err = new ErrorReporter();
		err.report(req, res, t);
	}

	protected void doGallery(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String pageId, String toolContextPath)
			throws ToolException, IOException
	{
		// check to default site id
		if (siteId == null)
		{
			if (session.getUserId() == null)
			{
				String forceLogin = req.getParameter(PARAM_FORCE_LOGIN);
				if (forceLogin == null || "yes".equalsIgnoreCase(forceLogin)
						|| "true".equalsIgnoreCase(forceLogin))
				{
					doLogin(req, res, session, req.getPathInfo(), false);
					return;
				}
				siteId = ServerConfigurationService.getGatewaySiteId();
			}
			else
			{
				siteId = SiteService.getUserSiteId(session.getUserId());
			}
		}

		// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			site = getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			doError(req, res, session, ERROR_GALLERY);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				doError(req, res, session, ERROR_GALLERY);
			}
			return;
		}

		// find the page, or use the first page if pageId not found
		SitePage page = site.getPage(pageId);
		if (page == null)
		{
			List pages = site.getOrderedPages();
			if (!pages.isEmpty())
			{
				page = (SitePage) pages.get(0);
			}
		}
		if (page == null)
		{
			doError(req, res, session, ERROR_GALLERY);
			return;
		}

		// store the last page visited
		session.setAttribute(ATTR_SITE_PAGE + siteId, page.getId());

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		PrintWriter out = startResponse(res, title, site.getSkin(), true);

		// the 'little' top area
		includeGalleryNav(out, req, session, siteId);

		String siteType = calcSiteType(siteId);
		out.println("<div id=\"container\""
				+ ((siteType != null) ? " class=\"" + siteType + "\"" : "") + ">");

		includeWorksite(req, res, session, site, page, toolContextPath, "gallery");
		out.println("<div>");

		includeBottom(out);

		// end the response
		endResponse(out);
	}

	protected void doGalleryTabs(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId) throws IOException
	{
		String skin = SiteService.getSiteSkin(siteId);

		// start the response
		PrintWriter out = startResponse(res, "Site Navigation", skin, false);

		// Remove the logout button from gallery since it is designed to be
		// included within
		// some other application (like a portal) which will want to control
		// logout.

		// includeTabs(out, req, session, siteId, "gallery", true);
		includeTabs(out, req, session, siteId, "gallery", false);

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
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		boolean resetDone = false;
		try
		{
			basicAuth.doLogin(req);
			// get the Sakai session
			Session session = SessionManager.getCurrentSession();

			// recognize what to do from the path
			String option = req.getPathInfo();

			// if missing, set it to home or gateway
			if ((option == null) || ("/".equals(option)))
			{
				if (session.getUserId() == null)
				{
					option = "/site/" + ServerConfigurationService.getGatewaySiteId();
				}
				else
				{
					option = "/site/" + SiteService.getUserSiteId(session.getUserId());
				}
			}

			// get the parts (the first will be "")
			String[] parts = option.split("/");

			// recognize and dispatch the 'tool' option: [1] = "tool", [2] =
			// placement id (of a site's tool placement), rest for the tool
			if ((parts.length > 2) && (parts[1].equals("tool")))
			{
				// Resolve the placements of the form
				// /portal/tool/sakai.resources?sakai.site=~csev
				String toolPlacement = getPlacement(req, res, session, parts[2], false);
				if (toolPlacement == null)
				{
					return;
				}
				parts[2] = toolPlacement;

				doTool(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}
			else if (enableDirect && (parts.length > 2)
					&& (parts[1].equals("directtool")))
			{
				// Resolve the placements of the form
				// /portal/tool/sakai.resources?sakai.site=~csev
				String toolPlacement = getPlacement(req, res, session, parts[2], false);
				if (toolPlacement == null)
				{
					return;
				}
				parts[2] = toolPlacement;

				doDirectTool(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}

			// These reet urls simply set a session value to indicate to reset
			// state and then redirect
			// This is necessary os that hte URL is clean and we do not see
			// resets on refresh
			else if ((parts.length > 2) && (parts[1].equals("tool-reset")))
			{
				String toolUrl = req.getContextPath() + "/tool"
						+ Web.makePath(parts, 2, parts.length);
				// Make sure to add the parameters such as panel=Main
				String queryString = Validator.generateQueryString(req);
				if (queryString != null)
				{
					toolUrl = toolUrl + "?" + queryString;
				}
				portalService.setResetState("true");
				resetDone = true;
				res.sendRedirect(toolUrl);
			}

			/**
			 * Title frames were no longer used in 2.3 and are not supported in
			 * 2.4 so we emit a WARN message here to help people with derived
			 * classes figure out the new way.
			 */

			// TODO: Remove after 2.4
			else if ((parts.length > 2) && (parts[1].equals("title")))
			{
				// Resolve the placements of the form
				// /portal/title/sakai.resources?sakai.site=~csev
				String toolPlacement = getPlacement(req, res, session, parts[2], false);
				if (toolPlacement == null)
				{
					return;
				}
				parts[2] = toolPlacement;

				doTitle(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}

			// recognize a dispatch the 'page' option (tools on a page)
			else if ((parts.length == 3) && (parts[1].equals("page")))
			{
				// Resolve the placements of the form
				// /portal/page/sakai.resources?sakai.site=~csev
				String pagePlacement = getPlacement(req, res, session, parts[2], true);
				if (pagePlacement == null)
				{
					return;
				}
				parts[2] = pagePlacement;

				doPage(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath());
			}

			// recognize a dispatch the 'worksite' option (pages navigation +
			// tools on a page)
			else if ((parts.length >= 3) && (parts[1].equals("worksite")))
			{
				// recognize an optional page/pageid
				String pageId = null;
				if ((parts.length == 5) && (parts[3].equals("page")))
				{
					pageId = parts[4];
				}

				doWorksite(req, res, session, parts[2], pageId, req.getContextPath()
						+ req.getServletPath());
			}

			// recognize a dispatch the 'gallery' option (site tabs + pages
			// navigation + tools on a page)
			else if ((parts.length >= 2) && (parts[1].equals("gallery")))
			{
				// recognize an optional page/pageid
				String pageId = null;
				if ((parts.length == 5) && (parts[3].equals("page")))
				{
					pageId = parts[4];
				}

				// site might be specified
				String siteId = null;
				if (parts.length >= 3)
				{
					siteId = parts[2];
				}

				doGallery(req, res, session, siteId, pageId, req.getContextPath()
						+ req.getServletPath());
			}

			// recognize a dispatch the 'site' option (site logo and tabs +
			// pages navigation + tools on a page)
			else if ((parts.length >= 2) && (parts[1].equals("site")))
			{
				// recognize an optional page/pageid
				String pageId = null;
				if ((parts.length == 5) && (parts[3].equals("page")))
				{
					pageId = parts[4];
				}

				// site might be specified
				String siteId = null;
				if (parts.length >= 3)
				{
					siteId = parts[2];
				}

				doSite(req, res, session, siteId, pageId, req.getContextPath()
						+ req.getServletPath());
			}

			// recognize site tabs
			else if ((parts.length == 3) && (parts[1].equals("site_tabs")))
			{
				doSiteTabs(req, res, session, parts[2]);
			}

			// recognize gallery tabs
			else if ((parts.length == 3) && (parts[1].equals("gallery_tabs")))
			{
				doGalleryTabs(req, res, session, parts[2]);
			}

			// recognize nav login
			else if ((parts.length == 3) && (parts[1].equals("nav_login")))
			{
				doNavLogin(req, res, session, parts[2]);
			}

			// recognize nav login for the gallery
			else if ((parts.length == 3) && (parts[1].equals("nav_login_gallery")))
			{
				doNavLoginGallery(req, res, session, parts[2]);
			}

			// recognize presence
			else if ((parts.length >= 3) && (parts[1].equals("presence")))
			{
				doPresence(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}

			// recognize help
			else if ((parts.length >= 2) && (parts[1].equals("help")))
			{
				doHelp(req, res, session, req.getContextPath() + req.getServletPath()
						+ Web.makePath(parts, 1, 2), Web.makePath(parts, 2, parts.length));
			}

			// recognize and dispatch the 'login' option
			else if ((parts.length == 2) && (parts[1].equals("relogin")))
			{
				// Note: here we send a null path, meaning we will NOT set it as
				// a possible return path
				// we expect we are in the middle of a login screen processing,
				// and it's already set (user login button is "ulogin") -ggolden
				doLogin(req, res, session, null, false);
			}

			// recognize and dispatch the 'login' option
			else if ((parts.length == 2) && (parts[1].equals("login")))
			{
				doLogin(req, res, session, "", false);
			}

			// recognize and dispatch the 'login' options
			else if ((parts.length == 2) && ((parts[1].equals("xlogin"))))
			{
				doLogin(req, res, session, "", true);
			}

			// recognize and dispatch the 'login' option for gallery
			else if ((parts.length == 2) && (parts[1].equals("login_gallery")))
			{
				doLogin(req, res, session, "/gallery", false);
			}

			// recognize and dispatch the 'logout' option
			else if ((parts.length == 2) && (parts[1].equals("logout")))
			{
				doLogout(req, res, session, null);
			}

			// recognize and dispatch the 'logout' option for gallery
			else if ((parts.length == 2) && (parts[1].equals("logout_gallery")))
			{
				doLogout(req, res, session, "/gallery");
			}

			// recognize error done
			else if ((parts.length >= 2) && (parts[1].equals("error-reported")))
			{
				doErrorDone(req, res);
			}

			// handle an unrecognized request
			else
			{
				doError(req, res, session, ERROR_SITE);
			}
		}
		catch (Throwable t)
		{
			doThrowableError(req, res, t);
		}

		// Make sure to clear any reset State at the end of the request unless
		// we *just* set it
		if (!resetDone)
		{
			portalService.setResetState(null);
		}

	}

	/**
	 * Title frames were no longer used in 2.3 and are not supported in 2.4 so
	 * these methods are made private to notify derived classes. they can be
	 * removed for 2.5 - csev
	 */

	protected void doTitle(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException
	{
		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		if (siteTool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(siteTool.getToolId());
		if (tool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// don't check permissions when just displaying the title...
		// // permission check - visit the site (unless the tool is configured
		// to bypass)
		// if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		// {
		// Site site = null;
		// try
		// {
		// site = SiteService.getSiteVisit(siteTool.getSiteId());
		// }
		// catch (IdUnusedException e)
		// {
		// doError(req, res, session, ERROR_WORKSITE);
		// return;
		// }
		// catch (PermissionException e)
		// {
		// // if not logged in, give them a chance
		// if (session.getUserId() == null)
		// {
		// doLogin(req, res, session, req.getPathInfo(), false);
		// }
		// else
		// {
		// doError(req, res, session, ERROR_WORKSITE);
		// }
		// return;
		// }
		// }

		includeTitle(tool, req, res, siteTool, siteTool.getSkin(), toolContextPath,
				toolPathInfo);
	}

	/**
	 * Output the content of the title frame for a tool.
	 */
	protected void includeTitle(ActiveTool tool, HttpServletRequest req,
			HttpServletResponse res, ToolConfiguration placement, String skin,
			String toolContextPath, String toolPathInfo) throws IOException
	{

		// TODO: After 2.3 and the background document is modified - this may no
		// longer be needed
		// as the title is simply in the background document

		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis()
				- (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res
				.addHeader("Cache-Control",
						"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		if (skin == null || skin.length() == 0)
			skin = ServerConfigurationService.getString("skin.default");
		String skinRepo = ServerConfigurationService.getString("skin.repo");

		// the title to display in the title frame
		String toolTitle = Web.escapeHtml(placement.getTitle());

		// for the reset button
		String resetActionUrl = toolContextPath + "?reset=true";
		boolean resetToolNow = "true".equals(req.getParameter("reset"));
		boolean showResetButton = !"false".equals(placement.getConfig().getProperty(
				TOOLCONFIG_SHOW_RESET_BUTTON));

		// for the help button
		// get the help document ID from the tool config (tool registration
		// usually).
		// The help document ID defaults to the tool ID
		boolean helpEnabledGlobally = ServerConfigurationService.getBoolean(
				"display.help.icon", true);
		boolean helpEnabledInTool = !"false".equals(placement.getConfig().getProperty(
				TOOLCONFIG_SHOW_HELP_BUTTON));
		boolean showHelpButton = helpEnabledGlobally && helpEnabledInTool;

		String helpActionUrl = "";
		if (showHelpButton)
		{
			String helpDocId = placement.getConfig().getProperty(
					TOOLCONFIG_HELP_DOCUMENT_ID);
			String helpDocUrl = placement.getConfig().getProperty(
					TOOLCONFIG_HELP_DOCUMENT_URL);
			if (helpDocUrl != null && helpDocUrl.length() > 0)
			{
				helpActionUrl = helpDocUrl;
			}
			else
			{
				if (helpDocId == null || helpDocId.length() == 0)
				{
					helpDocId = tool.getId();
				}
				helpActionUrl = ServerConfigurationService.getHelpUrl(helpDocId);
			}
		}

		PrintWriter out = res.getWriter();

		final String headHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
				+ "  <head>\n"
				+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
				+ "    <link href=\""
				+ skinRepo
				+ "/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
				+ "    <link href=\""
				+ skinRepo
				+ "/"
				+ skin
				+ "/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
				+ "    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />\n"
				+ "    <script type=\"text/javascript\" language=\"JavaScript\" src=\""
				+ getScriptPath()
				+ "headscripts.js\"></script>\n"
				+ "    <title>"
				+ toolTitle
				+ "</title>\n"
				+ "  </head>\n"
				+ "  <body>\n";
		final String tailHtml = "</body></html>\n";

		out.write(headHtml);

		out.write("<div class=\"portletTitle\">\n");
		out.write("\t<div class=\"title\">\n");
		if (showResetButton)
		{
			out.write("\t\t<a href=\"" + resetActionUrl + "\" title=\""
					+ Web.escapeHtml(rb.getString("sit_reset"))
					+ "\"><img src=\"/library/image/transparent.gif\" alt=\""
					+ Web.escapeHtml(rb.getString("sit_reset"))
					+ "\" border=\"1\" /></a>");
		}

		out.write("<h2>" + toolTitle + "\n" + "\t</h2></div>\n");
		out.write("\t<div class=\"action\">\n");
		if (showHelpButton)
		{
			out.write(makeHelpButton(helpActionUrl));
		}

		out.write("\t</div>\n");
		out.write("</div>\n");

		if (resetToolNow)
		{
			// cause main tool frame to be reset

			// clear the session data associated with the tool - should reset
			// the tool
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placement.getId());
			ts.clearAttributes();

			// redirect the main tool frame back to the initial tool URL.
			String mainFrameId = Web.escapeJavascript("Main" + placement.getId());
			String mainFrameUrl = ServerConfigurationService.getToolUrl() + "/"
					+ Web.escapeUrl(placement.getId()) + "?panel=Main";

			out.write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
			out.write("try\n");
			out.write("{\n");
			out
					.write("	if (parent." + mainFrameId
							+ ".location.toString().length > 1)\n");
			out.write("	{\n");
			out
					.write("		parent." + mainFrameId + ".location = '" + mainFrameUrl
							+ "';\n");
			out.write("	}\n");
			out.write("}\n");
			out.write("catch (e1)\n");
			out.write("{\n");
			out.write("	try\n");
			out.write("	{\n");
			out.write("		if (parent.parent." + mainFrameId
					+ ".location.toString().length > 1)\n");
			out.write("		{\n");
			out.write("			parent.parent." + mainFrameId + ".location = '" + mainFrameUrl
					+ "';\n");
			out.write("		}\n");
			out.write("	}\n");
			out.write("	catch (e2)\n");
			out.write("	{\n");
			out.write("	}\n");
			out.write("}\n");
			out.write("</script>\n");
		}

		out.write(tailHtml);
	}

	private String makeHelpButton(String helpActionUrl)
	{
		return ("\t\t<a accesskey=\"h\""
				+ " href=\""
				+ helpActionUrl
				+ "\" "
				+ " title=\""
				+ Web.escapeHtml(rb.getString("sit_help"))
				+ "\" "
				+ " target=\"_blank\" "
				+ " onclick=\"openWindow('"
				+ helpActionUrl
				+ "', 'Help'"
				+ ", 'resizable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false\">"
				+ "<img src=\"/library/image/transparent.gif\"" + " alt=\""
				+ Web.escapeHtml(rb.getString("sit_help")) + "\"" + " border=\"0\" /></a>\n");
	}

	protected void doLogin(HttpServletRequest req, HttpServletResponse res,
			Session session, String returnPath, boolean skipContainer)
			throws ToolException
	{
		try
		{
			if (basicAuth.doAuth(req, res))
			{
				// System.err.println("BASIC Auth Request Sent to the Browser
				// ");
				return;
			}
		}
		catch (IOException ioex)
		{
			throw new ToolException(ioex);

		}

		// setup for the helper if needed (Note: in session, not tool session,
		// special for Login helper)
		// Note: always set this if we are passed in a return path... a blank
		// return path is valid... to clean up from
		// possible abandened previous login attempt -ggolden
		if (returnPath != null)
		{
			// where to go after
			session.setAttribute(Tool.HELPER_DONE_URL, Web.returnUrl(req, returnPath));
		}

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");

		// to skip container auth for this one, forcing things to be handled
		// internaly, set the "extreme" login path
		String loginPath = (skipContainer ? "/xlogin" : "/relogin");

		String context = req.getContextPath() + req.getServletPath() + loginPath;
		tool.help(req, res, context, loginPath);
	}

	/**
	 * Process a logout
	 * 
	 * @param req
	 *        Request object
	 * @param res
	 *        Response object
	 * @param session
	 *        Current session
	 * @param returnPath
	 *        if not null, the path to use for the end-user browser redirect
	 *        after the logout is complete. Leave null to use the configured
	 *        logged out URL.
	 * @throws IOException
	 */
	protected void doLogout(HttpServletRequest req, HttpServletResponse res,
			Session session, String returnPath) throws ToolException
	{
		// where to go after
		if (returnPath == null)
		{
			// if no path, use the configured logged out URL
			String loggedOutUrl = ServerConfigurationService.getLoggedOutUrl();
			session.setAttribute(Tool.HELPER_DONE_URL, loggedOutUrl);
		}
		else
		{
			// if we have a path, use a return based on the request and this
			// path
			// Note: this is currently used only as "/gallery"
			// - we should really add a
			// ServerConfigurationService.getGalleryLoggedOutUrl()
			// and change the returnPath to a normal/gallery indicator -ggolden
			String loggedOutUrl = Web.returnUrl(req, returnPath);
			session.setAttribute(Tool.HELPER_DONE_URL, loggedOutUrl);
		}

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/logout";
		tool.help(req, res, context, "/logout");
	}

	protected void doNavLogin(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId) throws IOException
	{
		// start the response
		PrintWriter out = startResponse(res, "Login", null, false);

		includeLogo(out, req, session, siteId);
		out.println("<div class=\"divColor\" id=\"tabBottom\"><br /></div></div>");

		// end the response
		endResponse(out);
	}

	protected void doNavLoginGallery(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId) throws IOException
	{
		// start the response
		PrintWriter out = startResponse(res, "Login", null, false);

		includeGalleryLogin(out, req, session, siteId);
		out.println("<div class=\"divColor\" id=\"tabBottom\"><br /></div></div>");

		// end the response
		endResponse(out);
	}

	protected void doPage(HttpServletRequest req, HttpServletResponse res,
			Session session, String pageId, String toolContextPath) throws ToolException,
			IOException
	{
		// find the page from some site
		SitePage page = SiteService.findPage(pageId);
		if (page == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// permission check - visit the site
		Site site = null;
		try
		{
			site = SiteService.getSiteVisit(page.getSiteId());
		}
		catch (IdUnusedException e)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				doError(req, res, session, ERROR_WORKSITE);
			}
			return;
		}

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		PrintWriter out = startResponse(res, title, page.getSkin(), true);

		// div to wrap the works
		String siteType = calcSiteType(site.getId());
		out.println("<div id=\"container\""
				+ ((siteType != null) ? " class=\"" + siteType + "\"" : "") + ">");

		includePage(req, res, page, toolContextPath, "contentFull");
		out.println("</div>");

		// end the response
		endResponse(out);
	}

	/**
	 * Respond to data posting requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		try
		{
			basicAuth.doLogin(req);
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

			// recognize and dispatch the 'tool' option: [1] = "tool", [2] =
			// placement id (of a site's tool placement), rest for the tool
			if ((parts.length > 2) && (parts[1].equals("tool")))
			{
				doTool(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}
			else if (enableDirect && (parts.length > 2)
					&& (parts[1].equals("directtool")))
			{
				// Resolve the placements of the form
				// /portal/tool/sakai.resources?sakai.site=~csev
				String toolPlacement = getPlacement(req, res, session, parts[2], false);
				if (toolPlacement == null)
				{
					return;
				}
				parts[2] = toolPlacement;

				doDirectTool(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}

			/**
			 * Title frames were no longer used in 2.3 and are not supported in
			 * 2.4 so we emit a WARN message here to help people with derived
			 * classes figure out the new way.
			 */

			// TODO: Remove after 2.4
			else if ((parts.length > 2) && (parts[1].equals("title")))
			{
				doTitle(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
			}

			// recognize and dispatch the 'login' options
			else if ((parts.length == 2)
					&& ((parts[1].equals("login") || (parts[1].equals("xlogin")) || (parts[1]
							.equals("relogin")))))
			{
				postLogin(req, res, session, parts[1]);
			}

			// recognize help
			else if ((parts.length >= 2) && (parts[1].equals("help")))
			{
				doHelp(req, res, session, req.getContextPath() + req.getServletPath()
						+ Web.makePath(parts, 1, 2), Web.makePath(parts, 2, parts.length));
			}

			// recognize error feedback
			else if ((parts.length >= 2) && (parts[1].equals("error-report")))
			{
				doErrorReport(req, res);
			}

			// handle an unrecognized request
			else
			{
				doError(req, res, session, ERROR_SITE);
			}
		}
		catch (Throwable t)
		{
			doThrowableError(req, res, t);
		}
	}

	protected void doPresence(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String toolContextPath, String toolPathInfo)
			throws ToolException, IOException
	{
		// permission check - visit the site
		Site site = null;
		try
		{
			site = SiteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				doError(req, res, session, ERROR_WORKSITE);
			}
			return;
		}

		// get the skin for the site
		String skin = site.getSkin();

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.presence");
		if (tool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// form a placement based on the site and the fact that this is that
		// site's presence...
		// Note: the placement is transient, but will always have the same id
		// and context based on the siteId
		org.sakaiproject.util.Placement placement = new org.sakaiproject.util.Placement(
				siteId + "-presence", tool.getId(), tool, null, siteId, null);

		forwardTool(tool, req, res, placement, skin, toolContextPath, toolPathInfo);
	}

	protected void doHelp(HttpServletRequest req, HttpServletResponse res,
			Session session, String toolContextPath, String toolPathInfo)
			throws ToolException, IOException
	{
		// permission check - none

		// get the detault skin
		String skin = ServerConfigurationService.getString("skin.default");

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.help");
		if (tool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// form a placement based on ... help TODO: is this enough?
		// Note: the placement is transient, but will always have the same id
		// and (null) context
		org.sakaiproject.util.Placement placement = new org.sakaiproject.util.Placement(
				"help", tool.getId(), tool, null, null, null);

		forwardTool(tool, req, res, placement, skin, toolContextPath, toolPathInfo);
	}

	protected void doErrorReport(HttpServletRequest req, HttpServletResponse res)
			throws ToolException, IOException
	{
		setupForward(req, res, null, null);

		ErrorReporter err = new ErrorReporter();
		err.postResponse(req, res);
	}

	protected void doErrorDone(HttpServletRequest req, HttpServletResponse res)
			throws ToolException, IOException
	{
		setupForward(req, res, null, null);

		ErrorReporter err = new ErrorReporter();
		err.thanksResponse(req, res);
	}

	protected void doSite(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String pageId, String toolContextPath)
			throws ToolException, IOException
	{
		// default site if not set
		if (siteId == null)
		{
			if (session.getUserId() == null)
			{
				siteId = ServerConfigurationService.getGatewaySiteId();
			}
			else
			{
				siteId = SiteService.getUserSiteId(session.getUserId());
			}
		}

		// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			site = getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			doError(req, res, session, ERROR_SITE);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				doError(req, res, session, ERROR_SITE);
			}
			return;
		}

		// find the page, or use the first page if pageId not found
		SitePage page = site.getPage(pageId);
		if (page == null)
		{
			List pages = site.getOrderedPages();
			if (!pages.isEmpty())
			{
				page = (SitePage) pages.get(0);
			}
		}
		if (page == null)
		{
			doError(req, res, session, ERROR_SITE);
			return;
		}

		// store the last page visited
		session.setAttribute(ATTR_SITE_PAGE + siteId, page.getId());

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		PrintWriter out = startResponse(res, title, site.getSkin(), true);

		// the 'full' top area
		includeSiteNav(out, req, session, siteId);

		String siteType = calcSiteType(siteId);
		out.println("<div id=\"container\""
				+ ((siteType != null) ? " class=\"" + siteType + "\"" : "") + ">");

		includeWorksite(req, res, session, site, page, toolContextPath, "site");
		out.println("<div>");

		includeBottom(out);

		// end the response
		endResponse(out);
	}

	// Checks to see which form of tool or page placement we have. The normal
	// placement is
	// a GUID. However when the parameter sakai.site is added to the request,
	// the placement
	// can be of the form sakai.resources. This routine determines which form of
	// the
	// placement id, and if this is the second type, performs the lookup and
	// returns the
	// GUID of the placement. If we cannot resolve the pllacement, we simply
	// return
	// the passed in placement ID. If we cannot visit the site, we send the user
	// to login
	// processing and return null to the caller.

	protected String getPlacement(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, boolean doPage) throws ToolException
	{

		String siteId = req.getParameter(PARAM_SAKAI_SITE);
		if (siteId == null) return placementId; // Standard placement

		// find the site, for visiting
		// Sites like the !gateway site allow visits by anonymous
		Site site = null;
		try
		{
			site = SiteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			return placementId; // cannot resolve placement
		}
		catch (PermissionException e)
		{
			// If we are not logged in, try again after we log in, otherwise
			// punt
			if (session.getUserId() == null)
			{
				doLogin(req, res, session, req.getPathInfo() + "?sakai.site="
						+ res.encodeURL(siteId), false);
				return null;
			}
			return placementId; // cannot resolve placement
		}

		if (site == null) return placementId;
		ToolConfiguration toolConfig = site.getToolForCommonId(placementId);
		if (toolConfig == null) return placementId;

		if (doPage)
		{
			return toolConfig.getPageId();
		}
		else
		{
			return toolConfig.getId();
		}

	}

	protected void doSiteTabs(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId) throws IOException
	{
		// get the site's skin
		String skin = SiteService.getSiteSkin(siteId);

		// start the response
		PrintWriter out = startResponse(res, "Site Navigation", skin, false);

		includeLogo(out, req, session, siteId);
		includeTabs(out, req, session, siteId, "site", false);

		// end the response
		endResponse(out);
	}

	/**
	 * Do direct tool, takes the url, stored the destination and the target
	 * iframe in the session constructs and outer url and when a request comes
	 * in that matches the stored iframe it ejects the destination address
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @param placementId
	 * @param toolContextPath
	 * @param toolPathInfo
	 * @param placementId
	 * @throws ToolException
	 * @throws IOException
	 */
	protected void doDirectTool(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException
	{
		if (redirectIfLoggedOut(res)) return;

		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		if (siteTool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// Reset the tool state if requested
		if (portalService.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(siteTool.getToolId());
		if (tool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(siteTool.getSiteId());
			}
			catch (IdUnusedException e)
			{
				doError(req, res, session, ERROR_WORKSITE);
				return;
			}
			catch (PermissionException e)
			{
				// if not logged in, give them a chance
				if (session.getUserId() == null)
				{
					// let the tool do the the work (forward)
					StoredState ss = portalService.newStoredState("directtool", "tool");
					ss.setRequest(req);
					ss.setPlacement(siteTool);
					ss.setToolContextPath(toolContextPath);
					ss.setToolPathInfo(toolPathInfo);
					ss.setSkin(siteTool.getSkin());
					portalService.setStoredState(ss);

					doLogin(req, res, session, getPortalPageUrl(siteTool), false);
				}
				else
				{
					doError(req, res, session, ERROR_WORKSITE);
				}
				return;
			}
		}
		// let the tool do the the work (forward)
		StoredState ss = portalService.newStoredState("directtool", "tool");
		ss.setRequest(req);
		ss.setPlacement(siteTool);
		ss.setToolContextPath(toolContextPath);
		ss.setToolPathInfo(toolPathInfo);
		ss.setSkin(siteTool.getSkin());
		portalService.setStoredState(ss);

		forwardPortal(tool, req, res, siteTool, siteTool.getSkin(), toolContextPath,
				toolPathInfo);

	}

	protected void doTool(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException
	{
		if (redirectIfLoggedOut(res)) return;

		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		if (siteTool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// Reset the tool state if requested
		if (portalService.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(siteTool.getToolId());
		if (tool == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(siteTool.getSiteId());
			}
			catch (IdUnusedException e)
			{
				doError(req, res, session, ERROR_WORKSITE);
				return;
			}
			catch (PermissionException e)
			{
				// if not logged in, give them a chance
				if (session.getUserId() == null)
				{
					doLogin(req, res, session, req.getPathInfo(), false);
				}
				else
				{
					doError(req, res, session, ERROR_WORKSITE);
				}
				return;
			}
		}

		forwardTool(tool, req, res, siteTool, siteTool.getSkin(), toolContextPath,
				toolPathInfo);
	}

	protected void setupForward(HttpServletRequest req, HttpServletResponse res,
			Placement p, String skin) throws ToolException
	{
		// setup html information that the tool might need (skin, body on load,
		// js includes, etc).
		if (skin == null || skin.length() == 0)
			skin = ServerConfigurationService.getString("skin.default");
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String headCssToolBase = "<link href=\""
				+ skinRepo
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

		// to force all non-legacy tools to use the standard css
		// to help in transition (needs corresponding entry in properties)
		// if
		// ("true".equals(ServerConfigurationService.getString("skin.force")))
		// {
		// headJs = headJs + headCss;
		// }

		req.setAttribute("sakai.html.head", head);
		req.setAttribute("sakai.html.head.css", headCss);
		req.setAttribute("sakai.html.head.css.base", headCssToolBase);
		req.setAttribute("sakai.html.head.css.skin", headCssToolSkin);
		req.setAttribute("sakai.html.head.js", headJs);
		req.setAttribute("sakai.html.body.onload", bodyonload.toString());
	}

	/**
	 * Forward to the tool - but first setup JavaScript/CSS etc that the tool
	 * will render
	 */
	protected void forwardTool(ActiveTool tool, HttpServletRequest req,
			HttpServletResponse res, Placement p, String skin, String toolContextPath,
			String toolPathInfo) throws ToolException
	{

		// if there is a stored request state, and path, extract that from the
		// session and
		// reinstance it

		// let the tool do the the work (forward)
		if (enableDirect)
		{
			StoredState ss = portalService.getStoredState();
			if (ss == null || !toolContextPath.equals(ss.getToolContextPath()))
			{
				setupForward(req, res, p, skin);
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				tool.forward(req, res, p, toolContextPath, toolPathInfo);
			}
			else
			{
				HttpServletRequest sreq = ss.getRequest(req);
				Placement splacement = ss.getPlacement();
				String stoolContext = ss.getToolContextPath();
				String stoolPathInfo = ss.getToolPathInfo();
				ActiveTool stool = ActiveToolManager.getActiveTool(p.getToolId());
				String sskin = ss.getSkin();
				setupForward(sreq, res, splacement, sskin);
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				stool.forward(sreq, res, splacement, stoolContext, stoolPathInfo);
				// This is correct as we have checked the context path of the
				// target
				portalService.setStoredState(null);
			}
		}
		else
		{
			setupForward(req, res, p, skin);
			req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
			tool.forward(req, res, p, toolContextPath, toolPathInfo);
		}

	}

	protected void forwardPortal(ActiveTool tool, HttpServletRequest req,
			HttpServletResponse res, ToolConfiguration p, String skin,
			String toolContextPath, String toolPathInfo) throws ToolException,
			IOException
	{

		// if there is a stored request state, and path, extract that from the
		// session and
		// reinstance it

		// generate the forward to the tool page placement
		String portalPlacementUrl = "/portal" + getPortalPageUrl(p);
		res.sendRedirect(portalPlacementUrl);
		return;

	}

	protected String getPortalPageUrl(ToolConfiguration p)
	{
		return "/site/" + p.getSiteId() + "/page/" + p.getPageId();
	}

	protected void doWorksite(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String pageId, String toolContextPath)
			throws ToolException, IOException
	{
		// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			site = getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				doError(req, res, session, ERROR_WORKSITE);
			}
			return;
		}

		// find the page, or use the first page if pageId not found
		SitePage page = site.getPage(pageId);
		if (page == null)
		{
			List pages = site.getOrderedPages();
			if (!pages.isEmpty())
			{
				page = (SitePage) pages.get(0);
			}
		}
		if (page == null)
		{
			doError(req, res, session, ERROR_WORKSITE);
			return;
		}

		// store the last page visited
		session.setAttribute(ATTR_SITE_PAGE + siteId, page.getId());

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		PrintWriter out = startResponse(res, title, site.getSkin(), true);

		String siteType = calcSiteType(siteId);
		out.println("<div id=\"container\""
				+ ((siteType != null) ? " class=\"" + siteType + "\"" : "") + ">");

		includeWorksite(req, res, session, site, page, toolContextPath, "worksite");
		out.println("<div>");

		// end the response
		endResponse(out);
	}

	protected void endResponse(PrintWriter out)
	{
		out.println("</body></html>");
	}

	protected String getScriptPath()
	{
		String libPath = "/library";
		return libPath + "/js/";
	}

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Charon Portal";
	}

	protected void includeBottom(PrintWriter out)
	{
		String copyright = ServerConfigurationService.getString("bottom.copyrighttext");
		String service = ServerConfigurationService.getString("ui.service", "Sakai");
		String serviceVersion = ServerConfigurationService.getString("version.service",
				"?");
		String sakaiVersion = ServerConfigurationService.getString("version.sakai", "?");
		String server = ServerConfigurationService.getServerId();
		String[] bottomNav = ServerConfigurationService.getStrings("bottomnav");
		String[] poweredByUrl = ServerConfigurationService.getStrings("powered.url");
		String[] poweredByImage = ServerConfigurationService.getStrings("powered.img");
		String[] poweredByAltText = ServerConfigurationService.getStrings("powered.alt");

		out.println("<div id=\"footer\">");
		out.println("	<div class=\"footerExtNav\" align=\"center\">");
		out.println("	|");

		if ((bottomNav != null) && (bottomNav.length > 0))
		{
			for (int i = 0; i < bottomNav.length; i++)
			{
				out.println("	" + bottomNav[i] + " | ");
			}
		}

		out.println("	</div>");
		out.println("	<div id=\"footerInfo\">");
		if ((poweredByUrl != null) && (poweredByImage != null)
				&& (poweredByAltText != null)
				&& (poweredByUrl.length == poweredByImage.length)
				&& (poweredByUrl.length == poweredByAltText.length))
		{
			for (int i = 0; i < poweredByUrl.length; i++)
			{
				out.println("	<span class=\"skip\">"
						+ Web.escapeHtml(rb.getString("site_newwindow"))
						+ "</span>	<a href=\"" + poweredByUrl[i]
						+ "\" target=\"_blank\">" + "<img border=\"0\" src=\""
						+ poweredByImage[i] + "\" alt=\"" + poweredByAltText[i]
						+ "\" /></a>");
			}
		}
		else
		{
			out
					.println("		<span class=\"skip\">"
							+ Web.escapeHtml(rb.getString("site_newwindow"))
							+ "</span><a href=\"http://sakaiproject.org\" target=\"_blank\">"
							+ "<img border=\"0\" src=\"/library/image/sakai_powered.gif\" alt=\"Powered by Sakai\" /></a>");
		}
		out.println("	</div>");
		out.println("		<div class=\"sakaiCopyrightInfo\">" + copyright + "<br />");
		out.println("		" + service + " - " + serviceVersion + " - Sakai " + sakaiVersion
				+ " - Server \"" + server + "\"</div>");
		out.println("	</div>");
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");
	}

	protected void includeGalleryLogin(PrintWriter out, HttpServletRequest req,
			Session session, String siteId) throws IOException
	{
		out.println("<div class=\"siteNavBlock\">");
		out
				.println("<table class=\"mast-head\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
		out.println("	<tr>");
		out.println("		<td class=\"right mast-head-r right\">");
		this.includeLogin(out, req, session);
		out.println("		</td>");
		out.println("	</tr>");
		out.println("</table>");
	}

	protected void includeGalleryNav(PrintWriter out, HttpServletRequest req,
			Session session, String siteId)
	{
		boolean loggedIn = session.getUserId() != null;
		boolean topLogin = ServerConfigurationService.getBoolean("top.login", true);

		String siteNavUrl = null;

		if (loggedIn)
		{
			siteNavUrl = Web.returnUrl(req, "/gallery_tabs/" + Web.escapeUrl(siteId));
		}
		else
		{
			siteNavUrl = Web
					.returnUrl(req, "/nav_login_gallery/" + Web.escapeUrl(siteId));
		}

		// outer blocks and jump-to links
		out.println("	<div id=\"portalOuterContainer\">");
		out.println("	<div id=\"portalContainer\">");
		out.println("<div id=\"skipNav\">");
		String accessibilityURL = ServerConfigurationService
				.getString("accessibility.url");
		if (accessibilityURL != null && accessibilityURL != "")
		{
			out.println("<a href=\"" + accessibilityURL + "\" class=\"skip\" title=\""
					+ Web.escapeHtml(rb.getString("sit_accessibility"))
					+ "\" accesskey=\"0\">"
					+ Web.escapeHtml(rb.getString("sit_accessibility")) + "</a>");
		}
		out.println("	<a href=\"#tocontent\"  class=\"skip\" title=\""
				+ Web.escapeHtml(rb.getString("sit_jumpcontent")) + "\" accesskey=\"c\">"
				+ Web.escapeHtml(rb.getString("sit_jumpcontent")) + "</a>");
		out.println("	<a href=\"#totoolmenu\"  class=\"skip\" title=\""
				+ Web.escapeHtml(rb.getString("sit_jumptools")) + "\" accesskey=\"l\">"
				+ Web.escapeHtml(rb.getString("sit_jumptools")) + "</a>");
		out.println("	<a href=\"#sitetabs\" class=\"skip\" title=\""
				+ Web.escapeHtml(rb.getString("sit_jumpworksite"))
				+ "\" accesskey=\"w\">"
				+ Web.escapeHtml(rb.getString("sit_jumpworksite")) + "</a>");
		out.println("</div>");
		if (loggedIn)
		{
			out.println("<div id=\"headerMax\">");
		}
		else
		{
			out.println("<div id=\"headerMin\">");
		}

		try
		{
			if (loggedIn)
			{
				includeTabs(out, req, session, siteId, "gallery", false);
			}
			else
			{
				includeGalleryLogin(out, req, session, siteId);
				out
						.println("<div class=\"divColor\" id=\"tabBottom\"><br /></div></div>");
			}
		}
		catch (Exception any)
		{
		}

		out.println("</div>");
	}

	protected void includeLogo(PrintWriter out, HttpServletRequest req, Session session,
			String siteId) throws IOException
	{
		String skin = SiteService.getSiteSkin(siteId);
		if (skin == null)
		{
			skin = ServerConfigurationService.getString("skin.default");
		}
		String skinRepo = ServerConfigurationService.getString("skin.repo");

		String logo = skinRepo + "/" + skin + "/images/logo_inst.gif";
		String banner = skinRepo + "/" + skin + "/images/banner_inst.gif";

		String siteType = calcSiteType(siteId);
		String cssClass = (siteType != null) ? siteType : "undeterminedSiteType";
		out.println("<div id=\"siteNavWrapper\" class=\"" + cssClass + "\">");
		out.println("<div id=\"mastHead\">");
		out.println("	<div id=\"mastLogo\">");
		out.println("		<img title=\"Logo\" alt=\"Logo\" src=\"" + logo + "\" />");
		out.println("	</div>");
		out.println("	<div id=\"mastBanner\">");
		out.println("		<img title=\"Banner\" alt=\"Banner\" src=\"" + banner + "\" />");
		out.println("	</div>");
		out.println("	<div id=\"mastLogin\">");
		includeLogin(out, req, session);
		out.println("	</div>");
		out.println("</div>");
	}

	protected void includeLogin(PrintWriter out, HttpServletRequest req, Session session)
	{
		// for the main login/out link
		String logInOutUrl = Web.serverUrl(req);
		String message = null;
		String image1 = null;

		// for a possible second link
		String logInOutUrl2 = null;
		String message2 = null;
		String image2 = null;

		// check for the top.login (where the login fields are present instead
		// of a login link, but ignore it if container.login is set
		boolean topLogin = Boolean.TRUE.toString().equalsIgnoreCase(
				ServerConfigurationService.getString("top.login"));
		boolean containerLogin = Boolean.TRUE.toString().equalsIgnoreCase(
				ServerConfigurationService.getString("container.login"));
		if (containerLogin) topLogin = false;

		// if not logged in they get login
		if (session.getUserId() == null)
		{
			// we don't need any of this if we are doing top login
			if (!topLogin)
			{
				logInOutUrl += ServerConfigurationService.getString("portalPath")
						+ "/login";

				// let the login url be overridden by configuration
				String overrideLoginUrl = StringUtil
						.trimToNull(ServerConfigurationService.getString("login.url"));
				if (overrideLoginUrl != null) logInOutUrl = overrideLoginUrl;

				// check for a login text override
				message = StringUtil.trimToNull(ServerConfigurationService
						.getString("login.text"));
				if (message == null) message = rb.getString("log.login");

				// check for an image for the login
				image1 = StringUtil.trimToNull(ServerConfigurationService
						.getString("login.icon"));

				// check for a possible second, xlogin link
				if (Boolean.TRUE.toString().equalsIgnoreCase(
						ServerConfigurationService.getString("xlogin.enabled")))
				{
					// get the text and image as configured
					message2 = StringUtil.trimToNull(ServerConfigurationService
							.getString("xlogin.text"));
					image2 = StringUtil.trimToNull(ServerConfigurationService
							.getString("xlogin.icon"));
					logInOutUrl2 = ServerConfigurationService.getString("portalPath")
							+ "/xlogin";
				}
			}
		}

		// if logged in they get logout
		else
		{
			logInOutUrl += ServerConfigurationService.getString("portalPath") + "/logout";

			// check for a logout text override
			message = StringUtil.trimToNull(ServerConfigurationService
					.getString("logout.text"));
			if (message == null) message = rb.getString("sit_log");

			// check for an image for the logout
			image1 = StringUtil.trimToNull(ServerConfigurationService
					.getString("logout.icon"));

			// since we are doing logout, cancel top.login
			topLogin = false;
		}

		// put out the links version
		if (!topLogin)
		{
			out.println("<div id=\"loginLinks\">");
			out.println("			<a href=\"" + logInOutUrl + "\" target=\"_parent\" title=\""
					+ message + "\">"
					+ ((image1 == null) ? message : "<img src=\"" + image1 + "\"/>")
					+ "</a>");
			if (logInOutUrl2 != null)
			{
				out.println("			<a href=\""
						+ logInOutUrl2
						+ "\" target=\"_parent\" title=\""
						+ message2
						+ "\">"
						+ ((image2 == null) ? message2 : "<img alt=\"" + message2
								+ "\" src=\"" + image2 + "\"/>") + "</a>");
			}
			out.println("</div>");
		}

		// else put out the fields that will send to the login interface
		else
		{
			// find the login tool
			Tool loginTool = ToolManager.getTool("sakai.login");
			String eidWording = null;
			String pwWording = null;
			eidWording = StringUtil.trimToNull(rb.getString("log.userid"));
			pwWording = StringUtil.trimToNull(rb.getString("log.pass"));

			if (eidWording == null) eidWording = "eid";
			if (pwWording == null) pwWording = "pw";
			String loginWording = rb.getString("log.login");

			out
					.println("<form id=\"loginForm\" method=\"post\" action=\""
							+ ServerConfigurationService.getString("portalPath")
							+ "/xlogin\" enctype=\"application/x-www-form-urlencoded\" target=\"_parent\">");
			out.println("<label for=\"eid\">" + eidWording
					+ "</label><input name=\"eid\" id=\"eid\" type=\"text\" />");
			out.println("<label for=\"pw\">" + pwWording
					+ "</label><input name=\"pw\" type=\"password\" id=\"pw\" />");
			out.println("<input name=\"submit\" type=\"submit\" id=\"submit\" value=\""
					+ loginWording + "\" /> </form>");

			out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
			out.println("document.forms[0].eid.focus();");
			out.println("</script>");

			// setup for the redirect after login
			session.setAttribute(Tool.HELPER_DONE_URL, ServerConfigurationService
					.getPortalUrl());
		}
	}

	protected void includePage(HttpServletRequest req, HttpServletResponse res,
			SitePage page, String toolContextPath, String wrapperClass)
			throws IOException
	{
		PrintWriter out = res.getWriter();
		// divs to wrap the tools

		// divs to wrap the tools
		out.println("<div id=\"" + wrapperClass + "\">");

		// get the tools on this first column of page
		if (page.getLayout() == SitePage.LAYOUT_DOUBLE_COL)
		{
			out.println("<div id=\"col1of2\">");
		}
		else
		{
			out.println("<div id=\"col1\">");
		}

		Site site = null;
		try
		{
			site = SiteService.getSite(page.getSiteId());
		}
		catch (Exception ignoreMe)
		{
			// Non fatal - just assume null
			if (M_log.isTraceEnabled())
				M_log.trace("includePage unable to find site for page " + page.getId());
		}

		List tools = page.getTools(0);
		for (Iterator i = tools.iterator(); i.hasNext();)
		{
			ToolConfiguration placement = (ToolConfiguration) i.next();

			if (site != null)
			{
				boolean thisTool = allowTool(site, placement);
				// System.out.println(" Allow Tool Display -" +
				// placement.getTitle() + " retval = " + thisTool);
				if (!thisTool) continue; // Skip this tool if not allowed
			}

			// for this tool invocation, form the servlet context and path info
			String contextPath = ServerConfigurationService.getToolUrl() + "/"
					+ Web.escapeUrl(placement.getId());
			String pathInfo = null;

			// invoke the tool
			includeTool(req, res, placement);
		}
		out.println("</div>");

		// do the second column if needed
		if (page.getLayout() == SitePage.LAYOUT_DOUBLE_COL)
		{
			out.println("<div id=\"col2of2\">");
			tools = page.getTools(1);
			for (Iterator i = tools.iterator(); i.hasNext();)
			{
				ToolConfiguration placement = (ToolConfiguration) i.next();

				// for this tool invocation, form the servlet context and path
				// info
				String contextPath = ServerConfigurationService.getToolUrl() + "/"
						+ Web.escapeUrl(placement.getId());
				String pathInfo = null;

				// invoke the tool
				includeTool(req, res, placement);
			}
			out.println("</div>");
		}

		out.println("</div>");
	}

	protected boolean allowTool(Site site, Placement placement)
	{
		return toolHelper.allowTool(site, placement);
	}

	protected void includePageNav(HttpServletRequest req, HttpServletResponse res,
			Session session, Site site, SitePage page, String toolContextPath,
			String portalPrefix) throws IOException
	{
		PrintWriter out = res.getWriter();
		// If we have turned on auto-state reset on navigation, we generate the
		// "site-reset" "worksite-reset" and "gallery-reset" urls
		if ("true".equals(ServerConfigurationService.getString(CONFIG_AUTO_RESET)))
		{
			portalPrefix = portalPrefix + "-reset";
		}

		String pageUrl = Web.returnUrl(req, "/" + portalPrefix + "/"
				+ Web.escapeUrl(getSiteEffectiveId(site)) + "/page/");
		String pagePopupUrl = Web.returnUrl(req, "/page/");
		boolean showPresence = ServerConfigurationService.getBoolean(
				"display.users.present", true);
		boolean showHelp = ServerConfigurationService.getBoolean("display.help.menu",
				true);
		boolean loggedIn = session.getUserId() != null;
		String iconUrl = site.getIconUrlFull();
		boolean published = site.isPublished();
		String type = site.getType();

		out.println("<div class=\"divColor\" id=\"toolMenuWrap\">");
		out.println("	<div id=\"worksiteLogo\">");
		if (!published)
		{
			out.println("<p id=\"siteStatus\">" + "unpublished site" + "</p>");
		}
		if (type != null)
		{
			if (type.equals("project"))
			{
				out.println("<p id=\"siteType\">" + type + "</p>");
			}
		}
		if (iconUrl != null)
		{
			out.println("	<img src=\"" + iconUrl + "\" border=\"0\" />");
		}
		out.println("	</div>");

		// target of "jump to tools" link, header
		out.println("	<a id=\"totoolmenu\" class=\"skip\" name=\"totoolmenu\"></a>");
		out.println("	<h1 class=\"skip\">"
				+ Web.escapeHtml(rb.getString("sit_toolshead")) + "</h1>");

		out.println("	<div id=\"toolMenu\">");
		out.println("		<ul>");

		// order the pages based on their tools and the tool order for the site
		// type
		List pages = site.getOrderedPages();

		// gsilver - counter for tool accesskey attributes of <a>
		// int count = 0;

		for (Iterator i = pages.iterator(); i.hasNext();)
		{
			SitePage p = (SitePage) i.next();

			// check if current user has permission to see page
			// will draw page button if it have permission to see at least one
			// tool
			List pTools = p.getTools();
			Iterator iPt = pTools.iterator();

			boolean allowPage = false;
			while (iPt.hasNext())
			{
				ToolConfiguration placement = (ToolConfiguration) iPt.next();

				boolean thisTool = allowTool(site, placement);
				if (thisTool) allowPage = true;
				// System.out.println(" Allow Tool -" + tool.getTitle() + "
				// retval = " + thisTool + " page=" + allowPage);
			}

			if (!allowPage) continue;

			boolean current = (p.getId().equals(page.getId()) && !p.isPopUp());

			if (current)
			{
				out.print("			<li class=\"selectedTool\"><a ");
			}
			else
			{
				out.print("			<li><a ");
			}

			// removing accesskeys from tool menu
			// if (count < 10)
			// {
			// out.print("accesskey=\"" + count + "\" ");
			// }
			if (current)
			{
				out.print("class=\"selected\" ");
			}
			out.print("href=\"");
			if (current)
			{
				out.print("#");
			}
			else if (p.isPopUp())
			{
				out
						.print("javascript:;\" "
								+ "onclick=\"window.open('"
								+ pagePopupUrl
								+ Web.escapeUrl(p.getId())
								+ "'"
								+ ",'"
								+ Web.escapeJavascript(p.getTitle())
								+ "','resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')");
			}
			else
			{
				String pagerefUrl = pageUrl + Web.escapeUrl(p.getId());
				out.print(pagerefUrl);
			}
			out.println("\"><span>" + Web.escapeHtml(p.getTitle()) + "</span></a></li>");

			// count++;
		}

		if (showHelp)
		{
			String helpUrl = ServerConfigurationService.getHelpUrl(null);
			out.println("			<li>");

			// help gets its own accesskey - 6
			out
					.println("				<a  accesskey=\"6\" href=\""
							+ helpUrl
							+ "\" target=\"_blank\" "
							+ "onclick=\"openWindow('"
							+ helpUrl
							+ "', 'Help', 'resizable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false\">"
							+ "<span>" + rb.getString("sit_help") + "</span></a>");

			out.println("			</li>");
		}

		out.println("		</ul>");
		out.println("	</div>");
		if (showPresence && loggedIn)
		{
			String presenceUrl = Web.returnUrl(req, "/presence/"
					+ Web.escapeUrl(site.getId()));
			out.println("	<div id=\"presenceWrapper\">");
			out.println("		<div id=\"presenceTitle\">");
			out.println(Web.escapeHtml(rb.getString("sit_presencetitle")));
			out.println("	</div>");
			out.println("	<iframe ");
			out.println("		name=\"presenceIframe\"");
			out.println("		id=\"presenceIframe\"");
			out.println("		title=\""
					+ Web.escapeHtml(rb.getString("sit_presenceiframetit")) + "\"");
			out.println("		frameborder=\"0\"");
			out.println("		marginwidth=\"0\"");
			out.println("		marginheight=\"0\"");
			out.println("		scrolling=\"auto\"");
			out.println("		src=\"" + presenceUrl + "\"");
			out.println("	>");
			out.println("	</iframe>");
			out.println("	</div>");
		}
		out.println("</div>");

		// target of "jump to content" link and header for content

		out.println("	<h1 class=\"skip\">"
				+ Web.escapeHtml(rb.getString("sit_contentshead")) + "</h1>");
		out.println("	<a id=\"tocontent\" class=\"skip\" name=\"tocontent\"></a>");

	}

	protected void includeSiteNav(PrintWriter out, HttpServletRequest req,
			Session session, String siteId)
	{
		boolean loggedIn = session.getUserId() != null;
		boolean topLogin = ServerConfigurationService.getBoolean("top.login", true);

		String siteNavUrl = null;
		int height = 0;
		String siteNavClass = null;

		if (loggedIn)
		{
			siteNavUrl = Web.returnUrl(req, "/site_tabs/" + Web.escapeUrl(siteId));
			height = 104;
			siteNavClass = "sitenav-max";
		}
		else
		{
			siteNavUrl = Web.returnUrl(req, "/nav_login/" + Web.escapeUrl(siteId));
			height = 80;
			siteNavClass = "sitenav-log";
		}

		// outer blocks and jump to links
		out.println("	<div id=\"portalOuterContainer\">");
		out.println("	<div id=\"portalContainer\">");
		out.println("<div id=\"skipNav\">");

		String accessibilityURL = ServerConfigurationService
				.getString("accessibility.url");
		if (accessibilityURL != null && accessibilityURL != "")
		{
			out.println("<a href=\"" + accessibilityURL + "\" class=\"skip\" title=\""
					+ Web.escapeHtml(rb.getString("sit_accessibility"))
					+ "\" accesskey=\"0\">"
					+ Web.escapeHtml(rb.getString("sit_accessibility")) + "</a>");
		}
		out.println("<a href=\"#tocontent\"  class=\"skip\" title=\""
				+ Web.escapeHtml(rb.getString("sit_jumpcontent")) + "\" accesskey=\"c\">"
				+ Web.escapeHtml(rb.getString("sit_jumpcontent")) + "</a>");
		out.println("<a href=\"#totoolmenu\"  class=\"skip\" title=\""
				+ Web.escapeHtml(rb.getString("sit_jumptools")) + "\" accesskey=\"l\">"
				+ Web.escapeHtml(rb.getString("sit_jumptools")) + "</a>");
		out.println("<a href=\"#sitetabs\" class=\"skip\" title=\""
				+ Web.escapeHtml(rb.getString("sit_jumpworksite"))
				+ "\" accesskey=\"w\">"
				+ Web.escapeHtml(rb.getString("sit_jumpworksite")) + "</a>");
		out.println("</div>");
		if (loggedIn)
		{
			out.println("<div id=\"headerMax\">");
		}
		else
		{
			out.println("<div id=\"headerMin\">");
		}

		try
		{
			if (loggedIn)
			{
				includeLogo(out, req, session, siteId);
				includeTabs(out, req, session, siteId, "site", false);
			}
			else
			{
				includeLogo(out, req, session, siteId);
				out
						.println("<div class=\"divColor\" id=\"tabBottom\"><br /></div></div>");
			}
		}
		catch (Exception any)
		{
		}

		out.println("</div>");
	}

	protected void includeTabs(PrintWriter out, HttpServletRequest req, Session session,
			String siteId, String prefix, boolean addLogout) throws IOException
	{
		// for skinning
		String siteType = calcSiteType(siteId);

		// If we have turned on auto-state reset on navigation, we generate the
		// "site-reset" "worksite-reset" and "gallery-reset" urls
		if ("true".equals(ServerConfigurationService.getString(CONFIG_AUTO_RESET)))
		{
			prefix = prefix + "-reset";
		}

		// is the current site the end user's My Workspace?
		// Note: the site id can match the user's id or eid
		String curUserId = session.getUserId();
		String curUserEid = curUserId;
		if (siteId != null)
		{
			try
			{
				curUserEid = UserDirectoryService.getUserEid(curUserId);
			}
			catch (UserNotDefinedException e)
			{
			}
		}
		boolean curMyWorkspace = ((siteId == null) || (SiteService.isUserSite(siteId) && ((SiteService
				.getSiteUserId(siteId).equals(curUserId) || SiteService.getSiteUserId(
				siteId).equals(curUserEid)))));

		// if this is a My Workspace, it gets its own tab and should not be
		// considered in the other tab logic
		if (curMyWorkspace) siteId = null;

		// collect the user's sites
		List mySites = SiteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null,
				null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);

		// collect the user's preferences
		int prefTabs = 4;
		List prefExclude = new Vector();
		List prefOrder = new Vector();
		if (session.getUserId() != null)
		{
			Preferences prefs = PreferencesService.getPreferences(session.getUserId());
			ResourceProperties props = prefs.getProperties("sakai:portal:sitenav");
			try
			{
				prefTabs = (int) props.getLongProperty("tabs");
			}
			catch (Exception any)
			{
			}

			List l = props.getPropertyList("exclude");
			if (l != null)
			{
				prefExclude = l;
			}

			l = props.getPropertyList("order");
			if (l != null)
			{
				prefOrder = l;
			}
		}

		// the number of tabs to display
		int tabsToDisplay = prefTabs;

		// remove all in exclude from mySites
		mySites.removeAll(prefExclude);

		// re-order mySites to have order first, the rest later
		List ordered = new Vector();
		for (Iterator i = prefOrder.iterator(); i.hasNext();)
		{
			String id = (String) i.next();

			// find this site in the mySites list
			int pos = indexOf(id, mySites);
			if (pos != -1)
			{
				// move it from mySites to order
				Site s = (Site) mySites.get(pos);
				ordered.add(s);
				mySites.remove(pos);
			}
		}

		// pick up the rest of the sites
		ordered.addAll(mySites);
		mySites = ordered;

		// split into 2 lists - the first n, and the rest
		List moreSites = new Vector();
		if (mySites.size() > tabsToDisplay)
		{
			int remove = mySites.size() - tabsToDisplay;
			for (int i = 0; i < remove; i++)
			{
				Site site = (Site) mySites.get(tabsToDisplay);

				// add to more unless it's the current site (it will get an
				// extra tag)
				if (!site.getId().equals(siteId))
				{
					moreSites.add(site);
				}

				// remove from the display list
				mySites.remove(tabsToDisplay);
			}
		}

		// if more has just one, put it back on the main list
		if (moreSites.size() == 1)
		{
			mySites.add(moreSites.get(0));
			moreSites.clear();
		}

		// check if the current site is missing from the main list
		String extraTitle = null;
		if (siteId != null)
		{
			boolean extra = true;
			for (Iterator i = mySites.iterator(); i.hasNext();)
			{
				Site site = (Site) i.next();
				if (site.getId().equals(siteId))
				{
					extra = false;
					break;
				}
			}
			if (extra)
			{
				try
				{
					Site site = SiteService.getSite(siteId);
					extraTitle = site.getTitle();
				}
				catch (IdUnusedException e)
				{
					// check for another user's myWorkspace by eid
					if (SiteService.isUserSite(siteId))
					{
						String userEid = SiteService.getSiteUserId(siteId);
						try
						{
							String userId = UserDirectoryService.getUserId(userEid);
							Site site = SiteService.getSite(SiteService
									.getUserSiteId(userId));
							extraTitle = site.getTitle();
						}
						catch (UserNotDefinedException ee)
						{
							M_log.warn("includeTabs: cur site not found (not ~eid): "
									+ siteId);
						}
						catch (IdUnusedException ee)
						{
							M_log
									.warn("includeTabs: cur site not found (assumed ~eid, didn't find site): "
											+ siteId);
						}
					}
					else
					{
						M_log.warn("includeTabs: cur site not found: " + siteId);
					}
				}
			}
		}

		String cssClass = (siteType != null) ? "siteNavWrap " + siteType : "siteNavWrap";
		out.println("<div class=\"" + cssClass + "\">");
		out.println("	<div id=\"siteNav\">");
		// out.println(" <tr>");
		out.println("		<div id=\"linkNav\">");

		// target for "jump to tabs" link and header

		out.println("				<a id=\"sitetabs\" class=\"skip\" name=\"sitetabs\"></a>");
		out.println("				<h1 class=\"skip\">"
				+ Web.escapeHtml(rb.getString("sit_worksiteshead")) + "</h1>");

		out.println("				<ul id=\"siteLinkList\">");

		// myWorkspace
		if (curMyWorkspace)
		{
			out.println("						<li class=\"selectedTab\"><a href=\"#\"><span>"
					+ rb.getString("sit_mywor") + "</span></a></li>");
		}
		else
		{
			String siteUrl = Web.serverUrl(req)
					+ ServerConfigurationService.getString("portalPath") + "/" + prefix
					+ "/" + Web.escapeUrl(getUserEidBasedSiteId(session.getUserId()));
			out.println("						<li><a href=\"" + siteUrl
					+ "\" target=\"_parent\" title=\""
					+ Web.escapeHtml(rb.getString("sit_mywor")) + "\"><span>"
					+ Web.escapeHtml(rb.getString("sit_mywor")) + "</span></a></li>");
		}

		// first n tabs
		for (Iterator i = mySites.iterator(); i.hasNext();)
		{
			Site s = (Site) i.next();
			if (s.getId().equals(siteId))
			{
				out.println("							<li class=\"selectedTab\"><a href=\"#\"><span>"
						+ Web.escapeHtml(s.getTitle()) + "</span></a></li>");
			}
			else
			{
				String siteUrl = Web.serverUrl(req)
						+ ServerConfigurationService.getString("portalPath") + "/"
						+ prefix + "/" + Web.escapeUrl(getSiteEffectiveId(s));
				out
						.println("							<li><a href=\"" + siteUrl
								+ "\" target=\"_parent\" title=\""
								+ Web.escapeHtml(s.getTitle()) + " "
								+ Web.escapeHtml(rb.getString("sit_worksite"))
								+ "\"><span>" + Web.escapeHtml(s.getTitle())
								+ "</span></a></li>");
			}
		}

		// current site, if not in the list of first n tabs
		if (extraTitle != null)
		{
			out.println("						<li class=\"selectedTab\"><a><span>"
					+ Web.escapeHtml(extraTitle) + "</span></a></li>");
		}

		out.println("					<li style=\"display:none;border-width:0\" class=\"fixTabsIE\">"
				+ "<a href=\"javascript:void(0);\">#x20;</a></li>");
		out.println("				</ul>");
		out.println("			</div>");

		// more dropdown
		if (moreSites.size() > 0)
		{
			out.println("			<div id=\"selectNav\"><span class=\"skip\">"
					+ Web.escapeHtml(rb.getString("sit_selectmessage")) + "</span>");
			out.println("				<select ");
			out
					.println("						onchange=\"if (this.options[this.selectedIndex].value != '')"
							+ " { parent.location = this.options[this.selectedIndex].value; } else { this.selectedIndex = 0; }\">");
			out.println("					<option value=\"\" selected=\"selected\">"
					+ Web.escapeHtml(rb.getString("sit_more")) + "</option>");

			for (Iterator i = moreSites.iterator(); i.hasNext();)
			{
				Site s = (Site) i.next();
				String siteUrl = Web.serverUrl(req)
						+ ServerConfigurationService.getString("portalPath") + "/"
						+ prefix + "/" + getSiteEffectiveId(s);
				out.println("						<option title=\"" + Web.escapeHtml(s.getTitle()) + " "
						+ Web.escapeHtml(rb.getString("sit_worksite")) + "\" value=\""
						+ siteUrl + "\">" + Web.escapeHtml(s.getTitle()) + "</option> ");
			}

			out.println("				</select>");
			out.println("			</div>");
		}

		if (addLogout)
		{
			String logoutUrl = Web.serverUrl(req)
					+ ServerConfigurationService.getString("portalPath")
					+ "/logout_gallery";
			out.println("<div class=\"galleryLogin\">");
			out.println("	<a href=\"" + logoutUrl + "\" target=\"_parent\">"
					+ Web.escapeHtml(rb.getString("sit_log")) + "</a>");
			out.println("</div>");
		}
		// out.println(" </tr>");
		out.println("	</div>");
		out.println("<div class=\"divColor\" id=\"tabBottom\"></div></div>");
		if (addLogout)
		{
		}
		else
		{
			out.println("		</div>");
		}
	}

	protected void includeTool(HttpServletRequest req, HttpServletResponse res,
			ToolConfiguration placement) throws IOException
	{
		PrintWriter out = res.getWriter();

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(placement.getToolId());
		if (tool == null)
		{
			// doError(req, res, session);
			return;
		}

		// let the tool do some the work (include) (see note above)
		String toolUrl = ServerConfigurationService.getToolUrl() + "/"
				+ Web.escapeUrl(placement.getId());

		// Reset the tool state if requested
		if (portalService.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placement.getId());
			ts.clearAttributes();
		}

		// this is based on what varuna is currently putting out
		out.println("<div class=\"portlet\">");
		out.println("<div class=\"portletTitleWrap\">");

		// emit title information

		// for the reset button
		boolean showResetButton = !"false".equals(placement.getConfig().getProperty(
				TOOLCONFIG_SHOW_RESET_BUTTON));

		// for the help button
		// get the help document ID from the tool config (tool registration
		// usually).
		// The help document ID defaults to the tool ID
		boolean helpEnabledGlobally = ServerConfigurationService.getBoolean(
				"display.help.icon", true);
		boolean helpEnabledInTool = !"false".equals(placement.getConfig().getProperty(
				TOOLCONFIG_SHOW_HELP_BUTTON));
		boolean showHelpButton = helpEnabledGlobally && helpEnabledInTool;

		String helpActionUrl = "";
		if (showHelpButton)
		{
			String helpDocId = placement.getConfig().getProperty(
					TOOLCONFIG_HELP_DOCUMENT_ID);
			String helpDocUrl = placement.getConfig().getProperty(
					TOOLCONFIG_HELP_DOCUMENT_URL);
			if (helpDocUrl != null && helpDocUrl.length() > 0)
			{
				helpActionUrl = helpDocUrl;
			}
			else
			{
				if (helpDocId == null || helpDocId.length() == 0)
				{
					helpDocId = tool.getId();
				}
				helpActionUrl = ServerConfigurationService.getHelpUrl(helpDocId);
			}
		}

		out.write("<div class=\"portletTitle\">\n");
		out.write("\t<div class=\"title\">\n");

		// Charon is not use, if the target needs portal, then this will generate an NPE
		RenderResult result = ToolRenderService.render(null,placement, req, res,
				getServletContext());

		// This is a new-style reset where the button is in the background
		// document
		if (showResetButton)
		{
			// Note: This is somewhat brittle code as it assumes that the url
			// will
			// always be /tool/ - as an example - one thing that might fail
			// would
			// be if the /portal/ string were /tool/ so the right tool url would
			// be
			// http://localhost:8080/tool/tool/ - then this code would break

			// Send the reset url through a redirect step so that any future
			// refresh of the tool's iframe do not cause a reset
			String resetUrl = PortalStringUtil.replaceFirst(toolUrl, "/tool/",
					"/tool-reset/");
			out.write("\t\t<a href=\"" + resetUrl + "?panel=Main\" " + " target=\""
					+ Web.escapeJavascript("Main" + placement.getId()) + "\""
					+ " title=\"" + Web.escapeHtml(rb.getString("sit_reset"))
					+ "\"><img src=\"/library/image/transparent.gif\" alt=\""
					+ Web.escapeHtml(rb.getString("sit_reset"))
					+ "\" border=\"1\" /></a>");
		}
		out.write("<h2>" + result.getTitle() + "\n" + "\t</h2></div>\n");
		out.write("\t<div class=\"action\">\n");
		if (showHelpButton)
		{
			out.write(makeHelpButton(helpActionUrl));
		}
		out.write("\t</div><!--gsilver end of the action-->\n");
		out.write("</div><!--gsilver end of the portletTitle-->\n");
		out.write("</div><!--gsilver end of the portletTitleWrap-->\n");

		// Output the iframe for the tool content
		out.println("<div class=\"portletMainWrap\">");

		out.println(result.getContent());

		out
				.println("</div><!--gsilver end of the portletMainWrap--></div><!--gsilver end of the portlet-->");
	}

	protected void includeWorksite(HttpServletRequest req, HttpServletResponse res,
			Session session, Site site, SitePage page, String toolContextPath,
			String portalPrefix) throws IOException
	{
		// add the page navigation with presence
		includePageNav(req, res, session, site, page, toolContextPath, portalPrefix);

		// add the page
		includePage(req, res, page, toolContextPath, "content");
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
		portalService = org.sakaiproject.portal.api.cover.PortalService.getInstance();

		M_log.info("init()");

		basicAuth = new BasicAuth();
		basicAuth.init();

		enableDirect = "true".equals(ServerConfigurationService.getString(
				"charon.directurl", "true"));
	}

	/**
	 * Send the POST request to login
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @throws IOException
	 */
	protected void postLogin(HttpServletRequest req, HttpServletResponse res,
			Session session, String loginPath) throws ToolException
	{
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/" + loginPath;
		tool.help(req, res, context, "/" + loginPath);
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
			out.println("session: "
					+ s.getId()
					+ " user id: "
					+ s.getUserId()
					+ " enterprise id: "
					+ s.getUserEid()
					+ " started: "
					+ DateFormat.getDateInstance().format(new Date(s.getCreationTime()))
					+ " accessed: "
					+ DateFormat.getDateInstance().format(
							new Date(s.getLastAccessedTime())) + " inactive after: "
					+ s.getMaxInactiveInterval());
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
			out.println("tool session: "
					+ ts.getId()
					+ " started: "
					+ DateFormat.getDateInstance().format(new Date(ts.getCreationTime()))
					+ " accessed: "
					+ DateFormat.getDateInstance().format(
							new Date(ts.getLastAccessedTime())));
			if (html) out.println("<br />");
		}
	}

	protected PrintWriter startResponse(HttpServletResponse res, String title,
			String skin, boolean top) throws IOException
	{
		// headers
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis()
				- (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res
				.addHeader("Cache-Control",
						"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// get the writer
		PrintWriter out = res.getWriter();

		// form the head
		out
				.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
						+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
						+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">"
						+ "  <head>"
						+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");

		// pick the one full portal skin
		if (skin == null)
		{
			skin = ServerConfigurationService.getString("skin.default");
		}
		String skinRepo = ServerConfigurationService.getString("skin.repo");

		out.println("    <link href=\"" + skinRepo + "/" + skin
				+ "/portal.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />");
		out.println("    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />"
				+ "    <title>" + Web.escapeHtml(title) + "</title>"
				+ "    <script type=\"text/javascript\" language=\"JavaScript\" src=\""
				+ getScriptPath() + "headscripts.js\"></script>" + "  </head>");

		// start the body
		out
				.println("<body class=\"portalBody\">");

		// if top, mark this as the portal window
		if (top)
		{
			out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
			out.println("var sakaiPortalWindow = \"\";");
			out.println("</script>");
		}

		return out;
	}

	/**
	 * Returns the type ("course", "project", "workspace", "mySpecialSiteType",
	 * etc) of the given site; special handling of returning "workspace" for
	 * user workspace sites. This method is tightly coupled to site skinning.
	 */
	protected String calcSiteType(String siteId)
	{
		String siteType = null;
		if (siteId != null && siteId.length() != 0)
		{
			if (SiteService.isUserSite(siteId))
			{
				siteType = "workspace";
			}
			else
			{
				try
				{
					siteType = SiteService.getSite(siteId).getType();
				}
				catch (IdUnusedException ex)
				{
					// ignore, the site wasn't found
				}
			}
		}

		if (siteType != null && siteType.trim().length() == 0) siteType = null;
		return siteType;
	}

	/**
	 * Find the site in the list that has this id - return the position.
	 * 
	 * @param value
	 *        The site id to find.
	 * @param siteList
	 *        The list of Site objects.
	 * @return The index position in siteList of the site with site id = value,
	 *         or -1 if not found.
	 */
	protected int indexOf(String value, List siteList)
	{
		for (int i = 0; i < siteList.size(); i++)
		{
			Site site = (Site) siteList.get(i);
			if (site.equals(value))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Check for any just expired sessions and redirect
	 * 
	 * @return true if we redirected, false if not
	 */
	protected boolean redirectIfLoggedOut(HttpServletResponse res) throws IOException
	{
		// if we are in a newly created session where we had an invalid
		// (presumed timed out) session in the request,
		// send script to cause a sakai top level redirect
		if (ThreadLocalManager.get(SessionManager.CURRENT_INVALID_SESSION) != null)
		{
			String loggedOutUrl = ServerConfigurationService.getLoggedOutUrl();
			sendPortalRedirect(res, loggedOutUrl);
			return true;
		}

		return false;
	}

	/**
	 * Send a redirect so our Portal window ends up at the url, via javascript.
	 * 
	 * @param url
	 *        The redirect url
	 */
	protected void sendPortalRedirect(HttpServletResponse res, String url)
			throws IOException
	{
		PrintWriter out = startResponse(res, null, null, false);
		out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
		out.println("portalWindowRefresh('" + url + "');");
		out.println("</script>");
		endResponse(out);
	}

	/**
	 * Compute the string that will identify the user site for this user - use
	 * the EID if possible
	 * 
	 * @param userId
	 *        The user id
	 * @return The site "ID" but based on the user EID
	 */
	protected String getUserEidBasedSiteId(String userId)
	{
		try
		{
			// use the user EID
			String eid = UserDirectoryService.getUserEid(userId);
			return SiteService.getUserSiteId(eid);
		}
		catch (UserNotDefinedException e)
		{
			M_log.warn("getUserEidBasedSiteId: user id not found for eid: " + userId);
			return SiteService.getUserSiteId(userId);
		}
	}

	/**
	 * If this is a user site, return an id based on the user EID, otherwise
	 * just return the site id.
	 * 
	 * @param site
	 *        The site.
	 * @return The effective site id.
	 */
	protected String getSiteEffectiveId(Site site)
	{
		if (SiteService.isUserSite(site.getId()))
		{
			try
			{
				String userId = SiteService.getSiteUserId(site.getId());
				String eid = UserDirectoryService.getUserEid(userId);
				return SiteService.getUserSiteId(eid);
			}
			catch (UserNotDefinedException e)
			{
				M_log.warn("getSiteEffectiveId: user eid not found for user site: "
						+ site.getId());
			}
		}

		return site.getId();
	}

	/**
	 * Do the getSiteVisit, but if not found and the id is a user site, try
	 * translating from user EID to ID.
	 * 
	 * @param siteId
	 *        The Site Id.
	 * @return The Site.
	 * @throws PermissionException
	 *         If not allowed.
	 * @throws IdUnusedException
	 *         If not found.
	 */
	protected Site getSiteVisit(String siteId) throws PermissionException,
			IdUnusedException
	{
		try
		{
			return SiteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			if (SiteService.isUserSite(siteId))
			{
				try
				{
					String userEid = SiteService.getSiteUserId(siteId);
					String userId = UserDirectoryService.getUserId(userEid);
					String alternateSiteId = SiteService.getUserSiteId(userId);
					return SiteService.getSiteVisit(alternateSiteId);
				}
				catch (UserNotDefinedException ee)
				{
				}
			}

			// re-throw if that didn't work
			throw e;
		}
	}
}
