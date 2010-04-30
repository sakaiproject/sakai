/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

package org.sakaiproject.poll.logic;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.sakaiproject.poll.model.PollRolePerms;

public interface ExternalLogic {

	/**
	 * Check if this user has super admin access
	 * 
	 * @param userId the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin(String userId);
	
	
	/**
	 * Check if the current user has super admin access
	 * 
	 * @param userId the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin();
	
	
	
	
	/**
	 * @return the current location id of the current user
	 */
	public String getCurrentLocationId();
	
	
	/**
	 * @return the current location reference of the current user
	 */
	public String getCurrentLocationReference();
	

	/**
	 * @return the current sakai user id (not username)
	 */
	public String getCurrentUserId();
	
	/**
	 * Get the current user reference (/user/admin)
	 * @return
	 */
	public String getCurrentuserReference();
	
	/**
	 * is the current user allowed to perform the action in the current location?
	 * @param permission
	 * @param locationReference
	 */
	public boolean isAllowedInLocation(String permission, String locationReference);
	
	/**
	 * is the current user allowed to perform the action in the current location?
	 * @param permission
	 * @param locationReference
	 * @param the user
	 */
	public boolean isAllowedInLocation(String permission, String locationReference, String userRefence);
	
	/**
	 * Get the sites  users is a member of  
	 * @param userId
	 * @param permission
	 * @return a list of site references
	 */
	public List<String> getSitesForUser(String userId, String permission);
	
	/**
	 * Post a new event to the event tracking service
	 * @param eventId
	 * @param reference
	 * @param does the event modify state?
	 */
	public void postEvent(String eventId, String reference, boolean modify);
	
	/**
	 * Register a function with the Sakai Function manager
	 * @param function
	 */
	public void registerFunction(String function);
	
	/** 
	 *  get the correct Timezone for the the current user
	 * @return
	 */
	public TimeZone getLocalTimeZone();
	
	
	/**
	 * Get a list of RoleIds in a the given realms
	 * @param RealmId
	 * @return a list os frings of the role Ids
	 */
	public List<String> getRoleIdsInRealm(String realmId);
	
	
	/**
	 * is the role allowed to perform the function in the given realm?
	 * @param RoleId
	 * @param realmId
	 * @return
	 */
	public boolean isRoleAllowedInRealm(String roleId, String realmId, String permission);
	
	/**
	 * 
	 * @param locationReference
	 * @return
	 */
	public String getSiteTile(String locationReference);
	
	/**
	 * Get a site reference "/site/ABCD from its id (ABCD)
	 * @param siteId
	 * @return
	 */
	public String getSiteRefFromId(String siteId);
	/**
	 * Set the tool permissions for the given location
	 * @param permMap
	 * @param locationReference
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	public void setToolPermissions(Map<String, PollRolePerms> permMap, String locationReference) throws SecurityException, IllegalArgumentException;
	
	/**
	 * Get the Roles in a site
	 * @param locationReference
	 * @return
	 */
	public Map<String, PollRolePerms> getRoles(String locationReference);
	
	/**
	 * is the user using the "view as ..." feature
	 * @return
	 */
	public boolean userIsViewingAsRole();
	
}
