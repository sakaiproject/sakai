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
package org.sakaiproject.signup.api.repository;

import java.util.Date;
import java.util.List;

import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.springframework.data.SpringCrudRepository;

/**
 * Repository interface for SignupMeeting entity using Spring Data pattern.
 * Provides methods to access the database for retrieving, creating, updating
 * and removing SignupMeeting objects.
 *
 * @author Peter Liu
 */
public interface SignupMeetingRepository extends SpringCrudRepository<SignupMeeting, Long> {

	/**
	 * Returns all SignupMeetings for the site
	 *
	 * @param siteId a unique id which represents the current site
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> findAllBySiteId(String siteId);

	/**
	 * Returns a subset list of SignupMeetings from Now to searchEndDate for the site
	 *
	 * @param siteId a unique id which represents the current site
	 * @param searchEndDate date which constraints the search ending date
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> findBySiteIdAndStartTimeBefore(String siteId, Date searchEndDate);

	/**
	 * Returns a subset list of SignupMeetings from startDate to endDate for the site
	 *
	 * @param siteId a unique id which represents the current site
	 * @param startDate date which constraints the search starting date
	 * @param endDate date which constraints the search ending date
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> findBySiteIdAndDateRange(String siteId, Date startDate, Date endDate);

	/**
	 * Returns a subset list of SignupMeetings from startDate to endDate for the defined site
	 *
	 * @param siteId a unique id which represents the site
	 * @param startDate date which constraints the search starting date
	 * @param endDate date which constraints the search ending date
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> findInSiteByDateRange(String siteId, Date startDate, Date endDate);

	/**
	 * Returns a subset list of SignupMeetings from startDate to endDate for the defined sites
	 *
	 * @param siteIds a collection of unique ids which represents multiple sites
	 * @param startDate date which constraints the search starting date
	 * @param endDate date which constraints the search ending date
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> findInSitesByDateRange(List<String> siteIds, Date startDate, Date endDate);

	/**
	 * Returns a subset list of SignupMeetings with the same recurrenceId from a starting Date
	 *
	 * @param siteId a unique id which represents the current site
	 * @param recurrenceId recurrenceId which constraints the recurring meetings
	 * @param startDate date which constraints the search starting date
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> findRecurringMeetings(String siteId, Long recurrenceId, Date startDate);

	/**
	 * Returns a subset list of SignupMeetings from startDate to endDate for sites
	 * which have auto-reminder setting
	 *
	 * @param startDate date which constraints the search starting date
	 * @param endDate date which constraints the search ending date
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> findAutoReminderMeetings(Date startDate, Date endDate);

	/**
	 * Updates a list of SignupMeeting objects in the DB
	 *
	 * @param meetings a list of SignupMeeting objects
	 */
	void updateAll(List<SignupMeeting> meetings);

	/**
	 * Updates a list of SignupMeeting objects and removes specified timeslots
	 *
	 * @param meetings a list of SignupMeeting objects
	 * @param removedTimeslots a list of SignupTimeslot objects to be removed
	 */
	void updateMeetingsAndRemoveTimeslots(List<SignupMeeting> meetings, List<SignupTimeslot> removedTimeslots);

	/**
	 * Gets total Events record-Counts for auto-reminder process
	 *
	 * @param startDate search starting date
	 * @param endDate search ending date
	 * @return the total record counts
	 */
	int countAutoReminderMeetings(Date startDate, Date endDate);

	/**
	 * Gets all the Categories from a site
	 *
	 * @param siteId a site Id
	 * @return a list of Categories in a site
	 */
	List<String> findAllCategoriesBySiteId(String siteId);

	/**
	 * Gets all the Locations from a site
	 *
	 * @param siteId a site Id
	 * @return a list of Locations in a site
	 */
	List<String> findAllLocationsBySiteId(String siteId);

	/**
	 * Returns IDs of SignupMeetings from startDate to endDate for the defined site
	 * This method uses query cache to return only IDs for improved caching performance
	 *
	 * @param siteId a unique id which represents the site
	 * @param startDate date which constraints the search starting date
	 * @param endDate date which constraints the search ending date
	 * @return a list of SignupMeeting IDs
	 */
	List<Long> findIdsBySiteIdAndDateRange(String siteId, Date startDate, Date endDate);

	/**
	 * Returns IDs of SignupMeetings from startDate to endDate for the defined sites
	 * This method uses query cache to return only IDs for improved caching performance
	 *
	 * @param siteIds a collection of unique ids which represents multiple sites
	 * @param startDate date which constraints the search starting date
	 * @param endDate date which constraints the search ending date
	 * @return a list of SignupMeeting IDs
	 */
	List<Long> findIdsBySiteIdsAndDateRange(List<String> siteIds, Date startDate, Date endDate);
}
