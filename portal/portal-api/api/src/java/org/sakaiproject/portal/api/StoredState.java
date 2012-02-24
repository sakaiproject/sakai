/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.api;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.api.Placement;

/**
 * Stored state is used to store the request state over a number of requests, It
 * is used to restore state over a login sequence of during a direct placement
 * into a tool state.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public interface StoredState
{

	/**
	 * Get the Tool Context Path that the state was stored against
	 * @return
	 */
	String getToolContextPath();

	/**
	 * Get the request of the stored state
	 * @param req
	 * @return
	 */
	HttpServletRequest getRequest(HttpServletRequest req);

	/**
	 * Get the placement of the stored state
	 * @return
	 */
	Placement getPlacement();

	/**
	 * Get the Path info to the target tool
	 * @return
	 */
	String getToolPathInfo();

	/**
	 * Get the skin associated with the stored state
	 * @return
	 */
	String getSkin();

	/**
	 * Set the request in the stored state
	 * @param req
	 */
	void setRequest(HttpServletRequest req);

	/**
	 * set the placement 
	 * @param siteTool
	 */
	void setPlacement(Placement siteTool);

	/**
	 * set the tool context
	 * @param toolContextPath
	 */
	void setToolContextPath(String toolContextPath);

	/**
	 * set the tool path info
	 * @param toolPathInfo
	 */
	void setToolPathInfo(String toolPathInfo);

	/**
	 * set the skin
	 * @param skin
	 */
	void setSkin(String skin);

}
