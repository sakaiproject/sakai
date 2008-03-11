/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation.
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
package org.sakaiproject.api.app.messageforums.ui;

import java.util.List;
import java.util.Map;

public interface SynopticMFManager {	
	/**
	 * Return a Map used to determine which tools 
	 * (Messages & Forums, Messages, Forums) are in
	 * List of sites passed in
	 * @param siteList
	 * @return
	 */
	public Map<String, String> fillToolsInSites(List siteList);

	/**
	 * Returns Map of groups user a member of per
	 * site from List of sites passed in
	 * @param siteList
	 * @return
	 */
	public Map<String, String> getGroupMembershipsForSites(List<String> siteList);
	  
	/**
	 * Returns a Map<siteId, role> for all sites for current user
	 * 
	 * original query author: Chen Wen
	 */
	public Map<String, String> getUserRoleForAllSites();
}
