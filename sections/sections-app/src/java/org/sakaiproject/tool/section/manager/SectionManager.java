/**********************************************************************************
*
* $Id: $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.section.manager;

import java.util.List;
import java.util.Set;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
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
     * Adds the current user to a section as a student.  This is a convenience
     * method for addSectionMembership(currentUserId, Role.STUDENT, sectionId).
     * @param sectionId
     */
    public void joinSection(String sectionId);
    
    /**
     * Drops the current user's enrollment from a section.  This is a convenience
     * method for dropSectionMembership(currentUserId, sectionId).
     * 
     * @param sectionId
     */
    public void dropSection(String sectionId);
    
    /**
     * Switches a student's currently assigned section.  If the student is enrolled
     * in another section of the same type, that enrollment will be dropped.
     * 
     * This is a convenience method to allow a drop/add (a switch) in a single transaction.
     * 
     * @param newSectionId The new section id to which the student should be assigned
     */
    public void switchSection(String newSectionId);
    
    /**
     * Returns the total number of students enrolled in a section.  Useful for
     * comparing to the max number of enrollments allowed in a section.
     * 
     * @param sectionId
     * @return
     */
    public int getTotalEnrollments(String sectionId);
    
    /**
     * Adds a user to a section under the specified role.
     * 
     * @param userId
     * @param role
     * @param sectionUuid
     * @throws MembershipException A user can not be added to a section more than once, regardless of role.
     */
    public ParticipationRecord addSectionMembership(String userId, Role role, String sectionUuid)
        throws MembershipException;
    
    /**
     * Defines the complete set of users that make up the members of a section in
     * a given role.  This is useful when doing bulk modifications of section
     * membership.
     * 
     * @param userIds The set of userIds as strings
     * @param sectionId The sectionId
     */
    public void setSectionMemberships(Set userIds, Role role, String sectionId);
    
    /**
     * Removes a user from a section.
     * 
     * @param userId
     * @param sectionId
     */
    public void dropSectionMembership(String userId, String sectionId);
    
    /**
     * Adds a CourseSection to a parent CourseSection.  This assumes that meeting times
     * will not be handled by an external service.  The added functionality of
     * linking course sections to repeating events (meet every 2nd Tuesday of the
     * month at 3pm) is currently out of scope, so meetingTimes is represented
     * as a simple string.
     * 
     * @param parentSectionUuid
     * @param title
     * @param meetingTimes
     * @param sectionLeaderId
     * @param maxEnrollments
     * @param location
     * @return
     */
    public CourseSection addSection(String courseOfferingUuid, String title,
    		String meetingTimes, int maxEnrollments, String location, String category);

    /**
     * Updates the persistent representation of the given CourseSection.
     * 
     * @param section
     */
    public void updateSection(CourseSection section);
    
    /**
     * Disbands a course section.  This does not affect enrollment records for
     * the course.
     * 
     * @param section
     */
    public void disbandSection(CourseSection section);


    /**
     * Determines whether students can enroll themselves in a section.
     * 
     * @param sectionUuid
     * @return
     */
    public boolean isSelfRegistrationAllowed(String sectionUuid);
    
    /**
     * Sets the "self registration" status of a section.
     * 
     * @param sectionUuid
     * @param allowed
     */
    public void setSelfRegistrationAllowed(String sectionUuid, boolean allowed);
    
    /**
     * Determines whether students can switch sections once they are enrolled in
     * a section.
     * 
     * @param primarySectionUuid
     * @return
     */
    public boolean isSectionSwitchingAllowed(String primarySectionUuid);
    
    /**
     * Sets the "student switching" status of a primary section.
     * 
     * @param courseId
     * @param allowed
     */
    public void setSectionSwitchingAllowed(String sectionUuid, boolean allowed);
    
    /**
     * The Section Manager tool could use more specific queries on membership,
     * such as this:  getting all students in a primary section that are not
     * enrolled in any secondary sections of a given type.  For instance, 'Who
     * are the students who are not enrolled in any lab?'
     */
    public List getUnsectionedStudents(String siteContext, String category);

	/**
	 * Gets the list {@link org.sakaiproject.api.section.coursemanagement.User Users}
	 * that are teaching assistants in a section.
	 * 
	 * @param sectionUuid
	 * @return
	 */
    public List getTeachingAssistants(String sectionUuid);
    
    /**
     * @return The section awareness instance, which provides methods to read
     * section data.
     */
    public SectionAwareness getSectionAwareness();
}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
