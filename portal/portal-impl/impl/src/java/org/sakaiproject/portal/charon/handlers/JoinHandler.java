/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.charon.site.PortalSiteHelperImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.portal.util.URLUtils;

/**
 * Handler for managing the joining of a user to a site.
 * Handles URLs like /portal/join/siteId. Will get user to login first if
 * not already authenticated. Redirects back to site after a successful join
 * or sends the user to the error page.
 * @author buckett
 *
 */
public class JoinHandler extends BasePortalHandler
{

	private static final String URL_FRAGMENT = "join";

	public JoinHandler()
	{
		setUrlFragment(URL_FRAGMENT);
	}
	

	@Override
	public int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException
	{
		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				showJoin(parts, req, res, session);
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}
	
	@Override
	public int doPost(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException {
		
		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				doJoin(parts, req, res, session);
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}


	protected void doJoin(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session) throws ToolException, IOException {
		String siteId = parts[2];
		try
		{
			if (req.getParameter("join") != null) {
				Site site = portal.getSiteHelper().getSite(siteId);
				SiteService.getInstance().join(site.getId());
				sendToSite(res, site);
			} else {
				// The user didn't opt to join
				Site myWorkspace = portal.getSiteHelper().getMyWorkspace(session);
				if (myWorkspace != null)
				{
					sendToSite(res, myWorkspace);
				}
				else
				{
					portal.doError(req, res, session, Portal.ERROR_SITE);
				}
			}
		}
		catch (IdUnusedException e) {
			portal.doError(req, res, session, Portal.ERROR_SITE);
		} catch (PermissionException e) {
			portal.doError(req, res, session, Portal.ERROR_SITE);
		}
	}


	private void sendToSite(HttpServletResponse res, Site site)
			throws IOException, IdUnusedException {
		res.sendRedirect(site.getUrl());
	}


	protected void showJoin(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session) throws IOException, ToolException {
		// Handle user not logged in.
		if (session.getUserId() == null)
		{
			portal.doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
		}
		else
		{
			try
			{
				String siteId = parts[2];
				// We use this as it 
				Site site = portal.getSiteHelper().getSite(siteId);
				// Check that the current user can access the site before we redirect.
				if (site.getUserRole(session.getUserId()) != null && SiteService.allowAccessSite(site.getId()))
				{
					sendToSite(res, site);
					return;
				}
				if (site.isJoinable())
				{
					String siteType = portal.calcSiteType(site.getId());
					String serviceName = ServerConfigurationService.getString("ui.service", "Sakai");
					
					// SAK-29138
					List<String> siteProviders = (List<String>) PortalSiteHelperImpl.getProviderIDsForSite(site);
					String title = serviceName + " : " + portal.getSiteHelper().getUserSpecificSiteTitle(site, true, false, siteProviders);
					
					String skin = site.getSkin();
					PortalRenderContext context = portal.startPageContext(siteType, title, skin, req, site);
					context.put("currentSite", portal.getSiteHelper().convertSiteToMap(req, site, null, site.getId(), null, false, false, false, false, null, true, siteProviders));
					context.put("uiService", serviceName);
					
					boolean restrictedByAccountType = !SiteService.getInstance().isAllowedToJoin(site.getId());
					context.put("restrictedByAccountType", restrictedByAccountType);
					
					portal.sendResponse(context, res, "join", "text/html");
					return;
				}
			}
			catch (IdUnusedException e) {
			}
		}
		portal.doError(req, res, session, Portal.ERROR_SITE);
	}

}
