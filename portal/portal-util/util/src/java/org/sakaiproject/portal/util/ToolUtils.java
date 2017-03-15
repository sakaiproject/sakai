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

import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.Portal;
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
import org.sakaiproject.tool.cover.ToolManager;

// Yes - crazy and brittle - but better in one place
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.util.RequestFilter;

/**
 * A set of utilities provided by the portal for use by tools.
 *
 * Some of the methods pull the HttpServletRequest from ThreadLocal.  This means
 * they will fail badly if called before the RequestFilter is run.  Generally
 * this means that the calls are safe in core tool code.
 */
public class ToolUtils
{

	public static final String PORTAL_INLINE_EXPERIMENTAL = "portal.inline.experimental";
	public static final boolean PORTAL_INLINE_EXPERIMENTAL_DEFAULT = true;

	/**
	 * Determine if this is an inline request.
	 *
	 * @param <code>req</code>
	 *		The request object.  If you have no access to the request object,
	 *              you can leave this null and we will try to pull the request
	 *              from ThreadLocal - if we fail it is a RunTime exception.
	 * @return True if this is a request where a tool will be inlined.
	 */
	public static boolean isInlineRequest(HttpServletRequest req)
	{
		if ( req == null ) req = getRequestFromThreadLocal();
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
	 * Captures the rules for getting the URL of a page suitable for a GET request (call only from the tool)
	 *
	 * @param <code>site</code>
	 *		The site that contains the page
	 * @param <code>page</code>
	 *		The page URL.   If this has a logged in session, we use it to set the
	 *		default prefix if available.   This will use the default prefix if called
	 *		without a session (i.e. through /direct).
	 */
	public static String getPageUrl(Site site, SitePage page)
	{
		return getPageUrl(null, site, page);
	}

	/**
	 * Captures the rules for getting the URL of a page suitable for a GET request
	 *
	 * @param <code>req</code>
	 *		The request object.  If you have no access to the request object,
	 *              you can leave this null and we will try to pull the request
	 *              from ThreadLocal - if we fail it is a RunTime exception.
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
		if ( req == null ) req = getRequestFromThreadLocal();
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
	 *		The request object.  If you have no access to the request object,
	 *              you can leave this null and we will try to pull the request
	 *              from ThreadLocal - if we fail it is a RunTime exception.
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
		if(page == null) return "";
		//If portal's CONFIG_AUTO_RESET is not set, check for Site's CONFIG_AUTO_RESET value
		boolean resetSiteProperty = false;
		if(!reset){
			ResourceProperties siteProperties = site.getProperties();
			try {
				resetSiteProperty = siteProperties.getBooleanProperty(Portal.CONFIG_AUTO_RESET);
			} catch (EntityPropertyNotDefinedException e) {
				//do nothing let resetSiteProperty be set to false
			} catch (EntityPropertyTypeException e) {
				//do nothing let resetSiteProperty be set to false
			}
		}
		if ( req == null ) req = getRequestFromThreadLocal();
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

		pageUrl = Web.returnUrl(req, "/" + portalPrefix + "/" + Web.escapeUrl(effectiveSiteId));
		if (reset || resetSiteProperty) {
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
	 *		The request object.  If you have no access to the request object,
	 *              you can leave this null and we will try to pull the request
	 *              from ThreadLocal - if we fail it is a RunTime exception.
	 * @param <code>site</code>
	 *		The site
	 * @param <code>pageTool</code>
	 *		The placement / tool configuration
	 * @return The page if found otherwise null.
	 */
	public static String getPageUrlForTool(HttpServletRequest req, Site site, ToolConfiguration pageTool)
	{
		if ( req == null ) req = getRequestFromThreadLocal();
		SitePage thePage = getPageForTool(site, pageTool.getId());
		if ( thePage == null ) return null;
		return getPageUrl(req, site, thePage);
	}

	/**
	 * Determine if a particular placement is a JSR-168 portlet placement
	 *
	 * @param <code>placement</code>
	 *		The actual placement.
	 * @return Returns true for JSR_168 portlets
	 */
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

	/**
	 * Get the base URL for tools with no "border" - (i.e. within an iframe)
	 */

	public static String getToolBorderlessBaseUrl()
	{
		return ServerConfigurationService.getToolUrl();
	}

	/**
	 * Get the base URL for tools not including the ToolId (only works in tools)
	 */

	public static String getToolBaseUrl()
	{
		return getToolBaseUrl(null);
	}

	/**
	 * Get the base URL for tools not including the ToolId
	 *
	 * @param <code>req</code>
	 *		The request object.  If you have no access to the request object,
	 *              you can leave this null and we will try to pull the request
	 *              from ThreadLocal - if we fail it is a RunTime exception.
	 * @return Returns true for JSR_168 portlets
	 */
	public static String getToolBaseUrl(HttpServletRequest req)
	{
		if ( req == null ) req = getRequestFromThreadLocal();
		String retval = ServerConfigurationService.getToolUrl();
		if ( isInlineRequest(req) ) {
			String currentSiteId = ToolManager.getCurrentPlacement().getContext();
			retval = retval.replaceAll("tool$","site/"+currentSiteId+"/tool");
		}
		return retval;
	}

	/**
	 * Get the servlet request from thread local - call only after RequestFilter has run
	 *
	 * @return Returns the current servlet request or throws runtime error
	 */
	public static HttpServletRequest getRequestFromThreadLocal()
	{
		try {
			HttpServletRequest req = (HttpServletRequest) ThreadLocalManager.get(RequestFilter.CURRENT_HTTP_REQUEST);
			return req;
		} catch (Exception e) {
			throw new RuntimeException("This utility must be called after RequestFilter has run",e);
		}
	}

	/**
	 * Retriece a request parameter from ThreadLocal (only works after RequestFilter runs)
	 *
	 * @param <code>key</code>
	 *		The request object
	 * @return Returns true for JSR_168 portlets
	 */
	public static String getRequestParameter(String key)
	{
		return getRequestFromThreadLocal().getParameter(key);
	}
}

