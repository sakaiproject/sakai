/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.portal.charon.velocity;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.portal.api.cover.PortalService;

/**
 * @author ieb
 */
public class PortalRenderEngineContextListener implements ServletContextListener
{

	private static final Log log = LogFactory.getLog(PortalRenderEngineContextListener.class);
	private VelocityPortalRenderEngine vengine;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0)
	{
		PortalService.getInstance().removeRenderEngine(vengine);

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
			vengine.init();
			PortalService.getInstance().addRenderEngine(vengine);
		}
		catch (Exception ex)
		{
			log.error("Failed to register render engine with the portal service, this is probably fatal ", ex);
		}

	}

}
