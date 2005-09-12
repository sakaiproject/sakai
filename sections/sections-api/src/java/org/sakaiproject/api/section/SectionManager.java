/**********************************************************************************
*
* $Id: $
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
package org.sakaiproject.api.section;

import java.sql.Time;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.SectionEnrollments;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.exception.MembershipException;
import org.sakaiproject.api.section.facade.Role;

public interface SectionManager {

	/**
	 * Gets the course (whatever that means) associated with this site context.
	 * 
	 * @param siteContext The site context
	 * @return The course (whatever that means)
	 */
	public Course getCourse(String siteContext);
	
    /**
     * Gets the sections associated with this site context.
     * 
	 * @param siteContext The site context
	 * 
     * @return The List of
     * {@link org.sakaiproject.api.section.coursemanagement.CourseSection CourseSections}
     * associated with this site context.
     */
    public List getSections(String siteContext);
	
    /**
     * Lists the sections in this context that are a member of the given category.
     * 
	 * @param siteContext The site context
     * @param categoryId
     * 
     * @return A List of {@link org.sakaiproject.api.section.coursemanagement.CourseSection CourseSections}
     */
    public List getSectionsInCategory(String siteContext, String categoryId);

    /**
     * Gets a {@link org.sakaiproject.api.section.coursemanagement.CourseSection CourseSection}
     * by its uuid.
     * 
     * @param sectionUuid The uuid of a section
     * 
     * @return A section
     */
    public CourseSection getSection(String sectionUuid);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all instructors in the current site.
     * 
     * @param siteContext The current site context
     * @return The instructors
     */
    public List getSiteInstructors(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all TAs in the current site.
     * 
     * @param siteContext The current site context
     * @return The TAs
     */
    public List getSiteTeachingAssistants(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all TAs in a section.
     * 
     * @param sectionUuid The section uuid
     * @return The TAs
     */
    public List getSectionTeachingAssistants(String sectionUuid);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to the current site.
     * 
     * @param siteContext The current site context
     * @return The enrollments
     */
    public List getSiteEnrollments(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to a section.
     * 
     * @param sectionUuid The section uuid
     * @return The enrollments
     */
    public List getSectionEnrollments(String sectionUuid);

    /**
     * Finds a list of {@link org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to the current site and whose sort name, display name,
     * or display id start with the given string pattern.
     * 
     * @param siteContext The current site context
     * @param pattern The pattern to match students names or ids
     * 
     * @return The enrollments
     */
    public List findSiteEnrollments(String siteContext, String pattern);

    /**
	 * Gets a SectionEnrollments data structure for the given students.
	 * 
	 * @param siteContext The site context
	 * @param studentUuids The Set of userUuids to include in the SectionEnrollments
	 * 
	 * @return
	 */
	public SectionEnrollments getSectionEnrollmentsForStudents(String siteContext, Set studentUuids);
	
    /**
     * Adds the current user to a section as a student.  This is a convenience
     * method for addSectionMembership(currentUserId, Role.STUDENT, sectionId).
     * @param sectionUuid
     */
    public EnrollmentRecord joinSection(String sectionUuid);
        
    /**
     * Switches a student's currently assigned section.  If the student is enrolled
     * in another section of the same type, that enrollment will be dropped.
     * 
     * This is a convenience method to allow a drop/add (a switch) in a single transaction.
     * 
     * @param newSectionUuid The new section uuid to which the student should be assigned
     */
    public void switchSection(String newSectionUuid);
    
    /**
     * Returns the total number of students enrolled in a learning context.  Useful for
     * comparing to the max number of enrollments allowed in a section.
     * 
     * @param sectionUuid
     * @return
     */
    public int getTotalEnrollments(String learningContextUuid);
    
    /**
     * Adds a user to a section under the specified role.  If a student is added
     * to a section, s/he will be automatically removed from any other section
     * of the same category in this site.  So adding 'student1' to 'Lab1', for
     * example, will automatically remove 'student1' from 'Lab2'.  TAs may be
     * added to multiple sections in a site regardless of category.
     * 
     * @param userUuid
     * @param role
     * @param sectionUuid
     * @throws MembershipException Only students and TAs can be members of a
     * section.  Instructor roles are assigned only at the course level.
     */
    public ParticipationRecord addSectionMembership(String userUuid, Role role, String sectionUuid)
        throws MembershipException;
    
    /**
     * Defines the complete set of users that make up the members of a section in
     * a given role.  This is useful when doing bulk modifications of section
     * membership.
     * 
     * @param userUuids The set of userUuids as strings
     * @param sectionId The sectionId
     */
    public void setSectionMemberships(Set userUuids, Role role, String sectionId);
    
    /**
     * Removes a user from a section.
     * 
     * @param userUuid
     * @param sectionUuid
     */
    public void dropSectionMembership(String userUuid, String sectionUuid);
    
	public void dropEnrollmentFromCategory(String studentUuid, String siteContext, String category);

    /**
     * Adds a CourseSection to a parent CourseSection.  This assumes that meeting times
     * will not be handled by an external service.  The added functionality of
     * linking course sections to repeating events (meet every 2nd Tuesday of the
     * month at 3pm) is currently out of scope, so meetingTimes is represented
     * as a simple string.
     * 
     * @param courseUuid
     * @param title
     * @param category
     * @param maxEnrollments
     * @param location
     * @param startTime
     * @param startTimeAm
     * @param endTime
     * @param endTimeAm
     * @param monday
     * @param tuesday
     * @param wednesday
     * @param thursday
     * @param friday
     * @param saturday
     * @param sunday
     * @return
     */
    public CourseSection addSection(String courseUuid, String title,
    		String category, Integer maxEnrollments, String location, 
    		Time startTime, Time endTime,
    		boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
    		boolean friday, boolean saturday, boolean sunday);
	
    /**
     * Updates the persistent representation of the given CourseSection.  Once
     * a section is created, its category is immutable.
     * 
     * @param sectionUuid
     * @param title
     * @param maxEnrollments
     * @param location
     * @param startTime
     * @param startTimeAm
     * @param endTime
     * @param endTimeAm
     * @param monday
     * @param tuesday
     * @param wednesday
     * @param thursday
     * @param friday
     * @param saturday
     * @param sunday
     */
    public void updateSection(String sectionUuid, String title, Integer maxEnrollments,
    		String location, Time startTime, Time endTime,
    		boolean monday, boolean tuesday, boolean wednesday,
    		boolean thursday, boolean friday, boolean saturday, boolean sunday);
    
    /**
     * Disbands a course section.  This does not affect enrollment records for
     * the course.
     * 
     * @param sectionUuid
     */
    public void disbandSection(String sectionUuid);


    /**
     * Determines whether students can enroll themselves in a section.
     * 
     * @param courseUuid
     * @return
     */
    public boolean isSelfRegistrationAllowed(String courseUuid);
    
    /**
     * Sets the "self registration" status of a section.
     * 
     * @param courseUuid
     * @param allowed
     */
    public void setSelfRegistrationAllowed(String courseUuid, boolean allowed);
    
    /**
     * Determines whether students can switch sections once they are enrolled in
     * a section of a given category (for instance, swapping one lab for another).
     * 
     * @param courseUuid
     * @return
     */
    public boolean isSelfSwitchingAllowed(String courseUuid);
    
    /**
     * Sets the "student switching" status of a primary section.
     * 
     * @param courseId
     * @param allowed
     */
    public void setSelfSwitchingAllowed(String courseUuid, boolean allowed);
    
    /**
     * The Section Manager tool could use more specific queries on membership,
     * such as this:  getting all students in a primary section that are not
     * enrolled in any secondary sections of a given type.  For instance, 'Who
     * are the students who are not enrolled in any lab?'
     * 
     * @return A List of {@link
     * org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecords} of students who are enrolled in the course but are
     * not enrolled in a section of the given section category.
     */
    public List getUnsectionedEnrollments(String courseUuid, String category);

    /**
     * Gets all of the section enrollments for a user in a course.  Useful for
     * listing all of the sections in which a student is enrolled.
     * 
     * @param userUuid
     * @param courseUuid
     * @return A Set of EnrollmentRecords
     */
    public Set getSectionEnrollments(String userUuid, String courseUuid);


    /**
     * Gets the localized name of a given category.
     * 
     * @param categoryId A string identifying the category
     * @param locale The locale of the client
     * 
     * @return An internationalized string to display for this category.
     * 
     */
    public String getCategoryName(String categoryId, Locale locale);
   
    /**
     * Gets the list of section categories.  In sakai 2.1, there will be only a
     * single set of categories.  They will not be configurable on a per-course
     * or per-context bases.
     * 
     * @return A List of unique Strings that identify the available section
     * categories.  These should be internationalized for display using
     * {@link SectionAwareness#getCategoryName(String, Locale) getCategoryName}.
     */
    public List getSectionCategories();

	/**
	 * Gets a single enrollment record for a student in a site.
	 * 
	 * @param siteContext
	 * @param studentUuid
	 * @return The enrollment record linking this student to this site/course
	 */
    public User getSiteEnrollment(String siteContext, String studentUuid);

}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
