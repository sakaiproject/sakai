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
 *       http://www.opensource.org/licenses/ECL-2.0
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

import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.poll.model.PollRolePerms;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.tool.api.ToolSession;

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
	 * Given a userId, return the associated userEid
	 * @param userId The userId
	 * @return
	 * 	The userEid
	 */
	public String getUserEidFromId(String userId);
	
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
	 * @param siteId
	 * @return
	 */
	public String getSiteTile(String siteId);
	
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
	
	/**
	 * Notify a list of users that an option they voted for in a poll has been deleted.
	 * 
	 * @param userEids
	 * 	A List of user EID's that identify the users to be notified
	 * @param pollQuestion
	 * 	The text of the poll whose option was deleted
	 * @param siteTitle
	 * 	The title of the site that owns the option's poll
	 */
	public void notifyDeletedOption(List<String> userEids, String siteTitle, String pollQuestion);
	
	
	
	/**
	 * Needed to invoke helper tools
	 */
	public ToolSession getCurrentToolSession();
	
	/**
	 * Are charts enabled on the results page?
	 * poll.results.chart.enabled=true|false, default false
	 * @return
	 */
	public boolean isResultsChartEnabled();
	
	/**
	 * Are the public access options available to the Instructor?
	 * poll.allow.public.access=true|false, default false
	 * @return
	 */
	public boolean isShowPublicAccess();

	/**
	 * Is the current user using a mobile browser?
	 * @return
	 */
	public boolean isMobileBrowser();
	
	/**
	 * Get a list of the permission keys for the tool
	 * @return
	 */
	public List<String> getPermissionKeys();

    /**
     * Register a statement with the system LearningResourceStoreService
     */
    public void registerStatement(String pollText, Vote vote);

    /**
     * Register a statement with the system LearningResourceStoreService
     */
    public void registerStatement(String pollText, boolean newPoll);

}
