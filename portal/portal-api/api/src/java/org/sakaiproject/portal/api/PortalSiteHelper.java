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

package org.sakaiproject.portal.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.SiteView.View;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
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
	 * @param portal
	 * @param pageId
	 * @param site
	 * @return
	 */
	SitePage lookupSitePage(String pageId, Site site);
	
	/**
	 * Iterate through the pages in a site and return information in a 
	 * Map.
	 * @param req
	 * @param loggedIn
	 * @param site
	 * @param page
	 * @param toolContextPath
	 * @param portalPrefix
	 * @param doPages
	 * @param resetTools
	 * @param includeSummary
	 */
	Map pageListToMap(HttpServletRequest req, boolean loggedIn, Site site,
                        SitePage page, String toolContextPath, String portalPrefix, boolean doPages,
                        boolean resetTools, boolean includeSummary);

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
	public String lookupPageToAlias(String siteId, SitePage page);


}
