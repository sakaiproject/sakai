/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.section.support;

import java.sql.Time;
import java.util.List;
import java.util.Set;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;

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
	 */public CourseSection createSection(String courseUuid, String title, String category, Integer maxEnrollments,
			String location, Time startTime, Time endTime, boolean monday, boolean tuesday,
			boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday);
	
	/**
	 * Creates a new user.
	 * 
	 * @param userUuid
	 * @param displayName
	 * @param sortName
	 * @param displayId
	 * @return
	 */
	public User createUser(String userUuid, String displayName, String sortName, String displayId);
	
	/**
	 * Finds a user by their uuid.
	 * 
	 * @param userUuid
	 * @return
	 */
	public User findUser(String userUuid);

	/**
	 * Gets all of the site-scoped ParticipationRecords for a user.  This can be
	 * a mix of enrollments and instructor or TA records, since a user can play
	 * different roles in different sites.
	 * 
	 * @param userUuid
	 * @return
	 */public List getAllSiteMemberships(String userUuid);
	
	/**
	 * Gets all of the section-scoped ParticipationRecords for a user in a
	 * given site.  The returned Set should contain either enrollment, instructor,
	 * or TA records, since a user can not play different roles in the sections
	 * belonging to a single site.
	 * 
	 * @param userUuid
	 * @param siteContext
	 * @return
	 */
	 public Set getAllSectionMemberships(String userUuid, String siteContext);

	/**
	 * Adds a user to a site (or "Course").
	 * 
	 * @param userUuid
	 * @param siteContext
	 * @param role
	 * @return
	 */
	public ParticipationRecord addSiteMembership(String userUuid, String siteContext, Role role);
	
	/**
	 * Removes a user from a site (or "Course").
	 * 
	 * @param userUuid
	 * @param siteContext
	 */
	public void removeSiteMembership(String userUuid, String siteContext);

	/**
	 * Adds a user to a section under the given role.
	 * 
	 * @param userUuid
	 * @param sectionUuid
	 * @param role
	 * @return
	 */
	public ParticipationRecord addSectionMembership(String userUuid, String sectionUuid, Role role);

	/**
	 * Removes a user from membership in a section.
	 * 
	 * @param userUuid
	 * @param sectionUuid
	 */
	public void removeSectionMembership(String userUuid, String sectionUuid);

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
