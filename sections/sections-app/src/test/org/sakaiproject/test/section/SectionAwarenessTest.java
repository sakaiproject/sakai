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
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import junit.framework.Assert;

import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.section.api.CourseManager;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
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
public class SectionAwarenessTest extends SectionsTestBase{

	private Context context;
	private SectionManager secMgr;
	private SectionAwareness secAware;
	private CourseManager courseMgr;
	private UserManager userMgr;

    protected void onSetUpInTransaction() throws Exception {
    	context = (Context)applicationContext.getBean("org.sakaiproject.section.api.facade.manager.Context");
        secMgr = (SectionManager)applicationContext.getBean("org.sakaiproject.section.api.SectionManager");
        secAware = (SectionAwareness)applicationContext.getBean("org.sakaiproject.section.api.SectionAwareness");
        courseMgr = (CourseManager)applicationContext.getBean("org.sakaiproject.section.api.CourseManager");
        userMgr = (UserManager)applicationContext.getBean("org.sakaiproject.component.section.support.UserManager");
    }

    public void testSectionsAndCategories() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secAware.getSectionCategories(siteContext);
    	
    	// Add a course and a section to work from
    	courseMgr.createCourse(siteContext, "A course", false, false, false);
    	Course course = secMgr.getCourse(siteContext);
    	
    	String firstCategory = (String)categories.get(0);
    	CourseSection sec = secMgr.addSection(course.getUuid(), "A section", firstCategory, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);

    	// Assert that the course exists at this context
    	Assert.assertTrue(secMgr.getCourse(siteContext).getUuid().equals(course.getUuid()));
    	
    	// Assert that section awareness can retrieve the new section
    	List sections = secAware.getSections(siteContext);
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
    
    public void testSectionSorting() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secAware.getSectionCategories(siteContext);
    	
    	// Add a course and a section to work from
    	courseMgr.createCourse(siteContext, "A course", false, false, false);
    	Course course = secMgr.getCourse(siteContext);
    	
    	String firstCategory = (String)categories.get(0);
    	String secondCategory = (String)categories.get(1);
    	
    	CourseSection sec1 = secMgr.addSection(course.getUuid(), "aSection", firstCategory, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec2 = secMgr.addSection(course.getUuid(), "bSection", firstCategory, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);

    	CourseSection sec3 = secMgr.addSection(course.getUuid(), "aaSection", secondCategory, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec4 = secMgr.addSection(course.getUuid(), "bbSection", secondCategory, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);
    	
    	CourseSection sec5 = secMgr.addSection(course.getUuid(), "aaaSection", null, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec6 = secMgr.addSection(course.getUuid(), "bbbSection", null, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);

    	List sections = secAware.getSections(siteContext);
    	
    	// Ensure that sections in the same category sort by title
    	Assert.assertTrue(sections.indexOf(sec1) < sections.indexOf(sec2));
    	
    	// Ensure that secions in different categories sort by category
    	Assert.assertTrue(sections.indexOf(sec1) < sections.indexOf(sec3));

    	// Ensure that sections with null categories sort by title
    	Assert.assertTrue(sections.indexOf(sec5) < sections.indexOf(sec6));

    	// Ensure that sections with null categories sort after a section with a category
    	// This isn't possible in standalone... so do we need to test for it?
    	// Since the sorting is in the DB, we can't be sure where null categories will be sorted (first or last)
    	Assert.assertTrue(sections.indexOf(sec1) < sections.indexOf(sec5));
    	Assert.assertTrue(sections.indexOf(sec1) < sections.indexOf(sec6));
    }
    
    public void testSectionMembership() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secAware.getSectionCategories(siteContext);
    	
    	// Add a course and a section to work from
    	courseMgr.createCourse(siteContext, "A course", false, false, false);
    	Course course = secMgr.getCourse(siteContext);
    	String firstCategory = (String)categories.get(0);
    	CourseSection sec = secMgr.addSection(course.getUuid(), "A section", firstCategory, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);
    	
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
    
    public void testGetUnassignedMembers() throws Exception {
    	String siteContext = context.getContext(null);
    	List categories = secAware.getSectionCategories(siteContext);
    	
    	// Add a course and a section to work from
    	Course course = courseMgr.createCourse(siteContext, "A course", false, false, false);
    	
    	String firstCategory = (String)categories.get(0);
    	CourseSection sec = secMgr.addSection(course.getUuid(), "A section", firstCategory, Integer.valueOf(10), null, null, null, false,  false, false,  false, false, false, false);

		// Load students
		User student1 = userMgr.createUser("student1", "Joe Student", "Student, Joe", "jstudent");
		User student2 = userMgr.createUser("student2", "Jane Undergrad", "Undergrad, Jane", "jundergrad");
		User student3 = userMgr.createUser("student3", "Foo Bar", "Bar, Foo", "fbar");

		// Load TAs
		User ta1 = userMgr.createUser("ta1", "Mike Grad", "Grad, Mike", "mgrad");
		User ta2 = userMgr.createUser("ta2", "Sara Postdoc", "Postdoc, Sara", "spostdoc");

		// Load enrollments into the course
		ParticipationRecord siteEnrollment1 = courseMgr.addEnrollment(student1, course);
		ParticipationRecord siteEnrollment2 = courseMgr.addEnrollment(student2, course);
		ParticipationRecord siteEnrollment3 = courseMgr.addEnrollment(student3, course);
		
		// Load enrollments into sections
		ParticipationRecord sectionEnrollment1 = secMgr.addSectionMembership("student1", Role.STUDENT, sec.getUuid());
		
		// Load TAs into the course
		ParticipationRecord siteTaRecord1 = courseMgr.addTA(ta1, course);
		ParticipationRecord siteTaRecord2 = courseMgr.addTA(ta2, course);
		
		// Load TAs into the sections
		ParticipationRecord sectionTaRecord1 = secMgr.addSectionMembership("ta1", Role.TA, sec.getUuid());


		// Students 2 and 3 should be unsectioned
		List unassignedStudents = secAware.getUnassignedMembersInRole(siteContext, Role.STUDENT);
		Assert.assertEquals(unassignedStudents.size(), 2);
		Assert.assertTrue( ! unassignedStudents.contains(sectionEnrollment1));
		
		// TA2 should be unsectioned
		List unassignedTas = secAware.getUnassignedMembersInRole(siteContext, Role.TA);
		Assert.assertEquals(unassignedTas.size(), 1);
		Assert.assertTrue( ! unassignedTas.contains(sectionTaRecord1));
    }
}
