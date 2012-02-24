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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.StoredState;
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
import org.sakaiproject.util.Web;

/**
 * Handler to process directtool urls including storing destination state
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class DirectToolHandler extends BasePortalHandler
{
	public static final String URL_FRAGMENT = "directtool";

	public DirectToolHandler()
	{
		setUrlFragment(DirectToolHandler.URL_FRAGMENT);
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		return doGet(parts, req, res, session);
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if (portalService.isEnableDirect() && (parts.length > 2)
				&& (parts[1].equals(DirectToolHandler.URL_FRAGMENT)))
		{
			try
			{
				// Resolve the placements of the form
				// /portal/tool/sakai.resources?sakai.site=~csev
				String toolPlacement = portal.getPlacement(req, res, session, parts[2],
						false);
				if (toolPlacement == null)
				{
					return ABORT;
				}
				parts[2] = toolPlacement;

				return doDirectTool(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
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

	/**
	 * Do direct tool, takes the url, stored the destination and the target
	 * iframe in the session constructs and outer url and when a request comes
	 * in that matches the stored iframe it ejects the destination address
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @param placementId
	 * @param toolContextPath
	 * @param toolPathInfo
	 * @param placementId
	 * @throws ToolException
	 * @throws IOException
	 */
	public int doDirectTool(HttpServletRequest req, HttpServletResponse res,
			Session session, String placementId, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException
	{
		if (portal.redirectIfLoggedOut(res)) return ABORT;

		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		if (siteTool == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return END;
		}

		// Reset the tool state if requested
		if (portalService.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(siteTool.getToolId());
		if (tool == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return END;
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{
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
				site = SiteService.getSiteVisit(siteTool.getSiteId());
			}
			catch (IdUnusedException e)
			{
				portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				return END;
			}
			catch (PermissionException e)
			{
				// if not logged in, give them a chance
				if (session.getUserId() == null)
				{
					// let the tool do the the work (forward)
					StoredState ss = portalService.newStoredState("directtool", "tool");
					ss.setRequest(req);
					ss.setPlacement(siteTool);
					ss.setToolContextPath(toolContextPath);
					ss.setToolPathInfo(toolPathInfo);
					ss.setSkin(siteTool.getSkin());
					portalService.setStoredState(ss);

					portal.doLogin(req, res, session, portal.getPortalPageUrl(siteTool),
							false);
				}
				else
				{
					portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				}
				return END;
			}
		}
		// let the tool do the the work (forward)
		StoredState ss = portalService.newStoredState("directtool", "tool");
		ss.setRequest(req);
		ss.setPlacement(siteTool);
		ss.setToolContextPath(toolContextPath);
		ss.setToolPathInfo(toolPathInfo);
		ss.setSkin(siteTool.getSkin());
		portalService.setStoredState(ss);

		portal.forwardPortal(tool, req, res, siteTool, siteTool.getSkin(),
				toolContextPath, toolPathInfo);
		return END;

	}

}
