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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import junit.framework.Assert;

import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.section.api.CourseManager;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.section.api.facade.manager.Authz;
import org.sakaiproject.section.api.facade.manager.Context;

/**
 * Each test method is isolated in its own transaction, which is rolled back when
 * the method exits.  Since we can not assume that data will exist, we need to use
 * the SectionManager api to insert data before retrieving it with SectionAwareness.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class AuthzTest extends SectionsTestBase{

	private Authz authz;
	private Context context;
	private SectionManager secMgr;
	private CourseManager courseMgr;
	private UserManager userMgr;

    protected void onSetUpInTransaction() throws Exception {
    	authz = (Authz)applicationContext.getBean("org.sakaiproject.section.api.facade.manager.Authz");
    	context = (Context)applicationContext.getBean("org.sakaiproject.section.api.facade.manager.Context");
        secMgr = (SectionManager)applicationContext.getBean("org.sakaiproject.section.api.SectionManager");
        courseMgr = (CourseManager)applicationContext.getBean("org.sakaiproject.section.api.CourseManager");
        userMgr = (UserManager)applicationContext.getBean("org.sakaiproject.component.section.support.UserManager");
    }

    public void testSectionMembership() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secMgr.getSectionCategories(siteContext);
    	
    	// Add a course and a section to work from
    	Course course = courseMgr.createCourse(siteContext, "A course", false, false, false);
    	
    	CourseSection sec1 = secMgr.addSection(course.getUuid(), "A section", (String)categories.get(0), Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec2 = secMgr.addSection(course.getUuid(), "Another section", (String)categories.get(1), Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);
    	
		// Load students
		User student1 = userMgr.createUser("student1", "Joe Student", "Student, Joe", "jstudent");
		User student2 = userMgr.createUser("student2", "Jane Undergrad", "Undergrad, Jane", "jundergrad");

		// Load TAs
		User ta1 = userMgr.createUser("ta1", "Mike Grad", "Grad, Mike", "mgrad");
		User ta2 = userMgr.createUser("ta2", "Sara Postdoc", "Postdoc, Sara", "spostdoc");
		
		// Load instructors
		User instructor1 = userMgr.createUser("instructor1", "Bill Economist", "Economist, Bill", "beconomist");

		// Load enrollments into the course
		courseMgr.addEnrollment(student1, course);
		courseMgr.addEnrollment(student2, course);
		
		// Load enrollments into sections
		secMgr.addSectionMembership("student1", Role.STUDENT, sec1.getUuid());
		secMgr.addSectionMembership("student2", Role.STUDENT, sec2.getUuid());
		
		// Load TAs into the course
		courseMgr.addTA(ta1, course);
		courseMgr.addTA(ta2, course);
		
		// Load TAs into the sections
		secMgr.addSectionMembership("ta1", Role.TA, sec1.getUuid());
		secMgr.addSectionMembership("ta2", Role.TA, sec2.getUuid());
		
		// Load instructor into the courses
		courseMgr.addInstructor(instructor1, course);

		Assert.assertTrue(authz.isViewOwnSectionsAllowed(student1.getUserUid(), siteContext));
		Assert.assertTrue(authz.isViewOwnSectionsAllowed(student2.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isViewOwnSectionsAllowed(ta1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isViewOwnSectionsAllowed(ta2.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isViewOwnSectionsAllowed(instructor1.getUserUid(), siteContext));

		Assert.assertTrue(authz.isSectionEnrollmentMangementAllowed(instructor1.getUserUid(), siteContext));
		Assert.assertTrue(authz.isSectionEnrollmentMangementAllowed(ta1.getUserUid(), siteContext));
		Assert.assertTrue(authz.isSectionEnrollmentMangementAllowed(ta2.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionEnrollmentMangementAllowed(student1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionEnrollmentMangementAllowed(student2.getUserUid(), siteContext));

		Assert.assertTrue(authz.isSectionManagementAllowed(instructor1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionManagementAllowed(ta1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionManagementAllowed(ta2.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionManagementAllowed(student1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionManagementAllowed(student2.getUserUid(), siteContext));
		
		Assert.assertTrue(authz.isSectionOptionsManagementAllowed(instructor1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionOptionsManagementAllowed(ta1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionOptionsManagementAllowed(ta2.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionOptionsManagementAllowed(student1.getUserUid(), siteContext));
		Assert.assertTrue( ! authz.isSectionOptionsManagementAllowed(student2.getUserUid(), siteContext));
		
    }
}
