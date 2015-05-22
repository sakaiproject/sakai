/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.util;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.util.Web;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.SessionManager;

public class ToolUtils
{

	public static final String PORTAL_INLINE_EXPERIMENTAL = "portal.inline.experimental";
	public static final boolean PORTAL_INLINE_EXPERIMENTAL_DEFAULT = true;

	/**
	 * Determine if this is an inline request.
	 *
	 * @param <code>req</code>
	 *		The request object.
	 * @return True if this is a request where a tool will be inlined.
	 */
	public static boolean isInlineRequest(HttpServletRequest req)
	{
		// Note that with wrapped requests, URLUtils.getSafePathInfo may return null
		// so we use the request URI
		String uri = req.getRequestURI();
		if ( uri != null ) {
			String[] parts = uri.split("/");
			if ((parts.length >= 6) ) {
				return parts[4].equals("tool");
			}
			return false;
		}

		// Fall back to the system default
		return ServerConfigurationService.getBoolean(PORTAL_INLINE_EXPERIMENTAL, PORTAL_INLINE_EXPERIMENTAL_DEFAULT);
	}

	/**
	 * Captures the rules for getting the URL of a page suitable for a GET request
	 *
	 * @param <code>req</code>
	 *		The request object.
	 * @param <code>site</code>
	 *		The site that contains the page
	 * @param <code>page</code>
	 *		The page URL.   If this has a logged in session, we use it to set the
	 *		default prefix if available.   This will use the default prefix if called
	 *		without a session (i.e. through /direct).
	 */
	public static String getPageUrl(HttpServletRequest req, Site site, SitePage page)
	{
		// .../portal/site/1234
		String portalPrefix = "site";
		Session s = SessionManager.getCurrentSession();
		if ( s != null ) {
			String control = (String) s.getAttribute(PortalService.SAKAI_CONTROLLING_PORTAL);
			if ( control != null ) portalPrefix = control;
		}
		return getPageUrl(req, site, page, portalPrefix, true, null, null);
	}

	/**
	 * Captures the rules for getting the URL of a page suitable for a GET request
	 *
	 * @param <code>req</code>
	 *		The request object.
	 * @param <code>site</code>
	 *		The site that contains the page
	 * @param <code>page</code>
	 *		The page
	 * @param <code>portalPrefix</code>
	 *		The controlling portal.  Typically "site".  No slashes should be included
	 */
	public static String getPageUrl(HttpServletRequest req, Site site, SitePage page, String portalPrefix)
	{
		return getPageUrl(req, site, page, portalPrefix, true, null, null);
	}

	/**
	 * Captures the rules for getting the URL of a page suitable for a GET request
	 *
	 * @param <code>req</code>
	 *		The request object.
	 * @param <code>site</code>
	 *		The site that contains the page
	 * @param <code>page</code>
	 *		The page
	 * @param <code>portalPrefix</code>
	 *		The controlling portal.  Typically "site".  No slashes should be included
	 * @param <code>reset</code>
	 *		Should we reset the tool as part of this GET reference (typically true)
	 * @param <code>effectiveSiteId</code>
	 *		The effective site ID
	 * @param <code>pageAlias</code>
	 *		The alias for the page (typically null)
	 * @return The url for the page.
	 */
	public static String getPageUrl(HttpServletRequest req, Site site, SitePage page, 
		String portalPrefix, boolean reset, String effectiveSiteId, String pageAlias)
	{
		if ( effectiveSiteId == null ) effectiveSiteId = site.getId();
		if ( pageAlias == null ) pageAlias = page.getId();

		// The normal URL
		String pageUrl = Web.returnUrl(req, "/" + portalPrefix + "/"
				+ Web.escapeUrl(effectiveSiteId) + "/page/");
		pageUrl = pageUrl + Web.escapeUrl(pageAlias);

		List<ToolConfiguration> pTools = page.getTools();
		Iterator<ToolConfiguration> toolz = pTools.iterator();
		int count = 0;
		ToolConfiguration pageTool = null;
		while(toolz.hasNext()){
			count++;
			pageTool = toolz.next();
		}
		if ( count != 1 ) return pageUrl;
		if ( isPortletPlacement(pageTool) ) return pageUrl;

		boolean trinity = ServerConfigurationService.getBoolean(PORTAL_INLINE_EXPERIMENTAL, PORTAL_INLINE_EXPERIMENTAL_DEFAULT);
		if (!trinity) return pageUrl;

		pageUrl = Web.returnUrl(req, "/" + portalPrefix + "/" + effectiveSiteId);
		if (reset) {
			pageUrl = pageUrl + "/tool-reset/";
		} else {
			pageUrl = pageUrl + "/tool/";
		}
		return pageUrl + pageTool.getId();
	}

	/**
	 * Captures the rules for the various tools and when they want a popup
	 *
	 * @param <code>pageTool</code>
	 *		The tools configuration object.
	 * @return The url to be uded in the popup of null of there is no 
	 *		tool-requested popup.
	 */
	public static String getToolPopupUrl(ToolConfiguration pageTool)
	{
		Properties pro = pageTool.getConfig();
		String source = null;
		if ( "sakai.web.168".equals(pageTool.getToolId()) 
				&& "true".equals(pro.getProperty("popup")) ) {
			source = pro.getProperty("source");
		} else if ( "sakai.iframe".equals(pageTool.getToolId()) 
				&& "true".equals(pro.getProperty("popup")) ) {
			source = pro.getProperty("source");
		} else if ( "sakai.basiclti".equals(pageTool.getToolId()) 
				&& "on".equals(pro.getProperty("imsti.newpage")) ) {
			source = "/access/basiclti/site/"+pageTool.getContext()+"/"+pageTool.getId();
		}
		return source;
	}

	/**
	 * Look through the pages in a site and find the page that corresponds to a tool.
	 *
	 * @param <code>site</code>
	 *		The site
	 * @param <code>toolId</code>
	 *		The placement / tool ID
	 * @return The page if found otherwise null.
	 */
	public static SitePage getPageForTool(Site site, String toolId)
	{
		if ( site == null || toolId == null ) return null;
		List pages = site.getOrderedPages();
		for (Iterator i = pages.iterator(); i.hasNext();)
		{
			SitePage p = (SitePage) i.next();
			List<ToolConfiguration> pTools = p.getTools();
			Iterator<ToolConfiguration> toolz = pTools.iterator();
			while(toolz.hasNext()){
				ToolConfiguration tc = toolz.next();
				Tool to = tc.getTool();
				if ( toolId.equals(tc.getId()) ) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * Look through the pages in a site and get the page URL for a tool.
	 *
	 * @param <code>req</code>
	 *		The request object.
	 * @param <code>site</code>
	 *		The site
	 * @param <code>pageTool</code>
	 *		The placement / tool configuration
	 * @return The page if found otherwise null.
	 */
	public static String getPageUrlForTool(HttpServletRequest req, Site site, ToolConfiguration pageTool)
	{
		SitePage thePage = getPageForTool(site, pageTool.getId());
		if ( thePage == null ) return null;
		return getPageUrl(req, site, thePage);
	}

	public static boolean isPortletPlacement(Placement placement)
	{
		if (placement == null) return false;
		Tool t = placement.getTool();
		if (t == null) return false;
		Properties toolProps = t.getFinalConfig();
		if (toolProps == null) return false;
		String portletContext = toolProps.getProperty(PortalService.TOOL_PORTLET_CONTEXT_PATH);
		return (portletContext != null);
	}
}

