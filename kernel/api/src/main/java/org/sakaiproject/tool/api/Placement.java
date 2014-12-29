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

package org.sakaiproject.tool.api;

import java.util.Properties;

/**
 * <p>
 * Tool Placement models a particular tool places in a particular place within a Sakai navigation or portal location.
 * </p>
 */
public interface Placement
{
	/**
	 * Access the configuration properties, combined from placement and registration, for the tool placement. Placement values override registration. Access is read only.
	 * 
	 * @return The read-only combined configuration properties for the tool.
	 */
	Properties getConfig();

	/**
	 * Access the placement context.
	 * 
	 * @return The context associated with this tool placement.
	 */
	String getContext();

	/**
	 * Get the tool placement id.
	 * 
	 * @return The tool placement id.
	 */
	String getId();

	/**
	 * Access the configuration properties for this tool placement - not including those from the tool registration.
	 * 
	 * @return The configuration properties for this tool placement - not including those from the tool registration.
	 */
	Properties getPlacementConfig();

	/**
	 * Access the tool placement title.
	 * 
	 * @return The tool placement title.
	 */
	String getTitle();

	/**
	 * Access the tool placed with this placement.
	 * 
	 * @return The tool placed with this placement.
	 */
	Tool getTool();

	/**
	 * Access the well-known tool-id of the tool associated with this placement.
	 * 
	 * @return The tool id associated with this placement.
	 */
	String getToolId();

	/**
	 * Set the title for this tool placement. Non-null values override the tool registration title.
	 * 
	 * @param title
	 *        The tool placement title.
	 */
	void setTitle(String title);

	/**
	 * Set the tool for this tool placement.
	 * 
	 * @param toolId
	 *        The tool's well-known tool-id.
	 * @param tool
	 *        The tool.
	 */
	void setTool(String toolId, Tool tool);

	/**
	 * Save any changes to the placement.
	 */
	void save();
}
