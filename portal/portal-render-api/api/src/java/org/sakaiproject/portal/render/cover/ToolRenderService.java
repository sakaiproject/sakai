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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.portal.render.cover;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * ToolRenderService is a static cover for the
 * {@link org.sakaiproject.portal.render.api.ToolRenderService}
 * 
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public class ToolRenderService
{

	/** Possibly cached component instance. */
	private static org.sakaiproject.portal.render.api.ToolRenderService m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.portal.render.api.ToolRenderService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.portal.render.api.ToolRenderService) ComponentManager
						.get(org.sakaiproject.portal.render.api.ToolRenderService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.portal.render.api.ToolRenderService) ComponentManager
					.get(org.sakaiproject.portal.render.api.ToolRenderService.class);
		}
	}

	/**
	 * Preprocess the given request. Instructs the service to perform any
	 * preprocessing which may affect the state of the portlets (e.g. how they
	 * are rendered).
	 * 
	 * @param request
	 *        the current servlet request
	 * @param response
	 *        the current servlet response
	 * @param context
	 *        the application context
	 * @return true, if and only if processing should continue.
	 * @throws IOException
	 *         if an error occurs during preprocessing
	 */
	public static boolean preprocess(Portal portal, HttpServletRequest request,
			HttpServletResponse response, ServletContext context) throws IOException
	{
		org.sakaiproject.portal.render.api.ToolRenderService service = getInstance();
		if (service == null) return true;

		return service.preprocess(portal, request, response, context);
	}

	/**
	 * @param configuration
	 *        the tool which should be rendered
	 * @param request
	 *        the current servlet request
	 * @param response
	 *        the current servlet response
	 * @param context
	 *        the application context
	 * @return a rendered(able) content
	 * @throws IOException
	 *         if an error occurs during processing.
	 */
	public static RenderResult render(Portal portal, ToolConfiguration configuration,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException
	{
		org.sakaiproject.portal.render.api.ToolRenderService service = getInstance();
		if (service == null) return null;

		return service.render(portal,configuration, request, response, context);
	}

}
