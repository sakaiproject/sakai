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
import org.sakaiproject.portal.util.PortalSiteHelper;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;

/**
 * Handler for the gallery parts of the portal
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class GalleryHandler extends SiteHandler
{

	private static final String INCLUDE_GALLERY_NAV = "include-gallery-nav";

	private static final String INCLUDE_GALLERY_LOGIN = "include-gallery-login";

	private PortalSiteHelper siteHelper = new PortalSiteHelper();

	public GalleryHandler()
	{
		urlFragment = "gallery";
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if ((parts.length >= 2) && (parts[1].equals("gallery")))
		{
			// Indicate that we are the controlling portal
			session.setAttribute("sakai-controlling-portal",urlFragment);
			try
			{
				// recognize an optional page/pageid
				String pageId = null;
				if ((parts.length == 5) && (parts[3].equals("page")))
				{
					pageId = parts[4];
				}

				// site might be specified
				String siteId = null;
				if (parts.length >= 3)
				{
					siteId = parts[2];
				}

				doGallery(req, res, session, siteId, pageId, req.getContextPath()
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

	public void doGallery(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String pageId, String toolContextPath)
			throws ToolException, IOException
	{
		// check to default site id
		if (siteId == null)
		{
			if (session.getUserId() == null)
			{
				String forceLogin = req.getParameter(Portal.PARAM_FORCE_LOGIN);
				if (forceLogin == null || "yes".equalsIgnoreCase(forceLogin)
						|| "true".equalsIgnoreCase(forceLogin))
				{
					portal.doLogin(req, res, session, req.getPathInfo(), false);
					return;
				}
				siteId = ServerConfigurationService.getGatewaySiteId();
			}
			else
			{
				siteId = SiteService.getUserSiteId(session.getUserId());
			}
		}

		// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(Portal.ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			site = siteHelper.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			portal.doError(req, res, session, Portal.ERROR_GALLERY);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				portal.doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				portal.doError(req, res, session, Portal.ERROR_GALLERY);
			}
			return;
		}

		// Lookup the page in the site - enforcing access control
		// business rules
 		SitePage page = siteHelper.lookupSitePage(portal,pageId, site);
		if (page == null)
		{
			portal.doError(req, res, session, Portal.ERROR_GALLERY);
			return;
		}

		// store the last page visited
		session.setAttribute(Portal.ATTR_SITE_PAGE + siteId, page.getId());

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		String siteType = portal.calcSiteType(siteId);
		PortalRenderContext rcontext = portal.startPageContext(siteType, title, site
				.getSkin(), req);

		// the 'little' top area
		includeGalleryNav(rcontext, req, session, siteId, "gallery");

		includeWorksite(rcontext, res, req, session, site, page, toolContextPath,
				"gallery");

		portal.includeBottom(rcontext);

		portal.sendResponse(rcontext, res, "gallery", null);
	}

	protected void includeGalleryNav(PortalRenderContext rcontext,
			HttpServletRequest req, Session session, String siteId, String prefix)
	{
		if (rcontext.uses(INCLUDE_GALLERY_NAV))
		{

			boolean loggedIn = session.getUserId() != null;
			boolean topLogin = ServerConfigurationService.getBoolean("top.login", true);

			// outer blocks and jump-to links
			String accessibilityURL = ServerConfigurationService
					.getString("accessibility.url");
			rcontext.put("galleryHasAccessibilityURL", Boolean
					.valueOf((accessibilityURL != null && accessibilityURL != "")));

			rcontext.put("galleryAccessibilityURL", accessibilityURL);
			// rcontext.put("gallarySitAccessibility",
			// Web.escapeHtml(rb.getString("sit_accessibility")));
			// rcontext.put("gallarySitJumpcontent",
			// Web.escapeHtml(rb.getString("sit_jumpcontent")));
			// rcontext.put("gallarySitJumptools",
			// Web.escapeHtml(rb.getString("sit_jumptools")));
			// rcontext.put("gallarySitJumpworksite",
			// Web.escapeHtml(rb.getString("sit_")));
			rcontext.put("gallaryLoggedIn", Boolean.valueOf(loggedIn));

			try
			{
				if (loggedIn)
				{
					includeTabs(rcontext, req, session, siteId, prefix, false);
				}
				else
				{
					includeGalleryLogin(rcontext, req, session, siteId);
				}
			}
			catch (Exception any)
			{
			}
		}

	}

	protected void includeGalleryLogin(PortalRenderContext rcontext,
			HttpServletRequest req, Session session, String siteId) throws IOException
	{
		if (rcontext.uses(INCLUDE_GALLERY_LOGIN))
		{
			portal.includeLogin(rcontext, req, session);
		}
	}

}
