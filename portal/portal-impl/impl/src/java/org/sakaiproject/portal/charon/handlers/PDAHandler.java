/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.util.ByteArrayServletResponse;
import org.sakaiproject.site.api.Site;
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
public class PDAHandler extends PageHandler
{
	/**
	 * Key in the ThreadLocalManager for access to the current http response
	 * object.
	 */
	public final static String CURRENT_HTTP_RESPONSE = "org.sakaiproject.util.RequestFilter.http_response";

	private ToolHandler toolHandler = new ToolHandler();

	private static final Log log = LogFactory.getLog(PDAHandler.class);

	private static final String URL_FRAGMENT = "pda";

	public PDAHandler()
	{
		setUrlFragment(PDAHandler.URL_FRAGMENT);
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{

		if ((parts.length >= 2) && (parts[1].equals("pda")))
		{
			// Indicate that we are the controlling portal
			session.setAttribute("sakai-controlling-portal",PDAHandler.URL_FRAGMENT);
			try
			{

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

				// This is a pop-up page - it does exactly the same as
				// /portal/page
				// /portal/pda/site-id/page/page-id
				// 1 2 3 4
				String pageId = null;
				if ((parts.length == 5) && (parts[3].equals("page")))
				{
					doPage(req, res, session, parts[4], req.getContextPath()
							+ req.getServletPath());
					return END;
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
						portal.doLogin(req, res, session, req.getPathInfo(), false);
						return END;
					}
				}

				PortalRenderContext rcontext = portal.includePortal(req, res, session,
						siteId, toolId, req.getContextPath() + req.getServletPath(),
						"pda",
						/* doPages */false, /* resetTools */true,
						/* includeSummary */false, /* expandSite */false);

				//  TODO: Should this be a property?  Probably because it does cause an 
				// uncached SQL query
				portal.includeSubSites(rcontext, req, session,
                        		siteId,  req.getContextPath() + req.getServletPath(), "pda",
                        		/* resetTools */ true );

				// Add any device specific information to the context
				portal.setupMobileDevice(req, rcontext);

				// Optionally buffer tool content to eliminate iFrames
				bufferContent(req, res, session, parts, toolId, rcontext);

				portal.sendResponse(rcontext, res, "pda", null);
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
	 * Optionally actually grab the tool's output and include it in the same
	 * frame
	 */
	public void bufferContent(HttpServletRequest req, HttpServletResponse res,
			Session session, String[] parts, String toolId, PortalRenderContext rcontext)
	{

		if (toolId == null) return;

		String tidAllow = ServerConfigurationService
				.getString("portal.pda.iframesuppress");

		if (tidAllow.indexOf(":none:") >= 0) return;

		ToolConfiguration siteTool = SiteService.findTool(toolId);
		if (siteTool == null) return;

		// If the property is set and :all: is not specified, then the 
		// tools in the list are the ones that we accept
		if (tidAllow.trim().length() > 0 && tidAllow.indexOf(":all:") < 0)
		{
			if (tidAllow.indexOf(siteTool.getToolId()) < 0) return;
		}

		// If the property is set and :all: is specified, then the 
		// tools in the list are the ones that we render the old way
		if (tidAllow.indexOf(":all:") >= 0)
		{
			if (tidAllow.indexOf(siteTool.getToolId()) >= 0) return;
		}

		// Produce the buffered response
		ByteArrayServletResponse bufferedResponse = new ByteArrayServletResponse(res);

		try
		{
                	boolean retval = doToolBuffer(req, bufferedResponse, session, parts[4], 
				req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 5), 
				Web.makePath(parts, 5, parts.length));
			if ( ! retval ) return;
		}
		catch (Exception e)
		{
			return;
		}

		String responseStr = bufferedResponse.getInternalBuffer();
		if (responseStr == null || responseStr.length() < 1) return;

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

		if( tidAllow.indexOf(":debug:") >= 0 )
			log.info("Frameless HS="+headStart+" HE="+headEnd+" BS="+bodyStart+" BE="+bodyEnd);

		if (bodyEnd > bodyStart && bodyStart > headEnd && headEnd > headStart
				&& headStart > 1)
		{
			String headString = responseStr.substring(headStart + 1, headEnd);
			String bodyString = responseStr.substring(bodyStart + 1, bodyEnd);
			if (tidAllow.indexOf(":debug:") >= 0)
			{
				System.out.println(" ---- Head --- ");
				System.out.println(headString);
				System.out.println(" ---- Body --- ");
				System.out.println(bodyString);
			}
			rcontext.put("bufferedResponse", Boolean.TRUE);
			rcontext.put("responseHead", headString);
			rcontext.put("responseBody", bodyString);
		}
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
		if ("true".equals(req.getParameter(portalService.getResetStateParam()))
				|| "true".equals(portalService.getResetState()))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
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
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(siteTool.getSiteId());
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

		// System.out.println("portal.forwardTool siteTool="+siteTool+"
		// TCP="+toolContextPath+" TPI="+toolPathInfo);
		portal.forwardTool(tool, req, res, siteTool, siteTool.getSkin(), toolContextPath,
				toolPathInfo);

		return true;
	}
}
