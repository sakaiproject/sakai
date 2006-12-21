package org.sakaiproject.portal.render.fragment;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
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
import org.sakaiproject.util.ToolURLManagerImpl;
import org.sakaiproject.util.Web;

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
	public boolean accept(ToolConfiguration configuration,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext context)
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

	public boolean preprocess(HttpServletRequest request,
			HttpServletResponse response, ServletContext context)
			throws IOException
	{
		// for fragments there is no preprocessing, as this is all performed in
		// the render cycle
		return true;
	}

	public RenderResult render(final ToolConfiguration toolConfiguration,
			final HttpServletRequest request,
			final HttpServletResponse response, ServletContext context)
			throws IOException, ToolRenderException
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
									+ ServerConfigurationService
											.getGatewaySiteId();
						}
						else
						{
							option = "/site/"
									+ SiteService.getUserSiteId(session
											.getUserId());
						}
					}

					// get the parts (the first will be "")
					String[] parts = option.split("/");


					try
					{
						doTool(toolConfiguration, request, response, session,
								toolConfiguration.getId(), request.getContextPath()
										+ request.getServletPath()
										+ Web.makePath(parts, 1, 3), Web.makePath(
										parts, 3, parts.length));
					}
					catch (Exception e)
					{
						throw new ToolRenderException("Failed to perform render ",e);
					}
					
					// include will render to the output stream
					return "";
				}

				public String getTitle() throws ToolRenderException
				{
			        return Web.escapeHtml(toolConfiguration.getTitle());
				}

			};
			// do a named dispatch to the active tool with a fragment set

		}
		return null;
	}

	protected void doTool(ToolConfiguration toolConfiguration,
			HttpServletRequest req, HttpServletResponse res, Session session,
			String placementId, String toolContextPath, String toolPathInfo)
			throws ToolException, IOException
	{

		// Reset the tool state if requested
		if (  portal.isResetRequested(req) ) 
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(toolConfiguration
				.getToolId());
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
				throw new ToolRenderException(
						"No site permissions found for site "
								+ toolConfiguration.getSiteId());
			}
			catch (PermissionException e)
			{
				throw new ToolRenderException(
						"Permission Deined for placement "
								+ toolConfiguration.getId() + " in "
								+ toolConfiguration.getSiteId());
			}
		}

		forwardTool(tool, req, res, toolConfiguration, toolConfiguration
				.getSkin(), toolContextPath, toolPathInfo);
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
			HttpServletResponse res, Placement p, String skin,
			String toolContextPath, String toolPathInfo) throws ToolException
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
				HttpServletRequest sreq = ss.getRequest(req);
				Placement splacement = ss.getPlacement();
				String stoolContext = ss.getToolContextPath();
				String stoolPathInfo = ss.getToolPathInfo();
				ActiveTool stool = ActiveToolManager.getActiveTool(p
						.getToolId());
				String sskin = ss.getSkin();
				req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
				stool.include(sreq, res, splacement, stoolContext,
						stoolPathInfo);
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
