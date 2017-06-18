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

package org.sakaiproject.portal.render.compat;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.site.api.ToolConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * Render serivice used to support both Portlet and iframe based tools.
 * 
 * @author ddwolf
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class CompatibilityToolRenderService implements ToolRenderService
{
	private List renderServices = null;

	/**
	 * @param request
	 * @param response
	 * @param context
	 * @return
	 * @throws IOException
	 */
	public boolean preprocess(Portal portal, HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException
	{

		boolean continueProcessing = true;
		for (Iterator i = renderServices.iterator(); i.hasNext();)
		{
			ToolRenderService trs = (ToolRenderService) i.next();
			log.debug("Preprocessing with " + trs);
			continueProcessing = continueProcessing
					&& trs.preprocess(portal, request, response, context);
		}
		return continueProcessing;
	}

	public RenderResult render(Portal portal, ToolConfiguration configuration,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException
	{

		for (Iterator i = renderServices.iterator(); i.hasNext();)
		{
			ToolRenderService trs = (ToolRenderService) i.next();
			if (trs.accept(portal, configuration, request, response, context))
			{
				log.debug("Rendering with " + trs);
				return trs.render(portal, configuration, request, response, context);
			}
		}
		throw new ToolRenderException("No available Tool Render Service for the tool "
				+ configuration.getToolId());
	}

	public boolean accept(Portal portal, ToolConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context)
	{
		for (Iterator i = renderServices.iterator(); i.hasNext();)
		{
			ToolRenderService trs = (ToolRenderService) i.next();
			if (trs.accept(portal,configuration, request, response, context))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the renderServices
	 */
	public List getRenderServices()
	{
		return renderServices;
	}

	/**
	 * @param renderServices
	 *        the renderServices to set
	 */
	public void setRenderServices(List renderServices)
	{
		this.renderServices = renderServices;
	}

	public void reset( ToolConfiguration configuration)
	{
		for (Iterator i = renderServices.iterator(); i.hasNext();)
		{
			ToolRenderService trs = (ToolRenderService) i.next();
			trs.reset(configuration);
		}
	}

}
