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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Web;

/**
 * @author ieb
 */
public class PortletHandler extends PageHandler
{
	public PortletHandler()
	{
		urlFragment = "portlet";
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
		if ((parts.length >= 2) && (parts[1].equals("portlet")))
		{
			try
			{

				// /portal/portlet/site-id
				String siteId = null;
				if (parts.length >= 3)
				{
					siteId = parts[2];
				}

				// This is a pop-up page - it does exactly the same as
				// /portal/page
				// /portal/portlet/site-id/page/page-id
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
				// /portal/portlet/site-id/tool-reset/toolId
				// 0 1 2 3 4
				String toolId = null;
				if ((siteId != null) && (parts.length == 5)
						&& (parts[3].equals("tool-reset")))
				{
					toolId = parts[4];
					String toolUrl = req.getContextPath() + "/portlet/" + siteId
							+ "/tool" + Web.makePath(parts, 4, parts.length);
					String queryString = req.getQueryString();
					if (queryString != null)
					{
						toolUrl = toolUrl + "?" + queryString;
					}
					portalService.setResetState("true");
					res.sendRedirect(toolUrl);
					return RESET_DONE;
				}

				// Tool after the reset
				// /portal/portlet/site-id/tool/toolId
				if ((parts.length == 5) && (parts[3].equals("tool")))
				{
					toolId = parts[4];
				}

				String forceLogout = req.getParameter(Portal.PARAM_FORCE_LOGOUT);
				if ("yes".equalsIgnoreCase(forceLogout)
						|| "true".equalsIgnoreCase(forceLogout))
				{
					portal.doLogout(req, res, session, "/portlet");
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
						"portlet",
						/* doPages */false, /* resetTools */true,
						/* includeSummary */false, /* expandSite */false);

				portal.sendResponse(rcontext, res, "portlet", null);
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

}
