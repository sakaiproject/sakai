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

package org.sakaiproject.portal.portlet;

import java.io.Writer;

import javax.portlet.RenderRequest;

import org.sakaiproject.portal.portlet.velocity.VelocityPortletRenderEngine;

/**
 * Repesents the API used by the protal to comunicate with the RenderEngine
 * implimentation.
 * 
 * @author ieb
 */
public interface PortletRenderEngine
{

	/**
	 * the default render engine impliemtnation
	 */
	public static final String DEFAULT_RENDER_ENGINE = VelocityPortletRenderEngine.class
			.getName();

	/**
	 * Initialise the render engine
	 * 
	 * @throws Exception
	 */
	void init() throws Exception;

	/**
	 * generate a non thread safe render context for the current
	 * request/thread/operation
	 * @param request 
	 * 
	 * @return
	 */
	PortletRenderContext newRenderContext(RenderRequest request);

	/**
	 * Render a PortalRenderContext against a template. The real template may be
	 * based on a skining name, out output will be send to the Writer
	 * 
	 * @param template
	 * @param rcontext
	 * @param out
	 * @throws Exception
	 */
	void render(String template, PortletRenderContext rcontext, Writer out)
			throws Exception;

}
