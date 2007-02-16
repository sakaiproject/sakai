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

public interface StoredState
{

	String getToolContextPath();

	HttpServletRequest getRequest(HttpServletRequest req);

	Placement getPlacement();

	String getToolPathInfo();

	String getSkin();

	void setRequest(HttpServletRequest req);

	void setPlacement(Placement siteTool);

	void setToolContextPath(String toolContextPath);

	void setToolPathInfo(String toolPathInfo);

	void setSkin(String skin);

}
