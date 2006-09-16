/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/content/trunk/content-tool/tool/src/java/org/sakaiproject/content/tool/ResourcesAction.java $
 * $Id: ResourcesAction.java 13885 2006-08-21 16:03:28Z jimeng@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.rights.api;

public interface RightsService 
{
	/**
	 * @param entityRef
	 * @param rights
	 */
	public void setRightsAssignment(String entityRef, RightsAssignment rights);
	
	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment getRightsAssignment(String entityRef);
	
	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment addRightsAssignment(String entityRef);
	
	/**
	 * @param rights
	 */
	public void save(RightsAssignment rights);
	
	public UserRightsPolicy addUserRightsPolicy(String context, String userId);
	public SiteRightsPolicy addSiteRightsPolicy(String context);
	public UserRightsPolicy getUserRightsPolicy(String context, String userId);
	public SiteRightsPolicy getSiteRightsPolicy(String context);
	public void save(RightsPolicy policy);
		
}	// interface CopyrightService
