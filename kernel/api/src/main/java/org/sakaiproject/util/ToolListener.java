/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Iterator;
import java.util.Set;
import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.cover.ActiveToolManager;

/**
 * <p>
 * Webapp listener to detect webapp-housed tool registration.
 * </p>
 */
public class ToolListener implements ServletContextListener
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ToolListener.class);

	/**
	 * Initialize.
	 */
	public void contextInitialized(ServletContextEvent event)
	{
		// find the resources in the webapp in the /tools/ area TODO: param this
		Set paths = event.getServletContext().getResourcePaths("/tools/");
		if (paths == null) return;

		for (Iterator i = paths.iterator(); i.hasNext();)
		{
			String path = (String) i.next();

			// skip directories
			if (path.endsWith("/")) continue;

			// load this
			M_log.info("registering tools from resource: " + path);
			ActiveToolManager.register(new File(event.getServletContext().getRealPath(path)), event.getServletContext());
		}
	}

	/**
	 * Destroy.
	 */
	public void contextDestroyed(ServletContextEvent event)
	{
	}
}
