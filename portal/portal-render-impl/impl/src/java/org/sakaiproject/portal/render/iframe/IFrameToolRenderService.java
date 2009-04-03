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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.render.iframe;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.util.ByteArrayServletResponse;
import org.sakaiproject.portal.util.URLUtils;
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
 * I Frame tool renderer, renders the iframe header to contain the tool content
 * 
 * @author ddwolf
 * @since Sakai 2.4
 * @version $Rev$
 */
public class IFrameToolRenderService implements ToolRenderService
{

	private static final Log LOG = LogFactory.getLog(IFrameToolRenderService.class);

	private PortalService portalService;


	// private static ResourceLoader rb = new ResourceLoader("sitenav");

	public boolean preprocess(Portal portal, HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException, ToolRenderException
	{

		return true;
	}

	public RenderResult render(Portal portal, ToolConfiguration configuration,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException, ToolRenderException
	{

		final String titleString = Web.escapeHtml(configuration.getTitle());
		String toolUrl = ServerConfigurationService.getToolUrl() + "/"
				+ Web.escapeUrl(configuration.getId());
		StoredState ss = portalService.getStoredState();
		LOG.debug("Restoring Iframe [" + ss + "]");

		Map parametermap = ss == null ? request.getParameterMap() : ss
				.getRequest(request).getParameterMap();
		String URLstub = portalService.decodeToolState(parametermap, configuration
				.getId());
		if (URLstub != null)
		{
			toolUrl += URLstub;
		}
		toolUrl = URLUtils.addParameter(toolUrl, "panel", "Main");

		final StringBuilder sb = new StringBuilder();
		
		
		
		sb.append("<iframe").append("	name=\"").append(
				Web.escapeJavascript("Main" + configuration.getId())).append("\"\n")
				.append("	id=\"").append(
						Web.escapeJavascript("Main" + configuration.getId()))
				.append("\"\n	title=\"").append(titleString).append(" ").
				/* append(Web.escapeHtml(rb.getString("sit.contentporttit"))). */
				append("\"").append("\n").append("	class =\"portletMainIframe\"").append(
						"\n").append("	height=\"50\"").append("\n").append(
						"	width=\"100%\"").append("\n").append("	frameborder=\"0\"")
				.append("\n").append("	marginwidth=\"0\"").append("\n").append(
						"	marginheight=\"0\"").append("\n").append("	scrolling=\"auto\"")
				.append("\n").append("	src=\"").append(toolUrl).append("\">")
				.append("\n").append("</iframe>");
		
		final String[] buffered = bufferContent(portal,request, response, configuration);
		
		
		
		

		RenderResult result = new RenderResult()
		{
			public String getHead() {
				if ( buffered != null ) {
					return buffered[0];
				}
				return "";
			}
			public String getTitle()
			{
				return titleString;
			}

			public String getContent()
			{
				if ( buffered != null ) {
					return buffered[1];
				}
				return sb.toString();
			}

			public String getJSR168EditUrl()
			{
				return null;
			}

			public String getJSR168HelpUrl()
			{
				return null;
			}
		};

		return result;
	}

	public boolean accept(Portal portal, ToolConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context)
	{
		return true;
	}

	public void reset( ToolConfiguration configuration)
	{
	}

	/**
	 * @return the portalService
	 */
	public PortalService getPortalService()
	{
		return portalService;
	}

	/**
	 * @param portalService
	 *        the portalService to set
	 */
	public void setPortalService(PortalService portalService)
	{
		this.portalService = portalService;
	}

	public String[] bufferContent(Portal portal, HttpServletRequest req, HttpServletResponse res,
			ToolConfiguration toolConfig)
	{

		if ( toolConfig == null ) return null;
		if (toolConfig.getId() == null) return null;

		String tidAllow = ServerConfigurationService
				.getString("portal.experimental.iframesuppress");

		if (tidAllow == null) return null;

		if (tidAllow.indexOf(":all:") < 0)
		{
			if (tidAllow.indexOf(toolConfig.getToolId()) < 0) return null;
		}

		ByteArrayServletResponse bufferedResponse = new ByteArrayServletResponse(res);

		try
		{
			boolean retval = doToolBuffer(portal, req, bufferedResponse, toolConfig);
			if (!retval) return null;
		}
		catch (Exception e)
		{
			return null;
		}

		String responseStr = bufferedResponse.getInternalBuffer();
		if (responseStr == null || responseStr.length() < 1) return null;

		String responseStrLower = responseStr.toLowerCase();
		int headStart = responseStrLower.indexOf("<head");
		headStart = findEndOfTag(responseStrLower, headStart);
		int headEnd = responseStrLower.indexOf("</head");
		int bodyStart = responseStrLower.indexOf("<body");
		bodyStart = findEndOfTag(responseStrLower, bodyStart);

		// Some tools (Blogger for example) have multiple
		// head-body pairs - browsers seem to not care much about
		// this so we will do the same - so tht we can be
		// somewhat clean - we search for the "last" end
		// body tag - for the normal case there will only be one
		int bodyEnd = responseStrLower.lastIndexOf("</body");
		// If there is no body end at all or it is before the body
		// start tag we simply - take the rest of the response
		if (bodyEnd < bodyStart) bodyEnd = responseStrLower.length() - 1;

		if (tidAllow.indexOf(":debug:") >= 0)
		{
			LOG.info("Frameless HS=" + headStart + " HE=" + headEnd + " BS=" + bodyStart
					+ " BE=" + bodyEnd);
		}
		if (bodyEnd > bodyStart && bodyStart > headEnd && headEnd > headStart
				&& headStart > 1)
		{
			String headString = responseStr.substring(headStart + 1, headEnd);
			String bodyString = responseStr.substring(bodyStart + 1, bodyEnd);
			if (tidAllow.indexOf(":debug:") >= 0)
			{
				System.out.println(" ---- Head --- ");
				System.out.println(headString);
				System.out.println(" ---- Body --- ");
				System.out.println(bodyString);
			}
			String[] s = new String[2];
			s[0] = headString;
			s[1] = bodyString;
			return s;
		}
		return null;
	}

	private int findEndOfTag(String string, int startPos)
	{
		if (startPos < 1) return -1;
		for (int i = startPos; i < string.length(); i++)
		{
			if (string.charAt(i) == '>') return i;
		}
		return -1;
	}

	private boolean doToolBuffer(Portal portal, HttpServletRequest req, HttpServletResponse res, ToolConfiguration toolConfig) throws ToolException, IOException
	{



		// Reset the tool state if requested
		Session s = SessionManager.getCurrentSession();
		ToolSession ts = s.getToolSession(toolConfig.getId());
		
		if ("true".equals(req.getParameter(portalService.getResetStateParam()))
				|| "true".equals(portalService.getResetState()))
		{
			ts.clearAttributes();
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(toolConfig.getToolId());
		if (tool == null)
		{
			return false;
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(toolConfig.getSiteId());
			}
			catch (IdUnusedException e)
			{
				portal.doError(req, res, s, Portal.ERROR_WORKSITE);
				return false;
			}
			catch (PermissionException e)
			{
				return false;
			}
		}

		// System.out.println("portal.forwardTool siteTool="+siteTool+"
		// TCP="+toolContextPath+" TPI="+toolPathInfo);
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		String toolContextPath = req.getContextPath()
		+ req.getServletPath() + Web.makePath(parts, 1, 3);
		String toolPathInfo = Web.makePath(
				parts, 3, parts.length);
		
		portal.forwardTool(tool, req, res, toolConfig, toolConfig.getSkin(), toolContextPath,
				toolPathInfo);

		return true;
	}

}
