/**********************************************************************************
*
* $Id: SampleComponentTest.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
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
import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.test.section.manager.CourseManager;
import org.sakaiproject.test.section.manager.UserManager;
import org.sakaiproject.tool.section.manager.SectionManager;

/**
 * Each test method is isolated in its own transaction, which is rolled back when
 * the method exits.  Since we can not assume that data will exist, we need to use
 * the SectionManager api to insert data before retrieving it with SectionAwareness.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionAwarenessTest extends SectionsTestBase{
	private static final Log log = LogFactory.getLog(SectionAwarenessTest.class);
	
	private Context context;
	private SectionManager secMgr;
	private SectionAwareness secAware;
	private CourseManager courseMgr;
	private UserManager userMgr;

    protected void onSetUpInTransaction() throws Exception {
    	context = (Context)applicationContext.getBean("org.sakaiproject.api.section.facade.manager.Context");
        secMgr = (SectionManager)applicationContext.getBean("org.sakaiproject.tool.section.manager.SectionManager");
        secAware = (SectionAwareness)applicationContext.getBean("org.sakaiproject.api.section.SectionAwareness");
        courseMgr = (CourseManager)applicationContext.getBean("org.sakaiproject.test.section.manager.CourseManager");
        userMgr = (UserManager)applicationContext.getBean("org.sakaiproject.test.section.manager.UserManager");
    }

    public void testSectionsAndCategories() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secAware.getSectionCategories();
    	
    	// Add a course and a section to work from
    	courseMgr.createCourse(siteContext, "A course", false, false, false);
    	Course course = secMgr.getCourse(siteContext);
    	
    	String firstCategory = (String)categories.get(0);
    	CourseSection sec = secMgr.addSection(course.getUuid(), "A section", firstCategory, new Integer(10), null, null, null, false,  false, false,  false, false, false, false);

    	// Assert that the course exists at this context
    	Assert.assertTrue(secMgr.getCourse(siteContext).getUuid().equals(course.getUuid()));
    	
    	// Assert that section awareness can retrieve the new section
    	Set sections = secAware.getSections(siteContext);
    	Assert.assertTrue(sections.size() == 1);
    	Assert.assertTrue(sections.contains(sec));

    	// Assert that section awareness can retrieve the section by its uuid
    	CourseSection secByUuid = secAware.getSection(sec.getUuid());
    	Assert.assertTrue(secByUuid.equals(sec));

    	// Assert that section awareness can retrieve the section by its category
    	List sectionsInCategory = secAware.getSectionsInCategory(siteContext, firstCategory);
    	Assert.assertTrue(sectionsInCategory.contains(sec));

    	// Assert that section awareness can retrieve the category name
    	Assert.assertTrue(secAware.getCategoryName(firstCategory, Locale.US) != null);
    }
    
    public void testSectionMembership() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secAware.getSectionCategories();
    	
    	// Add a course and a section to work from
    	courseMgr.createCourse(siteContext, "A course", false, false, false);
    	Course course = secMgr.getCourse(siteContext);
    	String firstCategory = (String)categories.get(0);
    	CourseSection sec = secMgr.addSection(course.getUuid(), "A section", firstCategory, new Integer(10), null, null, null, false,  false, false,  false, false, false, false);
    	
		// Load students
		User student1 = userMgr.createUser("student1", "Joe Student", "Student, Joe", "jstudent");
		User student2 = userMgr.createUser("student2", "Jane Undergrad", "Undergrad, Jane", "jundergrad");

		// Load TAs
		User ta1 = userMgr.createUser("ta1", "Mike Grad", "Grad, Mike", "mgrad");
		User ta2 = userMgr.createUser("ta2", "Sara Postdoc", "Postdoc, Sara", "spostdoc");
		
		// Load instructors
		User instructor1 = userMgr.createUser("instructor1", "Bill Economist", "Economist, Bill", "beconomist");
		User instructor2 = userMgr.createUser("instructor2", "Amber Philosopher", "Philosopher, Amber", "aphilosopher");

		// Load other people
		User otherPerson = userMgr.createUser("other1", "Other Person", "Person, Other", "operson");

		// Load enrollments into the course
		ParticipationRecord siteEnrollment1 = courseMgr.addEnrollment(student1, course);
		ParticipationRecord siteEnrollment2 = courseMgr.addEnrollment(student2, course);
		
		// Load enrollments into sections
		ParticipationRecord sectionEnrollment1 = secMgr.addSectionMembership("student1", Role.STUDENT, sec.getUuid());
		ParticipationRecord sectionEnrollment2 = secMgr.addSectionMembership("student2", Role.STUDENT, sec.getUuid());
		
		// Load TAs into the course
		ParticipationRecord siteTaRecord1 = courseMgr.addTA(ta1, course);
		ParticipationRecord siteTaRecord2 = courseMgr.addTA(ta2, course);
		
		// Load TAs into the sections
		ParticipationRecord sectionTaRecord1 = secMgr.addSectionMembership("ta1", Role.TA, sec.getUuid());
		ParticipationRecord sectionTaRecord2 = secMgr.addSectionMembership("ta2", Role.TA, sec.getUuid());
		
		// Load instructors into the courses
		ParticipationRecord siteInstructorRecord1 = courseMgr.addInstructor(instructor1, course);
    	
    	// Assert that section awareness can find site members for each role
		List siteInstructors = secAware.getSiteMembersInRole(siteContext, Role.INSTRUCTOR);
    	Assert.assertTrue(siteInstructors.contains(siteInstructorRecord1));
    	
		List siteEnrollments = secAware.getSiteMembersInRole(siteContext, Role.STUDENT);
    	Assert.assertTrue(siteEnrollments.contains(siteEnrollment1));
    	Assert.assertTrue(siteEnrollments.contains(siteEnrollment2));
    	
		List siteTAs = secAware.getSiteMembersInRole(siteContext, Role.TA);
    	Assert.assertTrue(siteTAs.contains(siteTaRecord1));
    	Assert.assertTrue(siteTAs.contains(siteTaRecord2));
    	
    	// Assert that section awareness can find site members matching a string pattern in a role
    	List membersMatchingSearch = secAware.findSiteMembersInRole(siteContext, Role.STUDENT, "student");
    	Assert.assertTrue(membersMatchingSearch.contains(siteEnrollment1));
    	Assert.assertTrue( ! membersMatchingSearch.contains(siteEnrollment2));

    	// Assert that section awareness can find section members
    	List allSectionMembers = secAware.getSectionMembers(sec.getUuid());
    	Assert.assertTrue(allSectionMembers.contains(sectionEnrollment1));
    	Assert.assertTrue(allSectionMembers.contains(sectionEnrollment2));
    	Assert.assertTrue(allSectionMembers.contains(sectionTaRecord1));
    	Assert.assertTrue(allSectionMembers.contains(sectionTaRecord2));

    	// Make sure the site records are not returned
    	Assert.assertTrue( ! allSectionMembers.contains(siteEnrollment1));
    	Assert.assertTrue( ! allSectionMembers.contains(siteEnrollment2));
    	Assert.assertTrue( ! allSectionMembers.contains(siteTaRecord1));
    	Assert.assertTrue( ! allSectionMembers.contains(siteTaRecord2));
    	Assert.assertTrue( ! allSectionMembers.contains(siteInstructorRecord1));
    	
    	// Assert that section awareness can find section members for each role
    	List studentSectionRecords = secAware.getSectionMembersInRole(sec.getUuid(), Role.STUDENT);
    	Assert.assertTrue(studentSectionRecords.contains(sectionEnrollment1));
    	Assert.assertTrue(studentSectionRecords.contains(sectionEnrollment2));
    	
    	List taSectionRecords = secAware.getSectionMembersInRole(sec.getUuid(), Role.TA);
    	Assert.assertTrue(taSectionRecords.contains(sectionTaRecord1));
    	Assert.assertTrue(taSectionRecords.contains(sectionTaRecord2));
    }
}



/**********************************************************************************
 * $Id: $
 *********************************************************************************/
