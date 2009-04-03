/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rights.api;

import org.sakaiproject.exception.IdUnusedException;

public interface RightsService 
{
	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment addRightsAssignment(String entityRef);
	
	/**
	 * @param context
	 * @return
	 */
	public SiteRightsPolicy addSiteRightsPolicy(String context);
	
	/**
	 * @param context
	 * @param userId
	 * @return
	 */
	public UserRightsPolicy addUserRightsPolicy(String context, String userId);
	
	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment getRightsAssignment(String entityRef) throws IdUnusedException;
	
	/**
	 * @param context
	 * @return
	 */
	public SiteRightsPolicy getSiteRightsPolicy(String context);
	
	/**
	 * @param context
	 * @param userId
	 * @return
	 */
	public UserRightsPolicy getUserRightsPolicy(String context, String userId);
	
	/**
	 * @param rights
	 */
	public void save(RightsAssignment rights);
	
	/**
	 * @param policy
	 */
	public void save(RightsPolicy policy);
	
	/**
	 * @param entityRef
	 * @param rights
	 */
	public void setRightsAssignment(String entityRef, RightsAssignment rights);
		
}	// interface CopyrightService
