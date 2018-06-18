/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.charon.site.AllSitesViewImpl;
import org.sakaiproject.portal.charon.site.PortalSiteHelperImpl;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.portal.util.ByteArrayServletResponse;
import org.sakaiproject.util.Validator;
import lombok.extern.slf4j.Slf4j;


/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class SiteHandler extends WorksiteHandler
{

	private static final String INCLUDE_SITE_NAV = "include-site-nav";

	private static final String INCLUDE_LOGO = "include-logo";

	private static final String INCLUDE_TABS = "include-tabs";

	private static final String URL_FRAGMENT = "site";

	private static ResourceLoader rb = new ResourceLoader("sitenav");
	
	// When these strings appear in the URL they will be replaced by a calculated value based on the context.
	// This can be replaced by the users myworkspace.
	private String mutableSitename ="-";
	// This can be replaced by the page on which a tool appears.
	private String mutablePagename ="-";

	// SAK-29180 - Normalize the properties, keeping the legacy pda sakai.properties names through Sakai-11 at least
	private static final String BYPASS_URL_PROP = "portal.bypass";
	private static final String LEGACY_BYPASS_URL_PROP = "portal.pda.bypass";
	private static final String DEFAULT_BYPASS_URL = "\\.jpg$|\\.gif$|\\.js$|\\.png$|\\.jpeg$|\\.prf$|\\.css$|\\.zip$|\\.pdf\\.mov$|\\.json$|\\.jsonp$\\.xml$|\\.ajax$|\\.xls$|\\.xlsx$|\\.doc$|\\.docx$|uvbview$|linktracker$|hideshowcolumns$";

	// Make sure to lower-case the matching regex (i.e. don't use IResourceListener below)
	private static final String BYPASS_QUERY_PROP = "portal.bypass.query";
	private static final String LEGACY_BYPASS_QUERY_PROP = "portal.pda.bypass.query";
	private static final String DEFAULT_BYPASS_QUERY = "wicket:interface=.*iresourcelistener:|wicket:ajax=true";

	private static final String BYPASS_TYPE_PROP = "portal.bypass.type";
	private static final String LEGACY_BYPASS_TYPE_PROP = "portal.pda.bypass.type";
	private static final String DEFAULT_BYPASS_TYPE = "^application/|^image/|^audio/|^video/|^text/xml|^text/plain";

	private static final String IFRAME_SUPPRESS_PROP = "portal.iframesuppress";
	private static final String LEGACY_IFRAME_SUPPRESS_PROP = "portal.pda.iframesuppress";

	// SAK-27774 - We are going inline default but a few tools need a crutch 
	// This is Sakai 11 only so please do not back-port or merge this default value
	private static final String IFRAME_SUPPRESS_DEFAULT = ":all:sakai.gradebook.gwt.rpc:com.rsmart.certification:sakai.melete:sakai.rsf.evaluation";

	private static final long AUTO_FAVORITES_REFRESH_INTERVAL_MS = 30000;

	public SiteHandler()
	{
		setUrlFragment(SiteHandler.URL_FRAGMENT);
		mutableSitename =  ServerConfigurationService.getString("portal.mutable.sitename", "-");
		mutablePagename =  ServerConfigurationService.getString("portal.mutable.pagename", "-");
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if ((parts.length >= 2) && (parts[1].equals(SiteHandler.URL_FRAGMENT)))
		{
			// This is part of the main portal so we simply remove the attribute
			session.setAttribute(PortalService.SAKAI_CONTROLLING_PORTAL, null);
			// site might be specified
			String siteId = null;
			if (parts.length >= 3)
			{
				siteId = parts[2];
			}
			try
			{
				return doGet(parts, req, res, session, siteId);
			}
			catch (Exception ex)
			{
				throw new PortalHandlerException(ex);
			}
		}
		else
		{
			return NEXT;
		}
	}

	/**
	 * This extra method is so that we can pass in a different siteId to the one in the URL.
	 * @see #doGet(String[], HttpServletRequest, HttpServletResponse, Session, String)
	 */
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
					 Session session, String siteId) throws IOException, ToolException {

		// recognize an optional page/pageid
		String pageId = null;
		String toolId = null;

		// may also have the tool part, so check that length is 5 or greater.
		if ((parts.length >= 5) && (parts[3].equals("page")))
		{
			pageId = parts[4];
		}

		// Tool resetting URL - clear state and forward to the real tool
		// URL
		// /portal/site/site-id/tool-reset/toolId
		// 0 1 2 3 4
		if ((siteId != null) && (parts.length == 5) && (parts[3].equals("tool-reset")))
		{
			toolId = parts[4];
			String toolUrl = req.getContextPath() + "/site/" + siteId + "/tool"
					+ Web.makePath(parts, 4, parts.length);
			String queryString = Validator.generateQueryString(req);
			if (queryString != null)
			{
				toolUrl = toolUrl + "?" + queryString;
			}
			portalService.setResetState("true");
			res.sendRedirect(toolUrl);
			return RESET_DONE;
		}

		// Page resetting URL - clear state and forward to the real page
		// URL
		// /portal/site/site-id/page-reset/pageId
		// 0 1 2 3 4
		if ((siteId != null) && (parts.length == 5) && (parts[3].equals("page-reset")))
		{
			pageId = parts[4];
			Site site = null;
			try
			{
				// This should understand aliases as well as IDs
				site = portal.getSiteHelper().getSiteVisit(siteId);
			}
			catch (Exception e)
			{
				site = null;
			}

			SitePage page = null;
			if (site != null ) page = portal.getSiteHelper().lookupSitePage(pageId, site);

			boolean hasJSR168 = false;
			if (page != null)
			{
				Session s = SessionManager.getCurrentSession();
				Iterator<ToolConfiguration> toolz = page.getTools().iterator();
				while(toolz.hasNext()){
					ToolConfiguration pageTool = toolz.next();
					ToolSession ts = s.getToolSession(pageTool.getId());
					ts.clearAttributes();
					if ( portal.isPortletPlacement(pageTool) ) hasJSR168 = true;
				}
			}

			String pageUrl = req.getContextPath() + "/site/" + siteId + "/page"
					+ Web.makePath(parts, 4, parts.length);

			String queryString = Validator.generateQueryString(req);
			if (queryString != null)
			{
				pageUrl = pageUrl + "?" + queryString;
				if ( hasJSR168 ) pageUrl = pageUrl + "&sakai.state.reset=true";
			} else {
				if ( hasJSR168 ) pageUrl = pageUrl + "?sakai.state.reset=true";
			}
			portalService.setResetState("true");
			res.sendRedirect(pageUrl);
			return RESET_DONE;
		}

		// may also have the tool part, so check that length is 5 or greater.
		if ((parts.length >= 5) && (parts[3].equals("tool")))
		{
			toolId = parts[4];
		}

		String commonToolId = null;

		if(parts.length == 4)
		{
			commonToolId = parts[3];
		}

		doSite(req, res, session, siteId, pageId, toolId, commonToolId, parts,
				req.getContextPath() + req.getServletPath());
		return END;
	}

	public void doSite(HttpServletRequest req, HttpServletResponse res, Session session,
			String siteId, String pageId, String toolId,
			String commonToolId, String [] parts, String toolContextPath) throws ToolException,
			IOException
	{		
				
		// default site if not set
		String userId = session.getUserId();
		if (siteId == null)
		{
			if (userId == null)
			{
				siteId = portal.getSiteHelper().getGatewaySiteId();
				if (siteId == null)
				{
					siteId = ServerConfigurationService.getGatewaySiteId();
				}
			}
			else
			{
				// TODO Should maybe switch to portal.getSiteHelper().getMyWorkspace()
				AllSitesViewImpl allSites = (AllSitesViewImpl)portal.getSiteHelper().getSitesView(SiteView.View.ALL_SITES_VIEW, req, session, siteId);
				List<Map> sites = (List<Map>)allSites.getRenderContextObject();
				if (sites.size() > 0) {
					siteId = (String)sites.get(0).get("siteId");
				}
				else
					siteId = SiteService.getUserSiteId(userId);
			}
		}

		// Can get a URL like /portal/site/-/page/-/tool/sakai.rwiki.  
		// The "mutable" site and page can not be given specific values since the 
		// final resolution depends on looking up the specific placement of the tool 
		// with this common id in the my workspace for this user.
		
		// check for a mutable site to be resolved here
		if (mutableSitename.equalsIgnoreCase(siteId) && (session.getUserId() != null)) {
			siteId = SiteService.getUserSiteId(userId);
		}

		// find the site, for visiting
		boolean siteDenied = false;
		Site site = null;
		try
		{
			// This should understand aliases as well as IDs
			site = portal.getSiteHelper().getSiteVisit(siteId);
			
			// SAK-20509 remap the siteId from the Site object we now have, since it may have originally been an alias, but has since been translated.
			siteId = site.getId();
			
		}
		catch (IdUnusedException e)
		{
		}
		catch (PermissionException e)
		{
			if (ServerConfigurationService.getBoolean("portal.redirectJoin", true) &&
					userId != null && portal.getSiteHelper().isJoinable(siteId, userId))
			{
				String redirectUrl = Web.returnUrl(req, "/join/"+siteId);
				res.sendRedirect(redirectUrl);
				return;
			}

			siteDenied = true;
		}

		if (site == null)
		{				
			// if not logged in, give them a chance
			if (userId == null)
			{
				StoredState ss = portalService.newStoredState("directtool", "tool");
				ss.setRequest(req);
				ss.setToolContextPath(toolContextPath);
				portalService.setStoredState(ss);
				portal.doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
			}
			else
			{
				// Post an event for denied site visits by known users.
				// This can be picked up to check the user state and refresh it if stale,
				// such as showing links to sites that are no longer accessible.
				// It is also helpful for event log analysis for user trouble or bad behavior.
				if (siteDenied)
				{
					Event event = EventTrackingService.newEvent(SiteService.EVENT_SITE_VISIT_DENIED, siteId, false);
					EventTrackingService.post(event);
				}
				portal.doError(req, res, session, Portal.ERROR_SITE);
			}
			return;
		}
		
		// Supports urls like: /portal/site/{SITEID}/sakai.announcements
		if(site != null && commonToolId != null)
		{
			ToolConfiguration tc = null;
			if(!commonToolId.startsWith("sakai."))
			{
				// Try the most likely case first, that of common tool ids starting with 'sakai.'
				tc = site.getToolForCommonId("sakai." + commonToolId);
				if(tc == null)
				{
					// That failed, try the supplied tool id
					tc = site.getToolForCommonId(commonToolId);
				}
			}
			
			if(tc != null)
			{
				pageId = tc.getPageId();
			}
		}

		// Find the pageId looking backwards through the toolId
		if(site != null && pageId == null && toolId != null ) {
			SitePage p = (SitePage) ToolUtils.getPageForTool(site, toolId);
			if ( p != null ) pageId = p.getId();
		}

		// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(Portal.ATTR_SITE_PAGE + siteId);
		}

		// If the page is the mutable page name then look up the 
		// real page id from the tool name.
		if (mutablePagename.equalsIgnoreCase(pageId)) {
			pageId = findPageIdFromToolId(pageId, URLUtils.getSafePathInfo(req), site);
		}
		
		// clear the last page visited
		session.removeAttribute(Portal.ATTR_SITE_PAGE + siteId);

		// SAK-29138 - form a context sensitive title
		List<String> providers = PortalSiteHelperImpl.getProviderIDsForSites(((List<Site>) Arrays.asList(new Site[] { site }))).get(site.getReference());
		String title = ServerConfigurationService.getString("ui.service","Sakai") + " : "
				+ portal.getSiteHelper().getUserSpecificSiteTitle(site, false, false, providers);

		// Lookup the page in the site - enforcing access control
		// business rules
		SitePage page = portal.getSiteHelper().lookupSitePage(pageId, site);
		if (page != null)
		{
			if (ServerConfigurationService.getBoolean("portal.rememberSitePage", true)) {
				// store the last page visited
				session.setAttribute(Portal.ATTR_SITE_PAGE + siteId, page.getId());
			}
			title += " : " + page.getTitle();
		}

		// Check for incomplete URLs in the case of inlined tools
		boolean trinity = ServerConfigurationService.getBoolean(ToolUtils.PORTAL_INLINE_EXPERIMENTAL, ToolUtils.PORTAL_INLINE_EXPERIMENTAL_DEFAULT);
		if (trinity && toolId == null) {
			String pagerefUrl = ToolUtils.getPageUrl(req, site, page, getUrlFragment(),
				false, null, null);
			// http://localhost:8080/portal/site/963b28b/tool/0996adf
			String[] pieces = pagerefUrl.split("/");
			if ( pieces.length > 6 && "tool".equals(pieces[6]) ) {
				// SAK-25503 - This probably should be a log.debug later
				String queryString = req.getQueryString();
				if ( queryString != null ) pagerefUrl = pagerefUrl + '?' + queryString;
				log.warn("Redirecting tool inline url: "+pagerefUrl);
				res.sendRedirect(pagerefUrl);
				return;
			}
		}

		// See if we can buffer the content, if not, pass the request through
		String TCP = null;
		String toolPathInfo = null;
		boolean allowBuffer = false;
		Object BC = null;

		ToolConfiguration siteTool = null;
		if ( toolId != null ) {
			siteTool = SiteService.findTool(toolId);
			if ( siteTool != null && parts.length >= 5 ) {
				commonToolId = siteTool.getToolId();

				// Does the tool allow us to buffer?
				allowBuffer = allowBufferContent(req, site, siteTool);
				log.debug("allowBuffer="+allowBuffer+" url="+req.getRequestURL());

				if ( allowBuffer ) {
					TCP = req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 5);
					toolPathInfo = Web.makePath(parts, 5, parts.length);

					// Should we bypass buffering based on the request?
					boolean matched = checkBufferBypass(req, siteTool);

					if ( matched ) {
						log.debug("Bypassing buffer to forwardTool per configuration");
						ActiveTool tool = ActiveToolManager.getActiveTool(commonToolId);
						portal.forwardTool(tool, req, res, siteTool,
							siteTool.getSkin(), TCP, toolPathInfo);
						return;
					}
					// Inform calls to includeTool() that the default is 
					// this thread is inlining a tool.
					ThreadLocalManager.set("sakai:inline-tool","true");
				}
			}
		}

		// start the response
		String siteType = portal.calcSiteType(siteId);

		// Note that this does not call includeTool()
		PortalRenderContext rcontext = portal.startPageContext(siteType, title, site
				.getSkin(), req, site);

		if ( allowBuffer ) {
			log.debug("Starting the buffer process...");

			BC = bufferContent(req, res, session, toolId,
					TCP, toolPathInfo, siteTool);

			// If the buffered response was not parseable
			if ( BC instanceof ByteArrayServletResponse ) {
				ByteArrayServletResponse bufferResponse = (ByteArrayServletResponse) BC;
				StringBuffer queryUrl = req.getRequestURL();
				String queryString = req.getQueryString();
				if ( queryString != null ) queryUrl.append('?').append(queryString);
				String msg = "Post buffer bypass CTI="+commonToolId+" URL="+queryUrl;
				String redir = bufferResponse.getRedirect();

				// We are concerned when we neither got output, nor a redirect
				if ( redir != null ) {
					msg = msg + " redirect to="+redir;
					log.debug(msg);
				} else {
					log.warn(msg);
				}
				bufferResponse.forwardResponse();
				return;
			}
		}

		// Include the buffered content if we have it
		if ( BC instanceof Map ) {
			if ( req.getMethod().equals("POST") ) {
				StringBuffer queryUrl = req.getRequestURL();
				String queryString = req.getQueryString();
				if ( queryString != null ) queryUrl.append('?').append(queryString);
				log.debug("It is tacky to return markup on a POST CTI="+commonToolId+" URL="+queryUrl);
			}
			log.debug("BufferedResponse success");
			rcontext.put("bufferedResponse", Boolean.TRUE);
			Map<String,String> bufferMap = (Map<String,String>) BC;
			rcontext.put("responseHead", (String) bufferMap.get("responseHead"));
			rcontext.put("responseBody", (String) bufferMap.get("responseBody"));
		}

		rcontext.put("siteId", siteId);
		boolean showShortDescription = Boolean.valueOf(ServerConfigurationService.getBoolean("portal.title.shortdescription.show", false));

		if (showShortDescription) {
			rcontext.put("shortDescription", Web.escapeHtml(site.getShortDescription()));
		}
		
		if (SiteService.isUserSite(siteId)){
			rcontext.put("siteTitle", rb.getString("sit_mywor") );
			rcontext.put("siteTitleTruncated", rb.getString("sit_mywor") );
			rcontext.put("isUserSite", true);
		}else{
			rcontext.put("siteTitle", portal.getSiteHelper().getUserSpecificSiteTitle(site, false, true, providers));
			rcontext.put("siteTitleTruncated", Validator.escapeHtml(portal.getSiteHelper().getUserSpecificSiteTitle(site, true, false, providers)));
			rcontext.put("isUserSite", false);
		}
		
		addLocale(rcontext, site, session.getUserId());
		
		includeSiteNav(rcontext, req, session, siteId);

		includeWorksite(rcontext, res, req, session, site, page, toolContextPath,
					getUrlFragment());

		// Include sub-sites if appropriate
		// TODO: Think through whether we want reset tools or not
		portal.includeSubSites(rcontext, req, session, siteId, req.getContextPath()
				+ req.getServletPath(), getUrlFragment(),
				/* resetTools */false);

		portal.includeBottom(rcontext);

		//Log the visit into SAKAI_EVENT - begin
		try{
			boolean presenceEvents = ServerConfigurationService.getBoolean("presence.events.log", true);
			if (presenceEvents)
				org.sakaiproject.presence.cover.PresenceService.setPresence(siteId + "-presence");
		}catch(Exception e){}
		//End - log the visit into SAKAI_EVENT		

		rcontext.put("currentUrlPath", Web.serverUrl(req) + req.getContextPath()
				+ URLUtils.getSafePathInfo(req));

		//Find any quick links ready for display in the top navigation bar,
		//they can be set per site or for the whole portal.
		if (userId != null) {
			String skin = getSiteSkin(siteId);
			String quickLinksTitle = portalService.getQuickLinksTitle(skin);
			List<Map> quickLinks = portalService.getQuickLinks(skin);
			if (CollectionUtils.isNotEmpty(quickLinks)) {
				rcontext.put("quickLinksInfo", quickLinksTitle);
				rcontext.put("quickLinks", quickLinks);
			}
		}
		doSendResponse(rcontext, res, null);

		StoredState ss = portalService.getStoredState();
		if (ss != null && toolContextPath.equals(ss.getToolContextPath()))
		{
			// This request is the destination of the request
			portalService.setStoredState(null);
		}

	}

	/*
	 * If the page id is the mutablePageId then see if can resolve it from the
	 * the placement of the tool with a supplied tool id.
	 */
	private String findPageIdFromToolId(String pageId, String toolContextPath,
			Site site) {

		// If still can't find page id see if can determine it from a well known
		// tool id (assumes that such a tool is in the site and the first instance of 
		// the tool found would be the right one).
		String toolSegment = "/tool/";
		String toolId = null;

			try
			{
			// does the URL contain a tool id?
			if (toolContextPath.contains(toolSegment)) {
				toolId = toolContextPath.substring(toolContextPath.lastIndexOf(toolSegment)+toolSegment.length());
				ToolConfiguration toolConfig = site.getToolForCommonId(toolId);
				log.debug("trying to resolve page id from toolId: [{}]", toolId);
				if (toolConfig != null) {
					pageId = toolConfig.getPageId();
				}
			}

			}
			catch (Exception e) {
				log.error("exception resolving page id from toolid :["+toolId+"]",e);
			}

		return pageId;
	}

	/**
	 * Does the final render response, classes that extend this class
	 * may/will want to override this method to use their own template
	 * 
	 * @param rcontext
	 * @param res
	 * @param object
	 * @param b
	 * @throws IOException
	 */
	protected void doSendResponse(PortalRenderContext rcontext, HttpServletResponse res,
			String contentType) throws IOException
	{
		portal.sendResponse(rcontext, res, "site", null);
	}

	protected void includeSiteNav(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, String siteId)
	{
		if (session.getUserId() != null) {
			refreshAutoFavorites(session);
		}

		if (rcontext.uses(INCLUDE_SITE_NAV))
		{

			boolean loggedIn = session.getUserId() != null;
			boolean topLogin = ServerConfigurationService.getBoolean("top.login", true);


			String accessibilityURL = ServerConfigurationService
					.getString("accessibility.url");
			rcontext.put("siteNavHasAccessibilityURL", Boolean
					.valueOf((accessibilityURL != null && !accessibilityURL.equals(""))));
			rcontext.put("siteNavAccessibilityURL", accessibilityURL);
			// rcontext.put("siteNavSitAccessability",
			// Web.escapeHtml(rb.getString("sit_accessibility")));
			// rcontext.put("siteNavSitJumpContent",
			// Web.escapeHtml(rb.getString("sit_jumpcontent")));
			// rcontext.put("siteNavSitJumpTools",
			// Web.escapeHtml(rb.getString("sit_jumptools")));
			// rcontext.put("siteNavSitJumpWorksite",
			// Web.escapeHtml(rb.getString("sit_jumpworksite")));

			rcontext.put("siteNavTopLogin", Boolean.valueOf(topLogin));
			rcontext.put("siteNavLoggedIn", Boolean.valueOf(loggedIn));

			try
			{
				if (loggedIn)
				{
					includeLogo(rcontext, req, session, siteId);
					includeTabs(rcontext, req, session, siteId, getUrlFragment(), false);
				}
				else
				{
					includeLogo(rcontext, req, session, siteId);
					if (portal.getSiteHelper().doGatewaySiteList())
						includeTabs(rcontext, req, session, siteId, getUrlFragment(),
								false);
				}
			}
			catch (Exception any)
			{
			}
		}
	}

	final static String AUTO_FAVORITES_LAST_REFRESHED_TIME = "autoFavoritesLastRefreshedTime";

	private void refreshAutoFavorites(Session session) {
		Long lastRefreshTime = (Long)session.getAttribute(AUTO_FAVORITES_LAST_REFRESHED_TIME);

		if (lastRefreshTime == null) {
			lastRefreshTime = Long.valueOf(0);
		}

		long now = System.currentTimeMillis();

		if ((now - lastRefreshTime) > AUTO_FAVORITES_REFRESH_INTERVAL_MS) {
			// Fetch the list of favorites, which will in turn populate the auto favorites.
			try {
				new FavoritesHandler().userFavorites(session.getUserId());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			session.setAttribute(AUTO_FAVORITES_LAST_REFRESHED_TIME, now);
		}
	}

	public void includeLogo(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, String siteId) throws IOException
	{
		if (rcontext.uses(INCLUDE_LOGO))
		{

			String skin = getSiteSkin(siteId);
			String skinRepo = ServerConfigurationService.getString("skin.repo");
			rcontext.put("logoSkin", skin);
			rcontext.put("logoSkinRepo", skinRepo);
			String siteType = portal.calcSiteType(siteId);
			String cssClass = (siteType != null) ? siteType : "undeterminedSiteType";
			rcontext.put("logoSiteType", siteType);
			rcontext.put("logoSiteClass", cssClass);
			portal.includeLogin(rcontext, req, session);
		}
	}

	private String getSiteSkin(String siteId)
	{
		// First, try to get the skin the default way
		String skin = SiteService.getSiteSkin(siteId);
		// If this fails, try to get the real site id if the site is a user site
		if (skin == null && SiteService.isUserSite(siteId))
		{
			try
			{
				String userId = SiteService.getSiteUserId(siteId);
				
				// If the passed siteId is the users EID, convert it to the internal ID.
				// Most lookups should be EID, if most URLs contain internal ID, this results in lots of cache misses.
				try
				{
					userId = UserDirectoryService.getUserId(userId);
				}
				catch (UserNotDefinedException unde)
				{
					// Ignore
				}
				String alternateSiteId = SiteService.getUserSiteId(userId);
				skin = SiteService.getSiteSkin(alternateSiteId);
			}
			catch (Exception e)
			{
				// Ignore
			}
		}

		if (skin == null)
		{
			skin = ServerConfigurationService.getString("skin.default");
		}
		return skin;
	}

	public void includeTabs(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, String siteId, String prefix, boolean addLogout)
			throws IOException
	{

		if (rcontext.uses(INCLUDE_TABS))
		{

			// for skinning
			String siteType = portal.calcSiteType(siteId);

			// If we have turned on auto-state reset on navigation, we generate
			// the "site-reset" "worksite-reset" and "gallery-reset" urls
            if ("true".equalsIgnoreCase(ServerConfigurationService
					.getString(Portal.CONFIG_AUTO_RESET)))
			{
				prefix = prefix + "-reset";
			}

			boolean loggedIn = session.getUserId() != null;
			
			// Check to see if we display a link in the UI for swapping the view
			boolean roleswapcheck = false; // This variable will tell the UI if we will display any role swapping component; false by default
			String roleswitchvalue = SecurityService.getUserEffectiveRole(SiteService.siteReference(siteId)); // checks the session for a role swap value
			boolean roleswitchstate = false; // This variable determines if the site is in the switched state or not; false by default
			boolean allowroleswap = SiteService.allowRoleSwap(siteId) && !SecurityService.isSuperUser();
			
			// check for the site.roleswap permission
			if (allowroleswap || roleswitchvalue != null)
			{
				Site activeSite = null;
	            try
	            {
	            	activeSite = portal.getSiteHelper().getSiteVisit(siteId); // active site
	            }
            	catch(IdUnusedException ie)
	            {
            		log.error(ie.getMessage(), ie);
            		throw new IllegalStateException("Site doesn't exist!");
	            }
	            catch(PermissionException pe)
	            {
	            	log.error(pe.getMessage(), pe);
	            	throw new IllegalStateException("No permission to view site!");
	            }
	            // this block of code will check to see if the student role exists in the site.  It will be used to determine if we need to display any student view component
	            boolean roleInSite = false;
            	Set<Role> roles = activeSite.getRoles();

            	String externalRoles = ServerConfigurationService.getString("studentview.roles"); // get the roles that can be swapped to from sakai.properties
            	String[] svRoles = externalRoles.split(",");
            	List<String> svRolesFinal = new ArrayList<String>();

            	for (Role role : roles)
            	{
            		for (int i = 0; i < svRoles.length; i++)
            		{
            			if (svRoles[i].trim().equals(role.getId()))
            			{
            				roleInSite = true;
            				svRolesFinal.add(role.getId());
            			}
            		}
            	}
            	if (activeSite.getType() != null && roleInSite) // the type check filters out some of non-standard sites where swapping roles would not apply.  The boolean check makes sure a role is in the site
            	{
		            String switchRoleUrl = "";
		            Role userRole = activeSite.getUserRole(session.getUserId()); // the user's role in the site
		            //if the userRole is null, this means they are more than likely a Delegated Access user.  Since the security check has already allowed
		            //the user to "swaproles" @allowroleswap, we know they have access to this site
		            if (roleswitchvalue != null && (userRole == null || !userRole.getId().equals(roleswitchvalue)))
		            {
		            	switchRoleUrl = ServerConfigurationService.getPortalUrl()
						+ "/role-switch-out/"
						+ siteId
						+ "/?panel=Main";
		            	rcontext.put("roleUrlValue", roleswitchvalue);
		            	roleswitchstate = true; // We're in a switched state, so set to true
		            }
		            else
		            {
		            	if (svRolesFinal.size()>1)
		            	{
		            		rcontext.put("roleswapdropdown", true);
							switchRoleUrl = ServerConfigurationService.getPortalUrl()
							+ "/role-switch/"
							+ siteId
							+ "/";
							rcontext.put("panelString", "/?panel=Main");
		            	}
		            	else
		            	{
		            		rcontext.put("roleswapdropdown", false);
		            		switchRoleUrl = ServerConfigurationService.getPortalUrl()
							+ "/role-switch/"
							+ siteId
							+ "/"
							+ svRolesFinal.get(0)
							+ "/?panel=Main";
		            		rcontext.put("roleUrlValue", svRolesFinal.get(0));
		            	}
		            }
		            roleswapcheck = true; // We made it this far, so set to true to display a component
		            rcontext.put("siteRoles", svRolesFinal);
					rcontext.put("switchRoleUrl", switchRoleUrl);
            	}
			}

			rcontext.put("viewAsStudentLink", Boolean.valueOf(roleswapcheck)); // this will tell our UI if we want the link for swapping roles to display
			rcontext.put("roleSwitchState", roleswitchstate); // this will tell our UI if we are in a role swapped state or not

			int tabDisplayLabel = 1;
			boolean toolsCollapsed = false;

			if (loggedIn) 
			{
				Preferences prefs = PreferencesService.getPreferences(session.getUserId());
				ResourceProperties props = prefs.getProperties(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);

				try 
				{
					tabDisplayLabel = (int) props.getLongProperty("tab:label");
				} 
				catch (Exception any) 
				{
					tabDisplayLabel = 1;
				}

				try {
					toolsCollapsed = props.getBooleanProperty("toolsCollapsed");
				} catch (Exception any) {}
			}

			rcontext.put("tabDisplayLabel", tabDisplayLabel);
			rcontext.put("toolsCollapsed", Boolean.valueOf(toolsCollapsed));
			
			SiteView siteView = portal.getSiteHelper().getSitesView(
					SiteView.View.DHTML_MORE_VIEW, req, session, siteId);
			siteView.setPrefix(prefix);
			siteView.setToolContextPath(null);
			rcontext.put("tabsSites", siteView.getRenderContextObject());

			String cssClass = (siteType != null) ? "siteNavWrap " + siteType
					: "siteNavWrap";

			rcontext.put("tabsCssClass", cssClass);

			rcontext.put("tabsAddLogout", Boolean.valueOf(addLogout));
			if (addLogout)
			{
				String logoutUrl = Web.serverUrl(req)
						+ ServerConfigurationService.getString("portalPath")
						+ "/logout_gallery";
				rcontext.put("tabsLogoutUrl", logoutUrl);
				// rcontext.put("tabsSitLog",
				// Web.escapeHtml(rb.getString("sit_log")));
			}

			rcontext.put("tabsCssClass", cssClass);

			rcontext.put("tabsAddLogout", Boolean.valueOf(addLogout));
			if (addLogout)
			{
				String logoutUrl = Web.serverUrl(req)
						+ ServerConfigurationService.getString("portalPath")
						+ "/logout_gallery";
				rcontext.put("tabsLogoutUrl", logoutUrl);
				// rcontext.put("tabsSitLog",
				// Web.escapeHtml(rb.getString("sit_log")));
			}

			boolean allowAddSite = false;
			if(SiteService.allowAddCourseSite()) {
				allowAddSite = true;
			} else if (SiteService.allowAddPortfolioSite()) {
				allowAddSite = true;
			} else if (SiteService.allowAddProjectSite()) {
				allowAddSite = true;
			}

			rcontext.put("allowAddSite",allowAddSite);
		}
	}

// XXXX


	/*
	 * Check to see if this request should bypass buffering
	 */
	public boolean checkBufferBypass(HttpServletRequest req, ToolConfiguration siteTool)
	{
		String uri = req.getRequestURI();
		String commonToolId = siteTool.getToolId();
		boolean matched = false;
		// Check the URL for a pattern match
		String pattern = null;
		Pattern p = null;
		Matcher m = null;
		pattern = ServerConfigurationService .getString(LEGACY_BYPASS_URL_PROP, DEFAULT_BYPASS_URL);
		pattern = ServerConfigurationService .getString(BYPASS_URL_PROP, pattern);
		pattern = ServerConfigurationService .getString(LEGACY_BYPASS_URL_PROP+"."+commonToolId, pattern);
		pattern = ServerConfigurationService .getString(BYPASS_URL_PROP+"."+commonToolId, pattern);
		if ( pattern.length() > 1 ) {
			p = Pattern.compile(pattern);
			m = p.matcher(uri.toLowerCase());
			if ( m.find() ) {
				matched = true;
			}
		}

		// Check the query string for a pattern match
		pattern = ServerConfigurationService .getString(LEGACY_BYPASS_QUERY_PROP, DEFAULT_BYPASS_QUERY);
		pattern = ServerConfigurationService .getString(BYPASS_QUERY_PROP, pattern);
		pattern = ServerConfigurationService .getString(LEGACY_BYPASS_QUERY_PROP+"."+commonToolId, pattern);
		pattern = ServerConfigurationService .getString(BYPASS_QUERY_PROP+"."+commonToolId, pattern);
		String queryString = req.getQueryString();
		if ( queryString == null ) queryString = "";
		if ( pattern.length() > 1 ) {
			p = Pattern.compile(pattern);
			m = p.matcher(queryString.toLowerCase());
			if ( m.find() ) {
				matched = true;
			}
		}

		// wicket-ajax request can not be buffered (PRFL-405)
		if (Boolean.valueOf(req.getHeader("wicket-ajax"))) {
			matched = true;
		}
		return matched;
	}

	/*
	 * Check to see if this tool allows the buffering of content
	 */
	public boolean allowBufferContent(HttpServletRequest req, Site site, ToolConfiguration siteTool)
	{
		String tidAllow = ServerConfigurationService.getString(LEGACY_IFRAME_SUPPRESS_PROP, IFRAME_SUPPRESS_DEFAULT);
		tidAllow = ServerConfigurationService.getString(IFRAME_SUPPRESS_PROP, tidAllow);

		if (tidAllow.indexOf(":none:") >= 0) return false;

		// JSR-168 portlets do not operate in iframes
		if ( portal.isPortletPlacement(siteTool) ) return false;

		// If the property is set and :all: is not specified, then the 
		// tools in the list are the ones that we accept
		if (tidAllow.trim().length() > 0 && tidAllow.indexOf(":all:") < 0)
		{
			if (tidAllow.indexOf(siteTool.getToolId()) < 0) return false;
		}

		// If the property is set and :all: is specified, then the 
		// tools in the list are the ones that we render the old way
		if (tidAllow.indexOf(":all:") >= 0)
		{
			if (tidAllow.indexOf(siteTool.getToolId()) >= 0) return false;
		}

		// Need to make sure the user is allowed to visit this tool
		ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class.getName());
		boolean allowedUser = toolManager.allowTool(site, siteTool);
		if ( ! allowedUser ) return false;

		return true;
	}

	/*
	 * Optionally actually grab the tool's output and include it in the same
	 * frame.  Return value is a bit complex. 
	 * Boolean.FALSE - Some kind of failure
	 * ByteArrayServletResponse - Something that needs to be simply sent out (i.e. not bufferable)
	 * Map - Buffering is a success and map contains buffer pieces
	 */
	public Object bufferContent(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, String toolContextPath, String toolPathInfo, 
			ToolConfiguration siteTool)
	{
		log.debug("bufferContent starting");
		// Produce the buffered response
		ByteArrayServletResponse bufferedResponse = new ByteArrayServletResponse(res);

		try {
			// Prepare the session for the tools.  Handles session reset, reseturl
			// and helpurl for neo tools - we don't need the returned map
			Map discard = portal.includeTool(res, req, siteTool, true);

			boolean retval = doToolBuffer(req, bufferedResponse, session, placementId,
					toolContextPath, toolPathInfo);
			log.debug("bufferContent retval="+retval);

			if ( ! retval ) return Boolean.FALSE;

			// If the tool did a redirect - tell our caller to just complete the response
			if ( bufferedResponse.getRedirect() != null ) return bufferedResponse;

			// Check the response contentType for a pattern match
			String commonToolId = siteTool.getToolId();
			String pattern = ServerConfigurationService .getString(LEGACY_BYPASS_TYPE_PROP, DEFAULT_BYPASS_TYPE);
			pattern = ServerConfigurationService .getString(BYPASS_TYPE_PROP, pattern);
			pattern = ServerConfigurationService .getString(LEGACY_BYPASS_TYPE_PROP+"."+commonToolId, pattern);
			pattern = ServerConfigurationService .getString(BYPASS_TYPE_PROP+"."+commonToolId, pattern);
			if ( pattern.length() > 0 ) {
				String contentType = res.getContentType();
				if ( contentType == null ) contentType = "";
				Pattern p = Pattern.compile(pattern);
				Matcher mc = p.matcher(contentType.toLowerCase());
				if ( mc.find() ) return bufferedResponse;
			}
		} catch (ToolException | IOException e) {
			log.warn("Failed to buffer content.", e);
			return Boolean.FALSE;
		}
		String tidAllow = ServerConfigurationService.getString(LEGACY_IFRAME_SUPPRESS_PROP, IFRAME_SUPPRESS_DEFAULT);
		tidAllow = ServerConfigurationService.getString(IFRAME_SUPPRESS_PROP, tidAllow);
		boolean debug = tidAllow.contains(":debug:");

		String responseStr = bufferedResponse.getInternalBuffer();
		if (responseStr == null || responseStr.length() < 1) return Boolean.FALSE;

		PageParts pp = parseHtmlParts(responseStr, debug);
		if (pp != null)
		{
			if (debug)
			{
				log.info(" ---- Head --- ");
				log.info(pp.head);
				log.info(" ---- Body --- ");
				log.info(pp.body);
			}
			Map<String, String> m = new HashMap<>();
			m.put("responseHead", pp.head);
			m.put("responseBody", pp.body);
			log.debug("responseHead {} bytes, responseBody {} bytes",
					pp.head.length(), pp.body.length());
			return m;
		}
		log.debug("bufferContent could not find head/body content");
		return bufferedResponse;
	}

	/**
	 * Simple tuple so a method can return both a head and body.
	 */
	static class PageParts {
		String head;
		String body;
	}

	/**
	 * Attempts to find the HTML head and body in the document and return them back.
	 * @param responseStr The HTML to be parse
	 * @param debug If <code>true</code> then log where we found the head and body.
	 *
	 * @return <code>null</code> if we failed to parse the page or a PageParts object.
	 */
	PageParts parseHtmlParts(String responseStr, boolean debug) {
		// We can't lowercase the string and search in it as then the offsets don't match when a character is a
		// different length in upper and lower case
		int headStart = StringUtils.indexOfIgnoreCase(responseStr, "<head");
		headStart = findEndOfTag(responseStr, headStart);
		int headEnd = StringUtils.indexOfIgnoreCase(responseStr, "</head");
		int bodyStart = StringUtils.indexOfIgnoreCase(responseStr, "<body");
		bodyStart = findEndOfTag(responseStr, bodyStart);

		// Some tools (Blogger for example) have multiple
		// head-body pairs - browsers seem to not care much about
		// this so we will do the same - so that we can be
		// somewhat clean - we search for the "last" end
		// body tag - for the normal case there will only be one
		int bodyEnd = StringUtils.indexOfIgnoreCase(responseStr, "</body");
		// If there is no body end at all or it is before the body
		// start tag we simply - take the rest of the response
		if ( bodyEnd < bodyStart ) bodyEnd = responseStr.length() - 1;

		if(debug)
			log.info("Frameless HS="+headStart+" HE="+headEnd+" BS="+bodyStart+" BE="+bodyEnd);

		if (bodyEnd > bodyStart && bodyStart > headEnd && headEnd > headStart
				&& headStart > 1) {
			PageParts pp = new PageParts();
			pp.head = responseStr.substring(headStart + 1, headEnd);

			// SAK-29908
			// Titles come twice to view and tool title overwrites main title because
			// it is printed before.

			int titleStart = pp.head.indexOf("<title");
			int titleEnd = pp.head.indexOf("</title");
			titleEnd = findEndOfTag(pp.head, titleEnd);

			pp.head = (titleStart != -1 && titleEnd != -1) ? pp.head.substring(0, titleStart) + pp.head.substring(titleEnd + 1) : pp.head;
			// End SAK-29908

			pp.body = responseStr.substring(bodyStart + 1, bodyEnd);
			return pp;
		}
		return null;
	}

	private int findEndOfTag(String string, int startPos)
	{
		if (startPos < 1) return -1;
		for (int i = startPos; i < string.length(); i++)
		{
			if (string.charAt(i) == '>') return i;
		}
		return -1;
	}

	public boolean doToolBuffer(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException
	{

		if (portal.redirectIfLoggedOut(res)) return false;

		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		if (siteTool == null)
		{
			return false;
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(siteTool.getToolId());
		if (tool == null)
		{
			return false;
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{

			try
			{
				SiteService.getSiteVisit(siteTool.getSiteId());
			}
			catch (IdUnusedException e)
			{
				portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				return false;
			}
			catch (PermissionException e)
			{
				return false;
			}
		}

		log.debug("doToolBuffer siteTool="+siteTool+" TCP="+toolContextPath+" TPI="+toolPathInfo);

		portal.forwardTool(tool, req, res, siteTool, siteTool.getSkin(), toolContextPath,
				toolPathInfo);

		return true;
	}


}
