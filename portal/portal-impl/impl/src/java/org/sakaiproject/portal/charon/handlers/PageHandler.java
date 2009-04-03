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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public class PageHandler extends BasePortalHandler
{

	private static final String INCLUDE_PAGE = "include-page";

	private static final Log log = LogFactory.getLog(PageHandler.class);

	private static final String URL_FRAGMENT = "page";

	public PageHandler()
	{
		setUrlFragment(PageHandler.URL_FRAGMENT);
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
		if ((parts.length == 3) && (parts[1].equals(PageHandler.URL_FRAGMENT)))
		{
			try
			{
				// Resolve the placements of the form
				// /portal/page/sakai.resources?sakai.site=~csev
				String pagePlacement = portal.getPlacement(req, res, session, parts[2],
						true);
				if (pagePlacement == null)
				{
					return ABORT;
				}
				parts[2] = pagePlacement;

				doPage(req, res, session, parts[2], req.getContextPath()
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

	public void doPage(HttpServletRequest req, HttpServletResponse res, Session session,
			String pageId, String toolContextPath) throws ToolException, IOException
	{
		// find the page from some site
		SitePage page = SiteService.findPage(pageId);
		if (page == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		// permission check - visit the site
		Site site = null;
		try
		{
			site = SiteService.getSiteVisit(page.getSiteId());
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

				StoredState ss = portalService.newStoredState("", "");
				ss.setRequest(req);
				ss.setToolContextPath(toolContextPath);
				portalService.setStoredState(ss);
				portal.doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			}
			return;
		}

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		String siteType = portal.calcSiteType(site.getId());
		// start the response
		PortalRenderContext rcontext = portal.startPageContext(siteType, title, page
				.getSkin(), req);

		includePage(rcontext, res, req, session, page, toolContextPath, "contentFull");

		portal.sendResponse(rcontext, res, "page", null);

		StoredState ss = portalService.getStoredState();
		if (ss != null && toolContextPath.equals(ss.getToolContextPath()))
		{
			// This request is the destination of the request
			portalService.setStoredState(null);
		}

	}

	/**
	 * @param rcontext
	 * @param res
	 * @param req
	 * @param session
	 * @param page
	 * @param toolContextPath
	 * @param wrapperClass
	 * @return
	 * @throws IOException
	 */
	public void includePage(PortalRenderContext rcontext, HttpServletResponse res,
			HttpServletRequest req, Session session, SitePage page,
			String toolContextPath, String wrapperClass) throws IOException
	{
		int toolCount = 0;

		if (rcontext.uses(INCLUDE_PAGE))
		{

			// divs to wrap the tools
			rcontext.put("pageWrapperClass", wrapperClass);
			rcontext
					.put("pageColumnLayout",
							(page.getLayout() == SitePage.LAYOUT_DOUBLE_COL) ? "col1of2"
									: "col1");
			Site site = null;
			try
			{
				site = SiteService.getSite(page.getSiteId());
			}
			catch (Exception ignoreMe)
			{
				// Non fatal - just assume null
				if (log.isTraceEnabled())
					log.trace("includePage unable to find site for page " + page.getId());
			}
			{
				List<Map> toolList = new ArrayList<Map>();
				List tools = page.getTools(0);
				for (Iterator i = tools.iterator(); i.hasNext();)
				{
					ToolConfiguration placement = (ToolConfiguration) i.next();

					if (site != null)
					{
						boolean thisTool = portal.getSiteHelper().allowTool(site,
								placement);
						// System.out.println(" Allow Tool Display -" +
						// placement.getTitle() + " retval = " + thisTool);
						if (!thisTool) continue; // Skip this tool if not
						// allowed
					}

					Map m = portal.includeTool(res, req, placement);
					if (m != null)
					{
						toolCount++;
						toolList.add(m);
					}
				}
				rcontext.put("pageColumn0Tools", toolList);
			}

			rcontext.put("pageTwoColumn", Boolean
					.valueOf(page.getLayout() == SitePage.LAYOUT_DOUBLE_COL));

			// do the second column if needed
			if (page.getLayout() == SitePage.LAYOUT_DOUBLE_COL)
			{
				List<Map> toolList = new ArrayList<Map>();
				List tools = page.getTools(1);
				for (Iterator i = tools.iterator(); i.hasNext();)
				{
					ToolConfiguration placement = (ToolConfiguration) i.next();
					Map m = portal.includeTool(res, req, placement);
					if (m != null)
					{
						toolCount++;
						toolList.add(m);
					}
				}
				rcontext.put("pageColumn1Tools", toolList);
			}

			// Add footer variables to page template context- SAK-10312
			rcontext.put("pagepopup", page.isPopUp());

			if (!page.isPopUp())
			{

				String copyright = ServerConfigurationService
						.getString("bottom.copyrighttext");
				String service = ServerConfigurationService.getString("ui.service",
						"Sakai");
				String serviceVersion = ServerConfigurationService.getString(
						"version.service", "?");
				String sakaiVersion = ServerConfigurationService.getString(
						"version.sakai", "?");
				String server = ServerConfigurationService.getServerId();
				String[] bottomNav = ServerConfigurationService.getStrings("bottomnav");
				String[] poweredByUrl = ServerConfigurationService
						.getStrings("powered.url");
				String[] poweredByImage = ServerConfigurationService
						.getStrings("powered.img");
				String[] poweredByAltText = ServerConfigurationService
						.getStrings("powered.alt");

				{
					List<Object> l = new ArrayList<Object>();
					if ((bottomNav != null) && (bottomNav.length > 0))
					{
						for (int i = 0; i < bottomNav.length; i++)
						{
							l.add(bottomNav[i]);
						}
					}
					rcontext.put("bottomNav", l);
				}

				// rcontext.put("bottomNavSitNewWindow",
				// Web.escapeHtml(rb.getString("site_newwindow")));

				if ((poweredByUrl != null) && (poweredByImage != null)
						&& (poweredByAltText != null)
						&& (poweredByUrl.length == poweredByImage.length)
						&& (poweredByUrl.length == poweredByAltText.length))
				{
					{
						List<Object> l = new ArrayList<Object>();
						for (int i = 0; i < poweredByUrl.length; i++)
						{
							Map<String, Object> m = new HashMap<String, Object>();
							m.put("poweredByUrl", poweredByUrl[i]);
							m.put("poweredByImage", poweredByImage[i]);
							m.put("poweredByAltText", poweredByAltText[i]);
							l.add(m);
						}
						rcontext.put("bottomNavPoweredBy", l);

					}
				}
				else
				{
					List<Object> l = new ArrayList<Object>();
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("poweredByUrl", "http://sakaiproject.org");
					m.put("poweredByImage", "/library/image/sakai_powered.gif");
					m.put("poweredByAltText", "Powered by Sakai");
					l.add(m);
					rcontext.put("bottomNavPoweredBy", l);
				}

				rcontext.put("bottomNavService", service);
				rcontext.put("bottomNavCopyright", copyright);
				rcontext.put("bottomNavServiceVersion", serviceVersion);
				rcontext.put("bottomNavSakaiVersion", sakaiVersion);
				rcontext.put("bottomNavServer", server);
			}

		}
	}


}
