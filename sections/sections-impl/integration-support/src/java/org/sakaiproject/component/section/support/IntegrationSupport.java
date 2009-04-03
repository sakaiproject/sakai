/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.component.section.support;

import java.sql.Time;
import java.util.List;
import java.util.Set;

import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;

/**
 * Provides methods for manipulating users, courses, sections, and memberships.
 * Implementations do not provide any authorization, so they are to be used for
 * integration testing only.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface IntegrationSupport {
	/**
	 * Creates a new course.
	 * 
	 * @param siteContext
	 * @param title
	 * @param externallyManaged
	 * @param selfRegistrationAllowed
	 * @param selfSwitchingAllowed
	 * @return
	 */
	public Course createCourse(String siteContext, String title, boolean externallyManaged,
			boolean selfRegistrationAllowed, boolean selfSwitchingAllowed);
	
	/**
	 * Creates a new section.
	 * 
	 * @param courseUuid
	 * @param title
	 * @param category
	 * @param maxEnrollments
	 * @param location
	 * @param startTime
	 * @param endTime
	 * @param monday
	 * @param tuesday
	 * @param wednesday
	 * @param thursday
	 * @param friday
	 * @param saturday
	 * @param sunday
	 * @return
	 */
	public CourseSection createSection(String courseUuid, String title, String category, Integer maxEnrollments,
			String location, Time startTime, Time endTime, boolean monday, boolean tuesday,
			boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday);
	
	/**
	 * Creates a new user.
	 * 
	 * @param userUid
	 * @param displayName
	 * @param sortName
	 * @param displayId
	 * @return
	 */
	public User createUser(String userUid, String displayName, String sortName, String displayId);
	
	/**
	 * Finds a user by their uuid.
	 * 
	 * @param userUid
	 * @return
	 */
	public User findUser(String userUid);

	/**
	 * Gets all of the site-scoped ParticipationRecords for a user.  This can be
	 * a mix of enrollments and instructor or TA records, since a user can play
	 * different roles in different sites.
	 * 
	 * @param userUid
	 * @return
	 */
	public List getAllSiteMemberships(String userUid);
	
	/**
	 * Gets all of the section-scoped ParticipationRecords for a user in a
	 * given site.  The returned Set should contain either enrollment, instructor,
	 * or TA records, since a user can not play different roles in the sections
	 * belonging to a single site.
	 * 
	 * @param userUid
	 * @param siteContext
	 * @return
	 */
	 public Set getAllSectionMemberships(String userUid, String siteContext);

	/**
	 * Adds a user to a site (or "Course").
	 * 
	 * @param userUid
	 * @param siteContext
	 * @param role
	 * @return
	 */
	public ParticipationRecord addSiteMembership(String userUid, String siteContext, Role role);
	
	/**
	 * Removes a user from a site (or "Course").
	 * 
	 * @param userUid
	 * @param siteContext
	 */
	public void removeSiteMembership(String userUid, String siteContext);

	/**
	 * Adds a user to a section under the given role.
	 * 
	 * @param userUid
	 * @param sectionUuid
	 * @param role
	 * @return
	 */
	public ParticipationRecord addSectionMembership(String userUid, String sectionUuid, Role role);

	/**
	 * Removes a user from membership in a section.
	 * 
	 * @param userUid
	 * @param sectionUuid
	 */
	public void removeSectionMembership(String userUid, String sectionUuid);

}
