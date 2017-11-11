/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.logic;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.FormattedText;

/**
 * <P>
 * This is an interface to provides all necessary methods, which are depend on
 * the Sakai Services. This will allow the separation of Signup Tool and the
 * Sakai Tools
 * </P>
 */
public interface SakaiFacade {

	public final static String NO_LOCATION = "noLocationAvailable";

	public static final String SIGNUP_VIEW = "signup.view";

	public static final String SIGNUP_VIEW_ALL = "signup.view.all";

	public static final String SIGNUP_ATTEND = "signup.attend";

	public static final String SIGNUP_ATTEND_ALL = "signup.attend.all";

	/** Can create meetings in site and groups in that site* */
	public static final String SIGNUP_CREATE_SITE = "signup.create.site";

	public static final String SIGNUP_CREATE_GROUP = "signup.create.group";

	public static final String SIGNUP_CREATE_GROUP_ALL = "signup.create.group.all";

	public static final String SIGNUP_DELETE_SITE = "signup.delete.site";

	public static final String SIGNUP_DELETE_GROUP = "signup.delete.group";

	public static final String SIGNUP_DELETE_GROUP_ALL = "signup.delete.group.all";

	public static final String SIGNUP_UPDATE_SITE = "signup.update.site";

	public static final String SIGNUP_UPDATE_GROUP = "signup.update.group";

	public static final String SIGNUP_UPDATE_GROUP_ALL = "signup.update.group.all";

	public static final String STUDENT_ROLE_ID = "student";
	
	public static final String REALM_ID_FOR_LOGIN_REQUIRED_ONLY =".auth";

	public static final String GROUP_PREFIX = "SIGNUP_";
	
	// see https://jira.sakaiproject.org/browse/SAK-21403
	//this is currently hardcode but could be moved later
	public static final String GROUP_PROP_SITEINFO_VISIBLE = "group_prop_wsetup_created";
	
	public static final String GROUP_PROP_SIGNUP_IGNORE = "group_prop_signup_ignore";
	
	/**
	 * check to see if the user is Admin
	 * 
	 * @param userId
	 *            userId the internal user id (not username)
	 * @return true if the user is Admin role
	 */
	boolean isUserAdmin(String userId);

	/**
	 * get current userId
	 * 
	 * @return the current sakai user id (not username)
	 */
	public String getCurrentUserId();

	/**
	 * Get the display name for a user by their unique id
	 * 
	 * @param userId
	 *            the current sakai user id (not username)
	 * @return display name (probably firstname lastname) or "----------" (10
	 *         hyphens) if none found
	 */
	public String getUserDisplayLastFirstName(String userId);
	
	/**
	 * Get the display name for a user by their unique id
	 * 
	 * @param userId
	 *            the current sakai user id (not username)
	 * @return display name (probably  lastname,firstname) or "----------" (10
	 *         hyphens) if none found
	 */
	public String getUserDisplayName(String userId);

	/**
	 * get current site Id
	 * 
	 * @return the current location id of the current user
	 */
	public String getCurrentLocationId();

	/**
	 * get current site title
	 * 
	 * @param locationId
	 *            a unique id which represents the current location of the user
	 *            (entity reference)
	 * @return the title for the context or "--------" (8 hyphens) if none found
	 */
	public String getLocationTitle(String locationId);

	/**
	 * Check if this user has access to the site
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param permission
	 *            function like signup.view
	 * @param siteId
	 *            site context id
	 * @return true if the user has access, false otherwise
	 */
	boolean isAllowedSite(String userId, String permission, String siteId);

	/**
	 * Check if this user has access to the group in the site
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param permission
	 *            function like signup.create.group
	 * @param siteId
	 *            site context id
	 * @param groupId
	 *            group context id
	 * @return true if the user has access, false otherwise
	 */
	boolean isAllowedGroup(String userId, String permission, String siteId, String groupId);
	
	/**
	 * get the ToolManager object.
	 * 
	 * @return a ToolManager object.
	 */
	public ToolManager getToolManager();
	
	/**
	 * get all the published sites, which user joins in
	 * 
	 * @param userId
	 *            userId the internal user id (not username)
	 * @return a list of SignupSite objects
	 */
	public List<String> getUserPublishedSiteIds(String userId);

	/**
	 * get all the sites, which user joins in
	 * 
	 * @param userId
	 *            userId the internal user id (not username)
	 * @return a list of SignupSite objects
	 */
	public List<SignupSite> getUserSites(String userId);

	/**
	 * get internal user id
	 * 
	 * @param eid
	 *            a unique id (enterprise Id)
	 * @return String internal user id
	 * @throws UserNotDefinedException
	 *             throw if user is not found
	 */
	public String getUserId(String eid) throws UserNotDefinedException;
	
	/**
	 * get the User object
	 * 
	 * @param userId
	 *            a sakai internal user Id
	 * @return an User object
	 */
	public User getUser(String userId);
	
	/**
	 * get the User object but do not log any messages.
	 * 
	 * @param userId
	 *            a sakai internal user Id
	 * @return an User object
	 */
	public User getUserQuietly(String userId);
	
	/**
	 * Does this user exist in the system? This only logs at debug level if they don't exist.
	 * @param userId
	 * @return	true if exists, false if not
	 */
	public boolean checkForUser(String userId);

	/**
	 * get all coordinators, who have create meeting permission in the event/meeting
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @return a list of SignupMeeting objects
	 */
	public List<SignupUser> getAllPossibleCoordinators(SignupMeeting meeting);
	
	/**
	 * get all coordinators, who have create meeting permission in the event/meeting
	 * This method is much efficient and fast. It may have extra people, who don't have view permission,
	 * which we have not checked. The chances are very small since they are the site instructor/tf
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @return a list of SignupMeeting objects
	 */
	
	public List<SignupUser> getAllPossbileCoordinatorsOnFastTrack(SignupMeeting meeting);
	
	/**
	 * test whether a user has permission to create a meeting in a meeting
	 * @param meeting
	 * @param userId
	 * @return
	 */
	public boolean hasPermissionToCreate(SignupMeeting meeting, String userId);
	
	/**
	 * get all users, who have joined in the event/meeting
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @return a list of SignupMeeting objects
	 */
	public List<SignupUser> getAllUsers(SignupMeeting meeting);
	
	/**
	 * get all users, who have permission to attend the meeting
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @return a list of SignupMeeting objects
	 */
	public List<SignupUser> getAllPossibleAttendees(SignupMeeting meeting);

	/**
	 * get Calendar for this specific siteId
	 * 
	 * @param siteId
	 *            a unique id which represents the current site
	 * @return a Calendar object, will return <code>null</code> if something went very wrong
	 * @throws PermissionException
	 *             throw if user has no permission
	 */
	public Calendar getCalendar(String siteId) throws PermissionException;

	/**
	 * get Calendar object by calendar unique Id
	 * 
	 * @param calendarId
	 *            a unique Calendar Id
	 * @return a Calendar object, will return <code>null</code> if something went very wrong
	 * @throws PermissionException
	 *             throw if user has no permission
	 */
	public Calendar getCalendarById(String calendarId) throws PermissionException;

	/**
	 * get group object accourding to the siteId and groupId
	 * 
	 * @param siteId
	 *            a unique id which represents the current site
	 * @param groupId
	 *            a unique id which represents the current group
	 * @return a Group object
	 * @throws IdUnusedException
	 *             throw if siteId or groupId is not found
	 */
	public Group getGroup(String siteId, String groupId) throws IdUnusedException;

	/**
	 * get ServerConfigurationService object
	 * 
	 * @return a ServerConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService();

	/**
	 * set a Sakai SiteService
	 * 
	 * @return a SiteService
	 */
	public SiteService getSiteService();

	/**
	 * get current pageId,which is an unique id
	 * 
	 * @return an unique page Id
	 */
	public String getCurrentPageId();

	/**
	 * get site-signup pageId,which is an unique id *
	 * 
	 * @param siteId
	 *            a unique site Id
	 * 
	 * @return an unique page Id
	 */
	public String getSiteSignupPageId(String siteId);

	/**
	 * get a TimeService object from one of the Sakai services
	 * 
	 * @return a TimeService object.
	 */
	public TimeService getTimeService();
	
	/**
	 * get a ContentHostingService from one of the Sakai services
	 * @return a ContentHostingService object.
	 */
	public ContentHostingService getContentHostingService();
	
	
	/**
	 * get the user, who has permission to attend the meeting
	 * @param meeting
	 * 		a SignupMeeting object
	 * @param userId
	 * 		userId the internal user id (not username)
	 * @return
	 * 		a SignupUser object
	 */
	public SignupUser getSignupUser(SignupMeeting meeting, String userId);
	
	/**
	 * Get a list of users in the current site that have the given permission
	 * @param permission	the permission to check
	 * @return a List of Users that match the criteria
	 */
	public List<User> getUsersWithPermission(String permission);
	
	/**
	 * Find users by an email address. This may return multiples so logic is needed to deal with that.
	 * @param email
	 * @return	a list of user objects or an empty list if none.
	 */
	public Collection<User> getUsersByEmail(String email);
	
	/**
	 * Get a user by email address. Only use this if you are certain that there is only one user that matches,
	 * as it will only return the first user if there are multiples.
	 * 
	 * @param email
	 * @return	a User or null if no match
	 */
	public User getUserByEmail(String email);
	
	/**
	 * Find a user by their eid.
	 * @param eid
	 * @return a user object or null if not found
	 */
	public User getUserByEid(String eid);
	
	/**
	 * Is csv export enabled? signup.csv.export.enabled=true/false
	 * @return
	 */
	public boolean isCsvExportEnabled();

    /**
     * Allow calendar.revise.any for the current user
     */ 
    public SecurityAdvisor pushAllowCalendarEdit(Calendar calendar);

    /**
     * Standard privileged push for the current user
     */ 
    public SecurityAdvisor pushSecurityAdvisor();
    
    /**
     * Pop the specified security advisor
     */ 
    public void popSecurityAdvisor(SecurityAdvisor advisor);
		
	/**
	 * Create a group in the specified site with the given title and description and optionally, a list of user uuids to populate it.
	 * The title will be prefixed with the constant GROUP_PREFIX
	 * @param siteId		site to create this group in
	 * @param title			group title
	 * @param description	group description
	 * @param userIds		list of users to populate the group with, optional. 
	 * @return The groupId
	 */
	public String createGroup(String siteId, String title, String description, List<String> userUuids);
	
	/**
	 * Add the users to the given group in the given site
	 * @param userIds		Collection of users, could be a single user
	 * @param siteId		id of the site
	 * @param groupId		id of the group
	 * @return	true if users added, false if not
	 */
	public boolean addUsersToGroup(Collection<String> userIds, String siteId, String groupId, String timeslottoGroup);
	
	/**
	 * Remove the user from the given group in the given site
	 * @param userId		uuid of the user
	 * @param siteId		id of the site
	 * @param groupId		id of the group
	 * @return	true if user removed, false if not
	 */
	public boolean removeUserFromGroup(String userId, String siteId, String groupId);
	
	/**
	 * Get the list of users in a group
	 * @param siteId		id of the site
	 * @param groupId		id of the group
	 * @return list of uuids for users in the group
	 */	
	public List<String> getGroupMembers(String siteId, String groupId);
	
	/**
	 * Check if a group with the given id exists
	 * @param siteId		id of the site
	 * @param groupId		id of the group
	 * @return	true if group exists, false if not.
	 */
	public boolean checkForGroup(String siteId, String groupId);
	
	/**
	 * Synchronize the group title if group title has not been modified directly via Site-Info tool
	 * @param siteId		id of the site
	 * @param groupId		id of the group
	 * @param newTitle		new group title
	 * @return
	 */
	public boolean synchonizeGroupTitle(String siteId, String groupId, String newTitle);
	
	// Returns Google calendar if the calendar has been created in Google
	public Calendar getAdditionalCalendar(String siteId) throws PermissionException;

	/**
	 * @return Returns the FormattedText service for use in cleaning up HTML.
	 */
	public FormattedText getFormattedText();
}
