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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.portal.util.URLUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
@Slf4j
public class WorksiteHandler extends PageHandler
{
	private static final String INCLUDE_WORKSITE = "include-worksite";

	private static final String INCLUDE_PAGE_NAV = "include-page-nav";

	private static final String URL_FRAGMENT = "worksite";
	
	public WorksiteHandler()
	{
		setUrlFragment(WorksiteHandler.URL_FRAGMENT);
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
		if ((parts.length >= 3) && (parts[1].equals(WorksiteHandler.URL_FRAGMENT)))
		{
			// Indicate that we are the controlling portal
			session.setAttribute(PortalService.SAKAI_CONTROLLING_PORTAL,WorksiteHandler.URL_FRAGMENT);
			try
			{
				// recognize an optional page/pageid
				String pageId = null;
				if ((parts.length == 5) && (parts[3].equals("page")))
				{
					pageId = parts[4];
				}

				doWorksite(req, res, session, parts[2], pageId, req.getContextPath()
						+ req.getServletPath());
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

	public void doWorksite(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String pageId, String toolContextPath)
			throws ToolException, IOException
	{
		// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(Portal.ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			site = portal.getSiteHelper().getSiteVisit(siteId);
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

		// Lookup the page in the site - enforcing access control
		// business rules
		SitePage page = portal.getSiteHelper().lookupSitePage(pageId, site);
		if (page == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		// store the last page visited
		session.setAttribute(Portal.ATTR_SITE_PAGE + siteId, page.getId());

		// SAK-29138 - form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service","Sakai") + " : "
				+ portal.getSiteHelper().getUserSpecificSiteTitle( site, false ) + " : " + page.getTitle();

		// start the response
		String siteType = portal.calcSiteType(siteId);
		PortalRenderContext rcontext = portal.startPageContext(siteType, title, site
				.getSkin(), req, site);
		
		addLocale(rcontext, site);

		includeWorksite(rcontext, res, req, session, site, page, toolContextPath,
				getUrlFragment());

		portal.includeBottom(rcontext);


		portal.sendResponse(rcontext, res, "worksite", null);
	}

	/**
	 * @param rcontext
	 * @param res
	 * @param req
	 * @param session
	 * @param site
	 * @param page
	 * @param toolContextPath
	 * @param portalPrefix
	 * @return
	 * @throws IOException
	 */
	public void includeWorksite(PortalRenderContext rcontext, HttpServletResponse res,
			HttpServletRequest req, Session session, Site site, SitePage page,
			String toolContextPath, String portalPrefix) throws IOException
	{
		if (rcontext.uses(INCLUDE_WORKSITE))
		{
			if (rcontext.uses(INCLUDE_PAGE_NAV))
			{
    				boolean loggedIn = session.getUserId() != null;
				Map pageMap = portal.getSiteHelper().pageListToMap(req, loggedIn, site, page, toolContextPath, 
					portalPrefix, 
					/* doPages */true,
					/* resetTools */"true".equalsIgnoreCase(ServerConfigurationService
							.getString(Portal.CONFIG_AUTO_RESET)),
					/* includeSummary */false);
 				rcontext.put("sitePages", pageMap);
			}

			// add the page
			includePage(rcontext, res, req, session, page, toolContextPath, "content");
		}
	}
}
