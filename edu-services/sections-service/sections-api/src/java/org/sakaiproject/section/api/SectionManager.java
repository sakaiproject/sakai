/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.section.api;

import java.sql.Time;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;

import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.SectionEnrollments;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.MembershipException;
import org.sakaiproject.section.api.exception.SectionFullException;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.facade.Role;

/**
 * An internal service interface for the Section Manager Tool (AKA "Section Info")
 * to provide for the creation, modification, and removal of CourseSections, along
 * with the membership of of these sections.
 *
 * This service is not to be used outside of the Section Manager Tool.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
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
     * {@link org.sakaiproject.section.api.coursemanagement.CourseSection CourseSections}
     * associated with this site context.
     */
    public List<CourseSection> getSections(String siteContext);

    /**
     * Lists the sections in this context that are a member of the given category.
     *
	 * @param siteContext The site context
     * @param categoryId
     *
     * @return A List of {@link org.sakaiproject.section.api.coursemanagement.CourseSection CourseSections}
     */
    public List<CourseSection> getSectionsInCategory(String siteContext, String categoryId);

    /**
     * Gets a {@link org.sakaiproject.section.api.coursemanagement.CourseSection CourseSection}
     * by its uuid.
     *
     * @param sectionUuid The uuid of a section
     *
     * @return A section
     */
    public CourseSection getSection(String sectionUuid);

    /**
     * Gets a list of {@link org.sakaiproject.section.api.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all instructors in the current site.
     *
     * @param siteContext The current site context
     * @return The instructors
     */
    public List<ParticipationRecord> getSiteInstructors(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.section.api.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all TAs in the current site.
     *
     * @param siteContext The current site context
     * @return The TAs
     */
    public List<ParticipationRecord> getSiteTeachingAssistants(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.section.api.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all TAs in a section.
     *
     * @param sectionUuid The section uuid
     * @return The TAs
     */
    public List<ParticipationRecord> getSectionTeachingAssistants(String sectionUuid);

    /**
     * Gets a list of {@link org.sakaiproject.section.api.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to the current site.
     *
     * @param siteContext The current site context
     * @return The enrollments
     */
    public List<EnrollmentRecord> getSiteEnrollments(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.section.api.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to a section.
     *
     * @param sectionUuid The section uuid
     * @return The enrollments
     */
    public List<EnrollmentRecord> getSectionEnrollments(String sectionUuid);

    /**
     * Finds a list of {@link org.sakaiproject.section.api.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to the current site and whose sort name, display name,
     * or display id start with the given string pattern.
     *
     * @param siteContext The current site context
     * @param pattern The pattern to match students names or ids
     *
     * @return The enrollments
     */
    public List<EnrollmentRecord> findSiteEnrollments(String siteContext, String pattern);

    /**
	 * Gets a SectionEnrollments data structure for the given students.
	 *
	 * @param siteContext The site context
	 * @param studentUids The Set of userUids to include in the SectionEnrollments
	 *
	 * @return
	 */
	public SectionEnrollments getSectionEnrollmentsForStudents(String siteContext, Set studentUids);

    /**
     * Adds the current user to a section as a student.  This is a convenience
     * method for addSectionMembership(currentUserId, Role.STUDENT, sectionId).
     * @param sectionUuid
     * @throws RoleConfigurationException If there is no valid student role, or
     * if there is more than one group-scoped role flagged as a student role.
     */
    public EnrollmentRecord joinSection(String sectionUuid) throws RoleConfigurationException;

    /**
     * Adds the current user to a section as a student, enforcing a maximum permitted size.
     * @param sectionUuid
     * @param maxSize
     * @throws RoleConfigurationException If there is no valid student role, or
     * if there is more than one group-scoped role flagged as a student role.
     * @throws SectionFullException If adding the user would exceed the size limit
     */
    public EnrollmentRecord joinSection(String sectionUuid, int maxSize) throws RoleConfigurationException, SectionFullException;

    /**
     * Switches a student's currently assigned section.  If the student is enrolled
     * in another section of the same type, that enrollment will be dropped.
     *
     * This is a convenience method to allow a drop/add (a switch) in a single transaction.
     *
     * @param newSectionUuid The new section uuid to which the student should be assigned
     * @throws RoleConfigurationException If there is no valid student role, or
     * if there is more than one group-scoped role flagged as a student role.
     */
    public void switchSection(String newSectionUuid) throws RoleConfigurationException;

    /**
     * Switches a student's currently assigned section, enforcing a maximum permitted size.
     * If the student is enrolled in another section of the same type, that enrollment
     * will be dropped.
     *
     * This is a convenience method to allow a drop/add (a switch) in a single transaction.
     *
     * @param newSectionUuid The new section uuid to which the student should be assigned
     * @throws RoleConfigurationException If there is no valid student role, or
     * if there is more than one group-scoped role flagged as a student role.
     * @throws SectionFullException If adding the user would exceed the size limit
     */
    public void switchSection(String newSectionUuid, int maxSize) throws RoleConfigurationException, SectionFullException;

    /**
     * Returns the total number of students enrolled in a learning context.  Useful for
     * comparing to the max number of enrollments allowed in a section.
     *
     * @param sectionUuid
     * @return
     */
    public int getTotalEnrollments(String learningContextUuid);

    /**
     * Returns the total number of students enrolled in a learning context by section role.
     * Useful for comparing to the max number of enrollments allowed in a section.
     *
     * @param sectionUuid
     * @return
     */
    public Map getTotalEnrollmentsMap(String learningContextUuid);

    /**
     * Adds a user to a section under the specified role.  If a student is added
     * to a section, s/he will be automatically removed from any other section
     * of the same category in this site.  So adding 'student1' to 'Lab1', for
     * example, will automatically remove 'student1' from 'Lab2'.  TAs may be
     * added to multiple sections in a site regardless of category.
     *
     * @param userUid
     * @param role
     * @param sectionUuid
     * @throws MembershipException Only students and TAs can be members of a
     * section.  Instructor roles are assigned only at the course level.
     * @throws RoleConfigurationException Thrown when no sakai role can be
     * identified to represent the given role.
     */
    public ParticipationRecord addSectionMembership(String userUid, Role role, String sectionUuid)
        throws MembershipException, RoleConfigurationException;

    /**
     * Defines the complete set of users that make up the members of a section in
     * a given role.  This is useful when doing bulk modifications of section
     * membership.
     *
     * @param userUids The set of userUids as strings
     * @param sectionId The sectionId
     *
     * @throws RoleConfigurationException If there is no properly configured role
     * in the site matching the role specified.
     */
    public void setSectionMemberships(Set userUids, Role role, String sectionId)
    	throws RoleConfigurationException;

    /**
     * Removes a user from a section.
     *
     * @param userUid
     * @param sectionUuid
     */
    public void dropSectionMembership(String userUid, String sectionUuid);

	/**
	 * Removes the user from any enrollment to a section of the given category.
	 *
	 * @param studentUid
	 * @param siteContext
	 * @param category
	 */
    public void dropEnrollmentFromCategory(String studentUid, String siteContext, String category);

    /**
     * Adds a CourseSection with a single meeting time to a parent CourseSection.
     * This method is deprecated.  Please use addSection(String courseUuid, String title,
     * String category, Integer maxEnrollments, List meetings)
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
     *
     * @deprecated
     *
     * @return
     */
    public CourseSection addSection(String courseUuid, String title,
    		String category, Integer maxEnrollments, String location,
    		Time startTime, Time endTime,
    		boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
    		boolean friday, boolean saturday, boolean sunday);

    /**
     * Adds multiple sections at once.  This should help address the inefficiencies
     * described in http://bugs.sakaiproject.org/jira/browse/SAK-7503.
     *
     * Meeting times should be, but are not handled by an external calendar service.
     * So, the added functionality of linking course sections to repeating events (meet
     * every 2nd Tuesday of the month at 3pm) is currently out of scope.  Instead,
     * meetings are represented as a start time, end time, and seven booleans
     * representing the days that the section meets.
     *
     * @param courseUuid
     * @param sections
     */
    public Collection<CourseSection> addSections(String courseUuid, Collection<CourseSection> sections);


    /**
     * Updates the persistent representation of the given CourseSection.  Once
     * a section is created, its category is immutable.  This method will remove all
     * but one Meeting associated with this CourseSection.  To update a CourseSection
     * and all of its meetings, use updateSection(String sectionUuid, String title,
     * Integer maxEnrollments, List meetings).
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
     *
     * @deprecated
     */
    public void updateSection(String sectionUuid, String title, Integer maxEnrollments,
    		String location, Time startTime, Time endTime,
    		boolean monday, boolean tuesday, boolean wednesday,
    		boolean thursday, boolean friday, boolean saturday, boolean sunday);

    /**
     * Updates a section and all of its meetings.  Notice that you can not change a
     * section's category once it's been created.
     *
     * @param sectionUuid
     * @param title
     * @param maxEnrollments
     * @param meetings
     */
    public void updateSection(String sectionUuid, String title, Integer maxEnrollments, List<Meeting> meetings);

    /**
     * Disbands a course section.  This does not affect enrollment records for
     * the course.
     *
     * @param sectionUuid
     */
    public void disbandSection(String sectionUuid);

    /**
     * Disbands course sections.  This does not affect enrollment records for
     * the course.
     *
     * @param sectionUuids
     */
    public void disbandSections(Set<String> sectionUuids);


    /**
     * Determines whether students can enroll themselves in a section.
     *
     *
     * @param courseUuid
     * @return
     */
    public boolean isSelfRegistrationAllowed(String courseUuid);

    /**
     * Determines whether students can switch sections once they are enrolled in
     * a section of a given category (for instance, swapping one lab for another).
     *
     * @param courseUuid
     * @return
     */
    public boolean isSelfSwitchingAllowed(String courseUuid);

    /**
     * Sets the join/switch options for a course.
     *
     * @param courseUuid
     * @param joinAllowed
     * @param switchAllowed
     */
    public void setJoinOptions(String courseUuid, boolean joinAllowed, boolean switchAllowed);

    /**
     * Determines whether a course is externally managed.
     *
     * @param courseUuid
     * @return
     */
    public boolean isExternallyManaged(String courseUuid);

    /**
     * Sets a course as externally or internally managed.
     *
     * @param courseUuid
     * @param externallyManaged
     */
	public void setExternallyManaged(String courseUuid, boolean externallyManaged);

	/**
     * The Section Manager tool could use more specific queries on membership,
     * such as this:  getting all students in a primary section that are not
     * enrolled in any secondary sections of a given type.  For instance, 'Who
     * are the students who are not enrolled in any lab?'
     *
     * @return A List of {@link
     * org.sakaiproject.section.api.coursemanagement.EnrollmentRecord
     * EnrollmentRecords} of students who are enrolled in the course but are
     * not enrolled in a section of the given section category.
     */
    public List<EnrollmentRecord> getUnsectionedEnrollments(String courseUuid, String category);

    /**
     * Gets the enrollment size for a set of sections.
     *
     * @param sectionSet
     * @return A Map (sectionUuid, size) 
     */
    public Map getEnrollmentCount(List sectionSet);
    
    /**
     * Gets the list of Teaching Assistants for a set of sections 
     *
     * @param sectionSet
     * @return A Map (sectionUuid, List<ParticipationRecord> ) of TAs for each section 
     */
    public Map<String,List<ParticipationRecord>> getSectionTeachingAssistantsMap(List sectionSet);
    
    /**
     * Gets all of the section enrollments for a user in a course.  Useful for
     * listing all of the sections in which a student is enrolled.
     *
     * @param userUid
     * @param courseUuid
     * @return A Set of EnrollmentRecords
     */
    public Set<EnrollmentRecord> getSectionEnrollments(String userUid, String courseUuid);


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
     * Gets the list of section categories.  These are not configurable on a per-course
     * or per-context bases.
     *
     * @param siteContext The site context (which is not used in the
     * current implementation)
     *
     * @return A List of unique Strings that identify the available section
     * categories.
     */
    public List<String> getSectionCategories(String siteContext);

	/**
	 * Gets a single User object for a student in a site.
	 *
	 * @param siteContext Needed by the standalone implementation to find the user
	 * @param studentUid
	 * @return The User representing this student
	 */
    public User getSiteEnrollment(String siteContext, String studentUid);


    //// Configuration


    /**
	 * Describes the configuration for the SectionManager service and the Section
	 * Info tool:
	 *
	 * <ul>
	 * 	<li><b>MANUAL_MANDATORY</b> - The Section Info tool does not allow for
	 * externally managed sections, and sections will never be created automatically</li>
	 *
	 * 	<li><b>MANUAL_DEFAULT</b> - The Section Info tool allows the user
	 * to choose whether sections should be internally or externally managed.
	 * Sections will not be generated for sites unless a site maintainer switches the
	 * default "manual" setting to automatic.</li>
	 *
	 * 	<li><b>AUTOMATIC_DEFAULT</b> - The Section Info tool allows the user
	 * to choose whether sections should be internally or externally managed.
	 * Sections will be generated for sites associated with any number of rosters.
	 * The default setting for new sites will be automatic management of sections.</li>
	 *
	 * 	<li><b>AUTOMATIC_MANDATORY</b> - The Section Info tool does not allow
	 * for internally managed sections.  All sections are created automatically, based
	 * on the rosters associated with the site.</li>
	 * </ul>
	 *
	 */
	public enum ExternalIntegrationConfig {MANUAL_MANDATORY, MANUAL_DEFAULT, AUTOMATIC_DEFAULT, AUTOMATIC_MANDATORY};

	/**
	 * Gets the application-wide configuration setting.
	 *
	 * @param obj An object to pass any necessary context information.
	 * @return
	 */
	public ExternalIntegrationConfig getConfiguration(Object obj);

	public static final String CONFIGURATION_KEY="section.info.integration";
	/**
	* Determines when the section options are open to students.
	*
	* @param courseUuid
	* @return
	*/
	public Calendar getOpenDate(String courseUid);
	public void setOpenDate(String courseUuid,Calendar openDate);
}
