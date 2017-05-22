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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.util.Web;
import org.sakaiproject.portal.util.URLUtils;

/**
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class PresenceHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "presence";

	public PresenceHandler()
	{
		setUrlFragment(PresenceHandler.URL_FRAGMENT);
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{

		if ((parts.length >= 3) && (parts[1].equals(PresenceHandler.URL_FRAGMENT)))
		{
			try
			{
				doPresence(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
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

	public void doPresence(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String toolContextPath, String toolPathInfo)
			throws ToolException, IOException
	{
		// permission check - visit the site
		Site site = null;
		try
		{
			site = SiteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				portal.doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
			}
			else
			{
				portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			}
			return;
		}

		// get the skin for the site
		String skin = site.getSkin();

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.presence");
		if (tool == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		// form a placement based on the site and the fact that this is that
		// site's presence...
		// Note: the placement is transient, but will always have the same id
		// and context based on the siteId
		Placement placement = new org.sakaiproject.util.Placement(siteId + "-presence",
				tool.getId(), tool, null, siteId, null);

		portal
				.forwardTool(tool, req, res, placement, skin, toolContextPath,
						toolPathInfo);
	}

}
