/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.logic;

import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.List;

/**
 * An interface to abstract all Sakai related API calls in a central method that can be injected into our app.
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public interface SakaiProxy {

	/**
	 * Get current siteid
	 *
	 * @return the SiteID
	 */
	String getCurrentSiteId();

	/**
	 * Get the current site
	 *
	 * @return the Site Title
     */
	String getCurrentSiteTitle();

	/**
	 * Get current User
	 *
	 * @return the current User
     */
	User getCurrentUser();

	/**
	 * Get current user id
	 *
	 * @return the current User ID
	 */
	String getCurrentUserId();
	
	/**
	 * Get current user display name
	 *
	 * @return the current User DisplayName
	 */
	String getCurrentUserDisplayName();

	/**
	 * Get's the current user's role in the current site
	 *
	 * @return the current User's role in the current site
     */
	String getCurrentUserRoleInCurrentSite();

	/**
	 * Get current user's role in site.
	 *
	 * @return the Current User's role in a Site
     */
	String getCurrentUserRole(String siteId);

	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 *
	 * @return whether the current user is a superuser
	 */
	boolean isSuperUser();
	
	/**
	 * Post an event to Sakai
	 * 
	 * @param event			name of event
	 * @param reference		reference
	 * @param modify		true if something changed, false if just access
	 */
	void postEvent(String event, String reference, boolean modify);
		
	/**
	 * Get a configuration parameter as a boolean
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return the Boolean Config parameter
	 */
	boolean getConfigParam(String param, boolean dflt);
	
	/**
	 * Get a configuration parameter as a String
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return the Config Parameter
	 */
	String getConfigParam(String param, String dflt);

	/**
	 * Get the Current Site Membership IDs
	 *
	 * @return List of userIds in the current site
	 */
	List<String> getCurrentSiteMembershipIds();

	/**
	 * Get the site's membership Ids
	 * @param siteId the Site ID
	 * @return List of userIDs in SiteId
	 */
	List<String> getSiteMembershipIds(String siteId);

	/**
	 * Get the Users in the current Site
	 *
	 * @return List of Users in the current site
	 */
	List<User> getCurrentSiteMembership();

	/**
	 * Get the Users in Site
	 * @param siteId, siteId to get the users for
	 * @return List of Users
	 */
	List<User> getSiteMembership(String siteId);

	/**
	 * Get the Users in a group of the current Site
	 *
	 * @param groupId, the Group ID
	 * @return List of the User IDs
     */
	List<String> getGroupMembershipIdsForCurrentSite(String groupId);

	/**
	 * Get a Group's User IDs as Strings for a Site
	 *
	 * @param siteId, the SiteID
	 * @param groupId, the groupID
     * @return a List of Users in SiteId who are a part of GroupId
     */
	List<String> getGroupMembershipIds(String siteId, String groupId);

	/**
	 * Get Users of Group for Current Site
	 *
	 * @param groupId, the groupId
	 * @return a List of the Users
     */
	List<User> getGroupMembershipForCurrentSite(String groupId);

	/**
	 * Get Users in a Group of a Site
	 *
	 * @param siteId, the SiteID
	 * @param groupId, the Group ID
     * @return a List of Users in GroupId of SiteId
     */
	List<User> getGroupMembership(String siteId, String groupId);

	/**
	 * Get Users in a Section of a Site
	 *
	 * @param siteId, the SiteID
	 * @param groupId, the Group ID
	 * @return a List of Users in SectionId of SiteId
	 */
	List<User> getSectionMembership(String siteId, String groupId);

	/**
	 * Get all available Groups for Site
	 *
	 * @param siteId, the SiteId
	 * @return a List of the Group IDs as Strings
     */
	List<String> getAvailableGroupsForSite(String siteId);

	/**
	 * Get available groups for current site
	 *
	 * @return a List of the available groups for the current site
     */
	List<String> getAvailableGroupsForCurrentSite();

	/**
	 * Get the name of a group for a site that has a particular user as a member
	 * @return a String that is the title of the user's group within the site
	 */
	String getUserGroupWithinSite(List<String> groupIds, String userId, String siteId);

	/**
	 * get user
	 *
	 * @param userId, the userId
	 * @return the user
     */
	User getUser(String userId);

	/**
	 * get user
	 *
	 * @param userEid, the userId
	 * @return the user
	 */
	User getUserByEID(String userEid);

	/**
	 * get's a user sort name
	 *
	 * @param userId, the userID
	 * @return their sort name
     */
	String getUserSortName(String userId);

	/**
	 *
	 * @param userEid
	 * @return
	 */
	String getUserSortNameByEID(final String userEid);

	/**
	 * Get a user's display id (username) ex. jdoe1
	 *
	 * @param userId, the UserId
	 * @return their display name
     */
	String getUserDisplayId(String userId);

	/**
	 * Get's a Group's Title in Current Site
	 *
	 * @param groupId, the GroupID
	 * @return the title of the group
     */
	String getGroupTitleForCurrentSite(String groupId);

	/**
	 * Get the title for a group in a Site
	 *
	 * @param siteId, the siteID
	 * @param groupId, the GroupID
     * @return the title of the group
     */
	String getGroupTitle(String siteId, String groupId);
}
