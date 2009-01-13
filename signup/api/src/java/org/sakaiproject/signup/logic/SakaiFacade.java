/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic;

import java.util.List;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.UserNotDefinedException;

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
	 * get all users, who have joined in the event/meeting
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @return a list of SignupMeeting objects
	 */
	public List<SignupUser> getAllUsers(SignupMeeting meeting);

	/**
	 * get Calendar for this specific siteId
	 * 
	 * @param siteId
	 *            a unique id which represents the current site
	 * @return a Calendar object
	 * @throws IdUnusedException
	 *             throw if siteId is not found
	 * @throws PermissionException
	 *             throw if user has no permission
	 */
	public Calendar getCalendar(String siteId) throws IdUnusedException, PermissionException;

	/**
	 * get Calendar object by calendar unique Id
	 * 
	 * @param calendarId
	 *            a unique Calendar Id
	 * @return a Calendar object
	 * @throws IdUnusedException
	 *             throw if siteId is not found
	 * @throws PermissionException
	 *             throw if user has no permission
	 */
	public Calendar getCalendarById(String calendarId) throws IdUnusedException, PermissionException;

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
	 * get a TimeService object from one of the Sakai service
	 * 
	 * @return a TimeService object.
	 */
	public TimeService getTimeService();

}
