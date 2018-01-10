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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.test.section;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import junit.framework.Assert;

import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.section.api.CourseManager;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;

@Slf4j
public class CourseManagerTest extends SectionsTestBase {

	private CourseManager courseManager;
	private SectionManager sectionManager;
	private UserManager userManager;
	
    protected void onSetUpInTransaction() throws Exception {
    	courseManager = (CourseManager)applicationContext.getBean("org.sakaiproject.section.api.CourseManager");
    	sectionManager = (SectionManager)applicationContext.getBean("org.sakaiproject.section.api.SectionManager");
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
    }
    
    public void testRemoveOrphanedSectionMemberships() throws Exception {
    	Course course = courseManager.createCourse("site", "course title", false, false, false);
    	User ta1 = userManager.createUser("userUid1", "foo1", "bar1", "baz1");
    	User ta2 = userManager.createUser("userUid2", "foo2", "bar2", "baz2");
    	User student1 = userManager.createUser("userUid3", "foo3", "bar3", "baz3");
    	User student2 = userManager.createUser("userUid4", "foo4", "bar4", "baz4");
    	CourseSection section1 = sectionManager.addSection(course.getUuid(), "a section", "a category",
    			null, null, null, null, false, false, false, false, false, false, false);
    	CourseSection section2 = sectionManager.addSection(course.getUuid(), "another section", "another category",
    			null, null, null, null, false, false, false, false, false, false, false);

    	// Enroll the users in the course
    	courseManager.addTA(ta1, course);
    	courseManager.addTA(ta2, course);
    	courseManager.addEnrollment(student1, course);
    	courseManager.addEnrollment(student2, course);

    	// Enroll the users in both sections
    	sectionManager.addSectionMembership(ta1.getUserUid(), Role.TA, section1.getUuid());
    	sectionManager.addSectionMembership(ta1.getUserUid(), Role.TA, section2.getUuid());
    	sectionManager.addSectionMembership(ta2.getUserUid(), Role.TA, section1.getUuid());
    	sectionManager.addSectionMembership(ta2.getUserUid(), Role.TA, section2.getUuid());
    	sectionManager.addSectionMembership(student1.getUserUid(), Role.STUDENT, section1.getUuid());
    	sectionManager.addSectionMembership(student1.getUserUid(), Role.STUDENT, section2.getUuid());
    	sectionManager.addSectionMembership(student2.getUserUid(), Role.STUDENT, section1.getUuid());
    	sectionManager.addSectionMembership(student2.getUserUid(), Role.STUDENT, section2.getUuid());

    	// Remove the #2 users from the course
    	courseManager.removeCourseMembership(ta2.getUserUid(), course);
    	courseManager.removeCourseMembership(student2.getUserUid(), course);
    	
    	// Ensure that the second student and ta are no longer associated with any sections
    	List section1Tas = sectionManager.getSectionTeachingAssistants(section1.getUuid());
    	for(Iterator iter = section1Tas.iterator(); iter.hasNext();) {
    		ParticipationRecord record = (ParticipationRecord)iter.next();
    		if(record.getUser().getUserUid().equals(ta2.getUserUid())) {
    			fail();
    		}
    	}
    	List section2Tas = sectionManager.getSectionTeachingAssistants(section2.getUuid());
    	for(Iterator iter = section2Tas.iterator(); iter.hasNext();) {
    		ParticipationRecord record = (ParticipationRecord)iter.next();
    		if(record.getUser().getUserUid().equals(ta2.getUserUid())) {
    			fail();
    		}
    	}

    	List section1Students = sectionManager.getSectionTeachingAssistants(section1.getUuid());
    	for(Iterator iter = section1Students.iterator(); iter.hasNext();) {
    		ParticipationRecord record = (ParticipationRecord)iter.next();
    		if(record.getUser().getUserUid().equals(student2.getUserUid())) {
    			fail();
    		}
    	}
    	List section2Students = sectionManager.getSectionTeachingAssistants(section2.getUuid());
    	for(Iterator iter = section2Students.iterator(); iter.hasNext();) {
    		ParticipationRecord record = (ParticipationRecord)iter.next();
    		if(record.getUser().getUserUid().equals(student2.getUserUid())) {
    			fail();
    		}
    	}

    	Assert.assertEquals(1, sectionManager.getTotalEnrollments(section1.getUuid()));
    	Assert.assertEquals(1, sectionManager.getTotalEnrollments(section2.getUuid()));
    	
    }
    
}
