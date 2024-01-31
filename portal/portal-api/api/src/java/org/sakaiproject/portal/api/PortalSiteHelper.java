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

package org.sakaiproject.portal.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.SiteView.View;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;

/**
 * @author ieb
 *
 */
public interface PortalSiteHelper
{

	/**
	 * @param siteId
	 * @return
	 */
	Site getSiteVisit(String siteId) throws IdUnusedException, PermissionException;

	/**
	 * @param session
	 * @return
	 */
	Site getMyWorkspace(Session session);

	/**
	 * @param site
	 * @param placement
	 * @return
	 */
	boolean allowTool(Site site, Placement placement);


	/**
	 * @return
	 */
	boolean doGatewaySiteList();
	
	/**
	 * @return
	 */
	String getGatewaySiteId();

	/**
	 * @param pageId
	 * @param site
	 * @return
	 */
	SitePage lookupSitePage(String pageId, Site site);

	/**
	 * Produce a page and/or a tool list by iterating through the pages in a site.
	 *
	 * @param req the http request
	 * @param loggedIn if user is logged in
	 * @param site the site
	 * @param page the current page
	 * @param toolContextPath path for this tool
	 * @param portalPrefix portal prefix
	 * @param doPages true is best for the tabs-based portal and for RSS,
	 *                false is best for the portlet-style as it unrolls all of the tools unless a page is marked as a popup.
	 * @param resetTools true will generate resetting tool URLs
	 * @param includeSummary
	 * @return a map with information about the site's pages
	 */
	Map<String, Object> pageListToMap(HttpServletRequest req, boolean loggedIn, Site site,
                        SitePage page, String toolContextPath, String portalPrefix, boolean doPages,
                        boolean resetTools, boolean includeSummary);

	/**
	 * Explode a site into a map suitable for use in the map
	 *
	 */
	Map<String, Object> convertSiteToMap(HttpServletRequest req, Site s, String prefix,
										 String currentSiteId, String myWorkspaceSiteId, boolean includeSummary,
										 boolean expandSite, boolean resetTools, boolean doPages,
										 String toolContextPath, boolean loggedIn, List<String> siteProviders);

	/**
	 * This method takes a list of sites and organizes it into a list of maps of
	 * properties. There is an additional complication that the depth contains
	 * informaiton arround.
	 *
	 */
	List<Map<String, Object>> convertSitesToMaps(HttpServletRequest req, List<Site> mySites,
								 String prefix, String currentSiteId, String myWorkspaceSiteId,
								 boolean includeSummary, boolean expandSite, boolean resetTools,
								 boolean doPages, String toolContextPath, boolean loggedIn);

	/**
	 * SAK-29138 - Get the site or section title for the current user for the current site.
	 * Takes into account 'portal.use.sectionTitle' sakai.property; if set to true,
	 * this method will return the title of the section the current user is enrolled
	 * in for the site (if it can be found). Otherwise, it will return the site
	 * title (default behaviour)
	 * 
	 * @param site the site in question
	 * @param escaped true if you want the site title HTML escaped, false otherwise
	 * @return the site or section title
	 */
	String getUserSpecificSiteTitle( Site site, boolean escaped );

	Map<String, Object> getContextSitesWithPages(HttpServletRequest req, String currentSiteId, String toolContextPath, boolean loggedIn);

	/**
	 * Similar to getUserSpecificSiteTitle(Site site, boolean escaped), but also takes truncated parameter
	 * @param site
	 * @param truncated true if you want long titles to be truncated with an ellipses
	 * @param escaped
	 */
	String getUserSpecificSiteTitle(Site site, boolean truncated, boolean escaped);

	/**
	 * Similar to getUserSpecificSiteTitle(Site site, boolean escaped), but consumes the specified siteProviders (for performance savings)
	 * @param site
	 * @param truncated true if you want long titles to be truncated with an ellipses
	 * @param escaped
	 * @param siteProviders the site providers corresponding to the specified site; if null, they will be looked up
	 */
	String getUserSpecificSiteTitle(Site site, boolean truncated, boolean escaped, List<String> siteProviders);

	/**
	 * Generates a SiteView object from the current request and location
	 * @param view
	 * @param req
	 * @param session 
	 * @param siteId
	 * @return
	 */
	SiteView getSitesView(View view, HttpServletRequest req, Session session, String siteId);

	/**
	 * Find an alias for a page.
	 * @param siteId
	 * @param page
	 * @return <code>null</code> if no alias was found, otherwise the short alias for the page.
	 */
	String lookupPageToAlias(String siteId, SitePage page);

	/**
	 * Check if the site is joinable by the supplied user and the user isn't currently a member
	 * of the site.
	 * @param siteId The ID of the site.
	 * @param userId The ID of the user.
	 * @return <code>true</code> if the user can join the site.
	 */
	boolean isJoinable(String siteId, String userId);

	/**
	 * This looks up a site but also checks for any aliases.
	 * This doesn't do any permission checks.
	 * @param siteId The siteId/alias to lookup
	 * @return The site.
	 * @throws IdUnusedException If the siteId doesn't exist and there isn't an alias for this.
	 */
	Site getSite(String siteId) throws IdUnusedException;

	boolean checkGradebookVisibility(ToolConfiguration tc, Site site);
}
