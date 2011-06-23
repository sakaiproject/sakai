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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public class SiteHandler extends WorksiteHandler
{

	private static final String INCLUDE_SITE_NAV = "include-site-nav";

	private static final String INCLUDE_LOGO = "include-logo";

	private static final String INCLUDE_TABS = "include-tabs";

	private static final Log log = LogFactory.getLog(SiteHandler.class);

	private static final String URL_FRAGMENT = "site";

	private int configuredTabsToDisplay = 5;

	private boolean useDHTMLMore = false;
	
	private int showSearchWhen = 2; 

	// When these strings appear in the URL they will be replaced by a calculated value based on the context.
	// This can be replaced by the users myworkspace.
	private String mutableSitename ="-";
	// This can be replaced by the page on which a tool appears.
	private String mutablePagename ="-";

	public SiteHandler()
	{
		setUrlFragment(SiteHandler.URL_FRAGMENT);
		configuredTabsToDisplay = ServerConfigurationService.getInt(
				Portal.CONFIG_DEFAULT_TABS, 5);
		useDHTMLMore = Boolean.valueOf(ServerConfigurationService.getBoolean(
				"portal.use.dhtml.more", false));
		showSearchWhen = Integer.valueOf(ServerConfigurationService.getInt(
				"portal.show.search.when", 2));
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
			session.setAttribute("sakai-controlling-portal", null);
			try
			{
				// recognize an optional page/pageid
				String pageId = null;
				// may also have the tool part, so check that length is 5 or greater.
				if ((parts.length >= 5) && (parts[3].equals("page")))
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
				return END;
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

	public void doSite(HttpServletRequest req, HttpServletResponse res, Session session,
			String siteId, String pageId, String toolContextPath) throws ToolException,
			IOException
	{		
				
		boolean doFrameTop = "true".equals(req.getParameter("sakai.frame.top"));
		boolean doFrameSuppress = "true".equals(req.getParameter("sakai.frame.suppress"));

		// default site if not set
		if (siteId == null)
		{
			if (session.getUserId() == null)
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
				siteId = SiteService.getUserSiteId(session.getUserId());
			}
		}

		// Can get a URL like /portal/site/-/page/-/tool/sakai.rwiki.  
		// The "mutable" site and page can not be given specific values since the 
		// final resolution depends on looking up the specific placement of the tool 
		// with this common id in the my workspace for this user.
		
		// check for a mutable site to be resolved here
		if (mutableSitename.equalsIgnoreCase(siteId) && (session.getUserId() != null)) {
			siteId = SiteService.getUserSiteId(session.getUserId());
		}

		// if no page id, see if there was a last page visited for this site
		// if we are coming back from minimized navigation - go to the default
		// tool
		// Not the previous tool
		if (pageId == null && !doFrameSuppress)
		{
			pageId = (String) session.getAttribute(Portal.ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			Set<SecurityAdvisor> advisors = (Set<SecurityAdvisor>)session.getAttribute("sitevisit.security.advisor");
			if (advisors != null) {
				for (SecurityAdvisor advisor:advisors) {
					SecurityService.pushAdvisor(advisor);
					//session.removeAttribute("sitevisit.security.advisor");
				}
			}
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
		}

		if (site == null)
		{				
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				StoredState ss = portalService.newStoredState("directtool", "tool");
				ss.setRequest(req);
				ss.setToolContextPath(toolContextPath);
				portalService.setStoredState(ss);
				portal.doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				portal.doError(req, res, session, Portal.ERROR_SITE);
			}
			return;
		}

		// If the page is the mutable page name then look up the 
		// real page id from the tool name.
		if (mutablePagename.equalsIgnoreCase(pageId)) {
			pageId = findPageIdFromToolId(pageId, req.getPathInfo(), site);
		}
		
		// Lookup the page in the site - enforcing access control
		// business rules
		SitePage page = portal.getSiteHelper().lookupSitePage(pageId, site);
		if (page == null)
		{
			portal.doError(req, res, session, Portal.ERROR_SITE);
			return;
		}

		// store the last page visited
		session.setAttribute(Portal.ATTR_SITE_PAGE + siteId, page.getId());

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		String siteType = portal.calcSiteType(siteId);
		PortalRenderContext rcontext = portal.startPageContext(siteType, title, site
				.getSkin(), req);
		
		// Have we been requested to display minimized and are we logged in?
		if (session.getUserId() != null ) {
			Cookie c = portal.findCookie(req, portal.SAKAI_NAV_MINIMIZED);
                        String reqParm = req.getParameter(portal.SAKAI_NAV_MINIMIZED);
                	String minStr = ServerConfigurationService.getString("portal.allow.auto.minimize","true");
                	if ( c != null && "true".equals(c.getValue()) ) {
				rcontext.put(portal.SAKAI_NAV_MINIMIZED, Boolean.TRUE);
			} else if ( reqParm != null &&  "true".equals(reqParm) && ! "false".equals(minStr) ) {
				rcontext.put(portal.SAKAI_NAV_MINIMIZED, Boolean.TRUE);
			}
		}

		// should we consider a frameset ?
		boolean doFrameSet = includeFrameset(rcontext, res, req, session, page);
				
				
		setSiteLanguage(site);				
				
		
		includeSiteNav(rcontext, req, session, siteId);

		if ( !doFrameTop && !doFrameSet )
		{
			includeWorksite(rcontext, res, req, session, site, page, toolContextPath,
					getUrlFragment());

			// Include sub-sites if appropriate
			// TODO: Thing through whether we want reset tools or not
			portal.includeSubSites(rcontext, req, session, siteId, req.getContextPath()
					+ req.getServletPath(), getUrlFragment(),
			/* resetTools */false);

			portal.includeBottom(rcontext);
		}


//Log the visit into SAKAI_EVENT - begin
		try{
			boolean presenceEvents = ServerConfigurationService.getBoolean("presence.events.log", true);
			if (presenceEvents)
				org.sakaiproject.presence.cover.PresenceService.setPresence(siteId + "-presence");
		}catch(Exception e){}
//End - log the visit into SAKAI_EVENT		
		rcontext.put("currentUrlPath", Web.serverUrl(req) + req.getContextPath()
				+ req.getPathInfo());

		// end the response
		if (doFrameTop)
		{
			// Place the proper values in context for the Frame Top panel
			rcontext.put("sakaiFrameEdit", req.getParameter("sakai.frame.edit"));
			rcontext.put("sakaiFrameTitle", req.getParameter("sakai.frame.title"));
			rcontext.put("sakaiFrameReset", req.getParameter("sakai.frame.reset"));
			rcontext.put("sakaiFramePortlet", req.getParameter("sakai.frame.portlet"));
			doSendFrameTop(rcontext, res, null);
		}
		else if (doFrameSet)
		{
			doSendFrameSet(rcontext, res, null);
		}
		else
		{
			doSendResponse(rcontext, res, null);
		}

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
				if (log.isDebugEnabled()) {
					log.debug("trying to resolve page id from toolId: ["+toolId+"]");
				}
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
	 * @param rcontext
	 * @param res
	 * @param object
	 * @throws IOException 
	 */
	protected void doSendFrameSet(PortalRenderContext rcontext, 
		HttpServletResponse res, String contentType) 
		throws IOException
	{
		// if we realized that we needed a frameset, we could eliminate 90% of the 
		// view context processing. At the moment we do everything that we need for
		// a full page.... which is a waste.
		portal.sendResponse(rcontext, res, "site-frame-set", null);
	}

	/**
	 * Does the final framed render response, classes that extend this class
	 * may/will want to override this method to use their own template
	 * 
	 * @param rcontext
	 * @param res
	 * @param frameset
	 * @param object
	 * @param b
	 * @throws IOException
	 */
	protected void doSendFrameTop(PortalRenderContext rcontext,
			HttpServletResponse res, String contentType)
			throws IOException
	{
		portal.sendResponse(rcontext, res, "site-frame-top", null);
	}

	/**
	 * Does the final non framed render response, classes that extend this class
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
		String templates = ServerConfigurationService.getString("portal.templates", "neoskin");
		String prefix = ServerConfigurationService.getString("portal.neoprefix", "neo-");
		if ( "neoskin".equals(templates) ) skin = prefix + skin;
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
			if ("true".equals(ServerConfigurationService
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
		            if (roleswitchvalue != null && !userRole.getId().equals(roleswitchvalue))
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

			int tabsToDisplay = configuredTabsToDisplay;

			if (!loggedIn)
			{
				tabsToDisplay = ServerConfigurationService.getInt(
						"gatewaySiteListDisplayCount", tabsToDisplay);
			}
			else
			{
				Preferences prefs = PreferencesService
						.getPreferences(session.getUserId());
				ResourceProperties props = prefs.getProperties("sakai:portal:sitenav");
				try
				{
					tabsToDisplay = (int) props.getLongProperty("tabs");
				}
				catch (Exception any)
				{
				}
			}

			rcontext.put("useDHTMLMore", useDHTMLMore);
			rcontext.put("showSearchWhen", showSearchWhen);
			if (useDHTMLMore)
			{
				SiteView siteView = portal.getSiteHelper().getSitesView(
						SiteView.View.DHTML_MORE_VIEW, req, session, siteId);
				siteView.setPrefix(prefix);
				siteView.setToolContextPath(null);
				rcontext.put("tabsSites", siteView.getRenderContextObject());
			}
			else
			{
				SiteView siteView = portal.getSiteHelper().getSitesView(
						SiteView.View.DEFAULT_SITE_VIEW, req, session, siteId);
				siteView.setPrefix(prefix);
				siteView.setToolContextPath(null);
				rcontext.put("tabsSites", siteView.getRenderContextObject());
			}

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
		}
	}
	/**
	 * @param rcontext
	 * @param res
	 * @param req
	 * @param session
	 * @param page
	 * @return
	 * @throws IOException
	 */
	protected boolean includeFrameset(PortalRenderContext rcontext,
			HttpServletResponse res, HttpServletRequest req, Session session,
			SitePage page) throws IOException
	{
		if ( "true".equals(req.getParameter("sakai.frame.suppress")) ) {
			return false;
		}

		boolean framesetRequested = false;

		String framesetConfig = ServerConfigurationService
				.getString(Portal.FRAMESET_SUPPORT);
		if (framesetConfig == null || framesetConfig.trim().length() == 0
				|| "never".equals(framesetConfig))
		{
			// never do a frameset
			return false;
		}
		
		Site site = null;
		try
		{
			site = SiteService.getSite(page.getSiteId());
		}
		catch (Exception ignoreMe)
		{
			// Non fatal - just assume null
			if (log.isTraceEnabled())
				log.trace("includePage unable to find site for page " + page.getId());
		}
		
		Map singleToolMap = null;
		ToolConfiguration singleTool = null;
		List tools = page.getTools(0);
		int toolCount = 0;
		for (Iterator i = tools.iterator(); i.hasNext();)
		{
			ToolConfiguration placement = (ToolConfiguration) i.next();

			if (site != null)
			{
				boolean thisTool = portal.getSiteHelper().allowTool(site, placement);
				// System.out.println(" Allow Tool Display -" +
				// placement.getTitle() + " retval = " + thisTool);
				if (!thisTool) continue; // Skip this tool if not
				// allowed
			}

			if ( placement != null ) {
				singleTool = placement;
				singleToolMap = portal.includeTool(res, req, placement);
				toolCount++;
				if ( toolCount > 1 ) return false;
			}
		}

		// Determine if this page can be in a frame set, if so place the
		// appropriate materials into the context
		if (singleTool != null )
		{

			rcontext.put("singleToolMap", singleToolMap);

			String maximizedUrl = (String) session
					.getAttribute(Portal.ATTR_MAXIMIZED_URL);
			session.setAttribute(Portal.ATTR_MAXIMIZED_URL, null);

			if (maximizedUrl != null)
			{
				framesetRequested = true;
				rcontext.put("frameMaximizedUrl", maximizedUrl);
			}

			// If tool configuration property is set for tool - do request
			String toolConfigMax = singleTool.getConfig().getProperty(
					Portal.PREFER_MAXIMIZE);
			if ("true".equals(toolConfigMax)) {
				framesetRequested = true;
			}

			if ("always".equals(framesetConfig)) {
				framesetRequested = true;
			}
			if ("never".equals(framesetConfig)) {
				framesetRequested = false;
			}

			// JSR-168 portlets cannot be in a frameset unless they asked for
			// a maximized URL
			if (singleToolMap.get("isPortletPlacement") != null && maximizedUrl == null)
			{
				framesetRequested = false;
			}

			if (framesetRequested) rcontext.put("sakaiFrameSetRequested", Boolean.TRUE);
		}
		return framesetRequested;
	}
	
	/**
	 * *
	 * 
	 * @return Locale based on its string representation (language_region)
	 */
	private Locale getLocaleFromString(String localeString)
	{
		String[] locValues = localeString.trim().split("_");
		if (locValues.length >= 3)
			return new Locale(locValues[0], locValues[1], locValues[2]); // language, country, variant
		else if (locValues.length == 2)
			return new Locale(locValues[0], locValues[1]); // language, country
		else if (locValues.length == 1)
			return new Locale(locValues[0]); // language
		else
			return Locale.getDefault();
	}
	
		
	void setSiteLanguage(Site site)
	{
		ResourceLoader rl = new ResourceLoader();
				
		ResourcePropertiesEdit props = site.getPropertiesEdit();
				
		String locale_string = props.getProperty("locale_string");
						
		Locale loc;
				
		// if no language was specified when creating the site, set default language to session
		if(locale_string == null || locale_string == "")
		{					
			loc = rl.setContextLocale(null);
		}
		
		// if you have indicated a language when creating the site, set selected language to session
		else
		{				
			Locale locale = getLocaleFromString(locale_string);			
			loc = rl.setContextLocale(locale);			
		}
	}

}
