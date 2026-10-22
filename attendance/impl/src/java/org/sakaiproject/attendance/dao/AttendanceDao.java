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

package org.sakaiproject.attendance.dao;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceItemStats;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.AttendanceUserStats;
import org.sakaiproject.attendance.model.GradingRule;

/**
 * DAO interface for the Attendance Tool
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public interface AttendanceDao {
	/**
	 * Get an AttendanceSite
	 *
	 * @param siteId, the Sakai SiteID
	 * @return the AttendanceSite
     */
	AttendanceSite getAttendanceSite(String siteId);

	/**
	 * Return attendanceSite by ID
	 * @param id, the attendance Site ID
	 * @return {@link AttendanceSite}
	 */
	AttendanceSite getAttendanceSite(Long id);

	/**
	 * Add a new AttendanceSite record to the database
	 *
	 * @param as, the AttendanceSite to add
	 * @return success of the operation
     */
	boolean addAttendanceSite(AttendanceSite as);

	/**
	 * Updates an attendanceSite
	 *
	 * @param aS, the AttendanceSite with updated values
	 * @return success of the operation
     */
	boolean updateAttendanceSite(AttendanceSite aS);

	/**
	 * Gets a single AttendanceEvent from the db
	 * 
	 * @return the AttendanceEvent, may be null
	 */
	AttendanceEvent getAttendanceEvent(long id);

	/**
	 * get all the events by attendanceSite
	 *
	 * @param aS, the AttendanceSite
	 * @return a list of events or empty if no items.
     */
	List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS);

	/**
	 * Serializable function used to save an AttendanceEvent as part of the "Take Attendance Now" feature
	 *
	 * @param e, the AttendanceEvent
	 * @return the Long ID of the newly added AttendanceEvent
     */
	Serializable addAttendanceEventNow(AttendanceEvent e);

	/**
	 * Update an AttendanceEvent
	 *
	 * @param aE, the AttendanceEvent
	 * @return success of the operation
     */
	boolean updateAttendanceEvent(AttendanceEvent aE);

	/**
	 * Deletes an AttendanceEvent
	 *
	 * @param aE, the AttendanceEvent
	 * @return success of the operation
     */
	boolean deleteAttendanceEvent(AttendanceEvent aE);

	/**
	 * Get Status Record by ID
	 *
	 * @param id, the id of the status record
	 * @return the status record (may be null if DB operation fails)
     */
	AttendanceRecord getStatusRecord(long id);

	/**
	 * Updates an AttendanceRecord
	 *
	 * @param aR, the AttendanceRecord to update with new values
	 * @return success of the operation
     */
	boolean updateAttendanceRecord(AttendanceRecord aR);

	/**
	 * Update a set of AttendanceStatuses
	 *
	 * @param attendanceStatusList, a List of AttendanceStatuses
	 * @return success of the operation
     */
	void updateAttendanceStatuses(List<AttendanceStatus> attendanceStatusList);

	/**
	 * Get a list of the active statuses in an Attendance Site
	 *
	 * @param attendanceSite, the AttendanceSite
	 * @return List of Active AttendanceStatuses, null if DB issues
     */
	List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get a lit of all of the attendance statuses for a site
	 *
	 * @param attendanceSite, the AttendanceSite
	 * @return list of all AttendanceStatuses, null if DB issues
     */
	List<AttendanceStatus> getAllStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get an attendance status record by its id
	 *
	 * @param id, the ID of the AttendanceStatus
	 * @return the AttendanceStatus (null if DB issues)
     */
	AttendanceStatus getAttendanceStatusById(Long id);

	/**
	 * Get AttendanceGrade by ID
	 *
	 * @param id, the AttendanceGrade ID
	 * @return the AttendanceGrade (null if DB issues)
     */
	AttendanceGrade getAttendanceGrade(Long id);

	/**
	 * Get an AttendanceGrade
	 *
	 * @param userID, the userID of owner of Grade
	 * @param aS, the site the Grade is present in
     * @return the AttendanceGrade, null if DB issues
     */
	AttendanceGrade getAttendanceGrade(String userID, AttendanceSite aS);

	/**
	 * Get a list of AttendanceGrades
	 *
	 * @param aS, the AttendanceSite to get the AGs for.
	 * @return List of AttendanceGrades, null if DB issues
     */
	List<AttendanceGrade> getAttendanceGrades(AttendanceSite aS);
	
	/**
	 * Updates an AttendanceGrade
	 *
	 * @param aG, the AG to update
	 * @return success of operation
     */
	boolean updateAttendanceGrade(AttendanceGrade aG);

	/**
	 * Get the AttendanceUserStats for a User and Site
	 * @param userId
	 * @param aS
	 * @return
	 */
	AttendanceUserStats getAttendanceUserStats(String userId, AttendanceSite aS);

	/**
	 * Return the AttendanceUserStats for a Site
	 * @param aS, the AttendanceSite to get UserStats for
	 * @return a List of {@link AttendanceUserStats}
	 */
	List<AttendanceUserStats> getAttendanceUserStatsForSite(AttendanceSite aS);

	/**
	 * Update the AttendanceUserStats
	 * @param aUS, the AttendanceUserStats to update
	 * @return success or failure of the operation
	 */
	boolean updateAttendanceUserStats(AttendanceUserStats aUS);

	/**
	 * Get the AttendanceItemStats for an AttendanceEvent
	 * @param aE, the AttendanceEvent to get the stats for
	 * @return the AttendanceItemStats, null if DB issue
	 */
	AttendanceItemStats getAttendanceItemStats(AttendanceEvent aE);

	/**
	 * Update the AttendanceItemStats
	 * @param aIS, the AttendanceItemStats to update
	 * @return success or failure of the operation
	 */
	boolean updateAttendanceItemStats(AttendanceItemStats aIS);

	/**
	 * Return a batch of AttendanceSites
	 * @return a List of AttendanceSite IDs (max 5)
	 */
	List<Long> getAttendanceSiteBatch(Instant syncTime, Long lastId);

	/**
	 * Return a list of all AttendanceSites In Sync
	 * @return List of IDs
	 */
	List<Long> getAttendanceSitesInSync();

	/**
	 * Mark a set of AttendanceSites as currently syncing
	 * @param ids, a list of IDs to mark
	 * @return boolean value of success
	 */
	boolean markAttendanceSiteForSync(List<Long> ids, Instant syncTime);

	/**
	 * Returns a list containing all of the grading rules for a given AttendanceSite.
	 *
	 * @param attendanceSite, An existing attendance site.
	 * @return A list of all of the GradingRules for the provided attendance site.
	 */
	List<GradingRule> getGradingRulesForSite(AttendanceSite attendanceSite);

	/**
	 * Add a new GradingRule.
	 *
	 * @param gradingRule, The GradingRule to be added.
	 * @return The success of the save operation.
	 */
	boolean addGradingRule(GradingRule gradingRule);

	/**
	 * Delete an existing GradingRule.
	 *
	 * @param gradingRule, The GradingRule to be deleted.
	 * @return The success of the delete operation.
	 */
	boolean deleteGradingRule(GradingRule gradingRule);



	// Hibernate Object Fields
	String ID = "id";
	String IDS = "ids";
	String USER_ID = "userID";
	String SITE_ID = "siteID";
	String ATTENDANCE_SITE = "attendanceSite";
	String ATTENDANCE_EVENT = "attendanceEvent";
	String SYNC_TIME = "syncTime";
}
