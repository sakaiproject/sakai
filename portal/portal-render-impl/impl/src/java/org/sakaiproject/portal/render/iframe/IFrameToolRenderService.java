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

package org.sakaiproject.portal.render.iframe;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.api.Portal;
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
@Slf4j
public class IFrameToolRenderService implements ToolRenderService
{
	private final static String INVALID_PARAM_CHARS = ".*[\"'<>].*";

	@Setter private PortalService portalService;
	@Setter private ServerConfigurationService serverConfigurationService;

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
		String toolUrl = serverConfigurationService.getToolUrl() + "/" + Web.escapeUrl(configuration.getId());
		StoredState ss = portalService.getStoredState();
		log.debug("Restoring Iframe [" + ss + "]");

		Map parametermap = ss == null ? request.getParameterMap() : ss
				.getRequest(request).getParameterMap();
		String URLstub = portalService.decodeToolState(parametermap, configuration
				.getId());
		if (URLstub != null)
		{
			toolUrl += URLstub;
		}

		String sakaiPanel = request.getParameter("panel");
		if ( StringUtils.isNotBlank(sakaiPanel) && sakaiPanel.matches(INVALID_PARAM_CHARS) ) sakaiPanel=null;
		if ( sakaiPanel == null ) sakaiPanel="Main";
		toolUrl = URLUtils.addParameter(toolUrl, "panel", sakaiPanel);

		final StringBuilder sb = new StringBuilder();
	
                //SAK-18792 if we don't replace '&' characters we will end up with malformed XML. '&' signifies the beginning
                //   of an XML "entity" like &lt; &gt;, etc.	
	        toolUrl = toolUrl.replace("&", "&amp;");	
		
		// SAK-20462 - Pass through the sakai_action parameter
                String sakaiAction = request.getParameter("sakai_action");
                if ( StringUtils.isNotBlank(sakaiAction) && sakaiAction.matches(INVALID_PARAM_CHARS) ) sakaiAction=null;

		// Produce the iframe markup
		sb.append("<iframe").append("	name=\"").append(
				Web.escapeJavascript("Main" + configuration.getId())).append("\"\n")
				.append("	id=\"").append(
						Web.escapeJavascript("Main" + configuration.getId()))
				.append("\"\n	title=\"").append(titleString).append(" ").
				/* append(Web.escapeHtml(rb.getString("sit.contentporttit"))). */
				append("\"").append("\n").append("	class =\"portletMainIframe\"").append(
						"\n").append("	height=\"475\"").append("\n").append(
						"	width=\"100%\"").append("\n").append("	frameborder=\"0\"")
				.append("\n").append("	marginwidth=\"0\"").append("\n").append(
						"	marginheight=\"0\"").append("\n").append("	scrolling=\"auto\"")
				.append(" allowfullscreen=\"allowfullscreen\"")
				.append(" allow=\"").append(serverConfigurationService.getBrowserFeatureAllowString()).append("\"")
				.append("\n").append("	src=\"").append(toolUrl);

				boolean isFirstParam = (toolUrl.indexOf('?') >=0 ? false : true);
				if ( sakaiAction != null ) 
				{
					sb.append( isFirstParam ? '?' : '&');
					sb.append("sakai_action=").append(Web.escapeHtml(sakaiAction));
					isFirstParam = false;
				}

				String[] persistToIframe = request.getParameterValues("persist_to_iframe");
				if (persistToIframe != null) {
					for (String parameter : persistToIframe) {
						String parameterValue = request.getParameter(parameter);
						if (parameterValue == null || (StringUtils.isNotBlank(parameterValue) && parameterValue.matches(INVALID_PARAM_CHARS))) {
							continue;
						}
						sb.append(isFirstParam ? '?' : '&');
						sb.append(parameter + "=").append(Web.escapeHtml(parameterValue));
						isFirstParam = false;
					}
				}

		sb.append("\">") .append("\n").append("</iframe>");
		
		RenderResult result = new RenderResult() {
						
			public String getHead() {
				return "";
			}
			public String getTitle()
			{
				return titleString;
			}

			public String getContent()
			{
				return sb.toString();
			}

			public void setContent(String content)
			{
				return; // Not allowed
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
}
