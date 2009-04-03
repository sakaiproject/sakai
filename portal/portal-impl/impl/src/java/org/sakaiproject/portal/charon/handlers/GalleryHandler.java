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

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
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

	private static final String URL_FRAGMENT = "gallery";

	
	public GalleryHandler()
	{
		setUrlFragment(GalleryHandler.URL_FRAGMENT);
	}
	

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if ((parts.length >= 2) && (parts[1].equals(GalleryHandler.URL_FRAGMENT)))
		{
			// Indicate that we are the controlling portal
			session.setAttribute("sakai-controlling-portal",GalleryHandler.URL_FRAGMENT);
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

		// /Include the site materials
		doSite(req, res, session, siteId, pageId, req.getContextPath()
						+ req.getServletPath());
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.charon.handlers.SiteHandler#doSendFrameSet(org.sakaiproject.portal.api.PortalRenderContext, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@Override
	protected void doSendFrameSet(PortalRenderContext rcontext, 
		HttpServletResponse res, String contentType) 
		throws IOException
	{
		portal.sendResponse(rcontext, res, "gallery-frame-set", null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.charon.handlers.SiteHandler#doSendFrameTop(org.sakaiproject.portal.api.PortalRenderContext, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@Override
	protected void doSendFrameTop(PortalRenderContext rcontext, 
		HttpServletResponse res, String contentType) 
		throws IOException
	{
		portal.sendResponse(rcontext, res, "gallery-frame-top", null);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.charon.handlers.SiteHandler#doSendResponse(org.sakaiproject.portal.api.PortalRenderContext, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@Override
	protected void doSendResponse(PortalRenderContext rcontext, HttpServletResponse res, String contentType) throws IOException
	{
		portal.sendResponse(rcontext, res, "gallery", null);
	}
}
