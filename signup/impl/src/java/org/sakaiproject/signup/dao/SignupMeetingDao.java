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

package org.sakaiproject.signup.dao;

import java.util.Date;
import java.util.List;

import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.springframework.dao.DataAccessException;

/**
 * <p>
 * SignupMeetingDao is an interface, which provides methods to access the
 * database storage for retrieving, creating, updating and removing
 * SignupMeeting objects.
 * 
 * @author Peter Liu
 * 
 * </p>
 */
public interface SignupMeetingDao extends GeneralGenericDao {

	/**
	 * This returns a list of SignupMeeting for the site
	 * 
	 * @param siteId -
	 *            a unique id which represents the current site
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getAllSignupMeetings(String siteId);

	/**
	 * This returns a subset list of SignupMeeting from Now to searchEndDate for
	 * the site
	 * 
	 * @param currentSiteId
	 *            a unique id which represents the current site
	 * @param searchEndDate
	 *            date,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetings(String currentSiteId, Date searchEndDate);

	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the site
	 * 
	 * @param siteId
	 *            a unique id which represents the current site
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param endDate
	 *            date,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetings(String siteId, Date startDate, Date endDate);
	
	/**
	 * This returns a subset list of SignupMeetings from startDate to endDate for
	 * the defined site
	 * 
	 * @param siteId
	 *            a unique id which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param endDate
	 *            date,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetingsInSite(String siteId, Date startDate, Date endDate);

	/**
	 * This returns a subset list of SignupMeetings from startDate to endDate for
	 * the defined sites
	 * 
	 * @param siteIds
	 *            a collection of unique ids which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param endDate
	 *            date,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getSignupMeetingsInSites(List<String> siteIds, Date startDate, Date endDate);

	/**
	 * This returns a subset list of SignupMeetings with the same recurrenceId
	 * from a starting Date for the site
	 * 
	 * @param currentSiteId
	 *            a unique id which represents the current site
	 * @param recurrenceId
	 *            recurrenceId,which constraints the recurring meetings.
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getRecurringSignupMeetings(String currentSiteId, Long recurrenceId, Date startDate);

	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the sites, which have auto-reminder setting
	 * 
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param endDate
	 *            date,which constraints the search ending date.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getAutoReminderSignupMeetings(Date startDate, Date endDate);


	/**
	 * This saves meeting object into database
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object
	 * @return a unique Id for the SignupMeeting object from DB
	 */
	Long saveMeeting(SignupMeeting signupMeeting);

	/**
	 * This saves a list of meeting object into database
	 * 
	 * @param signupMeetings
	 * @param userId
	 */
	void saveMeetings(List<SignupMeeting> signupMeetings);

	/**
	 * This retrieve a SignupMeeting object from database according to the
	 * SignupMeeting Id
	 * 
	 * @param meetingId
	 *            a unique Id for SignupMeeting object
	 * @return a SignupMeeting object
	 */
	SignupMeeting loadSignupMeeting(Long meetingId);

	/**
	 * This updates the SignupMeeting object in the DB
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 * @throws DataAccessException
	 *             thrown if the data is not accessible
	 */
	void updateMeeting(SignupMeeting meeting) throws DataAccessException;

	/**
	 * This updates a list of SignupMeeting objects in the DB
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects
	 * @throws DataAccessException
	 *             thrown if the data is not accessible
	 */
	void updateMeetings(List<SignupMeeting> meetings) throws DataAccessException;
	
	/**
	 * This updates a list of SignupMeeting objects in the DB
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects
	 * @param removedTimeslots
	 *            a list of SignupTimeslot objects, which will be removed from the meeting
	 * @throws DataAccessException
	 *             thrown if the data is not accessible
	 */
	void updateModifiedMeetings(List<SignupMeeting> meetings,List<SignupTimeslot> removedTimeslots) throws DataAccessException;

	/**
	 * This deletes a list of SignupMeeting objects. It should remove all or
	 * none of them in one transaction
	 * 
	 * @param meetings
	 *            a list of SignupMeeting objects, which need to be removed
	 */
	void removeMeetings(List<SignupMeeting> meetings);

	/**
	 * Test to see if the event exists.
	 * 
	 * @param evnetId
	 *            a Long Id for event
	 * @return true if the event is existed.
	 */
	boolean isEventExisted(Long evnetId);
	
	/**
	 * Get total Events record-Counts for auto-reminder process
	 * @param startDate
	 * 			search starting date
	 * @param endDate
	 * 			search ending date
	 * @return the total record counts
	 */
	int getAutoReminderTotalEventCounts(Date startDate, Date endDate);
	
	/**
	 * Get all the Categories from a site
	 * @param siteId
	 * 		String - a site Id
	 * @return a list of Categories in a site
	 * @throws DataAccessException
	 */
	List<String> getAllCategories(String siteId) throws DataAccessException;
	
	/**
	 * Get all the Locations from a site
	 * @param siteId
	 * 		String - a site Id
	 * @return a list of Locations in a site
	 * @throws DataAccessException
	 */
	List<String> getAllLocations(String siteId) throws DataAccessException;

}
