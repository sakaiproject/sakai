/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package org.sakaiproject.portal.render.cover;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.portal.render.api.RenderResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * ToolRenderService is a static cover for the
 * {@link org.sakaiproject.portal.render.api.ToolRenderService}
 */
public class ToolRenderService {

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

	public static void preprocess(ToolConfiguration configuration,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
        throws IOException {
		org.sakaiproject.portal.render.api.ToolRenderService service = getInstance();
		if (service == null) return;

        service.preprocess(configuration, request, response, context);
    }


	public static RenderResult render(ToolConfiguration configuration,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
        throws IOException {
		org.sakaiproject.portal.render.api.ToolRenderService service = getInstance();
		if (service == null)
            return null;

        return service.render(configuration, request, response, context);
    }

}
