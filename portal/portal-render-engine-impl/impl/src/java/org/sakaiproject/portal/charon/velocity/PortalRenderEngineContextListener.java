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

package org.sakaiproject.portal.charon.velocity;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.cover.PortalService;
import org.sakaiproject.tool.cover.SessionManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Context Listener for the render engine.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class PortalRenderEngineContextListener implements ServletContextListener
{
	private VelocityPortalRenderEngine vengine;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0)
	{
		PortalService.getInstance().removeRenderEngine(Portal.DEFAULT_PORTAL_CONTEXT,
				vengine);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event)
	{
		try
		{
			vengine = new VelocityPortalRenderEngine();
			vengine.setContext(event.getServletContext());
			vengine.setServerConfigurationService(ServerConfigurationService.getInstance());
			vengine.setSessionManager(SessionManager.getInstance());
			vengine.init();
			vengine.setPortalService(PortalService.getInstance());
			PortalService.getInstance().addRenderEngine(Portal.DEFAULT_PORTAL_CONTEXT,
					vengine);
		}
		catch (Exception ex)
		{
			log
					.error(
							"Failed to register render engine with the portal service, this is probably fatal ",
							ex);
		}

	}

}
