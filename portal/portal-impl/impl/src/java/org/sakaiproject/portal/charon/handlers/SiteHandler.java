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
import java.util.Collections;
import java.util.Comparator;
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
import org.sakaiproject.entity.api.Reference;
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
import org.sakaiproject.alias.cover.AliasService;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.util.Web;

/**
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class SiteHandler extends WorksiteHandler
{

	private static final String INCLUDE_SITE_NAV = "include-site-nav";

	private static final String INCLUDE_LOGO = "include-logo";

	private static final String INCLUDE_TABS = "include-tabs";

	private static final Log log = LogFactory.getLog(SiteHandler.class);

	private PortalSiteHelper siteHelper = new PortalSiteHelper();

	private int configuredTabsToDisplay = 5;

	private boolean useDHTMLMore = false;

	public SiteHandler()
	{
		urlFragment = "site";
        configuredTabsToDisplay  = ServerConfigurationService.getInt(Portal.CONFIG_DEFAULT_TABS, 5);
        useDHTMLMore =  Boolean.valueOf(ServerConfigurationService.getBoolean("portal.use.dhtml.more", false));
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if ((parts.length >= 2) && (parts[1].equals("site")))
		{
			// This is part of the main portal so we simply remove the attribute
			session.setAttribute("sakai-controlling-portal",null);
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
			// continue on to alias check
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
				return;
			}
			// otherwise continue on to alias check
		}

		// Now check for site alias
		if ( site == null )
		{
			try
			{
				// First check for site alias
				if ( siteId!= null && !siteId.equals("") && !SiteService.siteExists(siteId) )
				{
					String refString = AliasService.getTarget(siteId);
					siteId = EntityManager.newReference(refString).getContainer();
				}
				
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
		}

		// Try to lookup alias if pageId not found
		if (pageId != null && !pageId.equals("") && site.getPage(pageId) == null)
		{
			try
			{
				String refString = AliasService.getTarget(pageId);
				pageId = EntityManager.newReference(refString).getId();
			}
			catch (IdUnusedException e) {
				log.warn("Alias does not resolve "+e.getMessage());
			}
		}
		
		// Lookup the page in the site - enforcing access control
		// business rules
		SitePage page = siteHelper.lookupSitePage(pageId, site);
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

		// Include sub-sites if appropriate
		// TODO: Thing through whether we want reset tools or not
		portal.includeSubSites(rcontext, req, session,
			siteId,  req.getContextPath() + req.getServletPath(), "site",
			/* resetTools */ false );

		portal.includeBottom(rcontext);

		// end the response
		portal.sendResponse(rcontext, res, "site", null);
		StoredState ss = portalService.getStoredState();
		if (ss != null && toolContextPath.equals(ss.getToolContextPath()))
		{
			// This request is the destination of the request
			portalService.setStoredState(null);
		}
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

			String skin = getSiteSkin(siteId);

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
				String alternateSiteId = SiteService.getUserSiteId(userId);
				skin = SiteService.getSiteSkin(alternateSiteId);
			}
			catch (Exception e)
			{
				// Ignore
			}
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
			String origPrefix = prefix;

			// If we have turned on auto-state reset on navigation, we generate
			// the "site-reset" "worksite-reset" and "gallery-reset" urls
			if ("true".equals(ServerConfigurationService
					.getString(Portal.CONFIG_AUTO_RESET)))
			{
				prefix = prefix + "-reset";
			}

			boolean loggedIn = session.getUserId() != null;
			// Get the user's My WorkSpace and its ID
			Site myWorkspaceSite = siteHelper.getMyWorkspace(session);
			String myWorkspaceSiteId = null;
			if (myWorkspaceSite != null)
			{
				myWorkspaceSiteId = siteHelper.getSiteEffectiveId(myWorkspaceSite);
			}

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
         
			// Get the list of sites in the right order,
			// My WorkSpace will be the first in the list
			List<Site> mySites = siteHelper.getAllSites(req, session, true);
         
			// if public workgroup/gateway site is not included, add to list
			boolean siteFound = false;
			for ( int i=0; i< mySites.size(); i++ )
			{
				if ( ((Site)mySites.get(i)).getId().equals(siteId) ) {
					siteFound = true;
				}
			}
			try
			{
				if (!siteFound) {
					mySites.add( SiteService.getSite(siteId) );
				} 
			}
			catch ( IdUnusedException e) {
				
			} // ignore
				
			// Note that if there are exactly one more site
			// than tabs allowed - simply put the site on
			// instead of a dropdown with one site
			List<Site> moreSites = new ArrayList<Site>();
			if (mySites.size() > (tabsToDisplay + 1))
			{
				// Check to see if the selected site is in the first
				// "tabsToDisplay" tabs
				boolean found = false;
				for (int i = 0; i < tabsToDisplay && i < mySites.size(); i++)
				{
					Site site = mySites.get(i);
					String effectiveId = siteHelper.getSiteEffectiveId(site);
					if (site.getId().equals(siteId) || effectiveId.equals(siteId))
						found = true;
				}

				// Save space for the current site
				if (!found) tabsToDisplay = tabsToDisplay - 1;
				if (tabsToDisplay < 2) tabsToDisplay = 2;

				// Create the list of "additional sites"- but do not
				// include the currently selected set in the list
				Site currentSelectedSite = null;

				int remove = mySites.size() - tabsToDisplay;
				for (int i = 0; i < remove; i++)
				{
					// We add the site the the drop-down
					// unless it it the current site in which case
					// we retain it for later
					Site site = mySites.get(tabsToDisplay);
					mySites.remove(tabsToDisplay);

					String effectiveId = siteHelper.getSiteEffectiveId(site);
					if (site.getId().equals(siteId) || effectiveId.equals(siteId))
					{
						currentSelectedSite = site;
					}
					else
					{
						moreSites.add(site);
					}
				}

				// check to see if we need to re-add the current site
				if ( currentSelectedSite != null ) 
				{
					mySites.add(currentSelectedSite);
				}
			}
			
			
			if (useDHTMLMore)
			{
				List<Site> allSites = new ArrayList<Site>();
				allSites.addAll(mySites);
				allSites.addAll(moreSites);
				// get Sections
				Map<String, List> termsToSites = new HashMap<String, List>();
				Map<String, List> tabsMoreTerms = new HashMap<String, List>();
				for (int i = 0; i < allSites.size(); i++)
				{
					Site site = allSites.get(i);
					ResourceProperties siteProperties = site.getProperties();

					String type = site.getType();
					String term = null;

					if ("course".equals(type))
					{
						term = siteProperties.getProperty("term");
					}
					else if ("project".equals(type))
					{
						term = "PROJECTS";
					}
					else if ("portfolio".equals(type))
					{
						term = "PORTFOLIOS";
					}
					else if ("admin".equals(type))
					{
						term = "ADMINISTRATION";
					}
					else
					{
						term = "OTHER";
					}

					List<Site> currentList = new ArrayList();
					if (termsToSites.containsKey(term))
					{
						currentList = termsToSites.get(term);
						termsToSites.remove(term);
					}
					currentList.add(site);
					termsToSites.put(term, currentList);
				}

				class TitleSorter implements Comparator<Map>
				{

					public int compare(Map first, Map second)
					{

						if (first == null && second == null) return 0;

						String firstTitle = (String) first.get("siteTitle");
						String secondTitle = (String) second.get("siteTitle");

						if (firstTitle != null)
							return firstTitle.compareToIgnoreCase(secondTitle);

						return 0;

					}

				}

				Comparator<Map> titleSorter = new TitleSorter();

				// now loop through each section and convert the Lists to maps
				for (String key : termsToSites.keySet())
				{
					List<Site> currentList = termsToSites.get(key);
					List<Map> temp = portal.convertSitesToMaps(req, currentList, prefix,
							siteId, myWorkspaceSiteId,
							/* includeSummary */false, /* expandSite */false,
							/* resetTools */"true".equals(ServerConfigurationService
									.getString(Portal.CONFIG_AUTO_RESET)),
							/* doPages */true, /* toolContextPath */null, loggedIn);

					Collections.sort(temp, titleSorter);

					tabsMoreTerms.put(key, temp);

				}

				String[] termOrder = ServerConfigurationService
						.getStrings("portal.term.order");
				List<String> tabsMoreSortedTermList = new ArrayList<String>();

				// Order term column headers according to order specified in
				// portal.term.order
				// Filter out terms for which user is not a member of any sites

				if (termOrder != null)
				{
					for (int i = 0; i < termOrder.length; i++)
					{

						if (tabsMoreTerms.containsKey(termOrder[i]))
						{

							tabsMoreSortedTermList.add(termOrder[i]);

						}

					}
				}

				Iterator i = tabsMoreTerms.keySet().iterator();
				while (i.hasNext())
				{
					String term = (String) i.next();
					if (!tabsMoreSortedTermList.contains(term))
					{
						tabsMoreSortedTermList.add(term);

					}
				}
				rcontext.put("tabsMoreTerms", tabsMoreTerms);
				rcontext.put("tabsMoreSortedTermList", tabsMoreSortedTermList);
			}
			rcontext.put("useDHTMLMore", useDHTMLMore);
		
		
			
			
			
		

			String cssClass = (siteType != null) ? "siteNavWrap " + siteType
					: "siteNavWrap";

			rcontext.put("tabsCssClass", cssClass);

			List<Map> l = portal.convertSitesToMaps(req, mySites, prefix, siteId,
					myWorkspaceSiteId,
					/* includeSummary */false, /* expandSite */false,
					/* resetTools */"true".equals(ServerConfigurationService
							.getString(Portal.CONFIG_AUTO_RESET)),
					/* doPages */true, /* toolContextPath */null, loggedIn);

			rcontext.put("tabsSites", l);

			rcontext.put("tabsMoreSitesShow", Boolean.valueOf(moreSites.size() > 0));

			// more dropdown
			if (moreSites.size() > 0)
			{
				List<Map> m = portal.convertSitesToMaps(req, moreSites, prefix, siteId,
						myWorkspaceSiteId,
						/* includeSummary */false, /* expandSite */false,
						/* resetTools */"true".equals(ServerConfigurationService
								.getString(Portal.CONFIG_AUTO_RESET)),
						/* doPages */true, /* toolContextPath */null, loggedIn);

				rcontext.put("tabsMoreSites", m);
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
