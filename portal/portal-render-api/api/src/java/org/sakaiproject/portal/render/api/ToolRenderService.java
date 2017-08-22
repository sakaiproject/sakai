/**
 * Copyright (c) 2003-2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.render.api;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Service responsible for preprocessing and rendering tools within a Sakai
 * portal.
 * 
 * @since Sakai 2.2.3
 * @version $Rev$
 */
public interface ToolRenderService
{

	/**
	 * Perfrorm any preperatory processing for the specified tool.
	 * 
	 * @param request
	 *        the servlet request
	 * @param response
	 *        the servlet response.
	 * @param context
	 *        the portal servlet context
	 * @return indicates whether or not processing should be continued.
	 * @throws IOException
	 *         if an error occurs during preprocessing.
	 */
	boolean preprocess(Portal portal, HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException;

	/**
	 * Render the tool.
	 * 
	 * @param toolConfiguration
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ToolRenderException
	 */
	RenderResult render(Portal portal, ToolConfiguration toolConfiguration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context) throws IOException,
			ToolRenderException;

	/**
	 * The render service will accept responsibility for a tool. This enables a
	 * controller to check if the render service can manage the tool
	 * 
	 * @param configuration
	 *        tool configuration for the tool in question
	 * @param request
	 * @param response
	 * @param context -
	 *        this is the servlet context handling the request (ie the portal)
	 * @return
	 */
	boolean accept(Portal portal, ToolConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context);

	/**
	 * reset the ToolConfiguration/Placement associated with the config
	 * 
	 * @param configuration
	 */
	void reset( ToolConfiguration configuration);

}
