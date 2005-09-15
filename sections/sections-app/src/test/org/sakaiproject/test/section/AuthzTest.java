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

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.SectionManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.test.section.manager.UserManager;

/**
 * Each test method is isolated in its own transaction, which is rolled back when
 * the method exits.  Since we can not assume that data will exist, we need to use
 * the SectionManager api to insert data before retrieving it with SectionAwareness.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class AuthzTest extends SectionsTestBase{
	private static final Log log = LogFactory.getLog(AuthzTest.class);
	
	private Authz authz;
	private Context context;
	private SectionManager secMgr;
	private CourseManager courseMgr;
	private UserManager userMgr;

    protected void onSetUpInTransaction() throws Exception {
    	authz = (Authz)applicationContext.getBean("org.sakaiproject.api.section.facade.manager.Authz");
    	context = (Context)applicationContext.getBean("org.sakaiproject.api.section.facade.manager.Context");
        secMgr = (SectionManager)applicationContext.getBean("org.sakaiproject.api.section.SectionManager");
        courseMgr = (CourseManager)applicationContext.getBean("org.sakaiproject.api.section.CourseManager");
        userMgr = (UserManager)applicationContext.getBean("org.sakaiproject.test.section.manager.UserManager");
    }

    public void testSectionMembership() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secMgr.getSectionCategories();
    	
    	// Add a course and a section to work from
    	Course course = courseMgr.createCourse(siteContext, "A course", false, false, false);
    	
    	CourseSection sec1 = secMgr.addSection(course.getUuid(), "A section", (String)categories.get(0), new Integer(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec2 = secMgr.addSection(course.getUuid(), "Another section", (String)categories.get(1), new Integer(10), null, null, null, false,  false, false,  false, false, false, false);
    	
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

		Assert.assertTrue(authz.getSiteRole(student1.getUserUuid(), siteContext).isStudent());
		Assert.assertTrue(authz.getSectionRole(student1.getUserUuid(), sec1.getUuid()).isStudent());
		Assert.assertTrue(authz.getSectionRole(student1.getUserUuid(), sec2.getUuid()).isNone());

		Assert.assertTrue(authz.getSiteRole(student2.getUserUuid(), siteContext).isStudent());
		Assert.assertTrue(authz.getSectionRole(student2.getUserUuid(), sec1.getUuid()).isNone());
		Assert.assertTrue(authz.getSectionRole(student2.getUserUuid(), sec2.getUuid()).isStudent());
		
		Assert.assertTrue(authz.getSiteRole(ta1.getUserUuid(), siteContext).isTeachingAssistant());
		Assert.assertTrue(authz.getSectionRole(ta1.getUserUuid(), sec1.getUuid()).isTeachingAssistant());
		Assert.assertTrue(authz.getSectionRole(ta1.getUserUuid(), sec2.getUuid()).isNone());

		Assert.assertTrue(authz.getSiteRole(ta2.getUserUuid(), siteContext).isTeachingAssistant());
		Assert.assertTrue(authz.getSectionRole(ta2.getUserUuid(), sec1.getUuid()).isNone());
		Assert.assertTrue(authz.getSectionRole(ta2.getUserUuid(), sec2.getUuid()).isTeachingAssistant());
		
		Assert.assertTrue(authz.getSiteRole(instructor1.getUserUuid(), siteContext).isInstructor());
    }
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
