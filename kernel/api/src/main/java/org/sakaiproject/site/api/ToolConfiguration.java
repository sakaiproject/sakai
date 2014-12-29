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

package org.sakaiproject.site.api;

import java.io.Serializable;

import org.sakaiproject.tool.api.Placement;

/**
 * <p>
 * ToolConfiguration is a the placement of a tool on a site page; a placement with layout information.
 * </p>
 */
public interface ToolConfiguration extends Placement, Serializable
{
	/**
	 * @return the layout hints for this tool.
	 */
	public String getLayoutHints();

	/**
	 * Set the layout hints.
	 * 
	 * @param hints
	 *        The layout hints.
	 */
	public void setLayoutHints(String hints);

	/**
	 * If the layout hints are a row,col format, return the two numbers, else return null.
	 */
	public int[] parseLayoutHints();

	/**
	 * @return the skin to use for this tool.
	 */
	public String getSkin();

	/**
	 * @return the page id for this tool.
	 */
	public String getPageId();

	/**
	 * @return the site id for this tool.
	 */
	public String getSiteId();

	/**
	 * Access the SitePage in which this tool configuration lives.
	 * 
	 * @return the SitePage in which this tool configuration lives.
	 */
	public SitePage getContainingPage();

	/**
	 * Move this tool one step towards the start of the order of tools in this page.
	 */
	public void moveUp();

	/**
	 * Move this tool one step towards the end of the order of tools in this page.
	 */
	public void moveDown();

	/**
	 * Access the tool's order in the page.
	 * 
	 * @return the tool's order in the page.
	 */
	public int getPageOrder();
}
