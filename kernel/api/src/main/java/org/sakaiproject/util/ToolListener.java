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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Webapp listener to detect webapp-housed tool registration.<br/>
 * SAK-8908:<br/>
 * Re-wrote the contextInitialized() method to add tool localization files to the
 * newly registered tool(s).  These files are required to be in the /tool/ directory
 * have have a name of the form [toolId][_][localCode].properties.  This format allows
 * tool litles (etc) to be localized even when multiple tool registrations are included.
 * </p>
 * This listener can be added to the web.xml file with a snippet like this:
 * <p>
 * <code>
 * &lt;listener&gt;<br/>
 * &nbsp;&lt;listener-class&gt;org.sakaiproject.util.ToolListener&lt;/listener-class&gt;<br/>
 * &lt;/listener&gt;<br/>
 * </code>
 * </p>
 */
public class ToolListener implements ServletContextListener
{
	private static final Logger M_log = LoggerFactory.getLogger(ToolListener.class);

	/**
	 * The content parameter in your web.xml specifying the webapp root relative
	 * path to look in for the tool registration files.
	 */
	public final static String PATH = ToolListener.class.getName()+".PATH";
	
	/**
	 * Initialize.
	 */
	public void contextInitialized(ServletContextEvent event)
	{
		final String sakaiHomePath = ServerConfigurationService.getSakaiHomePath();
		// The the location of resource and registration files.
		ServletContext context = event.getServletContext();
		String toolFolder = getToolsFolder(context);
		Set<String> paths = context.getResourcePaths(toolFolder);
		if (paths == null)
		{
			// Warn if the listener is setup but no tools found.
			M_log.warn("No tools folder found: "+ context.getRealPath(toolFolder));
			return;
		}
		int registered = 0;
		// First Pass: Search for tool registration files
		for(Iterator<String> i = paths.iterator(); i.hasNext();)
		{
			final String path = i.next();

			// skip directories
			if (path.endsWith("/")) continue;

			// If an XML file, use it as the tool registration file.
			if (path.endsWith(".xml"))
			{
				final File f = new File(sakaiHomePath + path);
				if(f.exists()) {
					ActiveToolManager.register(f, event.getServletContext());
					M_log.info("overriding tools configuration: registering tools from resource: " + sakaiHomePath + path);
				} else {
					M_log.info("registering tools from resource: " + path);
					ActiveToolManager.register(event.getServletContext().getResourceAsStream(path), event.getServletContext());
				}
				registered++;
			}
		}
		
		if (registered == 0)
		{
			// Probably misconfigured as we should have at least one registered.
			M_log.warn("No tools found to be registered.");
		}

		//	Second pass, search for message bundles.  Two passes are necessary to make sure the tool is registered first.
		for (Iterator j = paths.iterator(); j.hasNext();)
		{
			String path = (String) j.next();

			// skip directories
			if (path.endsWith("/")) continue;

			//	Check for a message properties file.
			if (path.endsWith(".properties"))
			{
				//	Extract the tool id from the resource file name.
				File reg = new File (path);
				String tn = reg.getName();
				String tid = null;
				if (tn.indexOf('_') == -1)
					tid = tn.substring (0, tn.lastIndexOf('.'));	//	Default file.
				else
					tid = tn.substring (0, tn.indexOf('_'));		//	Locale-based file.

				String msg = event.getServletContext().getRealPath(path.substring (0, path.lastIndexOf('.'))+".properties");
				if (tid != null)
				{
					ActiveToolManager.setResourceBundle (tid, msg);
					M_log.info("Added localization resources for " + tid);
				}
			}
		}
	}

	/**
	 * Destroy.
	 */
	public void contextDestroyed(ServletContextEvent event)
	{
	}

	/**
	 * This locates the tool registration folder inside the webapp.
	 * @param context The servlet context.
	 * @return The standard tool registration folder location or the configured value.
	 */
	protected String getToolsFolder(ServletContext context)
	{
		String path = context.getInitParameter(PATH);
		if (path == null)
		{
			path = "/tools/";
		}
		else
		{
			if (!path.startsWith("/"))
			{
				path = "/"+ path;
			}
			if (!path.endsWith("/"))
			{
				path = path+ "/";
			}
		}
		return path;
	}
}
