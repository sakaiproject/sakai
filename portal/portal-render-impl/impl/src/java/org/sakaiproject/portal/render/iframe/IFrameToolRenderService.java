/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.portal.render.iframe;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.site.api.ToolConfiguration;
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

	public boolean preprocess(HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException, ToolRenderException
	{

		return true;
	}

	public RenderResult render(ToolConfiguration configuration,
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

		RenderResult result = new RenderResult()
		{
			public String getTitle()
			{
				return titleString;
			}

			public String getContent()
			{
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

	public boolean accept(ToolConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context)
	{
		return true;
	}

	public void reset(ToolConfiguration configuration)
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
}
