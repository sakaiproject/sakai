/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.api;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.exception.IdExistsException;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;

/**
 * A service that provides for the administration of enterprise-defined course data.
 * This service is typically not used inside Sakai, and should not be exposed until
 * appropriate permission and reconciliation issues are solved.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface CourseManagementAdministration {

	/**
	 * Creates a new AcademicSession.
	 * 
	 * @param eid
	 * @param title
	 * @param description
	 * @param startDate
	 * @param endDate
	 * @throws IdExistsException
	 */
	public AcademicSession createAcademicSession(String eid, String title, String description,
			Date startDate, Date endDate) throws IdExistsException;

	/**
	 * Updates an existing AcademicSession.
	 * 
	 * @param academicSession The AcademicSession to be updated
	 */
	public void updateAcademicSession(AcademicSession academicSession);

	/**
	 * Removes all meetings from the section
	 *
	 * @param sectionEid
	 */
	public void removeAllSectionMeetings(String sectionEid);

	/**
	 * Removes an academic session and all CourseOfferings associated with this
	 * academic session.
	 * 
	 * @param eid The enterprise id of the academic session
	 */
	public void removeAcademicSession(String eid);
	
	/**
	 * Creates a new CourseSet.
	 * 
	 * @param eid
	 * @param title
	 * @param description
	 * @param category
	 * @param parentCourseSetEid The parent CourseSet's eid, or null if none.
	 * @throws IdExistsException
	 */
	public CourseSet createCourseSet(String eid, String title, String description, String category, String parentCourseSetEid)
		throws IdExistsException;
	
	/**
	 * Updates an existing CourseSet.
	 * 
	 * @param courseSet
	 */
	public void updateCourseSet(CourseSet courseSet);

	/**
	 * Removes a course set and any memberships in the course set.
	 * 
	 * @param eid The enterprise id of the course set
	 */
	public void removeCourseSet(String eid);
	
	/**
	 * Creates a new CanonicalCourse.
	 * 
	 * @param eid
	 * @param title
	 * @param description
	 * @throws IdExistsException
	 */
	public CanonicalCourse createCanonicalCourse(String eid, String title, String description)
		throws IdExistsException;
	
	/**
	 * Updates an existing CanonicalCourse.
	 * 
	 * @param canonicalCourse
	 */
	public void updateCanonicalCourse(CanonicalCourse canonicalCourse);
	
	/**
	 * Adds a CanonicalCourse to a CourseSet.
	 * 
	 * @param courseSetEid
	 * @param canonicalCourseEid
	 * @throws IdNotFoundException
	 */
	public void addCanonicalCourseToCourseSet(String courseSetEid, String canonicalCourseEid)
		throws IdNotFoundException;

	/**
	 * Removes a CanonicalCourse from a CourseSet.
	 * 
	 * @param courseSetEid
	 * @param canonicalCourseEid
	 * @return Whether the CanonicalCourse was a member of the CourseSet and
	 * was successfully removed.
	 */
	public boolean removeCanonicalCourseFromCourseSet(String courseSetEid, String canonicalCourseEid);
	
	/**
	 * Creates an equivalency (cross listing) between CanonicalCourses
	 * 
	 * @param canonicalCourses
	 */
	public void setEquivalentCanonicalCourses(Set canonicalCourses);
	
	/**
	 * Removes a CanonicalCourse from its set of equivalent CanonicalCourses, if it is
	 * a member of such a set.
	 * 
	 * @param canonicalCourse
	 * @return Whether the equivalency existed and was removed.
	 */
	public boolean removeEquivalency(CanonicalCourse canonicalCourse);
	
	/**
	 * Removes a canonical course and any course offerings associated with this
	 * canonical course.
	 * 
	 * @param eid The enterprise id of the canonical course
	 */
	public void removeCanonicalCourse(String eid);

	/**
	 * Creates a new CourseOffering.
	 * 
	 * @param eid
	 * @param title
	 * @param description
	 * @param academicSessionEid
	 * @param canonicalCourseEid
	 * @param startDate
	 * @param endDate
	 * @throws IdExistsException
	 */
	public CourseOffering createCourseOffering(String eid, String title, String description,
			String status, String academicSessionEid, String canonicalCourseEid, Date startDate, Date endDate)
			throws IdExistsException;

	/**
	 * Updates an existing CourseOffering.
	 * 
	 * @param courseOffering
	 */
	public void updateCourseOffering(CourseOffering courseOffering);
	
	/**
	 * Creates an equivalency (cross listing) betweencourseOfferings
	 * 
	 * @param courseOfferings
	 */
	public void setEquivalentCourseOfferings(Set courseOfferings);
	
	/**
	 * Removes a CourseOffering from its set of equivalent CourseOfferings, if it is
	 * a member of such a set.
	 * 
	 * @param courseOffering
	 * @return Whether the equivalency existed and was removed.
	 */
	public boolean removeEquivalency(CourseOffering courseOffering);
	
	/**
	 * Adds a CourseOffering to a CourseSet.
	 * 
	 * @param courseSetEid
	 * @param courseOfferingEid
	 */
	public void addCourseOfferingToCourseSet(String courseSetEid, String courseOfferingEid);

	/**
	 * Removes a CourseOffering from a CourseSet.
	 * 
	 * @param courseSetEid
	 * @param courseOfferingEid
	 * @return Whether the CourseOffering was in the CourseSet and was removed.
	 */
	public boolean removeCourseOfferingFromCourseSet(String courseSetEid, String courseOfferingEid);

	/**
	 * Removes a course offering, any memberships in the course offering, as well
	 * as sections and enrollment sets that belong to this course offering.
	 * 
	 * @param eid The enterprise id of the course offering
	 */
	public void removeCourseOffering(String eid);
	
	/**
	 * Creates a new EnrollmentSet.
	 * 
	 * @param eid
	 * @param title
	 * @param description
	 * @param category
	 * @param defaultEnrollmentCredits
	 * @param courseOfferingEid
	 * @param officialInstructors
	 * @throws IdExistsException
	 */
	public EnrollmentSet createEnrollmentSet(String eid, String title, String description,
			String category, String defaultEnrollmentCredits, String courseOfferingEid, Set officialInstructors)
			throws IdExistsException;
	
	/**
	 * Updates an existing EnrollmentSet.
	 * 
	 * @param enrollmentSet
	 */
	public void updateEnrollmentSet(EnrollmentSet enrollmentSet);
	
	/**
	 * Removes an enrollment set and all associated enrollments.
	 * 
	 * @param eid The enterprise id of the enrollment set
	 */
	public void removeEnrollmentSet(String eid);
	
	/**
	 * Adds an Enrollment to an EnrollmentSet.  If the user is already enrolled in the
	 * EnrollmentSet, the Enrollment record is updated for the user.
	 * 
	 * @param userId
	 * @param enrollmentSetEid
	 * @param enrollmentStatus
	 * @param credits
	 * @param gradingScheme
	 */
	public Enrollment addOrUpdateEnrollment(String userId, String enrollmentSetEid,
			String enrollmentStatus, String credits, String gradingScheme);

	/**
	 * Adds an Enrollment to an EnrollmentSet.  If the user is already enrolled in the
	 * EnrollmentSet, the Enrollment record is updated for the user.
	 * 
	 * @param userId
	 * @param enrollmentSetEid
	 * @param enrollmentStatus
	 * @param credits
	 * @param gradingScheme
	 * @param dropDate
	 */
	public Enrollment addOrUpdateEnrollment(String userId, String enrollmentSetEid,
			String enrollmentStatus, String credits, String gradingScheme, Date dropDate);

	/**
	 * Removes an Enrollment from an EnrollmentSet by setting the Enrollment to
	 * dropped=true.
	 * 
	 * @param userId
	 * @param enrollmentSetEid
	 * @return Whether the enrollment existed and was removed.
	 */
	public boolean removeEnrollment(String userId, String enrollmentSetEid);
	
	/**
	 * Creates a new Section.
	 * 
	 * @param eid
	 * @param title
	 * @param description
	 * @param category
	 * @param parentSectionEid
	 * @param courseOfferingEid
	 * @param enrollmentSetEid
	 * @throws IdExistsException
	 */
	public Section createSection(String eid, String title, String description,
			String category, String parentSectionEid, String courseOfferingEid,
			String enrollmentSetEid) throws IdExistsException;

	
	public SectionCategory addSectionCategory(String categoryCode, String categoryDescription);
	
	/**
	 * Creates a new meeting instance.  The meeting must be associated with a section
	 * and the section must be updated for the meeting to be persisted.
	 * 
	 * @param location The location of the meeting
	 * @param startTime The time that the section starts
	 * @param startTime The time that the section finishes
	 * @param notes Optional notes about this meeting
	 */
	public Meeting newSectionMeeting(String sectionEid, String location, Time startTime, Time finishTime, String notes);
	
	/**
	 * Updates an existing Section.
	 * 
	 * @param section
	 */
	public void updateSection(Section section);
	
	/**
	 * Removes a section and any memberships in the section.  If an enrollment set
	 * is attached to this section, it must be removed via removeEnrollmentSet
	 * before removing the section.
	 * 
	 * @param eid The enterprise id of the section
	 */
	public void removeSection(String eid);
	
	/**
	 * Adds a user to a CourseSet.  If the user is already a member of the CourseSet,
	 * update the user's role.
	 * 
	 * @param userId
	 * @param role
	 * @param courseSetEid
	 * @param status
	 * @throws IdNotFoundException If the CourseSet can not be found
	 */
    public Membership addOrUpdateCourseSetMembership(String userId, String role, String courseSetEid, String status) throws IdNotFoundException;
	
	/**
	 * Removes a user from a CourseSet.
	 * 
	 * @param userId
	 * @param courseSetEid
	 * @return Whether the user was a member of the CourseSet and was removed.
	 */
	public boolean removeCourseSetMembership(String userId, String courseSetEid);

	/**
	 * Adds a user to a CourseOffering.  If the user is already a member of the CourseOffering,
	 * update the user's role.
	 * 
	 * @param userId
	 * @param role
	 * @param courseOfferingEid
	 * @param status
	 */
    public Membership addOrUpdateCourseOfferingMembership(String userId, String role, String courseOfferingEid, String status);
	
	/**
	 * Removes a user from a CourseOffering.
	 * 
	 * @param userId
	 * @param courseOfferingEid
	 * @return Whether the user was a member of the CourseOffering and was
	 * removed.
	 */
	public boolean removeCourseOfferingMembership(String userId, String courseOfferingEid);
	
	/**
	 * Adds a user to a Section.  If the user is already a member of the Section,
	 * update the user's role.
	 * 
	 * @param userId
	 * @param role
	 * @param sectionEid
	 * @param status
	 */
    public Membership addOrUpdateSectionMembership(String userId, String role, String sectionEid, String status);
	
	/**
	 * Removes a user from a Section.
	 * 
	 * @param userId
	 * @param sectionEid
	 * @return Whether the user was a member of the Section and was removed.
	 */
	public boolean removeSectionMembership(String userId, String sectionEid);
	
	/**
	 * Determines which sessions will be returned by getCurrentAcademicSessions().
	 * 
	 * @param academicSessionEids
	 */
	public void setCurrentAcademicSessions(List<String> academicSessionEids);
}
