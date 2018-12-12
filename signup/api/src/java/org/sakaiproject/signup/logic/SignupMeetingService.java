/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.restful.SignupTargetSiteEventInfo;

/**
 * <p>
 * SignupMeetingService is an interface, which provides methods to manipulate
 * the SignupMeeting object to the DB, send email, post/edit Calendar and check
 * permission.
 * </p>
 */
public interface SignupMeetingService {

	/**
	 * This returns a list of SignupMeeting for a specified site that are
	 * available to the user in the site
	 * 
	 * @param currentSiteId
	 *            a unique id which represents the current site
	 * @param userId
	 *            the internal user id (not username)
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getAllSignupMeetings(String currentSiteId, String userId);

	/**
	 * This returns a subset list of SignupMeeting from Now to searchEndDate for
	 * the site
	 * 
	 * @param currentLocationId
	 *            a unique id which represents the current site Id
	 * @param currentUserId
	 *            the internal user id (not username)
	 * @param searchEndDate
	 *            date,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetings(String currentLocationId, String currentUserId, Date searchEndDate);

	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the site
	 * 
	 * @param currentSiteId
	 *            a unique id which represents the current site
	 * @param userId
	 *            the internal user id (not username)
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param endDate
	 *            date,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetings(String currentSiteId, String userId, Date startDate, Date endDate);
	
	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the site. The result might be 5 minutes old data due to ECache
	 * 
	 * @param siteId
	 *            unique id which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param timeFrameInDays
	 *            number of days ,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetingsInSiteWithCache(String siteId, Date startDate, int timeFrameInDays);
	
	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the sites, The result maybe 5 minutes old due to ECache
	 * 
	 * @param siteIds
	 *            a collection of unique ids which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param timeFrameInDays
	 *            number of days ,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetingsInSitesWithCache(List<String> siteIds, Date startDate, int timeFrameInDays);

	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the site.
	 * 
	 * @param siteId
	 *            unique id which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param endDate
	 *            end date ,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetingsInSite(String siteId, Date startDate, Date endDate);	
	
	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the sites with out cached
	 * 
	 * @param siteIds
	 *            a collection of unique ids which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param endDate
	 *            endDate ,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetingsInSites(List<String> siteIds, Date startDate, Date endDate);

	/**
	 * This returns a subset list of SignupMeetings with the same recurrenceId from starting date for
	 * the site
	 * 
	 * @param currentSiteId
	 *            a unique id which represents the current site
	 * @param userId
	 *            the internal user id (not username)
	 * @param recurrenceId
	 *            recurrenceId,which constraints the recurring meetings.         
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getRecurringSignupMeetings(String currentSiteId, String userId, Long recurrenceId, Date startDate);

	/**
	 * This saves meeting object into database
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object
	 * @param userId
	 *            the internal user id (not username)
	 * @return a unique Id for the SignupMeeting object from DB
	 * @throws PermissionException
	 *             thrown if the user does not have access
	 */
	Long saveMeeting(SignupMeeting signupMeeting, String userId) throws PermissionException;
	
	/**
	 * This saves meeting objects into database
	 * 
	 * @param signupMeeting
	 *            a list of SignupMeeting objects
	 * @param userId
	 *            the internal user id (not username)
	 * @return void
	 * @throws PermissionException
	 *             thrown if the user does not have access
	 */
	void saveMeetings(List<SignupMeeting> signupMeetings, String userId) throws PermissionException;

	/**
	 * This updates the SingupMeeting object into the database storage. If it's
	 * an organizer, permission: signup.update is required. Otherwise
	 * permission: signup.attend/signup.attend.all is required
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @param isOrganizer
	 *            true if the user is event-organizer
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void updateSignupMeeting(SignupMeeting meeting, boolean isOrganizer) throws Exception;
	
	/**
	 * This updates a list of SingupMeeting objects into the database storage. If it's
	 * an organizer, permission: signup.update is required. Otherwise
	 * permission: signup.attend/signup.attend.all is required
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects
	 * @param isOrganizer
	 *            true if the user is event-organizer
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void updateSignupMeetings(List<SignupMeeting> meetings, boolean isOrganizer) throws Exception;

	/**
	 * This updates a list of SingupMeeting objects into the database storage. If it's
	 * an organizer, permission: signup.update is required. Otherwise
	 * permission: signup.attend/signup.attend.all is required
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects
	 * @param removedTimeslots
	 *            a list of SignupTimeslot objects, which will be removed from the meeting
	 * @param isOrganizer
	 *            true if the user is event-organizer
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void updateModifiedMeetings(List<SignupMeeting> meetings, List<SignupTimeslot> removedTimeslots, boolean isOrganizer) throws Exception;
	
	/**
	 * This retrieve a SignupDefaultSiteEvent object from database according to the
	 * SignupMeeting Id. However, if the siteId is null, it will return a SignupTargetSiteEventInfo
	 *  object with a target siteId, in which the user has the highest permission level.
	 * 
	 * @param meetingId
	 *            a unique Id for SignupMeeting object
	 * @param userId
	 *            the internal user id (not username)
	 * @param siteId
	 *            a unique id which represents the current site
	 * @return a SignupTargetSiteEventInfo object
	 */
	SignupTargetSiteEventInfo loadSignupMeetingWithAutoSelectedSite(Long meetingId, String userId, String siteId);
	
	/**
	 * This retrieve a SignupMeeting object from database according to the
	 * SignupMeeting Id
	 * 
	 * @param meetingId
	 *            a unique Id for SignupMeeting object
	 * @param userId
	 *            the internal user id (not username)
	 * @param siteId
	 *            a unique id which represents the current site
	 * @return a SignupMeeting object
	 */
	SignupMeeting loadSignupMeeting(Long meetingId, String userId, String siteId);

	/**
	 * Test to see if the user has permission to create an event/meeting at site
	 * level in the site
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param siteId
	 *            a unique id which represents the current site
	 * @return true, if the user has permission to creating an event/meeting for
	 *         this site
	 */
	boolean isAllowedToCreateinSite(String userId, String siteId);

	/**
	 * Test to see if the user has permission to create an event/meeting at
	 * group level in the site
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param siteId
	 *            a unique id which represents the current site
	 * @param groupId
	 *            a unique id which represents the groupId at the site
	 * @return true, if the user has permission to creating an event/meeting at
	 *         group level in the site
	 */
	boolean isAllowedToCreateinGroup(String userId, String siteId, String groupId);

	/**
	 * Check permission for creating event/meeting either at a site level or at
	 * a group level
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param siteId
	 *            a unique id which represents the current site
	 * @return true, if the user has the permission to create
	 */
	boolean isAllowedToCreateAnyInSite(String userId, String siteId);

	/**
	 * This will send different kind of emails to all related participants in a
	 * meeting accourding to the meesage type
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object
	 * @param messageType
	 *            a string type, which classifies what type of message, which
	 *            should be emailed away
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void sendEmail(SignupMeeting signupMeeting, String messageType) throws Exception;

	/**
	 * An email will be sent to event/meeting organizer when attendee has taken
	 * some actions such as signup and cancel
	 * 
	 * @param eventTrackingInfo
	 *            an EventTrackingInfo object, which contains all the
	 *            information about user action such as signup and cancel as
	 *            well as auto-promotion
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void sendEmailToOrganizer(SignupEventTrackingInfo eventTrackingInfo) throws Exception;

	/**
	 * This sends cancellation email to event/meeting organizer as well as to
	 * the people on waiting list, who get promoted due to the attendee's
	 * cancellation
	 * 
	 * @param eventTrackingInfo
	 *            an EventTrackingInfo object, which contains all the
	 *            information about user action such as signup and cancel as
	 *            well as auto-promotion
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void sendCancellationEmail(SignupEventTrackingInfo eventTrackingInfo) throws Exception;
	
	/**
	 * This sends an email notifying the receiver that the comment has been modified
	 * 
	 * @param eventTrackingInfo
	 *            an EventTrackingInfo object, which contains all the
	 *            information about user action such as signup and cancel as
	 *            well as auto-promotion
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void sendUpdateCommentEmail(SignupEventTrackingInfo eventTrackingInfo) throws Exception;
	
	/**
	 * This will send email to participants by organizer
	 * 
	 * @param signupEventTrackingInfo
	 *            an EventTrackingInfo object, which contains all the
	 *            information about user action such as signup and cancel as
	 *            well as auto-promotion
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void sendEmailToParticipantsByOrganizerAction(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception;

	/**
	 * This method will post the event/meeting into the Calendar at Scheduler
	 * tool
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void postToCalendar(SignupMeeting signupMeeting) throws Exception;

	/**
	 * This method will modify the posted calendar at Scheduler tool for this
	 * event/meeting
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	public void modifyCalendar(SignupMeeting meeting) throws Exception;

	/**
	 * This deletes a list of SignupMeeting objects. It should remove all or
	 * none of them in one transaction
	 *
	 * Then sends email cancellation notification if the meeting has not already occurred.
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects, which will be deleted from
	 *            Database storage
	 * @throws Exception 
	 * 			thrown if something goes bad
	 */
	void removeMeetings(List<SignupMeeting> meetings) throws Exception;

	/**
	 * This method will remove a list of the posted Calendar for a set of modified
	 * events/meetings in Scheduler tool
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void removeCalendarEventsOnModifiedMeeting(List<SignupMeeting> meetings) throws Exception;
	
	/**
	 * This method will remove a list of the posted Calendar for the
	 * events/meetings in Scheduler tool
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void removeCalendarEvents(List<SignupMeeting> meetings) throws Exception;
	
	/**
	 * Test to see if the event exists.
	 * 
	 * @param eventId
	 *            a Long Id for event
	 * @return true if the event is existed.
	 */
	boolean isEventExisted(Long eventId);
	
	/**
	 * Send email to attendee when they sign up/cancel their attendance
	 * 
	 * @param eventTrackingInfo
	 *            an EventTrackingInfo object, which contains all the
	 *            information about user action such as signup and cancel
	 * @throws Exception
	 *             thrown if something goes bad
	 */
	void sendEmailToAttendee(SignupEventTrackingInfo eventTrackingInfo) throws Exception;
	
	/**
	 * Get all the Locations from a site
	 * @param siteId
	 * 		String - a site Id
	 * @return a list of Locations in a site
	 * @throws Exception
	 */
	List<String> getAllLocations(String siteId) throws Exception;
	
	/**
	 * Get all the Categories from a site
	 * @param siteId
	 * 		String - a site Id
	 * @return a list of Categories in a site
	 * @throws Exception
	 */
	List<String> getAllCategories(String siteId) throws Exception;
	
	/**
	 * 
	 * @param instant - The Instant that is going to be converted to display to the user, based on the locale
	 * @return a string with the converted instant to display, based on the locale
	 */
	String getUsersLocalDateTimeString(Instant instant);

}
