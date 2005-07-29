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

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.exception.MembershipException;
import org.sakaiproject.api.section.facade.Role;

public interface SectionManager {
    /**
     * Adds the current user to a section as a student.
     * @param sectionId
     */
    public void joinSection(String sectionId);
    
    /**
     * Drops the current user's enrollment from a section.
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
     * Adds a user to a section under the specified role.
     * 
     * @param userId
     * @param role
     * @param sectionId
     * @throws MembershipException A user can not be added to a section more than once, regardless of role.
     */
    public void addSectionMembership(String userId, Role role, String sectionId)
        throws MembershipException;
    
    /**
     * Removes a user from a section.
     * 
     * @param userId
     * @param role
     * @param sectionId
     */
    public void dropSectionMembership(String userId, String sectionId);
 
    /**
     * Adds a CourseSection to a CourseOffering.  This assumes that meeting times
     * will not be handled by an external service.  The added functionality of
     * linking course sections to repeating events (meet every 2nd Tuesday of the
     * month at 3pm) is currently out of scope, so meetingTimes is represented
     * as a simple string.
     * 
     * @param courseOfferingUuid
     * @param title
     * @param meetingTimes
     * @param sectionLeaderId
     * @param maxEnrollments
     * @param location
     * @return
     */
    public CourseSection addSection(String courseOfferingUuid, String title,
    		String meetingTimes, String sectionLeaderId, int maxEnrollments,
    		String location, String category);

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
     * Determines whether students can enroll themselves in course sections.
     * 
     * @param courseOfferingId
     * @return
     */
    public boolean isSelfRegistrationAllowed(String courseOfferingId);
    
    /**
     * Sets the "self registration" status of a course offering.
     * 
     * @param courseOfferingId
     * @param allowed
     */
    public void setSelfRegistrationAllowed(String courseOfferingId, boolean allowed);
    
    /**
     * Determines whether students can switch sections once they are enrolled in
     * a section.
     * 
     * @param courseId
     * @return
     */
    public boolean isSectionSwitchingAllowed(String courseId);
    
    /**
     * Sets the "student switching" status of a course offering.
     * 
     * @param courseId
     * @param allowed
     */
    public void setSectionSwitchingAllowed(String courseId, boolean allowed);
    
    /**
     * @return The section awareness instance.  Provides methods to read section
     * data.
     */
    public SectionAwareness getSectionAwareness();
    
}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
