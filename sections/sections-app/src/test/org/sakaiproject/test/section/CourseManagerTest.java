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

package org.sakaiproject.test.section;

import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.SectionManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.component.section.support.UserManager;

public class CourseManagerTest extends SectionsTestBase {
	private static final Log log = LogFactory.getLog(CourseManagerTest.class);

	private CourseManager courseManager;
	private SectionManager sectionManager;
	private UserManager userManager;
	
    protected void onSetUpInTransaction() throws Exception {
    	courseManager = (CourseManager)applicationContext.getBean("org.sakaiproject.api.section.CourseManager");
    	sectionManager = (SectionManager)applicationContext.getBean("org.sakaiproject.api.section.SectionManager");
        userManager = (UserManager)applicationContext.getBean("org.sakaiproject.component.section.support.UserManager");
    }
    
    public void testRemoveStudentFromCourse() throws Exception {
    	Course course = courseManager.createCourse("site", "course title", false, false, false);
    	User student1 = userManager.createUser("userUid", "foo", "bar", "baz");
    	CourseSection section1 = sectionManager.addSection(course.getUuid(), "a section", "a category",
    			null, null, null, null, false, false, false, false, false, false, false);
    	CourseSection section2 = sectionManager.addSection(course.getUuid(), "another section", "another category",
    			null, null, null, null, false, false, false, false, false, false, false);

    	// Enroll the user in the course
    	courseManager.addEnrollment(student1, course);

    	// Enroll the user as a student in both sections
    	sectionManager.addSectionMembership(student1.getUserUid(), Role.STUDENT, section1.getUuid());
    	sectionManager.addSectionMembership(student1.getUserUid(), Role.STUDENT, section2.getUuid());
    	
    	// Make sure the user is enrolled in the two sections
    	Collection enrollments = sectionManager.getSectionEnrollments(student1.getUserUid(), course.getUuid());
    	Assert.assertTrue(enrollments.size() == 2);
    	
    	// Remove the user from the course, and ensure that they are no longer in any sections
    	courseManager.removeUserFromAllSections(student1.getUserUid(), course.getSiteContext());

    	enrollments = sectionManager.getSectionEnrollments(student1.getUserUid(), course.getUuid());
    	Assert.assertTrue(enrollments.size() == 0);
    }

    public void testRemoveTaFromCourse() throws Exception {
    	Course course = courseManager.createCourse("site", "course title", false, false, false);
    	User ta1 = userManager.createUser("userUid", "foo", "bar", "baz");
    	CourseSection section1 = sectionManager.addSection(course.getUuid(), "a section", "a category",
    			null, null, null, null, false, false, false, false, false, false, false);
    	CourseSection section2 = sectionManager.addSection(course.getUuid(), "another section", "another category",
    			null, null, null, null, false, false, false, false, false, false, false);

    	// Enroll the user in the course
    	courseManager.addTA(ta1, course);

    	// Enroll the user as a student in both sections
    	sectionManager.addSectionMembership(ta1.getUserUid(), Role.TA, section1.getUuid());
    	sectionManager.addSectionMembership(ta1.getUserUid(), Role.TA, section2.getUuid());
    	
    	// Make sure the user is a member of two sections
    	Collection memberships1 = sectionManager.getSectionTeachingAssistants(section1.getUuid());
    	Collection memberships2 = sectionManager.getSectionTeachingAssistants(section2.getUuid());
    	Assert.assertTrue(memberships1.size() == 1);
    	Assert.assertTrue(memberships2.size() == 1);
    	
    	// Remove the user from the course, and ensure that they are no longer in any sections
    	courseManager.removeUserFromAllSections(ta1.getUserUid(), course.getSiteContext());

    	memberships1 = sectionManager.getSectionTeachingAssistants(section1.getUuid());
    	memberships2 = sectionManager.getSectionTeachingAssistants(section2.getUuid());
    	Assert.assertTrue(memberships1.size() == 0);
    	Assert.assertTrue(memberships2.size() == 0);
    }
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
