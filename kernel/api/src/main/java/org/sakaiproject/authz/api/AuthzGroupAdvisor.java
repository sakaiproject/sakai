/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 Apereo Foundation
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

package org.sakaiproject.authz.api;

/**
 * AuthzGroupAdvisor
 *
 * A AuthzGroupAdvisor can be registered with the AuthzGroupService, which will be called
 * prior to an AuthzGroup being saved. The Advisor allows for any customizations to be made
 * to an AuthzGroup before the changes are committed.
 * 
 * @author Earle Nietzel
 * Created on Jul 17, 2013
 * 
 */
public interface AuthzGroupAdvisor {

	/** 
	 * Called when a AuthzGroup is about to be saved
	 * 
	 * @param group The AuthzGroup being saved
	 */
	public void update(AuthzGroup group);

	/** 
	 * Called when a AuthzGroup group update is about to occur.
	 * Specifically addMemberToGroup and removeMemberFromGroup
	 * 
	 * @param group The AuthzGroup group being updated
	 * @param userId The id of the user being added or removed
	 * @param roleId The id of the users role
	 */
	public void groupUpdate(AuthzGroup group, String userId, String roleId);

	/**
	 * Called when a AuthzGroup is about to be removed
	 * 
	 * @param group The AuthzGroup being deleted
	 */
	public void remove(AuthzGroup group);
}
