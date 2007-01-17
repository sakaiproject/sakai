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
package uk.ac.cam.caret.sakai.rwiki.tool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.DataMigrationController;

/**
 * @author ieb
 */

public class ModelMigrationContextListener implements ServletContextListener
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent contextEvent)
	{
		try
		{
			WebApplicationContext wac = WebApplicationContextUtils
					.getWebApplicationContext(contextEvent.getServletContext());
			// Logger log = (Logger)wac.getBean("rwiki-logger");
			DataMigrationController dataMig = (DataMigrationController) wac
					.getBean("rwikiDataMigration");
			dataMig.update();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(
					"Data Migration Failed, you should investigate this before restarting, or remove the RWiki tool from Sakai",
					ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent contextEvent)
	{

	}

}
