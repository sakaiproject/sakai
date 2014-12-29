/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.tool.api;

import java.util.List;

import java.io.File;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.w3c.dom.Document;

/**
 * <p>
 * Extension API for ToolManager that introduces Servlet API specific activity.
 * </p>
 */
public interface ActiveToolManager extends ToolManager
{
	/**
	 * Add this tool to the registry.
	 * @param tool The Tool to register.
	 * @param config The ServletContext to be attached to the tool. Only applied to the default implementation,
	 *               as supplied through the XML signatures (usually via the ToolListener), or a bare Tool instance,
	 *               which will be decorated. Ignored if supplying a custom ActiveTool implementation (or proxy).
	 */
	void register(Tool tool, ServletContext config);
	
	/**
	 * Add tools in this XML DOM to the registry, using the Tool XML schema.
	 * @param toolXml The parsed XML DOM in which tools to be added to the registry are to be found.
	 */
	void register(Document toolXml, ServletContext config);
	
	/**
	 * Add tools in this file of Tool XML schema to the registry.
	 * @param toolXmlFile The file of Tool schema XML in which tools to be added to the registry are to be found.
	 */
	void register(File toolXmlFile, ServletContext config);

	/**
	 * Add tools in this stream of Tool XML schema to the registry.
	 * @param toolXmlStream The file of Tool schema XML in which tools to be added to the registry are to be found.
	 * @param config the Servlet context
	 */
	void register(InputStream toolXmlStream, ServletContext config);

	/**
	 * Parse a registration file and return a list of Tool Registrations
	 * @param toolXmlFile The file of Tool schema XML in which tools to be added to the registry are to be found.
	 */
	List<Tool> parseTools(File toolXmlFile);

	/**
	 * Parse a registration file and return a list of Tool Registrations
	 * @param toolXml The file of Tool schema XML in which tools to be added to the registry are to be found.
	 */
	List<Tool> parseTools(Document toolXml);

	/**
	 * Parse a registration file and return a list of Tool Registrations
	 * @param toolXmlStream The file of Tool schema XML in which tools to be added to the registry are to be found.
	 */
	List<Tool> parseTools(InputStream toolXmlStream);

	/**
	 * Find a tool with this well known id in the registry.
	 * @param id The tool's well known id.
	 * @return The Tool object that has this id, or null if not found.
	 */
	ActiveTool getActiveTool(String id);

}



