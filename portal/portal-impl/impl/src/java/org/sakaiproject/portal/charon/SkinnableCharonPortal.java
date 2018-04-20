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
package org.sakaiproject.portal.charon;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.portal.api.Editor;
import org.sakaiproject.portal.api.PageFilter;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalChatPermittedHelper;
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
import org.sakaiproject.portal.charon.handlers.FavoritesHandler;
import org.sakaiproject.portal.charon.handlers.GenerateBugReportHandler;
import org.sakaiproject.portal.charon.handlers.HelpHandler;
import org.sakaiproject.portal.charon.handlers.JoinHandler;
import org.sakaiproject.portal.charon.handlers.LoginHandler;
import org.sakaiproject.portal.charon.handlers.LogoutHandler;
import org.sakaiproject.portal.charon.handlers.NavLoginHandler;
import org.sakaiproject.portal.charon.handlers.OpmlHandler;
import org.sakaiproject.portal.charon.handlers.PageHandler;
import org.sakaiproject.portal.charon.handlers.PageResetHandler;
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
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.portal.util.ErrorReporter;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.portal.util.ToolURLManagerImpl;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.ToolURL;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.EditorConfiguration;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import lombok.extern.slf4j.Slf4j;

/**
 * <p/> Charon is the Sakai Site based portal.
 * </p>
 * 
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
@SuppressWarnings("deprecation")
@Slf4j
public class SkinnableCharonPortal extends HttpServlet implements Portal
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2645929710236293089L;

	/**
	 * messages.
	 */
	private static ResourceLoader rloader = new ResourceLoader("sitenav");
	private static ResourceLoader cmLoader = new Resource().getLoader("org.sakaiproject.portal.api.PortalService", "connection-manager");

	/**
	 * Parameter value to indicate to look up a tool ID within a site
	 */
	protected static final String PARAM_SAKAI_SITE = "sakai.site";

	private BasicAuth basicAuth = null;

	private boolean enableDirect = false;

	private PortalService portalService;
	
	private SecurityService securityService = null;

	//Get user preferences
	private PreferencesService preferencesService;

	/**
	 * Keyword to look for in sakai.properties copyright message to replace
	 * for the server's time's year for auto-update of Copyright end date
	 */
	private static final String SERVER_COPYRIGHT_CURRENT_YEAR_KEYWORD = "currentYearFromServer";

	/**
	 * Chat helper.
	 */
	private PortalChatPermittedHelper chatHelper;

	private static final String PADDING = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

	private static final String INCLUDE_BOTTOM = "include-bottom";

	private static final String INCLUDE_LOGIN = "include-login";

	private static final String INCLUDE_TITLE = "include-title";

    // SAK-22384
    private static final String MATHJAX_ENABLED = "mathJaxAllowed";
    private static final String MATHJAX_SRC_PATH_SAKAI_PROP = "portal.mathjax.src.path";
    private static final String MATHJAX_ENABLED_SAKAI_PROP = "portal.mathjax.enabled";
    private static final boolean ENABLED_SAKAI_PROP_DEFAULT = true;
    private static final String MATHJAX_SRC_PATH = ServerConfigurationService.getString(MATHJAX_SRC_PATH_SAKAI_PROP);
    private static final boolean MATHJAX_ENABLED_AT_SYSTEM_LEVEL = ServerConfigurationService.getBoolean(MATHJAX_ENABLED_SAKAI_PROP, ENABLED_SAKAI_PROP_DEFAULT) && !MATHJAX_SRC_PATH.trim().isEmpty();
    
	private PortalSiteHelper siteHelper = null;


	// private HashMap<String, PortalHandler> handlerMap = new HashMap<String,
	// PortalHandler>();

	private String gatewaySiteUrl;

	private WorksiteHandler worksiteHandler;

	private SiteHandler siteHandler;

	private String portalContext;

	private String PROP_PARENT_ID = SiteService.PROP_PARENT_ID;
	// 2.3 back port
	// public String String PROP_PARENT_ID = "sakai:parent-id";

	private String PROP_SHOW_SUBSITES  = SiteService.PROP_SHOW_SUBSITES ;
	
	// 2.3 back port
	// public String PROP_SHOW_SUBSITES = "sakai:show-subsites";
	
	private boolean forceContainer = false;

	private boolean sakaiTutorialEnabled = true;
	
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

	// define string that identifies this as the logged in users' my workspace
	private String myWorkspaceSiteId = "~";

	public String getPortalContext()
	{
		return portalContext;
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		log.info("destroy()");
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
				// This preseves the "bad" origin site ID.
				String[] parts = getParts(req);
				if (parts.length >= 3) {
					String siteId = parts[2];
					ThreadLocalManager.set(PortalService.SAKAI_PORTAL_ORIGINAL_SITEID, siteId);
				}
				siteHandler.doGet(parts, req, res, session, "!error");
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
		String title = ServerConfigurationService.getString("ui.service","Sakai") + " : Portal";

		// start the response
		PortalRenderContext rcontext = startPageContext("", title, null, req, null);

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
		log.debug("Checking subsite pref:{} pref={} show={}", site.getTitle(), pref, showSub);
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
			boolean showSubsitesAsFlyout = ServerConfigurationService.getBoolean("portal.showSubsitesAsFlyout",true);
			rcontext.put("showSubsitesAsFlyout", showSubsitesAsFlyout);
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


		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service","Sakai");
		if (site != null)
		{
			// SAK-29138
			title = title + ":" + siteHelper.getUserSpecificSiteTitle( site, false );
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

		PortalRenderContext rcontext = startPageContext(siteType, title, siteSkin, req, site);

		// Make the top Url where the "top" url is
		String portalTopUrl = Web.serverUrl(req)
		+ ServerConfigurationService.getString("portalPath") + "/";
		if (prefix != null) portalTopUrl = portalTopUrl + prefix + "/";

		rcontext.put("portalTopUrl", portalTopUrl);
		rcontext.put("loggedIn", StringUtils.isNotBlank(session.getUserId()));
		rcontext.put("siteId", siteId);

		if (placement != null)
		{
			Map m = includeTool(res, req, placement);
			if (m != null) rcontext.put("currentPlacement", m);
		}

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
		return ToolUtils.isPortletPlacement(placement);
	}

	public Map includeTool(HttpServletResponse res, HttpServletRequest req,
			ToolConfiguration placement) throws IOException
	{
		boolean toolInline = "true".equals(ThreadLocalManager.get("sakai:inline-tool"));		
		return includeTool(res, req, placement, toolInline);
	}

	// This will be called twice in the buffered scenario since we need to set
	// the session for neo tools with the sessio reset, helpurl and reseturl
	@Override
	@SuppressWarnings("unchecked")
	public Map includeTool(HttpServletResponse res, HttpServletRequest req,
			ToolConfiguration placement, boolean toolInline) throws IOException {
		
		RenderResult renderResult = null;
		if(!toolInline) {
			// if not already inlined, allow a final chance for a tool to be inlined, based on its tool configuration
			// set renderInline = true to enable this, in the tool config
			renderResult = this.getInlineRenderingForTool(res, req, placement);
			if(renderResult != null) {
				log.debug("Using buffered content rendering");
				toolInline = true;
			}
		}
		
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

		// emit title information
		String titleString = Web.escapeHtml(placement.getTitle());
		String toolId = Web.escapeHtml(placement.getToolId());

		// for the reset button
		String toolUrl = ServerConfigurationService.getToolUrl() + "/"
		   + Web.escapeUrl(placement.getId()) + "/";
		log.debug("includeTool toolInline={} toolUrl={}", toolInline, toolUrl);

		// Reset is different (and awesome) when inlining
		if ( toolInline ) {
			String newUrl = ToolUtils.getPageUrlForTool(req, site, placement);
			if ( newUrl != null ) toolUrl = newUrl;
		}

		// Reset the tool state if requested
		if (portalService.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placement.getId());
			ts.clearAttributes();
			portalService.setResetState(null);
			log.debug("includeTool state reset");
		}

		boolean showResetButton = !"false".equals(placement.getConfig().getProperty(
				Portal.TOOLCONFIG_SHOW_RESET_BUTTON));

		String resetActionUrl = PortalStringUtil.replaceFirst(toolUrl, "/tool/", "/tool-reset/");
		log.debug("includeTool resetActionUrl={}", resetActionUrl);

		// SAK-20462 - Pass through the sakai_action parameter
		String sakaiAction = req.getParameter("sakai_action");
		if ( sakaiAction != null && sakaiAction.matches(".*[\"'<>].*" ) ) sakaiAction=null;
		if ( sakaiAction != null ) resetActionUrl = URLUtils.addParameter(resetActionUrl, "sakai_action", sakaiAction);

		// Reset is different for Portlets
		if (isPortletPlacement(placement))
		{
			resetActionUrl = Web.serverUrl(req)
			+ ServerConfigurationService.getString("portalPath")
			+ URLUtils.getSafePathInfo(req) + "?sakai.state.reset=true";
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
		toolMap.put("toolInline", Boolean.valueOf(toolInline));

		// For JSR-168 portlets - this gets the content
		// For legacy tools, this returns the "<iframe" bit
		// For buffered legacy tools - the buffering is done outside of this
		
		if(renderResult == null) {
			//standard iframe
			log.debug("Using standard iframe rendering");
			renderResult = ToolRenderService.render(this, placement, req, res, getServletContext());
		}
				
		if (renderResult.getJSR168HelpUrl() != null)
		{
			toolMap.put("toolJSR168Help", Web.serverUrl(req) + renderResult.getJSR168HelpUrl());
		}

		// Must have site.upd to see the Edit button
		if (renderResult.getJSR168EditUrl() != null && site != null)
		{
			if (securityService.unlock(SiteService.SECURE_UPDATE_SITE, site
					.getReference()))
			{
				String editUrl = Web.serverUrl(req) + renderResult.getJSR168EditUrl();
				toolMap.put("toolJSR168Edit", editUrl);
				toolMap.put("toolJSR168EditEncode", URLUtils.encodeUrl(editUrl));
			}
		}

		toolMap.put("toolRenderResult", renderResult);
		toolMap.put("hasRenderResult", Boolean.TRUE);
		toolMap.put("toolUrl", toolUrl);
		
		// Allow a tool to suppress the rendering of its title nav. Defaults to false if not specified, and title nav is rendered.
		// Set suppressTitle = true to suppress
		boolean suppressTitle = BooleanUtils.toBoolean(placement.getConfig().getProperty("suppressTitle"));
		toolMap.put("suppressTitle", suppressTitle);

		Session s = SessionManager.getCurrentSession();
		ToolSession ts = s.getToolSession(placement.getId());

		if (isPortletPlacement(placement))
		{
			// If the tool has requested it, pre-fetch render output.
			String doPreFetch  = placement.getConfig().getProperty(Portal.JSR_168_PRE_RENDER);
			if ( ! "false".equals(doPreFetch) ) 
			{
				try {
					renderResult.getContent();
				} catch (Throwable t) {
					ErrorReporter err = new ErrorReporter();
					String str = err.reportFragment(req, res, t);
					renderResult.setContent(str);
				}
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
		toolMap.put("toolInline", Boolean.valueOf(toolInline));
		
		String directToolUrl = ServerConfigurationService.getPortalUrl() + "/" + DirectToolHandler.URL_FRAGMENT +"/" + Web.escapeUrl(placement.getId()) + "/";
		toolMap.put("directToolUrl", directToolUrl);
		
		//props to enable/disable the display on a per tool/placement basis
		//will be displayed if not explicitly disabled in the tool/placement properties
		boolean showDirectToolUrl = !"false".equals(placement.getConfig().getProperty(Portal.TOOL_DIRECTURL_ENABLED_PROP));
		toolMap.put("showDirectToolUrl", showDirectToolUrl);
		
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
			String option = URLUtils.getSafePathInfo(req);

			String[] parts = getParts(req);

			Map<String, PortalHandler> handlerMap = portalService.getHandlerMap(this);

			// begin SAK-19089
			// if not logged in and accessing "/", redirect to gatewaySiteUrl
			if ((gatewaySiteUrl != null) && (option == null || "/".equals(option) ) 
					&& (session.getUserId() == null)) 
			{
				// redirect to gatewaySiteURL 
				res.sendRedirect(gatewaySiteUrl);
				return;
			}
			// end SAK-19089

			// Look up the handler and dispatch
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

				for (Iterator<PortalHandler> i = handlerMap.values().iterator(); i.hasNext();)
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

	private String[] getParts(HttpServletRequest req) {

		String option = URLUtils.getSafePathInfo(req);
		//FindBugs thinks this is not used but is passed to the portal handler
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
		return parts;
	}

	public void doLogin(HttpServletRequest req, HttpServletResponse res, Session session,
			String returnPath, boolean skipContainer) throws ToolException
	{
		try
		{
			if (basicAuth.doAuth(req, res))
			{
				log.debug("BASIC Auth Request Sent to the Browser");
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
		
		// SAK-16370 to allow multiple logout urls
		String loggedOutUrl = null;
		String userType = UserDirectoryService.getCurrentUser().getType();
		if(userType == null) {		
			loggedOutUrl = ServerConfigurationService.getLoggedOutUrl();
		} else {
			loggedOutUrl = ServerConfigurationService.getString("loggedOutUrl." + userType, ServerConfigurationService.getLoggedOutUrl());
		}
		
		if ( returnPath != null ) 
		{
			loggedOutUrl = loggedOutUrl + returnPath;
		}
		session.setAttribute(Tool.HELPER_DONE_URL, loggedOutUrl);

		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/logout";
		tool.help(req, res, context, "/logout");
	}

	public PortalRenderContext startPageContext(String siteType, String title,
			String skin, HttpServletRequest request, Site site)
	{
		PortalRenderEngine rengine = portalService
		.getRenderEngine(portalContext, request);
		PortalRenderContext rcontext = rengine.newRenderContext(request);

		skin = getSkin(skin);
		String skinRepo = ServerConfigurationService.getString("skin.repo");

		rcontext.put("pageSkinRepo", skinRepo);
		rcontext.put("pageSkin", skin);
		rcontext.put("pageTitle", Web.escapeHtml(title));
		rcontext.put("pageScriptPath", PortalUtils.getScriptPath());
		rcontext.put("pageWebjarsPath", PortalUtils.getWebjarsPath());
		rcontext.put("portalCDNPath", PortalUtils.getCDNPath());
		rcontext.put("portalCDNQuery", PortalUtils.getCDNQuery());
		rcontext.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("Portal"));
		rcontext.put("pageTop", Boolean.valueOf(true));
		rcontext.put("rloader", rloader);
		rcontext.put("cmLoader", cmLoader);
		//rcontext.put("browser", new BrowserDetector(request));
		// Allow for inclusion of extra header code via property
		String includeExtraHead = ServerConfigurationService.getString("portal.include.extrahead", "");
		rcontext.put("includeExtraHead",includeExtraHead);

		String universalAnalyticsId =  ServerConfigurationService.getString("portal.google.universal_analytics_id", null);
		if ( universalAnalyticsId != null ) {
			rcontext.put("googleUniversalAnalyticsId", universalAnalyticsId);
		}

		String analyticsId =  ServerConfigurationService.getString("portal.google.analytics_id", null);
		if ( analyticsId != null ) {
			rcontext.put("googleAnalyticsId", analyticsId);
			rcontext.put("googleAnalyticsDomain", 
				ServerConfigurationService.getString("portal.google.analytics_domain"));
			rcontext.put("googleAnalyticsDetail", 
				ServerConfigurationService.getBoolean("portal.google.analytics_detail", false));
		}

		//SAK-29668
		String googleTagManagerContainerId =  ServerConfigurationService.getString("portal.google.tag.manager.container_id", null);
		if ( googleTagManagerContainerId != null ) {
			rcontext.put("googleTagManagerContainerId", googleTagManagerContainerId);
		}

				
		User currentUser = UserDirectoryService.getCurrentUser();
		Role role = site != null && currentUser != null ? site.getUserRole(currentUser.getId()) : null;
		
		Preferences prefs = preferencesService.getPreferences(currentUser.getId());
		String editorType = prefs.getProperties(PreferencesService.EDITOR_PREFS_KEY).getProperty(PreferencesService.EDITOR_PREFS_TYPE);

		rcontext.put("loggedIn", StringUtils.isNotBlank(currentUser.getId()));
		rcontext.put("userId", currentUser.getId());
		rcontext.put("userEid", currentUser.getEid());
		rcontext.put("userType", currentUser.getType());
		rcontext.put("userSiteRole", role != null ? role.getId() : "");
		rcontext.put("editorType", editorType);

		rcontext.put("loggedOutUrl",ServerConfigurationService.getLoggedOutUrl());
		rcontext.put("portalPath",ServerConfigurationService.getPortalUrl());
		rcontext.put("timeoutDialogEnabled",Boolean.valueOf(ServerConfigurationService.getBoolean("timeoutDialogEnabled", true)));
		rcontext.put("timeoutDialogWarningSeconds", Integer.valueOf(ServerConfigurationService.getInt("timeoutDialogWarningSeconds", 600)));
		// rcontext.put("sitHelp", Web.escapeHtml(rb.getString("sit_help")));
		// rcontext.put("sitReset", Web.escapeHtml(rb.getString("sit_reset")));
		
		//SAK-29457 Add warning about cookie use
		String cookieNoticeText = rloader.getFormattedMessage("cookie_notice_text", ServerConfigurationService.getString("portal.cookie.policy.warning.url","/library/content/cookie_policy.html"));
		rcontext.put("cookieNoticeEnabled", ServerConfigurationService.getBoolean("portal.cookie.policy.warning.enabled",false));
		rcontext.put("cookieNoticeText", cookieNoticeText);

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

                // Get the tool header properties
                Properties props = toolHeaderProperties(request, skin, site, null);
                for(Object okey : props.keySet() ) 
                {
                        String key = (String) okey;
			String keyund = key.replace('.','_');
                        rcontext.put(keyund,props.getProperty(key));
                }

		// Copy the minimization preferences to the context
		String enableGAM = ServerConfigurationService.getString("portal.use.global.alert.message","false");
		rcontext.put("portal_use_global_alert_message",Boolean.valueOf( enableGAM ) ) ;
		// how many tools to show in portal pull downs
		rcontext.put("maxToolsInt", Integer.valueOf(ServerConfigurationService.getInt("portal.tool.menu.max", 10)));

		rcontext.put("toolDirectUrlEnabled", ServerConfigurationService.getBoolean("portal.tool.direct.url.enabled", true));
		rcontext.put("toolShortUrlEnabled", ServerConfigurationService.getBoolean("shortenedurl.portal.tool.enabled", true));
		
		//SAK-32224. Ability to disable the animated tool menu by property
		rcontext.put("scrollingToolbarEnabled", ServerConfigurationService.getBoolean("portal.scrolling.toolbar.enabled",true));
		
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
				log.debug("POST FAILED, REDIRECT ?");
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
			String option = URLUtils.getSafePathInfo(req);

			// if missing, we have a stray post
			if ((option == null) || ("/".equals(option)))
			{
				doError(req, res, session, ERROR_SITE);
				return;
			}

			// get the parts (the first will be "")
			String[] parts = option.split("/");


			Map<String, PortalHandler> handlerMap = portalService.getHandlerMap(this);

			// Look up handler and dispatch
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
				for (Iterator<PortalHandler> i = handlerMap.values().iterator(); i.hasNext();)
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
	 *
	 * If the reference is to the magical, indexical MyWorkspace site ('~')
	 * then replace ~ by their Home.  Give them a chance to login
	 * if necessary.
	 */

	public String getPlacement(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, boolean doPage) throws ToolException
	{
		String siteId = req.getParameter(PARAM_SAKAI_SITE);
		if (siteId == null) return placementId; // Standard placement

		// Try to resolve the indexical MyWorkspace reference
		if (myWorkspaceSiteId.equals(siteId)) {
		    // If not logged in then allow login.  You can't go to your workspace if 
		    // you aren't known to the system.
		    if (session.getUserId() == null)
			{
			    doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
			}
		    // If the login was successful lookup the myworkworkspace site.
		    if (session.getUserId() != null) {
			siteId=getUserEidBasedSiteId(session.getUserEid());
		    }
		}

		// find the site, for visiting
		// Sites like the !gateway site allow visits by anonymous
		Site site = null;
		try
		{
			site = getSiteHelper().getSiteVisit(siteId);
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
				doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
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

	// Note - When modifying this code, make sure to review
	// org.sakaiproject.editor.EditorServlet.java
	// as it includes these values when it is running in its own frame
	public Properties toolHeaderProperties(HttpServletRequest req, String skin, Site site, Placement placement) 
	{
		Properties retval = new Properties();

		boolean isInlineReq = ToolUtils.isInlineRequest(req);
		String headCss = CSSUtils.getCssHead(skin, isInlineReq);
		
		Editor editor = portalService.getActiveEditor(placement);
		String preloadScript = editor.getPreloadScript() == null ? ""
				: "<script type=\"text/javascript\">" 
				+ editor.getPreloadScript() 
				+ "</script>\n";
		String editorScript = editor.getEditorUrl() == null ? ""
				: "<script type=\"text/javascript\" src=\"" 
				+ PortalUtils.getCDNPath()
				+ editor.getEditorUrl() 
				+ PortalUtils.getCDNQuery()
				+ "\"></script>\n";
		String launchScript = editor.getLaunchUrl() == null ? ""
				: "<script type=\"text/javascript\" src=\"" 
				+ PortalUtils.getCDNPath()
				+ editor.getLaunchUrl() 
				+ PortalUtils.getCDNQuery()
				+ "\"></script>\n";
		
		StringBuilder headJs = new StringBuilder();

        // SAK-22384
        if (site != null && MATHJAX_ENABLED_AT_SYSTEM_LEVEL)
        {
                if (site != null)
                {                           
                    String strMathJaxEnabledForSite = site.getProperties().getProperty(MATHJAX_ENABLED);
                    if (StringUtils.isNotBlank(strMathJaxEnabledForSite))
                    {
                        if (Boolean.valueOf(strMathJaxEnabledForSite))
                        {
                            // this call to MathJax.Hub.Config seems to be needed for MathJax to work in IE
                            headJs.append("<script type=\"text/x-mathjax-config\">\nMathJax.Hub.Config({\ntex2jax: { inlineMath: [['\\\\(','\\\\)']] }\n});\n</script>\n");
                            headJs.append("<script src=\"").append(MATHJAX_SRC_PATH).append("\"  language=\"JavaScript\" type=\"text/javascript\"></script>\n");
                        }                     
                    }
                }
        }

		String contentItemUrl = portalService.getContentItemUrl(site);
		headJs.append("<script type=\"text/javascript\" src=\"");
		headJs.append(PortalUtils.getCDNPath());
		headJs.append("/library/js/headscripts.js");
		headJs.append(PortalUtils.getCDNQuery());
		headJs.append("\"></script>\n");
		headJs.append("<script type=\"text/javascript\">var sakai = sakai || {}; sakai.editor = sakai.editor || {}; " +
				"sakai.editor.editors = sakai.editor.editors || {}; " +
				"sakai.editor.editors.ckeditor = sakai.editor.editors.ckeditor || {}; " +
				"sakai.locale = sakai.locale || {};\n");
		headJs.append("sakai.locale.userCountry = '" + rloader.getLocale().getCountry() + "';\n");
		headJs.append("sakai.locale.userLanguage = '" + rloader.getLocale().getLanguage() + "';\n");
		headJs.append("sakai.locale.userLocale = '" + rloader.getLocale().toString() + "';\n");
		headJs.append("sakai.editor.collectionId = '" + portalService.getBrowserCollectionId(placement) + "';\n");
		headJs.append("sakai.editor.enableResourceSearch = " + EditorConfiguration.enableResourceSearch() + ";\n");
		if ( contentItemUrl != null ) {
			headJs.append("sakai.editor.contentItemUrl = '" + contentItemUrl + "';\n");
		} else {
			headJs.append("sakai.editor.contentItemUrl = false;\n");
		}
		headJs.append("sakai.editor.siteToolSkin = '" + CSSUtils.getCssToolSkin(skin) + "';\n");
		headJs.append("sakai.editor.sitePrintSkin = '" + CSSUtils.getCssPrintSkin(skin) + "';\n");
		headJs.append("sakai.editor.editors.ckeditor.browser = '"+ EditorConfiguration.getCKEditorFileBrowser()+ "';\n");
		headJs.append("</script>\n");
		headJs.append(preloadScript);
		headJs.append(editorScript);
		headJs.append(launchScript);

		Session s = SessionManager.getCurrentSession();
		String userWarning = (String) s.getAttribute("userWarning");
		if (StringUtils.isNotEmpty(userWarning)) {
			headJs.append("<script type=\"text/javascript\">");
			headJs.append("if ( window.self !== window.top ) {");
			headJs.append(" setTimeout(function(){ window.top.portal_check_pnotify() }, 3000);");
			headJs.append("}</script>");
		}

		// TODO: Should we include jquery here?  See includeStandardHead.vm
		String head = headCss + headJs.toString();

		retval.setProperty("sakai.html.head", head);
		retval.setProperty("sakai.html.head.css", headCss);
		retval.setProperty("sakai.html.head.lang", rloader.getLocale().getLanguage());
		retval.setProperty("sakai.html.head.css.base", CSSUtils.getCssToolBaseLink(skin, isInlineReq));
		retval.setProperty("sakai.html.head.css.skin", CSSUtils.getCssToolSkinLink(skin, isInlineReq));
		retval.setProperty("sakai.html.head.js", headJs.toString());

		return retval;
	}

	public void setupForward(HttpServletRequest req, HttpServletResponse res,
			Placement p, String skin) throws ToolException
        {
                Site site = null;
		if ( p != null ) {
			try {
				site = SiteService.getSite(p.getContext());
			}
			catch (IdUnusedException ex) {
				log.debug(ex.getMessage());
			}
                }

		// Get the tool header properties
		Properties props = toolHeaderProperties(req, skin, site, p);
		for(Object okey : props.keySet() ) 
		{
			String key = (String) okey;
			req.setAttribute(key,props.getProperty(key));
		}

		StringBuilder bodyonload = new StringBuilder();
		if (p != null)
		{
			String element = Web.escapeJavascript("Main" + p.getId());
			bodyonload.append("setMainFrameHeight('" + element + "');");
		}
		bodyonload.append("setFocus(focus_path);");
		req.setAttribute("sakai.html.body.onload", bodyonload.toString());

		portalService.getRenderEngine(portalContext, req).setupForward(req, res, p, skin);
	}

	// SAK-28086 - Wrapped Requests have issues with NATIVE_URL
	String fixPath1(String s, String c, StringBuilder ctx) {
		if (s != null && s.startsWith(c)) {
			int i = s.indexOf("/", 6);
			if (i >= 0) {
				ctx.append(s.substring(0,i));
				s = s.substring(i);
			} else {
				ctx.append(s);
				s = null;
			}
		}
		return s;
	}

	String fixPath(String s, StringBuilder ctx) {
		s = fixPath1(s, "/site/", ctx);
		s = fixPath1(s, "/tool/", ctx);
		s = fixPath1(s, "/page/", ctx);
		return s;
	}

	/**
	 * Forward to the tool - but first setup JavaScript/CSS etc that the tool
	 * will render
	 */
	public void forwardTool(ActiveTool tool, HttpServletRequest req,
		HttpServletResponse res, Placement p, String skin, String toolContextPath,
		String toolPathInfo) throws ToolException
	{
		// SAK-29656 - Make sure the request URL and toolContextPath treat tilde encoding the same way
		//
		// Since we cannot easily change what the request object already knows as its URL,
		// we patch the toolContextPath to match the tilde encoding in the request URL.
		//
		// This is what we would see in Chrome and Firefox.  Firefox fails with Wicket
		// Chrome: forwardtool call http://localhost:8080/portal/site/~csev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf
		//    toolPathInfo null ctx /portal/site/~csev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf
		// Firefox: http://localhost:8080/portal/site/%7ecsev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf/
		//    toolPathInfo null ctx /portal/site/~csev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf

		String reqUrl = req.getRequestURL().toString();
		if ( reqUrl.indexOf(toolContextPath) < 0 ) {
			log.debug("Mismatch between request url {} and toolContextPath {}", reqUrl, toolContextPath);
			if ( toolContextPath.indexOf("/~") > 0 && reqUrl.indexOf("/~") < 1 ) {
				if ( reqUrl.indexOf("/%7e") > 0 ) {
					toolContextPath = toolContextPath.replace("/~","/%7e");
				} else {
					toolContextPath = toolContextPath.replace("/~","/%7E");
				}
			}
		}
		log.debug("forwardtool call {} toolPathInfo {} ctx {}", req.getRequestURL(), toolPathInfo, toolContextPath);

		// if there is a stored request state, and path, extract that from the
		// session and reinstance it

		StringBuilder ctx = new StringBuilder(toolContextPath);
		toolPathInfo = fixPath(toolPathInfo, ctx);
		toolContextPath = ctx.toString();
		boolean needNative = false;

		// let the tool do the the work (forward)
		if (enableDirect)
		{
			StoredState ss = portalService.getStoredState();
			if (ss == null || !toolContextPath.equals(ss.getToolContextPath()))
			{
				setupForward(req, res, p, skin);
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				log.debug("tool forward 1 {} context {}", toolPathInfo, toolContextPath);
				needNative = (req.getAttribute(Tool.NATIVE_URL) != null);
				if (needNative)
					req.removeAttribute(Tool.NATIVE_URL);
				tool.forward(req, res, p, toolContextPath, toolPathInfo);
				if (needNative)
					req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
			}
			else
			{
				log.debug("Restoring StoredState [{}]", ss);
				HttpServletRequest sreq = ss.getRequest(req);
				Placement splacement = ss.getPlacement();
				StringBuilder sctx = new StringBuilder(ss.getToolContextPath());
				String stoolPathInfo = fixPath(ss.getToolPathInfo(), sctx);
				String stoolContext = sctx.toString();

				ActiveTool stool = ActiveToolManager.getActiveTool(p.getToolId());
				String sskin = ss.getSkin();
				setupForward(sreq, res, splacement, sskin);
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				log.debug("tool forward 2 {} context {}", stoolPathInfo, stoolContext);
				needNative = (sreq.getAttribute(Tool.NATIVE_URL) != null);
				if (needNative)
					sreq.removeAttribute(Tool.NATIVE_URL);
				stool.forward(sreq, res, splacement, stoolContext, stoolPathInfo);
				if (needNative)
					sreq.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
				// this is correct as we have checked the context path of the
				// tool
				portalService.setStoredState(null);
			}
		}
		else
		{
			setupForward(req, res, p, skin);
			req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
			log.debug("tool forward 3 {} context {}", toolPathInfo, toolContextPath);
			needNative = (req.getAttribute(Tool.NATIVE_URL) != null);
			if (needNative)
				req.removeAttribute(Tool.NATIVE_URL);
			tool.forward(req, res, p, toolContextPath, toolPathInfo);
			if (needNative)
				req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		}

	}

	public void forwardPortal(ActiveTool tool, HttpServletRequest req,
			HttpServletResponse res, ToolConfiguration p, String skin,
			String toolContextPath, String toolPathInfo) throws ToolException,
			IOException
	{
		String portalPath = ServerConfigurationService.getString("portalPath", "/portal");

		// if there is a stored request state, and path, extract that from the
		// session and reinstance it

		// generate the forward to the tool page placement
		String portalPlacementUrl = portalPath + getPortalPageUrl(p) + "?" + req.getQueryString();
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
		
		StringBuilder portalPageUrl = new StringBuilder();
		
		portalPageUrl.append("/site/");
		portalPageUrl.append(p.getSiteId());
		portalPageUrl.append("/page/");
		portalPageUrl.append(page);

		return portalPageUrl.toString();
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
			String thisUser = SessionManager.getCurrentSessionUserId();
			
			//Get user preferences
            Preferences prefs = preferencesService.getPreferences(thisUser);

			boolean showServerTime = ServerConfigurationService.getBoolean("portal.show.time", true);
			if (showServerTime) {
					rcontext.put("showServerTime","true");
					Calendar now = Calendar.getInstance();
					Date nowDate = new Date(now.getTimeInMillis());

					//first set server date and time
					TimeZone serverTz = TimeZone.getDefault();
					now.setTimeZone(serverTz);

					rcontext.put("serverTzDisplay",
									serverTz.getDisplayName(
											serverTz.inDaylightTime(nowDate),
											TimeZone.SHORT
											)
									);

					rcontext.put("serverTzGMTOffset",
									String.valueOf(
											now.getTimeInMillis() + now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)
											)
									);

					//provide the user's preferred timezone information if it is different

					//Get the Properties object that holds user's TimeZone preferences 
					ResourceProperties tzprops = prefs.getProperties(TimeService.APPLICATION_ID);

					//Get the ID of the timezone using the timezone key.
					//Default to 'localTimeZone' (server timezone?)
					String preferredTzId = (String) tzprops.get(TimeService.TIMEZONE_KEY);

					if (preferredTzId != null && !preferredTzId.equals(serverTz.getID())) {
							TimeZone preferredTz = TimeZone.getTimeZone(preferredTzId);

							now.setTimeZone(preferredTz);

							rcontext.put("showPreferredTzTime", "true");

							//now set up the portal information
							rcontext.put("preferredTzDisplay",
											preferredTz.getDisplayName(
													preferredTz.inDaylightTime(nowDate),
													TimeZone.SHORT
													)
											);

							rcontext.put("preferredTzGMTOffset",
											String.valueOf(
													now.getTimeInMillis() + now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)
													)
											);
					} else {
							rcontext.put("showPreferredTzTime", "false");
					}
			}
			
			rcontext.put("pagepopup", false);

			String copyright = ServerConfigurationService
			.getString("bottom.copyrighttext");

			/**
			 * Replace keyword in copyright message from sakai.properties 
			 * with the server's current year to auto-update of Copyright end date 
			 */
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
			String currentServerYear = simpleDateFormat.format(new Date());
			copyright = copyright.replaceAll(SERVER_COPYRIGHT_CURRENT_YEAR_KEYWORD, currentServerYear);

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

                        boolean neoChatAvailable
                            = ServerConfigurationService.getBoolean("portal.neochat", false)
                                && chatHelper.checkChatPermitted(thisUser);

                        rcontext.put("neoChat", neoChatAvailable);
                        rcontext.put("portalChatPollInterval", 
				ServerConfigurationService.getInt("portal.chat.pollInterval", 5000));
                        rcontext.put("neoAvatar", 
				ServerConfigurationService.getBoolean("portal.neoavatar", true));
                        rcontext.put("neoChatVideo", 
				ServerConfigurationService.getBoolean("portal.chat.video", true));
                        rcontext.put("portalVideoChatTimeout", 
				ServerConfigurationService.getInt("portal.chat.video.timeout", 25));

                        if(sakaiTutorialEnabled && thisUser != null) {
                        	if (!("1".equals(prefs.getProperties().getProperty("sakaiTutorialFlag")))) {
                        		rcontext.put("tutorial", true);
                        		//now save this in the user's preferences so we don't show it again
                        		PreferencesEdit preferences = null;
                        		try {
                        			preferences = preferencesService.edit(thisUser);
                        			ResourcePropertiesEdit props = preferences.getPropertiesEdit();
                        			props.addProperty("sakaiTutorialFlag", "1");
                        			preferencesService.commit(preferences);   
                        		} catch (SakaiException e1) {
                        			log.error(e1.getMessage(), e1);
                        		}
                        	}
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

			boolean useBullhornAlerts = ServerConfigurationService.getBoolean("portal.bullhorns.enabled", false);
			rcontext.put("useBullhornAlerts", useBullhornAlerts);
			if (useBullhornAlerts) {
				int bullhornAlertInterval = ServerConfigurationService.getInt("portal.bullhorns.poll.interval", 10000);
				rcontext.put("bullhornsPollInterval", bullhornAlertInterval);
			}

			// SAK-25931 - Do not remove this from session here - removal is done by /direct
	                Session s = SessionManager.getCurrentSession();
			String userWarning = (String) s.getAttribute("userWarning");
			rcontext.put("userWarning", new Boolean(StringUtils.isNotEmpty(userWarning)));

			if (ServerConfigurationService.getBoolean("pasystem.enabled", true)) {
			    PASystem paSystem = (PASystem) ComponentManager.get(PASystem.class);
			    rcontext.put("paSystemEnabled", true);
			    rcontext.put("paSystem", paSystem);
			}
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
			String logoutWarningMessage = "";

			// for showing user display name and id next to logout (SAK-10492)
			String loginUserDispName = null;
			String loginUserDispId = null;
			String loginUserId = null;
			String loginUserFirstName = null;
			boolean displayUserloginInfo = ServerConfigurationService.
			getBoolean("display.userlogin.info", true);

			// check for the top.login (where the login fields are present
			// instead
			// of a login link, but ignore it if container.login is set
			boolean topLogin = ServerConfigurationService.getBoolean("top.login", true);
			boolean containerLogin = ServerConfigurationService.getBoolean("container.login", false);
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
					String overrideLoginUrl = StringUtils
					.trimToNull(ServerConfigurationService.getString("login.url"));
					if (overrideLoginUrl != null) logInOutUrl = overrideLoginUrl;

					// check for a login text override
					message = StringUtils.trimToNull(ServerConfigurationService
							.getString("login.text"));
					if (message == null) message = rloader.getString("log.login");

					// check for an image for the login
					image1 = StringUtils.trimToNull(ServerConfigurationService
							.getString("login.icon"));

					// check for a possible second, xlogin link
					if (Boolean.TRUE.toString().equalsIgnoreCase(
							ServerConfigurationService.getString("xlogin.enabled")))
					{
						// get the text and image as configured
						message2 = StringUtils.trimToNull(ServerConfigurationService
								.getString("xlogin.text"));
						if (message2 == null) message2 = rloader.getString("log.xlogin");
						image2 = StringUtils.trimToNull(ServerConfigurationService
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
					loginUserDispId = Validator.escapeHtml(thisUser.getDisplayId());
					loginUserId = Validator.escapeHtml(thisUser.getId());
					loginUserDispName = Validator.escapeHtml(thisUser.getDisplayName());
					loginUserFirstName = Validator.escapeHtml(thisUser.getFirstName());
				}
				
				// check if current user is being impersonated (by become user/sutool)
				String impersonatorDisplayId = getImpersonatorDisplayId();
				if (!impersonatorDisplayId.isEmpty())
				{
					message = rloader.getFormattedMessage("sit_return", impersonatorDisplayId);
				}

				// check for a logout text override
				if (message == null)
				{
					message = ServerConfigurationService.getString("logout.text", rloader.getString("sit_log"));
				}

				// check for an image for the logout
				image1 = StringUtils.trimToNull(ServerConfigurationService
						.getString("logout.icon"));

				// since we are doing logout, cancel top.login
				topLogin = false;
				
				logoutWarningMessage = ServerConfigurationService.getBoolean("portal.logout.confirmation",false)?rloader.getString("sit_logout_warn"):"";
			}
			rcontext.put("userIsLoggedIn", session.getUserId() != null);
			rcontext.put("loginTopLogin", Boolean.valueOf(topLogin));
			rcontext.put("logoutWarningMessage", logoutWarningMessage);

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
				String eidWording = null;
				String pwWording = null;
				eidWording = StringUtils.trimToNull(rloader.getString("log.userid"));
				pwWording = StringUtils.trimToNull(rloader.getString("log.pass"));
				String eidPlaceholder = StringUtils.trimToNull(rloader.getString("log.inputuserplaceholder"));
				String pwPlaceholder = StringUtils.trimToNull(rloader.getString("log.inputpasswordplaceholder"));

				if (eidWording == null) eidWording = "eid";
				if (pwWording == null) pwWording = "pw";
				if (eidPlaceholder == null ) eidPlaceholder = "";
				if (pwPlaceholder == null ) pwPlaceholder = "";
				String loginWording = rloader.getString("log.login");

				rcontext.put("loginPortalPath", ServerConfigurationService
						.getString("portalPath"));
				rcontext.put("loginEidWording", eidWording);
				rcontext.put("loginPwWording", pwWording);
				rcontext.put("loginWording", loginWording);
				rcontext.put("eidPlaceholder", eidPlaceholder);
				rcontext.put("pwPlaceholder", pwPlaceholder);

				// setup for the redirect after login
				session.setAttribute(Tool.HELPER_DONE_URL, ServerConfigurationService
						.getPortalUrl());
			}

			if (displayUserloginInfo)
			{
				rcontext.put("loginUserDispName", loginUserDispName);
				rcontext.put("loginUserFirstName", loginUserFirstName);
				rcontext.put("loginUserDispId", loginUserDispId);
				rcontext.put("loginUserId", loginUserId);
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
		securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		chatHelper = org.sakaiproject.portal.api.cover.PortalChatPermittedHelper.getInstance();
		preferencesService = ComponentManager.get(PreferencesService.class);

		log.info("init()");

		forceContainer = ServerConfigurationService.getBoolean("login.use.xlogin.to.relogin", true);

		handlerPrefix = ServerConfigurationService.getString("portal.handler.default", "site");
		
		gatewaySiteUrl = ServerConfigurationService.getString("gatewaySiteUrl", null);
		
		sakaiTutorialEnabled = ServerConfigurationService.getBoolean("portal.use.tutorial", true);

		basicAuth = new BasicAuth();
		basicAuth.init();

		enableDirect = portalService.isEnableDirect();
		// do this before adding handlers to prevent handlers registering 2
		// times.
		// if the handlers were already there they will be re-registered,
		// but when they are added again, they will be replaced.
		// warning messages will appear, but the end state will be the same.
		portalService.addPortal(this);

		worksiteHandler = new WorksiteHandler();
		siteHandler = new SiteHandler();

		addHandler(siteHandler);
		addHandler(new SiteResetHandler());

		addHandler(new ToolHandler());
		addHandler(new ToolResetHandler());
		addHandler(new PageResetHandler());
		addHandler(new PageHandler());
		addHandler(worksiteHandler);
		addHandler(new WorksiteResetHandler());
		addHandler(new RssHandler());
		addHandler(new AtomHandler());
		addHandler(new OpmlHandler());
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
		addHandler(new JoinHandler());
		addHandler(new FavoritesHandler());
		addHandler(new GenerateBugReportHandler());
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
		log.info("Log marker " + se.getMethodName() + ":" + se.getFileName() + ":"
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
		PortalRenderContext rcontext = startPageContext("", null, null, null, null);
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
			log.warn("getUserEidBasedSiteId: user id not found for eid: " + userId);
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
	
	/**
	 * Find a cookie by this name from the request
	 * 
	 * @param req
	 *        The servlet request.
	 * @param name
	 *        The cookie name
	 * @return The cookie of this name in the request, or null if not found.
	 */
	public Cookie findCookie(HttpServletRequest req, String name) 
	{
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(name)) {
					return cookies[i];
				}
			}
		}
		return null;
	}

	/**
	 * Do the getSiteSkin, adjusting for the overall skin/templates for the portal.
	 * 
	 * @return The skin
	 */
	protected String getSiteSkin(String siteId)
	{
		String skin = SiteService.getSiteSkin(siteId);
		return getSkin(skin);
	}

	/**
	 * Do the getSkin, adjusting for the overall skin/templates for the portal.
	 * 
	 * @return The skin
	 */
	protected String getSkin(String skin)
	{
		return CSSUtils.adjustCssSkinFolder(skin);
	}
	
	/**
	 * Renders the content of a tool into a {@link BufferedContentRenderResult}
	 * @param res {@link HttpServletResponse}
	 * @param req {@link HttpServletRequest} 
	 * @param placement {@link ToolConfiguration}
	 * @return {@link BufferedContentRenderResult} with a head and body representing the appropriate bits for the tool or null if unable to render.
	 */
	RenderResult getInlineRenderingForTool(HttpServletResponse res, HttpServletRequest req, ToolConfiguration placement) {
		
		RenderResult rval = null;
			
		// allow a final chance for a tool to be inlined, based on it's tool configuration
		// set renderInline = true to enable this
		boolean renderInline = BooleanUtils.toBoolean(placement.getConfig().getProperty("renderInline"));
			
		if(renderInline) {
			
			//build tool context path directly to the tool
			String toolContextPath = req.getContextPath() + req.getServletPath() + "/site/" + placement.getSiteId() + "/tool/" + placement.getId();
			
			// setup the rest of the params
			String[] parts = getParts(req);
			String toolPathInfo = Web.makePath(parts, 5, parts.length);
			Session session = SessionManager.getCurrentSession();

			// get the buffered content
			Object buffer = this.siteHandler.bufferContent(req, res, session, placement.getId(), toolContextPath, toolPathInfo, placement);
			
			if (buffer instanceof Map) {
				Map<String,String> bufferMap = (Map<String,String>) buffer;
				rval = new BufferedContentRenderResult(placement, bufferMap.get("responseHead"), bufferMap.get("responseBody"));
			}
		}
		
		return rval;
	}
	
	/**
	 * Checks if current user is being impersonated (via become user/sutool) and returns displayId of
	 * the impersonator. Adapted from SkinnableLogin's isImpersonating()
	 * @return displayId of impersonator, or empty string if not being impersonated
	 */
	private String getImpersonatorDisplayId()
	{
		Session currentSession = SessionManager.getCurrentSession();
		UsageSession originalSession = (UsageSession) currentSession.getAttribute(UsageSessionService.USAGE_SESSION_KEY);
		
		if (originalSession != null)
		{
			String originalUserId = originalSession.getUserId();
			if (!StringUtils.equals(currentSession.getUserId(), originalUserId))
			{
				try
				{
					User originalUser = UserDirectoryService.getUser(originalUserId);
					return originalUser.getDisplayId();
				}
				catch (UserNotDefinedException e)
				{
					log.debug("Unable to retrieve user for id: {}", originalUserId);
				}
			}
		}
		
		return "";
	}

}
