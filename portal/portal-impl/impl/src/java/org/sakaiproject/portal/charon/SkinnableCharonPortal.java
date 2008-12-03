/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.wurfl.wurflapi.CapabilityMatrix;
import net.sourceforge.wurfl.wurflapi.ObjectsManager;
import net.sourceforge.wurfl.wurflapi.UAManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PageFilter;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalSiteHelper;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.charon.handlers.AtomHandler;
import org.sakaiproject.portal.charon.handlers.DirectToolHandler;
import org.sakaiproject.portal.charon.handlers.ErrorDoneHandler;
import org.sakaiproject.portal.charon.handlers.ErrorReportHandler;
import org.sakaiproject.portal.charon.handlers.GalleryHandler;
import org.sakaiproject.portal.charon.handlers.GalleryResetHandler;
import org.sakaiproject.portal.charon.handlers.HelpHandler;
import org.sakaiproject.portal.charon.handlers.LoginHandler;
import org.sakaiproject.portal.charon.handlers.LogoutHandler;
import org.sakaiproject.portal.charon.handlers.NavLoginHandler;
import org.sakaiproject.portal.charon.handlers.OpmlHandler;
import org.sakaiproject.portal.charon.handlers.PDAHandler;
import org.sakaiproject.portal.charon.handlers.PageHandler;
import org.sakaiproject.portal.charon.handlers.PresenceHandler;
import org.sakaiproject.portal.charon.handlers.ReLoginHandler;
import org.sakaiproject.portal.charon.handlers.RoleSwitchHandler;
import org.sakaiproject.portal.charon.handlers.RoleSwitchOutHandler;
import org.sakaiproject.portal.charon.handlers.RssHandler;
import org.sakaiproject.portal.charon.handlers.SiteHandler;
import org.sakaiproject.portal.charon.handlers.SiteResetHandler;
import org.sakaiproject.portal.charon.handlers.StaticScriptsHandler;
import org.sakaiproject.portal.charon.handlers.StaticStylesHandler;
import org.sakaiproject.portal.charon.handlers.TimeoutDialogHandler;
import org.sakaiproject.portal.charon.handlers.ToolHandler;
import org.sakaiproject.portal.charon.handlers.ToolResetHandler;
import org.sakaiproject.portal.charon.handlers.WorksiteHandler;
import org.sakaiproject.portal.charon.handlers.WorksiteResetHandler;
import org.sakaiproject.portal.charon.handlers.XLoginHandler;
import org.sakaiproject.portal.charon.site.PortalSiteHelperImpl;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.cover.ToolRenderService;
import org.sakaiproject.portal.util.BrowserDetector;
import org.sakaiproject.portal.util.ErrorReporter;
import org.sakaiproject.portal.util.ToolURLManagerImpl;
import org.sakaiproject.portal.util.URLUtils;
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
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * <p/> Charon is the Sakai Site based portal.
 * </p>
 * 
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class SkinnableCharonPortal extends HttpServlet implements Portal
{
	/**
	 * Our log (commons).
	 */
	private static Log M_log = LogFactory.getLog(SkinnableCharonPortal.class);

	/**
	 * messages.
	 */
	private static ResourceLoader rloader = new ResourceLoader("sitenav");

	/**
	 * Parameter value to indicate to look up a tool ID within a site
	 */
	protected static final String PARAM_SAKAI_SITE = "sakai.site";

	private BasicAuth basicAuth = null;

	private boolean enableDirect = false;

	private PortalService portalService;

	private static final String PADDING = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

	private static final String INCLUDE_BOTTOM = "include-bottom";

	private static final String INCLUDE_LOGIN = "include-login";

	private static final String INCLUDE_TITLE = "include-title";

	private PortalSiteHelper siteHelper = null;


	// private HashMap<String, PortalHandler> handlerMap = new HashMap<String,
	// PortalHandler>();

	private GalleryHandler galleryHandler;

	private WorksiteHandler worksiteHandler;

	private SiteHandler siteHandler;

	private String portalContext;

        private String PROP_PARENT_ID = SiteService.PROP_PARENT_ID;
        // 2.3 back port
        // public String String PROP_PARENT_ID = "sakai:parent-id";

        private String PROP_SHOW_SUBSITES  = SiteService.PROP_SHOW_SUBSITES ;
        // 2.3 back port
 	// public String PROP_SHOW_SUBSITES = "sakai:show-subsites";

        // http://wurfl.sourceforge.net/
	private boolean wurflLoaded = false;
	public CapabilityMatrix cm = null;
	public UAManager uam = null;

	private boolean forceContainer = false;

	private String handlerPrefix;

	private PageFilter pageFilter = new PageFilter() {

		public List filter(List newPages, Site site)
		{
			return newPages;
		}

		public List<Map> filterPlacements(List<Map> l, Site site)
		{
			return l;
		}
		
	};


	public String getPortalContext()
	{
		return portalContext;
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
		portalService.removePortal(this);

		super.destroy();
	}

	public void doError(HttpServletRequest req, HttpServletResponse res, Session session,
			int mode) throws ToolException, IOException
	{
		if (ThreadLocalManager.get(ATTR_ERROR) == null)
		{
			ThreadLocalManager.set(ATTR_ERROR, ATTR_ERROR);

			// send to the error site
			switch (mode)
			{
				case ERROR_SITE:
				{
					siteHandler.doSite(req, res, session, "!error", null, req
							.getContextPath()
							+ req.getServletPath());
					break;
				}
				case ERROR_GALLERY:
				{
					galleryHandler.doGallery(req, res, session, "!error", null, req
							.getContextPath()
							+ req.getServletPath());
					break;
				}
				case ERROR_WORKSITE:
				{
					worksiteHandler.doWorksite(req, res, session, "!error", null, req
							.getContextPath()
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
		PortalRenderContext rcontext = startPageContext("", title, null, req);

		showSession(rcontext, true);

		showSnoop(rcontext, true, getServletConfig(), req);

		sendResponse(rcontext, res, "error", null);
	}

	private void showSnoop(PortalRenderContext rcontext, boolean b,
			ServletConfig servletConfig, HttpServletRequest req)
	{
		Enumeration e = null;

		rcontext.put("snoopRequest", req.toString());

		if (servletConfig != null)
		{
			Map<String, Object> m = new HashMap<String, Object>();
			e = servletConfig.getInitParameterNames();

			if (e != null)
			{
				boolean first = true;
				while (e.hasMoreElements())
				{
					String param = (String) e.nextElement();
					m.put(param, servletConfig.getInitParameter(param));
				}
			}
			rcontext.put("snoopServletConfigParams", m);
		}
		rcontext.put("snoopRequest", req);

		e = req.getHeaderNames();
		if (e.hasMoreElements())
		{
			Map<String, Object> m = new HashMap<String, Object>();
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				m.put(name, req.getHeader(name));
			}
			rcontext.put("snoopRequestHeaders", m);
		}

		e = req.getParameterNames();
		if (e.hasMoreElements())
		{
			Map<String, Object> m = new HashMap<String, Object>();
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				m.put(name, req.getParameter(name));
			}
			rcontext.put("snoopRequestParamsSingle", m);
		}

		e = req.getParameterNames();
		if (e.hasMoreElements())
		{
			Map<String, Object> m = new HashMap<String, Object>();
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				String[] vals = (String[]) req.getParameterValues(name);
				StringBuilder sb = new StringBuilder();
				if (vals != null)
				{
					sb.append(vals[0]);
					for (int i = 1; i < vals.length; i++)
						sb.append("           ").append(vals[i]);
				}
				m.put(name, sb.toString());
			}
			rcontext.put("snoopRequestParamsMulti", m);
		}

		e = req.getAttributeNames();
		if (e.hasMoreElements())
		{
			Map<String, Object> m = new HashMap<String, Object>();
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				m.put(name, req.getAttribute(name));

			}
			rcontext.put("snoopRequestAttr", m);
		}
	}

	protected void doThrowableError(HttpServletRequest req, HttpServletResponse res,
			Throwable t)
	{
		ErrorReporter err = new ErrorReporter();
		err.report(req, res, t);
	}

	/*
	 * 
	 * 
	 * Include the children of a site
	 */

	// TODO: Extract to a provider
	
   public void includeSubSites(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, String siteId, String toolContextPath, 
			String prefix, boolean resetTools) 
		// throws ToolException, IOException
	{
		if ( siteId == null || rcontext == null ) return;

		// Check the setting as to whether we are to do this
		String pref = ServerConfigurationService.getString("portal.includesubsites");
		if ( "never".equals(pref) ) return;

		Site site = null;
		try
		{
			site = siteHelper.getSiteVisit(siteId);
		}
		catch (Exception e)
		{
			return;
		}
		if ( site == null ) return;

		
		
		
		ResourceProperties rp = site.getProperties();
		String showSub = rp.getProperty(PROP_SHOW_SUBSITES);
               	// System.out.println("Checking subsite pref:"+site.getTitle()+" pref="+pref+" show="+showSub);
		if ( "false".equals(showSub) ) return;

		if ( "false".equals(pref) )
		{
			if ( ! "true".equals(showSub) ) return;
		}

		SiteView siteView = siteHelper.getSitesView(SiteView.View.SUB_SITES_VIEW,req, session, siteId);
		if ( siteView.isEmpty() ) return;
		
		siteView.setPrefix(prefix);
		siteView.setToolContextPath(toolContextPath);
		siteView.setResetTools(resetTools);
				
		if( !siteView.isEmpty() ) {
			rcontext.put("subSites", siteView.getRenderContextObject());
		}
	}

	/*
	 * Produce a portlet like view with the navigation all at the top with
	 * implicit reset
	 */
	public PortalRenderContext includePortal(HttpServletRequest req,
			HttpServletResponse res, Session session, String siteId, String toolId,
			String toolContextPath, String prefix, boolean doPages, boolean resetTools,
			boolean includeSummary, boolean expandSite) throws ToolException, IOException
	{

		String errorMessage = null;

		// find the site, for visiting
		Site site = null;
		try
		{
			site = siteHelper.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			errorMessage = "Unable to find site: " + siteId;
			siteId = null;
			toolId = null;
		}
		catch (PermissionException e)
		{
			if (session.getUserId() == null)
			{
				errorMessage = "No permission for anonymous user to view site: " + siteId;
			}
			else
			{
				errorMessage = "No permission to view site: " + siteId;
			}
			siteId = null;
			toolId = null; // Tool needs the site and needs it to be visitable
		}

		// Get the Tool Placement
		ToolConfiguration placement = null;
		if (site != null && toolId != null)
		{
			placement = SiteService.findTool(toolId);
			if (placement == null)
			{
				errorMessage = "Unable to find tool placement " + toolId;
				toolId = null;
			}

			boolean thisTool = siteHelper.allowTool(site, placement);
			if (!thisTool)
			{
				errorMessage = "No permission to view tool placement " + toolId;
				toolId = null;
				placement = null;
			}
		}

		// Get the user's My WorkSpace and its ID
		Site myWorkspaceSite = siteHelper.getMyWorkspace(session);
		String myWorkspaceSiteId = null;
		if (myWorkspaceSite != null)
		{
			myWorkspaceSiteId = myWorkspaceSite.getId();
		}

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service");
		if (site != null)
		{
			title = title + ":" + site.getTitle();
			if (placement != null) title = title + " : " + placement.getTitle();
		}

		// start the response
		String siteType = null;
		String siteSkin = null;
		if (site != null)
		{
			siteType = calcSiteType(siteId);
			siteSkin = site.getSkin();
		}

		PortalRenderContext rcontext = startPageContext(siteType, title, siteSkin, req);

		// Make the top Url where the "top" url is
		String portalTopUrl = Web.serverUrl(req)
				+ ServerConfigurationService.getString("portalPath") + "/";
		if (prefix != null) portalTopUrl = portalTopUrl + prefix + "/";

		rcontext.put("portalTopUrl", portalTopUrl);
		rcontext.put("loggedIn", Boolean.valueOf(session.getUserId() != null));

		if (placement != null)
		{
			Map m = includeTool(res, req, placement);
			if (m != null) rcontext.put("currentPlacement", m);
		}

		boolean loggedIn = session.getUserId() != null;

		if (site != null)
		{
			SiteView siteView = siteHelper.getSitesView(SiteView.View.CURRENT_SITE_VIEW, req, session, siteId );
			siteView.setPrefix(prefix);
			siteView.setResetTools(resetTools);
			siteView.setToolContextPath(toolContextPath);
			siteView.setIncludeSummary(includeSummary);
			siteView.setDoPages(doPages);
			if ( !siteView.isEmpty() ) {
				rcontext.put("currentSite", siteView.getRenderContextObject());
			}
		}

		//List l = siteHelper.convertSitesToMaps(req, mySites, prefix, siteId, myWorkspaceSiteId,
		//		includeSummary, expandSite, resetTools, doPages, toolContextPath,
		//		loggedIn);

		SiteView siteView = siteHelper.getSitesView(SiteView.View.ALL_SITES_VIEW, req, session, siteId );
		siteView.setPrefix(prefix);
		siteView.setResetTools(resetTools);
		siteView.setToolContextPath(toolContextPath);
		siteView.setIncludeSummary(includeSummary);
		siteView.setDoPages(doPages);
		siteView.setExpandSite(expandSite);
		
		rcontext.put("allSites", siteView.getRenderContextObject());

		includeLogin(rcontext, req, session);
		includeBottom(rcontext);

		return rcontext;
	}

	public boolean isPortletPlacement(Placement placement)
	{
		if (placement == null) return false;
		Tool t = placement.getTool();
		Properties toolProps = t.getFinalConfig();
		if (toolProps == null) return false;
		String portletContext = toolProps
				.getProperty(PortalService.TOOL_PORTLET_CONTEXT_PATH);
		return (portletContext != null);
	}

	public Map includeTool(HttpServletResponse res, HttpServletRequest req,
			ToolConfiguration placement) throws IOException
	{

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(placement.getToolId());
		if (tool == null)
		{
			// doError(req, res, session);
			return null;
		}

		// Get the Site - we could change the API call in the future to
		// pass site in, but that would break portals that extend Charon
		// so for now we simply look this up here.
		String siteId = placement.getSiteId();
		Site site = null;
		try
		{
			site = SiteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			site = null;
		}
		catch (PermissionException e)
		{
			site = null;
		}

		// FIXME: This does not look absolutely right,
		// this appears to say, reset all tools on the page since there
		// is no filtering of the tool that is bing reset, surely there
		// should be a check which tool is being reset, rather than all
		// tools on the page.
		// let the tool do some the work (include) (see note above)

		String toolUrl = ServerConfigurationService.getToolUrl() + "/"
				+ Web.escapeUrl(placement.getId()) + "/";
		String titleString = Web.escapeHtml(placement.getTitle());
		String toolId = Web.escapeHtml(placement.getToolId());

		// Reset the tool state if requested
		if ("true".equals(req.getParameter(portalService.getResetStateParam()))
				|| "true".equals(portalService.getResetState()))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placement.getId());
			ts.clearAttributes();
		}

		// emit title information

		// for the reset button
		boolean showResetButton = !"false".equals(placement.getConfig().getProperty(
				Portal.TOOLCONFIG_SHOW_RESET_BUTTON));

		String resetActionUrl = PortalStringUtil.replaceFirst(toolUrl, "/tool/",
				"/tool-reset/")
				+ "?panel=Main";

		// Reset is different for Portlets
		if (isPortletPlacement(placement))
		{
			resetActionUrl = Web.serverUrl(req)
					+ ServerConfigurationService.getString("portalPath")
					+ req.getPathInfo() + "?sakai.state.reset=true";
		}

		// for the help button
		// get the help document ID from the tool config (tool registration
		// usually).
		// The help document ID defaults to the tool ID
		boolean helpEnabledGlobally = ServerConfigurationService.getBoolean(
				"display.help.icon", true);
		boolean helpEnabledInTool = !"false".equals(placement.getConfig().getProperty(
				Portal.TOOLCONFIG_SHOW_HELP_BUTTON));
		boolean showHelpButton = helpEnabledGlobally && helpEnabledInTool;

		String helpActionUrl = "";
		if (showHelpButton)
		{
			String helpDocUrl = placement.getConfig().getProperty(
					Portal.TOOLCONFIG_HELP_DOCUMENT_URL);
			String helpDocId = placement.getConfig().getProperty(
					Portal.TOOLCONFIG_HELP_DOCUMENT_ID);
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

		Map<String, Object> toolMap = new HashMap<String, Object>();
		RenderResult result = ToolRenderService.render(this,placement, req, res,
				getServletContext());

		if (result.getJSR168HelpUrl() != null)
		{
			toolMap.put("toolJSR168Help", Web.serverUrl(req) + result.getJSR168HelpUrl());
		}

		// Must have site.upd to see the Edit button
		if (result.getJSR168EditUrl() != null && site != null)
		{
			if (SecurityService.unlock(SiteService.SECURE_UPDATE_SITE, site
					.getReference()))
			{
				String editUrl = Web.serverUrl(req) + result.getJSR168EditUrl();
				toolMap.put("toolJSR168Edit", editUrl);
				toolMap.put("toolJSR168EditEncode", URLUtils.encodeUrl(editUrl));
			}
		}

		toolMap.put("toolRenderResult", result);
		toolMap.put("hasRenderResult", Boolean.valueOf(true));
		toolMap.put("toolUrl", toolUrl);
		if (isPortletPlacement(placement))
		{
			// If the tool has requested it, pre-fetch render output.
			String doPreFetch  = placement.getConfig().getProperty(Portal.JSR_168_PRE_RENDER);
			if ( "true".equals(doPreFetch) ) 
			{
				result.getContent();
			}
                
			toolMap.put("toolPlacementIDJS", "_self");
			toolMap.put("isPortletPlacement", Boolean.TRUE);
		}
		else
		{
			toolMap.put("toolPlacementIDJS", Web.escapeJavascript("Main"
					+ placement.getId()));
		}
		toolMap.put("toolResetActionUrl", resetActionUrl);
		toolMap.put("toolResetActionUrlEncode", URLUtils.encodeUrl(resetActionUrl));
		toolMap.put("toolTitle", titleString);
		toolMap.put("toolTitleEncode", URLUtils.encodeUrl(titleString));
		toolMap.put("toolShowResetButton", Boolean.valueOf(showResetButton));
		toolMap.put("toolShowHelpButton", Boolean.valueOf(showHelpButton));
		toolMap.put("toolHelpActionUrl", helpActionUrl);
		toolMap.put("toolId", toolId);
		return toolMap;
	}


	/**
	 * Respond to navigation / access requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws javax.servlet.ServletException.
	 * @throws java.io.IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{

		int stat = PortalHandler.NEXT;
		try
		{
			basicAuth.doLogin(req);
			if (!ToolRenderService.preprocess(this,req, res, getServletContext()))
			{
				return;
			}

			// Check to see if the pre-process step has redirected us - if so,
			// our work is done here - we will likely come back again to finish
			// our
			// work.
			if (res.isCommitted())
			{
				return;
			}

			// get the Sakai session
			Session session = SessionManager.getCurrentSession();

			// recognize what to do from the path
			String option = req.getPathInfo();

			String[] parts = {};

			if (option == null || "/".equals(option))
			{
				// Use the default handler prefix
				parts = new String[]{"", handlerPrefix};
			}
			else
			{
				//get the parts (the first will be "")
				parts = option.split("/");
			}

			Map<String, PortalHandler> handlerMap = portalService.getHandlerMap(this);
			PortalHandler ph = handlerMap.get(parts[1]);
			if (ph != null)
			{
				stat = ph.doGet(parts, req, res, session);
				if (res.isCommitted())
				{
					if (stat != PortalHandler.RESET_DONE)
					{
						portalService.setResetState(null);
					}
					return;
				}
			}
			if (stat == PortalHandler.NEXT)
			{

				List<PortalHandler> urlHandlers;
				for (Iterator<PortalHandler> i = handlerMap.values().iterator(); i
						.hasNext();)
				{
					ph = i.next();
					stat = ph.doGet(parts, req, res, session);
					if (res.isCommitted())
					{
						if (stat != PortalHandler.RESET_DONE)
						{
							portalService.setResetState(null);
						}
						return;
					}
					// this should be
					if (stat != PortalHandler.NEXT)
					{
						break;
					}
				}
			}
			if (stat == PortalHandler.NEXT)
			{
				doError(req, res, session, Portal.ERROR_SITE);
			}

		}
		catch (Throwable t)
		{
			doThrowableError(req, res, t);
		}

		// Make sure to clear any reset State at the end of the request unless
		// we *just* set it
		if (stat != PortalHandler.RESET_DONE)
		{
			portalService.setResetState(null);
		}
	}

	public void doLogin(HttpServletRequest req, HttpServletResponse res, Session session,
			String returnPath, boolean skipContainer) throws ToolException
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
			String returnUrl = Web.returnUrl(req, returnPath);
			if (req.getQueryString() != null )
				returnUrl += "?"+req.getQueryString();
			session.setAttribute(Tool.HELPER_DONE_URL, returnUrl);
		}

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");

		// to skip container auth for this one, forcing things to be handled
		// internaly, set the "extreme" login path
		
		String loginPath = (!forceContainer  && skipContainer ? "/xlogin" : "/relogin");

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
	public void doLogout(HttpServletRequest req, HttpServletResponse res,
			Session session, String returnPath) throws ToolException
	{
		String loggedOutUrl = ServerConfigurationService.getLoggedOutUrl();
		if ( returnPath != null ) 
		{
			loggedOutUrl = loggedOutUrl + returnPath;
		}
		session.setAttribute(Tool.HELPER_DONE_URL, loggedOutUrl);

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/logout";
		tool.help(req, res, context, "/logout");
	}

        /** Set up the WURFL objects - to use most classes will
         *  extend the register method and call this setup.
         */
        public void setupWURFL()
        {
		// Only do this once
		if ( wurflLoaded ) return;
		wurflLoaded = true;
                try {
                        ObjectsManager.initFromWebApplication(getServletContext());
                        uam = ObjectsManager.getUAManagerInstance();
                        cm = ObjectsManager.getCapabilityMatrixInstance();
                        if ( cm == null ) uam = null;
                        if ( uam == null ) cm = null;
                        if ( cm == null )
                        {
                                M_log.info("WURFL Initialization failed - PDA support may be limited");
                        }
                        else
                        {
                                M_log.info("WURFL Initialization cm="+cm+" uam="+uam);
                        }
                }
                catch (Exception e)
                {
                        M_log.info("WURFL Initialization failed - PDA support may be limited "+e);
                }
        }

        // Read the Wireless Universal Resource File and determine the display size
        // http://wurfl.sourceforge.net/
        public void setupMobileDevice(HttpServletRequest req, PortalRenderContext rcontext)
        {
		setupWURFL();
                if ( cm == null || uam == null ) return;

                String userAgent = req.getHeader("user-agent");
                String device = uam.getDeviceIDFromUALoose(userAgent);
		// System.out.println("device="+device+" agent="+userAgent);

                // Not a mobile device
                if ( device == null || device.length() < 1 || device.startsWith("generic") ) return;
                rcontext.put("wurflDevice",device);

                // Check to see if we have too few columns of text
                String columns = cm.getCapabilityForDevice(device,"columns");
                {
                        int icol = -1;
                        try { icol = Integer.parseInt(columns); } catch (Throwable t) { icol = -1; }
                        if ( icol > 1 && icol < 50 )
                        {
                                rcontext.put("wurflSmallDisplay",Boolean.TRUE);
                                return;
                        }
                }
                // Check if we have too few pixels
                String width = cm.getCapabilityForDevice(device,"resolution_width");
                if ( width != null && width.length() > 1 )
                {
                        int iwidth = -1;
                        try { iwidth = Integer.parseInt(width); } catch (Throwable t) { iwidth = -1; }
                        if ( iwidth > 1 && iwidth < 400 )
                        {
                                rcontext.put("wurflSmallDisplay",Boolean.TRUE);
                                return;
                        }
                }
        }


	public PortalRenderContext startPageContext(String siteType, String title,
			String skin, HttpServletRequest request)
	{
		PortalRenderEngine rengine = portalService
				.getRenderEngine(portalContext, request);
		PortalRenderContext rcontext = rengine.newRenderContext(request);

		if (skin == null)
		{
			skin = ServerConfigurationService.getString("skin.default");
		}
		String skinRepo = ServerConfigurationService.getString("skin.repo");

		rcontext.put("pageSkinRepo", skinRepo);
		rcontext.put("pageSkin", skin);
		rcontext.put("pageTitle", Web.escapeHtml(title));
		rcontext.put("pageScriptPath", getScriptPath());
		rcontext.put("pageTop", Boolean.valueOf(true));
		rcontext.put("rloader", rloader);
		rcontext.put("browser", new BrowserDetector(request));
		
		Session s = SessionManager.getCurrentSession();
		rcontext.put("loggedIn", Boolean.valueOf(s.getUserId() != null));
		
		// rcontext.put("sitHelp", Web.escapeHtml(rb.getString("sit_help")));
		// rcontext.put("sitReset", Web.escapeHtml(rb.getString("sit_reset")));

		if (siteType != null && siteType.length() > 0)
		{
			siteType = "class=\"" + siteType + "\"";
		}
		else
		{
			siteType = "";
		}
		rcontext.put("pageSiteType", siteType);
		rcontext.put("toolParamResetState", portalService.getResetStateParam());

		return rcontext;
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
		int stat = PortalHandler.NEXT;
		try
		{
			basicAuth.doLogin(req);
			if (!ToolRenderService.preprocess(this,req, res, getServletContext()))
			{
				// System.err.println("POST FAILED, REDIRECT ?");
				return;
			}

			// Check to see if the pre-process step has redirected us - if so,
			// our work is done here - we will likely come back again to finish
			// our
			// work. T

			if (res.isCommitted())
			{
				return;
			}

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

			Map<String, PortalHandler> handlerMap = portalService.getHandlerMap(this);

			PortalHandler ph = handlerMap.get(parts[1]);
			if (ph != null)
			{
				stat = ph.doPost(parts, req, res, session);
				if (res.isCommitted())
				{
					return;
				}
			}
			if (stat == PortalHandler.NEXT)
			{

				List<PortalHandler> urlHandlers;
				for (Iterator<PortalHandler> i = handlerMap.values().iterator(); i
						.hasNext();)
				{
					ph = i.next();
					stat = ph.doPost(parts, req, res, session);
					if (res.isCommitted())
					{
						return;
					}
					// this should be
					if (stat != PortalHandler.NEXT)
					{
						break;
					}

				}
			}
			if (stat == PortalHandler.NEXT)
			{
				doError(req, res, session, Portal.ERROR_SITE);
			}

		}
		catch (Throwable t)
		{
			doThrowableError(req, res, t);
		}
	}

	/*
	 * Checks to see which form of tool or page placement we have. The normal
	 * placement is a GUID. However when the parameter sakai.site is added to
	 * the request, the placement can be of the form sakai.resources. This
	 * routine determines which form of the placement id, and if this is the
	 * second type, performs the lookup and returns the GUID of the placement.
	 * If we cannot resolve the placement, we simply return the passed in
	 * placement ID. If we cannot visit the site, we send the user to login
	 * processing and return null to the caller.
	 */

	public String getPlacement(HttpServletRequest req, HttpServletResponse res,
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

	public void setupForward(HttpServletRequest req, HttpServletResponse res,
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

		portalService.getRenderEngine(portalContext, req).setupForward(req, res, p, skin);
	}

	/**
	 * Forward to the tool - but first setup JavaScript/CSS etc that the tool
	 * will render
	 */
	public void forwardTool(ActiveTool tool, HttpServletRequest req,
			HttpServletResponse res, Placement p, String skin, String toolContextPath,
			String toolPathInfo) throws ToolException
	{

		// if there is a stored request state, and path, extract that from the
		// session and reinstance it

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
				M_log.debug("Restoring StoredState [" + ss + "]");
				HttpServletRequest sreq = ss.getRequest(req);
				Placement splacement = ss.getPlacement();
				String stoolContext = ss.getToolContextPath();
				String stoolPathInfo = ss.getToolPathInfo();
				ActiveTool stool = ActiveToolManager.getActiveTool(p.getToolId());
				String sskin = ss.getSkin();
				setupForward(sreq, res, splacement, sskin);
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				stool.forward(sreq, res, splacement, stoolContext, stoolPathInfo);
				// this is correct as we have checked the context path of the
				// tool
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

	public void forwardPortal(ActiveTool tool, HttpServletRequest req,
			HttpServletResponse res, ToolConfiguration p, String skin,
			String toolContextPath, String toolPathInfo) throws ToolException,
			IOException
	{

		// if there is a stored request state, and path, extract that from the
		// session and reinstance it

		// generate the forward to the tool page placement
		String portalPlacementUrl = "/portal" + getPortalPageUrl(p);
		res.sendRedirect(portalPlacementUrl);
		return;

	}

	public String getPortalPageUrl(ToolConfiguration p)
	{
		SitePage sitePage = p.getContainingPage();
		String page = getSiteHelper().lookupPageToAlias(p.getSiteId(), sitePage);
		if (page == null)
		{
			// Fall back to default of using the page Id.
			page = p.getPageId();
		}
		return "/site/" + p.getSiteId() + "/page/" + page;
	}

	protected String getScriptPath()
	{
		return "/library/js/";
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

	public void includeBottom(PortalRenderContext rcontext)
	{
		if (rcontext.uses(INCLUDE_BOTTOM))
		{
			rcontext.put("pagepopup", false);

			String copyright = ServerConfigurationService
					.getString("bottom.copyrighttext");
			String service = ServerConfigurationService.getString("ui.service", "Sakai");
			String serviceVersion = ServerConfigurationService.getString(
					"version.service", "?");
			String sakaiVersion = ServerConfigurationService.getString("version.sakai",
					"?");
			String server = ServerConfigurationService.getServerId();
			String[] bottomNav = ServerConfigurationService.getStrings("bottomnav");
			String[] poweredByUrl = ServerConfigurationService.getStrings("powered.url");
			String[] poweredByImage = ServerConfigurationService
					.getStrings("powered.img");
			String[] poweredByAltText = ServerConfigurationService
					.getStrings("powered.alt");

			{
				List<Object> l = new ArrayList<Object>();
				if ((bottomNav != null) && (bottomNav.length > 0))
				{
					for (int i = 0; i < bottomNav.length; i++)
					{
						l.add(bottomNav[i]);
					}
				}
				rcontext.put("bottomNav", l);
			}

			// rcontext.put("bottomNavSitNewWindow",
			// Web.escapeHtml(rb.getString("site_newwindow")));

			if ((poweredByUrl != null) && (poweredByImage != null)
					&& (poweredByAltText != null)
					&& (poweredByUrl.length == poweredByImage.length)
					&& (poweredByUrl.length == poweredByAltText.length))
			{
				{
					List<Object> l = new ArrayList<Object>();
					for (int i = 0; i < poweredByUrl.length; i++)
					{
						Map<String, Object> m = new HashMap<String, Object>();
						m.put("poweredByUrl", poweredByUrl[i]);
						m.put("poweredByImage", poweredByImage[i]);
						m.put("poweredByAltText", poweredByAltText[i]);
						l.add(m);
					}
					rcontext.put("bottomNavPoweredBy", l);

				}
			}
			else
			{
				List<Object> l = new ArrayList<Object>();
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("poweredByUrl", "http://sakaiproject.org");
				m.put("poweredByImage", "/library/image/sakai_powered.gif");
				m.put("poweredByAltText", "Powered by Sakai");
				l.add(m);
				rcontext.put("bottomNavPoweredBy", l);
			}

			rcontext.put("bottomNavService", service);
			rcontext.put("bottomNavCopyright", copyright);
			rcontext.put("bottomNavServiceVersion", serviceVersion);
			rcontext.put("bottomNavSakaiVersion", sakaiVersion);
			rcontext.put("bottomNavServer", server);
		}
	}

	public void includeLogin(PortalRenderContext rcontext, HttpServletRequest req,
			Session session)
	{
		if (rcontext.uses(INCLUDE_LOGIN))
		{

			// for the main login/out link
			String logInOutUrl = Web.serverUrl(req);
			String message = null;
			String image1 = null;

			// for a possible second link
			String logInOutUrl2 = null;
			String message2 = null;
			String image2 = null;

			// for showing user display name and id next to logout (SAK-10492)
			String loginUserDispName = null;
			String loginUserDispId = null;
			boolean displayUserloginInfo = ServerConfigurationService.
					getBoolean("display.userlogin.info", false);

			// check for the top.login (where the login fields are present
			// instead
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
					if (message == null) message = rloader.getString("log.login");

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
				logInOutUrl += ServerConfigurationService.getString("portalPath")
						+ "/logout";

				// get current user display id and name
				if (displayUserloginInfo)
				{
					User thisUser = UserDirectoryService.getCurrentUser();
					loginUserDispId = thisUser.getDisplayId();
					loginUserDispName = thisUser.getDisplayName();
				}

				// check for a logout text override
				message = StringUtil.trimToNull(ServerConfigurationService
						.getString("logout.text"));
				if (message == null) message = rloader.getString("sit_log");

				// check for an image for the logout
				image1 = StringUtil.trimToNull(ServerConfigurationService
						.getString("logout.icon"));

				// since we are doing logout, cancel top.login
				topLogin = false;
			}
			rcontext.put("loginTopLogin", Boolean.valueOf(topLogin));

			if (!topLogin)
			{

				rcontext.put("loginLogInOutUrl", logInOutUrl);
				rcontext.put("loginMessage", message);
				rcontext.put("loginImage1", image1);
				rcontext.put("loginHasImage1", Boolean.valueOf(image1 != null));
				rcontext.put("loginLogInOutUrl2", logInOutUrl2);
				rcontext.put("loginHasLogInOutUrl2", Boolean
						.valueOf(logInOutUrl2 != null));
				rcontext.put("loginMessage2", message2);
				rcontext.put("loginImage2", image2);
				rcontext.put("loginHasImage2", Boolean.valueOf(image2 != null));
				// put out the links version

				// else put out the fields that will send to the login interface
			}
			else
			{
				// find the login tool
				Tool loginTool = ToolManager.getTool("sakai.login");
				String eidWording = null;
				String pwWording = null;
				eidWording = StringUtil.trimToNull(rloader.getString("log.userid"));
				pwWording = StringUtil.trimToNull(rloader.getString("log.pass"));

				if (eidWording == null) eidWording = "eid";
				if (pwWording == null) pwWording = "pw";
				String loginWording = rloader.getString("log.login");

				rcontext.put("loginPortalPath", ServerConfigurationService
						.getString("portalPath"));
				rcontext.put("loginEidWording", eidWording);
				rcontext.put("loginPwWording", pwWording);
				rcontext.put("loginWording", loginWording);

				// setup for the redirect after login
				session.setAttribute(Tool.HELPER_DONE_URL, ServerConfigurationService
						.getPortalUrl());
			}

			if (displayUserloginInfo)
			{
				rcontext.put("loginUserDispName", loginUserDispName);
				rcontext.put("loginUserDispId", loginUserDispId);
			}
			rcontext.put("displayUserloginInfo", displayUserloginInfo && loginUserDispId != null);
		}
	}



	/**
	 * @param rcontext
	 * @param res
	 * @param req
	 * @param session
	 * @param site
	 * @param page
	 * @param toolContextPath
	 * @param portalPrefix
	 * @return
	 * @throws IOException
	 */
	public void includeWorksite(PortalRenderContext rcontext, HttpServletResponse res,
			HttpServletRequest req, Session session, Site site, SitePage page,
			String toolContextPath, String portalPrefix) throws IOException
	{
		 worksiteHandler.includeWorksite(rcontext, res, req, session, site, page,
				toolContextPath, portalPrefix);
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
		portalContext = config.getInitParameter("portal.context");
		if (portalContext == null || portalContext.length() == 0)
		{
			portalContext = DEFAULT_PORTAL_CONTEXT;
		}
		
		boolean findPageAliases = ServerConfigurationService.getBoolean("portal.use.page.aliases", false);
		
		siteHelper = new PortalSiteHelperImpl(this, findPageAliases);
		
		portalService = org.sakaiproject.portal.api.cover.PortalService.getInstance();
		M_log.info("init()");
		
		forceContainer = ServerConfigurationService.getBoolean("login.use.xlogin.to.relogin", true);
		
		handlerPrefix = ServerConfigurationService.getString("portal.handler.default", "site");

		basicAuth = new BasicAuth();
		basicAuth.init();

		enableDirect = portalService.isEnableDirect();
		// do this before adding handlers to prevent handlers registering 2
		// times.
		// if the handlers were already there they will be re-registered,
		// but when they are added again, they will be replaced.
		// warning messages will appear, but the end state will be the same.
		portalService.addPortal(this);

		galleryHandler = new GalleryHandler();
		worksiteHandler = new WorksiteHandler();
		siteHandler = new SiteHandler();

		addHandler(siteHandler);
		addHandler(new SiteResetHandler());

		addHandler(new ToolHandler());
		addHandler(new ToolResetHandler());
		addHandler(new PageHandler());
		addHandler(worksiteHandler);
		addHandler(new WorksiteResetHandler());
		addHandler(new RssHandler());
		addHandler(new PDAHandler());
		addHandler(new AtomHandler());
		addHandler(new OpmlHandler());
		addHandler(galleryHandler);
		addHandler(new GalleryResetHandler());
		addHandler(new NavLoginHandler());
		addHandler(new PresenceHandler());
		addHandler(new HelpHandler());
		addHandler(new ReLoginHandler());
		addHandler(new LoginHandler());
		addHandler(new XLoginHandler());
		addHandler(new LogoutHandler());
		addHandler(new ErrorDoneHandler());
		addHandler(new ErrorReportHandler());
		addHandler(new StaticStylesHandler());
		addHandler(new StaticScriptsHandler());
		addHandler(new DirectToolHandler());
		addHandler(new RoleSwitchHandler());
		addHandler(new RoleSwitchOutHandler());
		addHandler(new TimeoutDialogHandler());
	}

	/**
	 * Register a handler for a URL stub
	 * 
	 * @param handler
	 */
	private void addHandler(PortalHandler handler)
	{
		portalService.addHandler(this, handler);
	}

	private void removeHandler(String urlFragment)
	{
		portalService.removeHandler(this, urlFragment);
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
	 * @param rcontext
	 *        The print writer
	 * @param html
	 *        If true, output in HTML, else in text.
	 */
	protected void showSession(PortalRenderContext rcontext, boolean html)
	{
		// get the current user session information
		Session s = SessionManager.getCurrentSession();
		rcontext.put("sessionSession", s);
		ToolSession ts = SessionManager.getCurrentToolSession();
		rcontext.put("sessionToolSession", ts);
	}

	public void sendResponse(PortalRenderContext rcontext, HttpServletResponse res,
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
		res
				.addHeader("Cache-Control",
						"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// get the writer
		PrintWriter out = res.getWriter();

		try
		{
			PortalRenderEngine rengine = rcontext.getRenderEngine();
			rengine.render(template, rcontext, out);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to render template ", e);
		}

	}

	/**
	 * Returns the type ("course", "project", "workspace", "mySpecialSiteType",
	 * etc) of the given site; special handling of returning "workspace" for
	 * user workspace sites. This method is tightly coupled to site skinning.
	 */
	public String calcSiteType(String siteId)
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

	private void logXEntry()
	{
		Exception e = new Exception();
		StackTraceElement se = e.getStackTrace()[1];
		M_log.info("Log marker " + se.getMethodName() + ":" + se.getFileName() + ":"
				+ se.getLineNumber());
	}

	/**
	 * Check for any just expired sessions and redirect
	 * 
	 * @return true if we redirected, false if not
	 */
	public boolean redirectIfLoggedOut(HttpServletResponse res) throws IOException
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
		PortalRenderContext rcontext = startPageContext("", null, null, null);
		rcontext.put("redirectUrl", url);
		sendResponse(rcontext, res, "portal-redirect", null);
	}

	/**
	 * Compute the string that will identify the user site for this user - use
	 * the EID if possible
	 * 
	 * @param userId
	 *        The user id
	 * @return The site "ID" but based on the user EID
	 */
	public String getUserEidBasedSiteId(String userId)
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.Portal#getPageFilter()
	 */
	public PageFilter getPageFilter()
	{
		return pageFilter;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.Portal#setPageFilter(org.sakaiproject.portal.api.PageFilter)
	 */
	public void setPageFilter(PageFilter pageFilter)
	{
		this.pageFilter = pageFilter;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.Portal#getSiteHelper()
	 */
	public PortalSiteHelper getSiteHelper()
	{
		return this.siteHelper;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.Portal#getSiteNeighbourhoodService()
	 */
	public SiteNeighbourhoodService getSiteNeighbourhoodService()
	{
		return portalService.getSiteNeighbourhoodService();
	}
	

}
