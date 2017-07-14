/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.render.fragment;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.util.ToolURLManagerImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.ToolURL;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Web;
import lombok.extern.slf4j.Slf4j;

/**
 * Attempts to render a tool as a fragment rather than an Iframe.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class FragmentToolRenderService implements ToolRenderService
{
	private static final String TOOL_FRAGMENT_PRODUCER_ID = "fragment-producer";

	private PortalService portal;

	public FragmentToolRenderService()
	{
	}

	/**
	 * This is called during render to accept the request into this tool. If the
	 * placement is handled by the FragmentToolRenderService, this should return
	 * true, then the render will be invoked.
	 */
	public boolean accept(Portal portal, ToolConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context)
	{
		return isFragmentTool(configuration);
	}

	private boolean isFragmentTool(ToolConfiguration configuration)
	{
		Properties placementProperties = configuration.getConfig();
		String fragmentCapable = placementProperties
				.getProperty(TOOL_FRAGMENT_PRODUCER_ID);

		if (fragmentCapable == null || fragmentCapable.length() == 0)
		{
			return false;
		}
		return true;
	}

	public boolean preprocess(Portal portal, HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException
	{
		// for fragments there is no preprocessing, as this is all performed in
		// the render cycle
		return true;
	}

	public RenderResult render(Portal portal, final ToolConfiguration toolConfiguration,
			final HttpServletRequest request, final HttpServletResponse response,
			ServletContext context) throws IOException, ToolRenderException
	{
		if (isFragmentTool(toolConfiguration))
		{
			return new RenderResult()
			{

				public String getContent() throws ToolRenderException
				{
					Session session = SessionManager.getCurrentSession();
					// recognize what to do from the path
					String option = request.getPathInfo();

					// if missing, set it to home or gateway
					if ((option == null) || ("/".equals(option)))
					{
						if (session.getUserId() == null)
						{
							option = "/site/"
									+ ServerConfigurationService.getGatewaySiteId();
						}
						else
						{
							option = "/site/"
									+ SiteService.getUserSiteId(session.getUserId());
						}
					}

					// get the parts (the first will be "")
					String[] parts = option.split("/");

					try
					{
						doTool(toolConfiguration, request, response, session,
								toolConfiguration.getId(), request.getContextPath()
										+ request.getServletPath()
										+ Web.makePath(parts, 1, 3), Web.makePath(parts,
										3, parts.length));
					}
					catch (Exception e)
					{
						throw new ToolRenderException("Failed to perform render ", e);
					}

					// include will render to the output stream
					return "";
				}
	
				public void setContent(String content) 
				{
					return; // Not allowed
				}

				public String getTitle() throws ToolRenderException
				{
					return Web.escapeHtml(toolConfiguration.getTitle());
				}

				public String getJSR168EditUrl()
				{
					return null;
				}

				public String getJSR168HelpUrl()
				{
					return null;
				}

				public String getHead()
				{
					return "";
				}
			};
			// do a named dispatch to the active tool with a fragment set

		}
		return null;
	}

	protected void doTool(ToolConfiguration toolConfiguration, HttpServletRequest req,
			HttpServletResponse res, Session session, String placementId,
			String toolContextPath, String toolPathInfo) throws ToolException,
			IOException
	{

		// Reset the tool state if requested
		if (portal.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
			portal.setResetState(null);
			log.debug("Tool state reset");
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(toolConfiguration.getToolId());
		if (tool == null)
		{
			throw new ToolRenderException(
					"Failed to render fragment, no Active Tool found for "
							+ toolConfiguration.getToolId());
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(toolConfiguration.getSiteId());
			}
			catch (IdUnusedException e)
			{
				throw new ToolRenderException("No site permissions found for site "
						+ toolConfiguration.getSiteId());
			}
			catch (PermissionException e)
			{
				throw new ToolRenderException("Permission Deined for placement "
						+ toolConfiguration.getId() + " in "
						+ toolConfiguration.getSiteId());
			}
		}

		forwardTool(tool, req, res, toolConfiguration, toolConfiguration.getSkin(),
				toolContextPath, toolPathInfo);
	}

	/**
	 * Taken from Charon, should be in a service
	 * 
	 * @param tool
	 * @param req
	 * @param res
	 * @param p
	 * @param skin
	 * @param toolContextPath
	 * @param toolPathInfo
	 * @throws ToolException
	 */
	protected void forwardTool(ActiveTool tool, HttpServletRequest req,
			HttpServletResponse res, Placement p, String skin, String toolContextPath,
			String toolPathInfo) throws ToolException
	{

		// if there is a stored request state, and path, extract that from the
		// session and
		// reinstance it

		// let the tool do the the work (forward)
		if (portal.isEnableDirect())
		{
			StoredState ss = portal.getStoredState();
			if (ss == null || !toolContextPath.equals(ss.getToolContextPath()))
			{
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				tool.include(req, res, p, toolContextPath, toolPathInfo);
			}
			else
			{
				log.debug("Restoring Fragment  StoredState [" + ss + "]");

				HttpServletRequest sreq = ss.getRequest(req);
				Placement splacement = ss.getPlacement();
				String stoolContext = ss.getToolContextPath();
				String stoolPathInfo = ss.getToolPathInfo();
				ActiveTool stool = ActiveToolManager.getActiveTool(p.getToolId());
				String sskin = ss.getSkin();
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				stool.include(sreq, res, splacement, stoolContext, stoolPathInfo);
				// this is correct as we have already checked the context path
				// of the desitination
				portal.setStoredState(null);
			}
		}
		else
		{
			req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
			tool.include(req, res, p, toolContextPath, toolPathInfo);
		}

	}

	public void setPortalService(PortalService portal)
	{
		this.portal = portal;
	}

	public void reset(ToolConfiguration configuration)
	{
	}
}
