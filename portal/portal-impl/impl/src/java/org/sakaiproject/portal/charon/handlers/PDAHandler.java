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
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.util.ByteArrayServletResponse;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * 
 * @author csev
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
@SuppressWarnings("deprecation")
public class PDAHandler extends SiteHandler
{
	/**
	 * Key in the ThreadLocalManager for access to the current http response
	 * object.
	 */
	public final static String CURRENT_HTTP_RESPONSE = "org.sakaiproject.util.RequestFilter.http_response";

	private ToolHandler toolHandler = new ToolHandler();

	private static final Log log = LogFactory.getLog(PDAHandler.class);

	private static final String URL_FRAGMENT = "pda";
	private static final String SAKAI_COOKIE_DOMAIN = "sakai.cookieDomain"; //RequestFilter.SAKAI_COOKIE_DOMAIN
	
	private static final String TOOLCONFIG_SHOW_RESET_BUTTON = "reset.button";

    private static final String BYPASS_URL_PROP = "portal.pda.bypass";
	private static final String DEFAULT_BYPASS_URL = "\\.jpg$|\\.gif$|\\.js$|\\.png$|\\.jpeg$|\\.prf$|\\.css$|\\.zip$|\\.pdf\\.mov$|\\.json$|\\.jsonp$\\.xml$|\\.ajax$|\\.xls$|\\.xlsx$|\\.doc$|\\.docx$|uvbview$|linktracker$|hideshowcolumns$";

	// Make sure to lower-case the matching regex (i.e. don't use IResourceListener below)
    private static final String BYPASS_QUERY_PROP = "portal.pda.bypass.query";
	private static final String DEFAULT_BYPASS_QUERY = "wicket:interface=.*iresourcelistener:|wicket:ajax=true";

    private static final String BYPASS_TYPE_PROP = "portal.pda.bypass.type";
	private static final String DEFAULT_BYPASS_TYPE = "^application/|^image/|^audio/|^video/|^text/xml|^text/plain";

	private static final String IFRAME_SUPPRESS_PROP = "portal.pda.iframesuppress";
	// SAK-22285 - says these fail in a frame
	// private static final String IFRAME_SUPPRESS_DEFAULT = ":all:sakai.profile2:sakai.synoptic.messagecenter:sakai.sitestats:sakai.sitestats.admin";
	// SAK-25494 with the post bufffer check now working, it seems as though we can inline everything
	private static final String IFRAME_SUPPRESS_DEFAULT = ":all:";
	
	public PDAHandler()
	{
		setUrlFragment(PDAHandler.URL_FRAGMENT);
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
			{
		if ((parts.length == 3) && parts[1].equals(PDAHandler.URL_FRAGMENT) && parts[2].equals(XLoginHandler.URL_FRAGMENT))
		{
			try
			{
				portal.doLogin(req, res, session, "/pda", true);
				return END;
			}
			catch (Exception ex)
			{
				throw new PortalHandlerException(ex);
			}
		} else if ((parts.length >= 2) && (parts[1].equals("pda")))
		{
			// Indicate that we are the controlling portal
			session.setAttribute(PortalService.SAKAI_CONTROLLING_PORTAL,PDAHandler.URL_FRAGMENT);
			try
			{

				//check if we want to force back to the classic view
				String forceClassic = req.getParameter(Portal.FORCE_CLASSIC_REQ_PARAM);
				if(StringUtils.equals(forceClassic, "yes")){

					log.debug("PDAHandler - force.classic");

					//set the portal mode cookie to force classic
					Cookie c = new Cookie(Portal.PORTAL_MODE_COOKIE_NAME, Portal.FORCE_CLASSIC_COOKIE_VALUE);
					c.setPath("/");
					c.setMaxAge(-1);

					//need to set domain and https as per RequestFilter
					if (System.getProperty(SAKAI_COOKIE_DOMAIN) != null) {
						c.setDomain(System.getProperty(SAKAI_COOKIE_DOMAIN));
					}
					if (req.isSecure() == true) {
						c.setSecure(true);
					}
					res.addCookie(c);

					//redirect to classic view
					res.sendRedirect(req.getContextPath());
				}


				// /portal/pda/site-id
				String siteId = null;
				if (parts.length >= 3)
				{
					siteId = parts[2];
				}

				// SAK-12873
				// If we have no site at all and are not logged in - and there is 
				// only one gateway site, go directly to the gateway site
				if ( siteId == null && session.getUserId() == null) 
				{
					String siteList = ServerConfigurationService
					.getString("gatewaySiteList");
					String gatewaySiteId = ServerConfigurationService.getGatewaySiteId();
					if ( siteList.trim().length() == 0  && gatewaySiteId.trim().length() != 0 ) {
						siteId = gatewaySiteId;
					}
				}

				// Tool resetting URL - clear state and forward to the real tool
				// URL
				// /portal/pda/site-id/tool-reset/toolId
				// 0 1 2 3 4
				String toolId = null;
				if ((siteId != null) && (parts.length == 5)
						&& (parts[3].equals("tool-reset")))
				{
					toolId = parts[4];
					String toolUrl = req.getContextPath() + "/pda/" + siteId + "/tool"
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

				// Tool after the reset
				// /portal/pda/site-id/tool/toolId
				if ((parts.length > 4) && (parts[3].equals("tool")))
				{
					// look for page and pick up the top-left tool to show
					toolId = parts[4];
				}

				String forceLogout = req.getParameter(Portal.PARAM_FORCE_LOGOUT);
				if ("yes".equalsIgnoreCase(forceLogout)
						|| "true".equalsIgnoreCase(forceLogout))
				{
					portal.doLogout(req, res, session, "/pda");
					return END;
				}

				if (session.getUserId() == null)
				{
					String forceLogin = req.getParameter(Portal.PARAM_FORCE_LOGIN);
					if ("yes".equalsIgnoreCase(forceLogin)
							|| "true".equalsIgnoreCase(forceLogin))
					{
						portal.doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
						return END;
					}
				}
				
				SitePage page = null;
				// /portal/site/site-id/page/page-id
				// /portal/pda/site-id/page/page-id
				// 1 2 3 4
				if ((parts.length == 5) && (parts[3].equals("page")))
				{
					// look for page and pick up the top-left tool to show
					String pageId = parts[4];
					page = SiteService.findPage(pageId);
					if (page == null)
					{
						portal.doError(req, res, session, Portal.ERROR_WORKSITE);
						return END;
					}
					else
					{
						List<ToolConfiguration> tools = page.getTools(0);
						if (tools != null && !tools.isEmpty())
						{
							toolId = tools.get(0).getId();
						}
						parts[3]="tool";
						parts[4]=toolId;
					}
				}

				// Set the site language
				Site site = null;
				if (siteId == null && session.getUserId() != null) {
					site = portal.getSiteHelper().getMyWorkspace(session);
				} else {
					try {
						Set<SecurityAdvisor> advisors = (Set<SecurityAdvisor>) session.getAttribute("sitevisit.security.advisor");
						if (advisors != null) {
							for (SecurityAdvisor advisor : advisors) {
								SecurityService.pushAdvisor(advisor);
							}
						}

						// This should understand aliases as well as IDs
						site = portal.getSiteHelper().getSiteVisit(siteId);
					} catch (IdUnusedException e) {
					} catch (PermissionException e) {
					}
				}
				if (site != null) {
					super.setSiteLanguage(site);
				}

				// See if we can buffer the content, if not, pass the request through
				boolean allowBuffer = false;
				ToolConfiguration siteTool = SiteService.findTool(toolId);
				String commonToolId = null;

				String toolContextPath = null;
				String toolPathInfo = null;

				if ( parts.length >= 5 ) {
					toolContextPath = req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 5);
					toolPathInfo = Web.makePath(parts, 5, parts.length);
				}
				Object BC = null;
				if ( siteTool != null && parts.length >= 5 ) {
					commonToolId = siteTool.getToolId();

					// Does the tool allow us to buffer?
					allowBuffer = allowBufferContent(req, siteTool);

					if ( allowBuffer ) {

						// Should we bypass buffering based on the request?
						boolean matched = checkBufferBypass(req, siteTool);

						if ( matched ) {
							ActiveTool tool = ActiveToolManager.getActiveTool(commonToolId);
							portal.forwardTool(tool, req, res, siteTool, 
								siteTool.getSkin(), toolContextPath, toolPathInfo);
							return END;
						}
					}
				}

				// Prepare for the full output...
				PortalRenderContext rcontext = portal.includePortal(req, res, session,
						siteId, toolId, req.getContextPath() + req.getServletPath(),
						"pda",
						/* doPages */false, /* resetTools */true,
						/* includeSummary */false, /* expandSite */false);

				if ( allowBuffer ) {
					BC = bufferContent(req, res, session, toolId, 
							toolContextPath, toolPathInfo, siteTool);

					// If the buffered response was not parseable
					if ( BC instanceof ByteArrayServletResponse ) {
						ByteArrayServletResponse bufferResponse = (ByteArrayServletResponse) BC;
						StringBuffer queryUrl = req.getRequestURL();
						String queryString = req.getQueryString();
						if ( queryString != null ) queryUrl.append('?').append(queryString);
						// SAK-25494 - This probably should be a log.debug later
						String msg = "Post buffer bypass CTI="+commonToolId+" URL="+queryUrl;
						String redir = bufferResponse.getRedirect();
						if ( redir != null ) msg = msg + " redirect to="+redir;
						log.warn(msg);
						bufferResponse.forwardResponse();
						return END;
					}
				}

				//  TODO: Should this be a property?  Probably because it does cause an 
				// uncached SQL query
				portal.includeSubSites(rcontext, req, session,
						siteId,  req.getContextPath() + req.getServletPath(), "pda",
						/* resetTools */ true );

				// Add the buttons
				if ( siteTool != null ) {
					boolean showResetButton = !"false".equals(siteTool.getConfig().getProperty(
								TOOLCONFIG_SHOW_RESET_BUTTON));
					rcontext.put("showResetButton", Boolean.valueOf(showResetButton));
					if (toolContextPath != null && showResetButton)
					{
						rcontext.put("resetActionUrl", toolContextPath.replace("/tool/", "/tool-reset/"));
					}
				}

				// Include the buffered content if we have it
				if ( BC instanceof Map ) {
					rcontext.put("bufferedResponse", Boolean.TRUE);
					Map<String,String> bufferMap = (Map<String,String>) BC;
					rcontext.put("responseHead", (String) bufferMap.get("responseHead"));
					rcontext.put("responseBody", (String) bufferMap.get("responseBody"));
				}

				// Add any device specific information to the context
				portal.setupMobileDevice(req, rcontext);
				
				addLocale(rcontext,site);

				portal.sendResponse(rcontext, res, "pda", null);
				
				try{
					boolean presenceEvents = ServerConfigurationService.getBoolean("presence.events.log", true);
					if (presenceEvents)
						org.sakaiproject.presence.cover.PresenceService.setPresence(siteId + "-presence");
				}catch(Exception e){
					return END;
				}
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
		pattern = ServerConfigurationService .getString(BYPASS_URL_PROP, DEFAULT_BYPASS_URL);
		pattern = ServerConfigurationService .getString(BYPASS_URL_PROP+"."+commonToolId, pattern);
		if ( pattern.length() > 1 ) {
			p = Pattern.compile(pattern);
			m = p.matcher(uri.toLowerCase());
			if ( m.find() ) {
				matched = true;
			}
		}

		// Check the query string for a pattern match
		pattern = ServerConfigurationService .getString(BYPASS_QUERY_PROP, DEFAULT_BYPASS_QUERY);
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
	public boolean allowBufferContent(HttpServletRequest req, ToolConfiguration siteTool)
	{
		String tidAllow = ServerConfigurationService.getString(IFRAME_SUPPRESS_PROP, IFRAME_SUPPRESS_DEFAULT);

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
		// Produce the buffered response
		ByteArrayServletResponse bufferedResponse = new ByteArrayServletResponse(res);

		try {
			boolean retval = doToolBuffer(req, bufferedResponse, session, placementId,
					toolContextPath, toolPathInfo);

			if ( ! retval ) return Boolean.FALSE;

			// If the tool did a redirect - tell our caller to just complete the response
			if ( bufferedResponse.getRedirect() != null ) return bufferedResponse;

			// Check the response contentType for a pattern match
			String commonToolId = siteTool.getToolId();
			String pattern = ServerConfigurationService .getString(BYPASS_TYPE_PROP, DEFAULT_BYPASS_TYPE);
			pattern = ServerConfigurationService .getString(BYPASS_TYPE_PROP+"."+commonToolId, pattern);
			if ( pattern.length() > 0 ) {
				String contentType = res.getContentType();
				if ( contentType == null ) contentType = "";
				Pattern p = Pattern.compile(pattern);
				Matcher mc = p.matcher(contentType.toLowerCase());
				if ( mc.find() ) return bufferedResponse;
			}
		} catch (ToolException e) {
			return Boolean.FALSE;
		} catch (IOException e) {
			return Boolean.FALSE;
		}

		String responseStr = bufferedResponse.getInternalBuffer();
		if (responseStr == null || responseStr.length() < 1) return Boolean.FALSE;

		String responseStrLower = responseStr.toLowerCase();
		int headStart = responseStrLower.indexOf("<head");
		headStart = findEndOfTag(responseStrLower, headStart);
		int headEnd = responseStrLower.indexOf("</head");
		int bodyStart = responseStrLower.indexOf("<body");
		bodyStart = findEndOfTag(responseStrLower, bodyStart);

		// Some tools (Blogger for example) have multiple 
		// head-body pairs - browsers seem to not care much about
		// this so we will do the same - so that we can be
		// somewhat clean - we search for the "last" end
		// body tag - for the normal case there will only be one
		int bodyEnd = responseStrLower.lastIndexOf("</body");
		// If there is no body end at all or it is before the body 
		// start tag we simply - take the rest of the response
		if ( bodyEnd < bodyStart ) bodyEnd = responseStrLower.length() - 1;

		String tidAllow = ServerConfigurationService.getString(IFRAME_SUPPRESS_PROP, IFRAME_SUPPRESS_DEFAULT);
		if( tidAllow.indexOf(":debug:") >= 0 )
			log.info("Frameless HS="+headStart+" HE="+headEnd+" BS="+bodyStart+" BE="+bodyEnd);

		if (bodyEnd > bodyStart && bodyStart > headEnd && headEnd > headStart
				&& headStart > 1)
		{
			Map m = new HashMap<String,String> ();
			String headString = responseStr.substring(headStart + 1, headEnd);
			String bodyString = responseStr.substring(bodyStart + 1, bodyEnd);
			if (tidAllow.indexOf(":debug:") >= 0)
			{
				System.out.println(" ---- Head --- ");
				System.out.println(headString);
				System.out.println(" ---- Body --- ");
				System.out.println(bodyString);
			}
			m.put("responseHead", headString);
			m.put("responseBody", bodyString);
			return m;
		}
		return bufferedResponse;
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

		// Reset the tool state if requested
		if (portalService.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
			portalService.setResetState(null);
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
