/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.portal.api;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.api.Placement;

/**
 * Stored state is used to store the request state over a number of requests,
 * It is used to restore state over a login sequence of during a direct placement 
 * into a tool state.
 * @author ieb
 *
 */
public interface StoredState
{

	/**
	 * 
	 * @return
	 */
	String getToolContextPath();

	/**
	 * 
	 * @param req
	 * @return
	 */
	HttpServletRequest getRequest(HttpServletRequest req);

	/**
	 * 
	 * @return
	 */
	Placement getPlacement();

	/**
	 * 
	 * @return
	 */
	String getToolPathInfo();

	/**
	 * 
	 * @return
	 */
	String getSkin();

	/**
	 * 
	 * @param req
	 */
	void setRequest(HttpServletRequest req);

	/**
	 * 
	 * @param siteTool
	 */
	void setPlacement(Placement siteTool);

	/**
	 * 
	 * @param toolContextPath
	 */
	void setToolContextPath(String toolContextPath);

	/**
	 * 
	 * @param toolPathInfo
	 */
	void setToolPathInfo(String toolPathInfo);

	/**
	 * 
	 * @param skin
	 */
	void setSkin(String skin);

}
