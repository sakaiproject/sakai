/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.tool.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * ToolManager is a static Cover for the {@link org.sakaiproject.tool.api.ToolManager ToolManager}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class ToolManager
{
	/** Possibly cached component instance. */
	private static org.sakaiproject.tool.api.ToolManager m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 *
	 * @return the component instance.
	 */
	public static org.sakaiproject.tool.api.ToolManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.tool.api.ToolManager) ComponentManager
						.get(org.sakaiproject.tool.api.ToolManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.tool.api.ToolManager) ComponentManager.get(org.sakaiproject.tool.api.ToolManager.class);
		}
	}

	public static void register(org.sakaiproject.tool.api.Tool param0)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return;

		manager.register(param0);
	}

	public static void register(org.w3c.dom.Document param0)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return;

		manager.register(param0);
	}

	public static void register(java.io.InputStream param0)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return;

		manager.register(param0);
	}

	public static void register(java.io.File param0)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return;

		manager.register(param0);
	}

	public static org.sakaiproject.tool.api.Tool getTool(java.lang.String param0)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return null;

		return manager.getTool(param0);
	}

	public static java.util.Set findTools(java.util.Set param0, java.util.Set param1)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return null;

		return manager.findTools(param0, param1);
	}

	public static org.sakaiproject.tool.api.Tool getCurrentTool()
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return null;

		return manager.getCurrentTool();
	}

	public static org.sakaiproject.tool.api.Placement getCurrentPlacement()
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return null;

		return manager.getCurrentPlacement();
	}

	public static void setResourceBundle (String toolId, String filename)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		manager.setResourceBundle (toolId, filename);
	}
	
	public static boolean isVisible (org.sakaiproject.site.api.Site site, org.sakaiproject.site.api.ToolConfiguration config)
	{
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		return manager.isVisible (site, config);
	}
	
	public static String getLocalizedToolProperty(String toolId, String key){
		org.sakaiproject.tool.api.ToolManager manager = getInstance();
		if (manager == null) return null;

		return manager.getLocalizedToolProperty(toolId, key);
	}	
	
}
