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

package org.sakaiproject.portal.charon;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;

/**
 * @author ieb
 *
 */
public interface Portal
{
	
	
	/**
	 * Error response modes.
	 */
	public static final int ERROR_SITE = 0;

	public static final int ERROR_GALLERY = 1;

	public static final int ERROR_WORKSITE = 2;
	/**
	 * Parameter value to allow anonymous users of gallery mode to be sent to the gateway site as anonymous user (like the /portal URL) instead of making them log in (like worksite, site, and tool URLs).
	 */
	public static final String PARAM_FORCE_LOGIN = "force.login";

	public static final String PARAM_FORCE_LOGOUT = "force.logout";
	/**
	 * ThreadLocal attribute set while we are processing an error.
	 */
	public static final String ATTR_ERROR = "org.sakaiproject.portal.error";
	/**
	 * Session attribute root for storing a site's last page visited - just append the site id.
	 */
	public static final String ATTR_SITE_PAGE = "sakai.portal.site.";

	/**
	 * Configuration option to enable/disable state reset on navigation change
	 */
	public static final String CONFIG_AUTO_RESET = "portal.experimental.auto.reset";

	/**
	 * Names of tool config/registration attributes that control the rendering of the tool's titlebar
	 */
	public static final String TOOLCONFIG_SHOW_RESET_BUTTON = "reset.button";

	public static final String TOOLCONFIG_SHOW_HELP_BUTTON = "help.button";

	public static final String TOOLCONFIG_HELP_DOCUMENT_ID = "help.id";

	public static final String TOOLCONFIG_HELP_DOCUMENT_URL = "help.url";


	/**
	 * @param rcontext
	 * @param res
	 * @param string
	 * @param string2
	 * @throws IOException 
	 */
	void sendResponse(PortalRenderContext rcontext, HttpServletResponse res, String string, String string2) throws IOException;

	/**
	 * @param req
	 * @param res
	 * @param session
	 * @param string
	 * @param b
	 * @return
	 * @throws ToolException 
	 */
	String getPlacement(HttpServletRequest req, HttpServletResponse res, Session session, String string, boolean b) throws ToolException;



	/**
	 * @param req
	 * @param res
	 * @param session
	 * @param string
	 * @param b
	 * @throws ToolException 
	 */
	void doLogin(HttpServletRequest req, HttpServletResponse res, Session session, String string, boolean b) throws ToolException;

	/**
	 * @param req
	 * @param res
	 * @param session
	 * @param string
	 * @throws ToolException 
	 */
	void doLogout(HttpServletRequest req, HttpServletResponse res, Session session, String string) throws ToolException;







	/**
	 * 
	 * @param siteType
	 * @param title
	 * @param skin
	 * @param request
	 * @return
	 */
	PortalRenderContext startPageContext(String siteType, String title, String skin, HttpServletRequest request);

	/**
	 * @param res
	 * @return
	 * @throws IOException 
	 */
	boolean redirectIfLoggedOut(HttpServletResponse res) throws IOException;

	/**
	 * @param siteTool
	 * @return
	 */
	String getPortalPageUrl(ToolConfiguration siteTool);


	/**
	 * @param req
	 * @param res
	 * @param session
	 * @param error_worksite2
	 * @throws IOException 
	 * @throws ToolException 
	 */
	void doError(HttpServletRequest req, HttpServletResponse res, Session session, int error_worksite2) throws ToolException, IOException;

	/**
	 * @param tool
	 * @param req
	 * @param res
	 * @param siteTool
	 * @param skin
	 * @param toolContextPath
	 * @param toolPathInfo
	 * @throws IOException 
	 * @throws ToolException 
	 */
	void forwardPortal(ActiveTool tool, HttpServletRequest req, HttpServletResponse res, ToolConfiguration siteTool, String skin, String toolContextPath, String toolPathInfo) throws ToolException, IOException;

	/**
	 * @param req
	 * @param res
	 * @param p
	 * @param skin
	 */
	void setupForward(HttpServletRequest req, HttpServletResponse res, Placement p, String skin) throws ToolException;

	/**
	 * @param rcontext
	 */
	void includeBottom(PortalRenderContext rcontext);

	/**
	 * @param siteId
	 * @return
	 */
	String calcSiteType(String siteId);


	/**
	 * @param rcontext
	 * @param req
	 * @param session
	 */
	void includeLogin(PortalRenderContext rcontext, HttpServletRequest req, Session session);


	/**
	 * @param tool
	 * @param req
	 * @param res
	 * @param siteTool
	 * @param skin
	 * @param toolContextPath
	 * @param toolPathInfo
	 * @throws ToolException 
	 */
	void forwardTool(ActiveTool tool, HttpServletRequest req, HttpServletResponse res, Placement placement, String skin, String toolContextPath, String toolPathInfo) throws ToolException;



	/**
	 * @param userId
	 * @return
	 */
	String getUserEidBasedSiteId(String userId);

	/**
	 * 
	 * @param req
	 * @param mySites
	 * @param prefix
	 * @param currentSiteId
	 * @param myWorkspaceSiteId
	 * @param includeSummary
	 * @param expandSite
	 * @param resetTools
	 * @param doPages
	 * @param toolContextPath
	 * @param loggedIn
	 * @return
	 */
	List<Map> convertSitesToMaps(HttpServletRequest req, List mySites, String prefix, String currentSiteId,
			String myWorkspaceSiteId, boolean includeSummary, boolean expandSite, boolean resetTools, boolean doPages,
			String toolContextPath, boolean loggedIn);


	/**
	 * 
	 * @param req
	 * @param s
	 * @param prefix
	 * @param currentSiteId
	 * @param myWorkspaceSiteId
	 * @param includeSummary
	 * @param expandSite
	 * @param resetTools
	 * @param doPages
	 * @param toolContextPath
	 * @param loggedIn
	 * @return
	 */
	Map convertSiteToMap(HttpServletRequest req, Site s, String prefix, String currentSiteId, String myWorkspaceSiteId,
			boolean includeSummary, boolean expandSite, boolean resetTools, boolean doPages, String toolContextPath,
			boolean loggedIn);

	/**
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @param siteId
	 * @param toolId
	 * @param toolContextPath
	 * @param prefix
	 * @param doPages
	 * @param resetTools
	 * @param includeSummary
	 * @param expandSite
	 * @return
	 * @throws ToolException
	 * @throws IOException
	 */
	PortalRenderContext includePortal(HttpServletRequest req, HttpServletResponse res, Session session, String siteId,
			String toolId, String toolContextPath, String prefix, boolean doPages, boolean resetTools, boolean includeSummary,
			boolean expandSite) throws ToolException, IOException;

	/**
	 * @param res
	 * @param req
	 * @param placement
	 * @return
	 * @throws IOException 
	 */
	Map includeTool(HttpServletResponse res, HttpServletRequest req, ToolConfiguration placement) throws IOException;






}
