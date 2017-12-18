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
package org.sakaiproject.test.section.dataload;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.section.api.CourseManager;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.test.section.SectionsTestBase;

@Slf4j
public class DataLoadTest extends SectionsTestBase {

	public DataLoadTest() {
    	// Don't roll these tests back, since they are intended to load data
		setDefaultRollback(false);
	}
    
	protected CourseManager courseManager;
	protected SectionManager sectionManager;
    protected UserManager userManager;
    
	protected void onSetUpInTransaction() throws Exception {
		courseManager = (CourseManager)applicationContext.getBean("org.sakaiproject.section.api.CourseManager");
		sectionManager = (SectionManager)applicationContext.getBean("org.sakaiproject.section.api.SectionManager");
		userManager = (UserManager)applicationContext.getBean("org.sakaiproject.component.section.support.UserManager");
    }

	public void testLoadData() {
		// Load courses
		Course course1 = courseManager.createCourse("site1", "A Course for Site #1", false, false, true);
		Course course2 = courseManager.createCourse("site2", "A Course for Site #2", false, false, false);
		Course course3 = courseManager.createCourse("site3", "A Course for Site #3", false, false, false);

		// Load sections
    	CourseSection lab1 = sectionManager.addSection(course1.getUuid(), "Lab 1", "section.category.lab", Integer.valueOf(20), "Dank basement lab", new Time(new Date().getTime()), new Time(new Date().getTime()), true, false, true,  false, false, false, false);
    	CourseSection lab2 = sectionManager.addSection(course1.getUuid(), "Lab 2", "section.category.lab", Integer.valueOf(20), "Dank basement lab", new Time(new Date().getTime()), new Time(new Date().getTime()), false, true, false, true, false, false, false);
    	CourseSection disc1 = sectionManager.addSection(course1.getUuid(), "Disc 1", "section.category.discussion", Integer.valueOf(30), "Sunny classroom", new Time(new Date().getTime()), new Time(new Date().getTime()), true, false, true,  false, true, false, false);
		
		// Load students
		User studenta = userManager.createUser("studenta", "Joe Student", "Student, Joe", "jstudent");
		User studentb = userManager.createUser("studentb", "Jane Undergrad", "Undergrad, Jane", "jundergrad");
		User studentc = userManager.createUser("studentc", "Max Guest", "Guest, Max", "mguest");

		List studentList = new ArrayList();
		for(int i = 0; i < 100; i++) {
			studentList.add(userManager.createUser("student" + i, "Test Student " + i, "Student, Test " + i, "tstudent" + i));
		}
		
		// Load TAs
		User ta1 = userManager.createUser("ta1", "Mike Grad", "Grad, Mike", "mgrad");
		User ta2 = userManager.createUser("ta2", "Sara Hyphenated-Elongated-Postdoc", "Hyphenated-Elongated-Postdoc, Sara", "shyphenatedelongatedpostdoc");
		
		// Load instructors
		User instructor1 = userManager.createUser("instructor1", "Bill Economist", "Economist, Bill", "beconomist");
		User instructor2 = userManager.createUser("instructor2", "Amber Philosopher", "Philosopher, Amber", "aphilosopher");

		// Load other people
		userManager.createUser("other1", "Other Person", "Person, Other", "operson");

		// Load enrollments into the courses
		courseManager.addEnrollment(studenta, course1);
		courseManager.addEnrollment(studenta, course2);
		courseManager.addEnrollment(studenta, course3);

		courseManager.addEnrollment(studentb, course1);
		courseManager.addEnrollment(studentb, course2);
		courseManager.addEnrollment(studentb, course3);

		courseManager.addEnrollment(studentc, course1);
		courseManager.addEnrollment(studentc, course2);
		courseManager.addEnrollment(studentc, course3);
		
		for(Iterator iter = studentList.iterator(); iter.hasNext();) {
			User user = (User)iter.next();
			courseManager.addEnrollment(user, course1);
			courseManager.addEnrollment(user, course2);
			courseManager.addEnrollment(user, course3);
		}
		
		// Load enrollments into sections
		try {
			sectionManager.addSectionMembership("studenta", Role.STUDENT, lab1.getUuid());
			sectionManager.addSectionMembership("studentb", Role.STUDENT, lab2.getUuid());
			sectionManager.addSectionMembership("studentc", Role.STUDENT, disc1.getUuid());
		} catch (RoleConfigurationException rce) {
			log.error(rce);
			fail();
		}
		
		// Load TAs into the course
		courseManager.addTA(ta1, course1);
		courseManager.addTA(ta1, course2);
		courseManager.addTA(ta1, course3);

		courseManager.addTA(ta2, course1);
		courseManager.addTA(ta2, course2);
		courseManager.addTA(ta2, course3);
		
		// Load TAs into the sections
		try {
			sectionManager.addSectionMembership("ta1", Role.TA, lab1.getUuid());
			sectionManager.addSectionMembership("ta1", Role.TA, disc1.getUuid());
			sectionManager.addSectionMembership("ta2", Role.TA, lab2.getUuid());
			sectionManager.addSectionMembership("ta2", Role.TA, disc1.getUuid());
		} catch (RoleConfigurationException rce) {
			log.error(rce);
			fail();
		}
		
		// Load instructors into the courses
		courseManager.addInstructor(instructor1, course1);
		courseManager.addInstructor(instructor2, course2);
		courseManager.addInstructor(instructor2, course3);
	}
	
}
