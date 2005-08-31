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

package org.sakaiproject.test.section.dataload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.test.section.SectionsTestBase;
import org.sakaiproject.test.section.manager.CourseManager;
import org.sakaiproject.test.section.manager.UserManager;
import org.sakaiproject.tool.section.manager.SectionManager;

public class DataLoadTest extends SectionsTestBase {
	private static Log log = LogFactory.getLog(DataLoadTest.class);
	
	public DataLoadTest() {
    	// Don't roll these tests back, since they are intended to load data
		setDefaultRollback(false);
	}
    
	protected CourseManager courseManager;
	protected SectionManager sectionManager;
    protected UserManager userManager;
    
	protected void onSetUpInTransaction() throws Exception {
		courseManager = (CourseManager)applicationContext.getBean("org.sakaiproject.test.section.manager.CourseManager");
		sectionManager = (SectionManager)applicationContext.getBean("org.sakaiproject.tool.section.manager.SectionManager");
		userManager = (UserManager)applicationContext.getBean("org.sakaiproject.test.section.manager.UserManager");
    }

	public void testLoadData() {
		// Load courses
		Course course1 = courseManager.createCourse("site1", "A Course for Site #1", false, false, false);
		Course course2 = courseManager.createCourse("site2", "A Course for Site #2", false, false, false);
		Course course3 = courseManager.createCourse("site3", "A Course for Site #3", false, false, true);

		// Load sections
    	CourseSection lab1 = sectionManager.addSection(course1.getUuid(), "Lab 1", "section.category.lab", 20, "Dank basement lab", "9:00", true, "12:00", false, true, false, true,  false, false, false, false);
    	CourseSection lab2 = sectionManager.addSection(course1.getUuid(), "Lab 2", "section.category.lab", 20, "Dank basement lab", "9:00", true, "12:00", false, false, true, false, true, false, false, false);
    	CourseSection disc1 = sectionManager.addSection(course1.getUuid(), "Disc 1", "section.category.discussion", 30, "Sunny classroom", "3:00", false, "4:00", false, true, false, true,  false, true, false, false);
		
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
		User ta2 = userManager.createUser("ta2", "Sara Postdoc", "Postdoc, Sara", "spostdoc");
		
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
		sectionManager.addSectionMembership("studenta", Role.STUDENT, lab1.getUuid());
		sectionManager.addSectionMembership("studentb", Role.STUDENT, lab2.getUuid());
		sectionManager.addSectionMembership("studentc", Role.STUDENT, disc1.getUuid());
		
		// Load TAs into the course
		courseManager.addTA(ta1, course1);
		courseManager.addTA(ta1, course2);
		courseManager.addTA(ta1, course3);

		courseManager.addTA(ta2, course1);
		courseManager.addTA(ta2, course2);
		courseManager.addTA(ta2, course3);
		
		// Load TAs into the sections
		sectionManager.addSectionMembership("ta1", Role.TA, lab1.getUuid());
		sectionManager.addSectionMembership("ta1", Role.TA, disc1.getUuid());
		sectionManager.addSectionMembership("ta2", Role.TA, lab2.getUuid());
		sectionManager.addSectionMembership("ta2", Role.TA, disc1.getUuid());
		
		// Load instructors into the courses
		courseManager.addInstructor(instructor1, course1);
		courseManager.addInstructor(instructor2, course2);
		courseManager.addInstructor(instructor2, course3);
	}
	
}

/**********************************************************************************
 * $Id$
 *********************************************************************************/
