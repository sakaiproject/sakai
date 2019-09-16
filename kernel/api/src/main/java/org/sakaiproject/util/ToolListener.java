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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.ActiveToolManager;

/**
 * <p>
 * Webapp listener to detect webapp-housed tool registration.<br/>
 * SAK-8908:<br/>
 * Re-wrote the contextInitialized() method to add tool localization files to the
 * newly registered tool(s).  These files are required to be in the tools directory
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
 * <p>
 * By default the tools directories looked at in the webapp are /tools/ and /WEB-INF/tools/ . It is
 * recommended that all tool registration files are put in /WEB-INF/tools/ as the files don't need to
 * served up by the container, however /tools/ is supported for backwards compatibility. If you wish to
 * use a custom location set an parameter on the tool listener:
 * </p>
 * <p>
 * <code>
 * &lt;context-param&gt;<br>
 * &nbsp;&lt;param-name&gt;org.sakaiproject.util.ToolListener.PATH&lt;/param-name&gt;<br>
 * &nbsp;&lt;param-value&gt;/mypath/&lt;/param-value&gt;<br>
 * &lt;/context-param&gt;<br>
 * </code>
 * </p>
 */
@Slf4j
public class ToolListener implements ServletContextListener
{
	/**
	 * The content parameter in your web.xml specifying the webapp root relative
	 * path to look in for the tool registration files.
	 */
	public final static String PATH = ToolListener.class.getName()+".PATH";

	private final ActiveToolManager activeToolManager;
	private final ServerConfigurationService serverConfigurationService;

	public ToolListener()
	{
		activeToolManager = ComponentManager.get(ActiveToolManager.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
	}

	public ToolListener(ActiveToolManager activeToolManager, ServerConfigurationService serverConfigurationService)
	{
		this.activeToolManager = activeToolManager;
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * Initialize.
	 */
	public void contextInitialized(ServletContextEvent event)
	{
		final String sakaiHomePath = serverConfigurationService.getSakaiHomePath();
		// The the location of resource and registration files.
		ServletContext context = event.getServletContext();
		Set<String> paths = getToolsPaths(context);
		if (paths == null) return;
		int registered = 0;
		// First Pass: Search for tool registration files
		for (final String path : paths) {
			// skip directories
			if (path.endsWith("/")) continue;

			// If an XML file, use it as the tool registration file.
			if (path.endsWith(".xml")) {
				String file = path.substring(path.lastIndexOf("/") + 1);
				// overrides are always in a folder called /tools/
				final File f = new File(sakaiHomePath + "/tools/" + file);
				if (f.exists()) {
					activeToolManager.register(f, context);
					log.info("overriding tools configuration: registering tools from resource: " + sakaiHomePath + path);
				} else {
					log.info("registering tools from resource: " + path);
					activeToolManager.register(context.getResourceAsStream(path), context);
				}
				registered++;
			}
		}
		
		if (registered == 0)
		{
			// Probably misconfigured as we should have at least one registered.
			log.warn("No tools found to be registered.");
		}

		//	Second pass, search for message bundles.  Two passes are necessary to make sure the tool is registered first.
		for (String path : paths) {
			// skip directories
			if (path.endsWith("/")) continue;

			//	Check for a message properties file.
			if (path.endsWith(".properties")) {
				//	Extract the tool id from the resource file name.
				File reg = new File(path);
				String tn = reg.getName();
				String tid;
				if (tn.indexOf('_') == -1)
					tid = tn.substring(0, tn.lastIndexOf('.'));    //	Default file.
				else
					tid = tn.substring(0, tn.indexOf('_'));        //	Locale-based file.

				String msg = context.getRealPath(path.substring(0, path.lastIndexOf('.')) + ".properties");
				activeToolManager.setResourceBundle(tid, msg);
				log.info("Added localization " + tn + "resources for " + tid);
			}
		}
	}

	/**
	 * This looks for the tools folders that shoudl be used.
	 * @param context The ServletContext
	 * @return A list of all possible tool registration files or <code>null</code> if the containing folders weren't found.
	 */
	private Set<String> getToolsPaths(ServletContext context) {
		Collection<String> toolFolders = getToolsFolders(context);
		Set<String> paths = new HashSet<>();
		toolFolders.stream().map(context::getResourcePaths).filter(files -> files != null).forEach(paths::addAll);
		if (paths.isEmpty())
		{
			// Warn if the listener is setup but no tools found.
			log.warn("No tools folder found: "+
				toolFolders.stream().map(context::getRealPath).collect(Collectors.joining(", ")));
			return null;
		}
		return paths;
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
	protected Collection<String> getToolsFolders(ServletContext context)
	{
		String path = context.getInitParameter(PATH);
		Collection<String> paths;
		if (path == null)
		{
			Collection<String> defaultPaths = new LinkedList<>();
			defaultPaths.add("/WEB-INF/tools/");
			defaultPaths.add("/tools/");
			paths = defaultPaths;
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
			paths = Collections.singleton(path);
		}
		return paths;
	}
}
