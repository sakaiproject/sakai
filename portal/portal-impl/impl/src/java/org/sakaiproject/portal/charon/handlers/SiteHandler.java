/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.util.PortalSiteHelper;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Web;

/**
 * @author ieb
 */
public class SiteHandler extends WorksiteHandler
{

	private static final String INCLUDE_SITE_NAV = "include-site-nav";

	private static final String INCLUDE_LOGO = "include-logo";

	private static final String INCLUDE_TABS = "include-tabs";

	private static final Log log = LogFactory.getLog(SiteHandler.class);

	private PortalSiteHelper siteHelper = new PortalSiteHelper();

	public SiteHandler()
	{
		urlFragment = "site";
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if ((parts.length >= 2) && (parts[1].equals("site")))
		{
			try
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
			pageId = (String) session.getAttribute(Portal.ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			site = siteHelper.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			portal.doError(req, res, session, Portal.ERROR_SITE);
			return;
		}
		catch (PermissionException e)
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

		// the 'full' top area
		includeSiteNav(rcontext, req, session, siteId);

		includeWorksite(rcontext, res, req, session, site, page, toolContextPath, "site");

		portal.includeBottom(rcontext);

		// end the response
		portal.sendResponse(rcontext, res, "site", null);
		portalService.setStoredState(null);
	}

	protected void includeSiteNav(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, String siteId)
	{
		if (rcontext.uses(INCLUDE_SITE_NAV))
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

			String accessibilityURL = ServerConfigurationService
					.getString("accessibility.url");
			rcontext.put("siteNavHasAccessibilityURL", Boolean
					.valueOf((accessibilityURL != null && accessibilityURL != "")));
			rcontext.put("siteNavAccessibilityURL", accessibilityURL);
			// rcontext.put("siteNavSitAccessability",
			// Web.escapeHtml(rb.getString("sit_accessibility")));
			// rcontext.put("siteNavSitJumpContent",
			// Web.escapeHtml(rb.getString("sit_jumpcontent")));
			// rcontext.put("siteNavSitJumpTools",
			// Web.escapeHtml(rb.getString("sit_jumptools")));
			// rcontext.put("siteNavSitJumpWorksite",
			// Web.escapeHtml(rb.getString("sit_jumpworksite")));

			rcontext.put("siteNavLoggedIn", Boolean.valueOf(loggedIn));

			try
			{
				if (loggedIn)
				{
					includeLogo(rcontext, req, session, siteId);
					includeTabs(rcontext, req, session, siteId, "site", false);
				}
				else
				{
					includeLogo(rcontext, req, session, siteId);
					if (siteHelper.doGatewaySiteList())
						includeTabs(rcontext, req, session, siteId, "site", false);
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

			String skin = SiteService.getSiteSkin(siteId);
			if (skin == null)
			{
				skin = ServerConfigurationService.getString("skin.default");
			}
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

	public void includeTabs(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, String siteId, String prefix, boolean addLogout)
			throws IOException
	{

		if (rcontext.uses(INCLUDE_TABS))
		{

			// for skinning
			String siteType = portal.calcSiteType(siteId);
			String origPrefix = prefix;

			// If we have turned on auto-state reset on navigation, we generate
			// the
			// "site-reset" "worksite-reset" and "gallery-reset" urls
			if ("true".equals(ServerConfigurationService
					.getString(Portal.CONFIG_AUTO_RESET)))
			{
				prefix = prefix + "-reset";
			}

			boolean loggedIn = session.getUserId() != null;
			boolean curMyWorkspace = false;
			String myWorkspaceSiteId = null;
			int prefTabs = 4;
			int tabsToDisplay = prefTabs;

			// Get the list of sites in the right order, don't include My
			// WorkSpace
			List<Site> mySites = siteHelper.getAllSites(req, session, false);
			if (!loggedIn)
			{
				prefTabs = ServerConfigurationService.getInt(
						"gatewaySiteListDisplayCount", prefTabs);
			}
			else
			{
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

				// TODO: Clean this mess up and just put the workspace in the
				// context
				// and let the vm figure it out using isMyWorkSpace
				curMyWorkspace = ((siteId == null) || (SiteService.isUserSite(siteId) && ((SiteService
						.getSiteUserId(siteId).equals(curUserId) || SiteService
						.getSiteUserId(siteId).equals(curUserEid)))));

				// if this is a My Workspace, it gets its own tab and should not
				// be
				// considered in the other tab logic - but save the ID for later
				if (curMyWorkspace)
				{
					myWorkspaceSiteId = siteId;
					siteId = null;
				}

				// collect the user's preferences
				if (session.getUserId() != null)
				{
					Preferences prefs = PreferencesService.getPreferences(session
							.getUserId());
					ResourceProperties props = prefs
							.getProperties("sakai:portal:sitenav");
					try
					{
						prefTabs = (int) props.getLongProperty("tabs");
					}
					catch (Exception any)
					{
					}
				}
			} // End if ( loggedIn )

			// Prepare for display across the top...
			// split into 2 lists - the first n, and the rest
			List<Site> moreSites = new ArrayList<Site>();
			if (mySites.size() > tabsToDisplay)
			{
				int remove = mySites.size() - tabsToDisplay;
				for (int i = 0; i < remove; i++)
				{
					Site site = mySites.get(tabsToDisplay);

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
						if (loggedIn && SiteService.isUserSite(siteId))
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
								log.warn("includeTabs: cur site not found (not ~eid): "
										+ siteId);
							}
							catch (IdUnusedException ee)
							{
								log
										.warn("includeTabs: cur site not found (assumed ~eid, didn't find site): "
												+ siteId);
							}
						}
						else
						{
							log.warn("includeTabs: cur site not found: " + siteId);
						}
					}
				}
			}

			String cssClass = (siteType != null) ? "siteNavWrap " + siteType
					: "siteNavWrap";

			if (loggedIn)
			{
				String mySiteUrl = Web.serverUrl(req)
						+ ServerConfigurationService.getString("portalPath")
						+ "/"
						+ prefix
						+ "/"
						+ Web
								.escapeUrl(portal.getUserEidBasedSiteId(session
										.getUserId()));
				rcontext.put("tabsSiteUrl", mySiteUrl);
			}

			rcontext.put("tabsCssClass", cssClass);
			// rcontext.put("tabsSitWorksiteHead",
			// Web.escapeHtml(rb.getString("sit_worksiteshead")));
			rcontext.put("tabsCurMyWorkspace", Boolean.valueOf(curMyWorkspace));
			// rcontext.put("tabsSitMyWorkspace", rb.getString("sit_mywor"));

			// rcontext.put("tabsSitWorksite",
			// Web.escapeHtml(rb.getString("sit_worksite")));

			List<Map> l = portal.convertSitesToMaps(req, mySites, origPrefix, siteId,
					myWorkspaceSiteId,
					/* includeSummary */false, /* expandSite */false,
					/* resetTools */"true".equals(ServerConfigurationService
							.getString(Portal.CONFIG_AUTO_RESET)),
					/* doPages */true, /* toolContextPath */null, loggedIn);

			rcontext.put("tabsSites", l);

			rcontext.put("tabsHasExtraTitle", Boolean.valueOf(extraTitle != null));

			// current site, if not in the list of first n tabs
			if (extraTitle != null)
			{
				rcontext.put("tabsExtraTitle", Web.escapeHtml(extraTitle));
			}

			rcontext.put("tabsMoreSitesShow", Boolean.valueOf(moreSites.size() > 0));
			// rcontext.put("tabsSitMore",
			// Web.escapeHtml(rb.getString("sit_more")));
			// rcontext.put("tabsSitSelectMessage",
			// Web.escapeHtml(rb.getString("sit_selectmessage")));
			// more dropdown
			if (moreSites.size() > 0)
			{

				l = new ArrayList<Map>();

				for (Iterator i = moreSites.iterator(); i.hasNext();)
				{
					Map<String, Object> m = new HashMap<String, Object>();

					Site s = (Site) i.next();
					String siteUrl = Web.serverUrl(req)
							+ ServerConfigurationService.getString("portalPath") + "/"
							+ prefix + "/" + siteHelper.getSiteEffectiveId(s);
					m.put("siteTitle", Web.escapeHtml(s.getTitle()));
					m.put("siteUrl", siteUrl);
					l.add(m);
				}
				rcontext.put("tabsMoreSites", l);
			}

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

}
