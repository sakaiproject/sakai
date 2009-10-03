/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.coursemanagement.impl.CourseOfferingCmImpl;
import org.sakaiproject.coursemanagement.impl.DataLoader;

public class CourseManagementServiceTest extends CourseManagementTestBase {
	private static final Log log = LogFactory.getLog(CourseManagementServiceTest.class);
	
	private CourseManagementService cm;
	private DataLoader loader;
	
	protected void onSetUpBeforeTransaction() throws Exception {
    }

	protected void onSetUpInTransaction() throws Exception {
    	cm = (CourseManagementService)applicationContext.getBean(CourseManagementService.class.getName());
		loader = (DataLoader)applicationContext.getBean(DataLoader.class.getName());
		loader.load();
	}
	
	public void testGetAcademicSessions() throws Exception {
		Assert.assertEquals(1, cm.getAcademicSessions().size());
	}
	
	public void testGetCurrentAcademicSessions() throws Exception {
		Assert.assertEquals(1, cm.getCurrentAcademicSessions().size());
	}	
	
	public void testGetAcademicSessionById() throws Exception {
		AcademicSession term = cm.getAcademicSession("F2006");
		Assert.assertEquals("Fall 2006", term.getTitle());
		try {
			cm.getAcademicSession("bad eid");
			fail();
		} catch (IdNotFoundException ide) {}
	}
	
	public void testGetCourseSets() throws Exception {
		Assert.assertEquals(2, cm.getCourseSets().size());
	}

	public void testGetCourseSetsFromCourseOffering() throws Exception {
		CourseOffering co = cm.getCourseOffering("BIO101_F2006_01");
		CourseSet bio = cm.getCourseSet("BIO_DEPT");
		CourseSet bioChem = cm.getCourseSet("BIO_CHEM_GROUP");
		
		// Ensure that the CourseSet EIDs can be retrieved from the CourseOffering
		Assert.assertEquals(2, co.getCourseSetEids().size());

		// Ensure that the set of CourseSets contains the right objects
		Set courseSetsFromCo = ((CourseOfferingCmImpl)co).getCourseSets();
		Assert.assertTrue(courseSetsFromCo.contains(bio));
		Assert.assertTrue(courseSetsFromCo.contains(bioChem));
	}

	public void testGetChildCourseSets() throws Exception {
		CourseSet parent = (CourseSet)cm.getCourseSet("BIO_DEPT");
		Assert.assertEquals(1, cm.getChildCourseSets(parent.getEid()).size());		
		
		try {
			cm.getChildCourseSets("bad eid");
			fail();
		} catch (IdNotFoundException ide) {}
	}
	
	public void testGetCourseSetMembers() throws Exception {
		Set members = cm.getCourseSetMemberships("BIO_DEPT");
		Assert.assertEquals(1, members.size());
		try {
			cm.getCourseSetMemberships("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}
	
	public void testGetCanonicalCourse() throws Exception {
		Assert.assertEquals("Biology 101", cm.getCanonicalCourse("BIO101").getTitle());
		try {
			cm.getCanonicalCourse("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}

	public void testGetEquivalentCanonicalCourses() throws Exception {
		Set equivalents = cm.getEquivalentCanonicalCourses("BIO101");
		Assert.assertEquals(1, equivalents.size());
		Assert.assertTrue(!equivalents.contains(cm.getCanonicalCourse("BIO101")));
		try {
			cm.getEquivalentCanonicalCourses("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}

	public void testGetCanonicalCoursesFromCourseSet() throws Exception {
		Assert.assertEquals(1, cm.getCanonicalCourses("BIO_DEPT").size());
		Assert.assertEquals(2, cm.getCanonicalCourses("BIO_CHEM_GROUP").size());
		try {
			cm.getCanonicalCourses("bad eid");
			fail();
		} catch (IdNotFoundException ide) {}
	}

	public void testGetCourseOfferingsFromCourseSet() throws Exception {
		Assert.assertEquals(1, cm.getCourseOfferingsInCourseSet("BIO_DEPT").size());
		Assert.assertEquals(2, cm.getCourseOfferingsInCourseSet("BIO_CHEM_GROUP").size());
		try {
			cm.getCourseOfferingsInCourseSet("bad eid");
			fail();
		} catch (IdNotFoundException ide) {}
	}

	public void testGetCourseOfferingsFromCanonicalCourse() throws Exception {
		Assert.assertEquals(1, cm.getCourseOfferingsInCanonicalCourse("BIO101").size());
		try {
			cm.getCourseOfferingsInCanonicalCourse("bad eid");
			fail();
		} catch (IdNotFoundException ide) {}
	}

	public void testGetCourseOffering() throws Exception {
		Assert.assertNotNull(cm.getCourseOffering("BIO101_F2006_01"));
		try {
			cm.getCourseOffering("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}

	public void testGetEquivalentCourseOfferings() throws Exception {
		Set equivalents = cm.getEquivalentCourseOfferings("BIO101_F2006_01");
		Assert.assertEquals(1, equivalents.size());
		try {
			cm.getEquivalentCourseOfferings("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}
	
	public void testGetSectionByEid() throws Exception {
		Assert.assertNotNull(cm.getSection("BIO101_F2006_01_SEC01"));
	}

	public void testGetSectionMembers() throws Exception {
		Assert.assertEquals(1, cm.getSectionMemberships("BIO101_F2006_01_SEC01").size());
		try {
			cm.getSectionMemberships("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}

	public void testGetSectionsFromCourseOffering() throws Exception {
		Assert.assertEquals(1, cm.getSections("BIO101_F2006_01").size());
		try {
			cm.getSections("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}
	
	public void testGetChildSections() throws Exception {
		Assert.assertEquals(1, cm.getChildSections("BIO101_F2006_01_SEC01").size());
		try {
			cm.getChildSections("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}
	
	public void testGetEnrollmentSet() throws Exception {
		Assert.assertNotNull(cm.getEnrollmentSet("BIO101_F2006_01_ES01"));
		try {
			cm.getEnrollmentSet("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}

	public void testGetEnrollmentSetFromCourseOffering() throws Exception {
		Assert.assertEquals(1, cm.getEnrollmentSets("BIO101_F2006_01").size());
		try {
			cm.getEnrollmentSets("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}

	public void testGetEnrollments() throws Exception {
		Assert.assertEquals(1, cm.getEnrollments("BIO101_F2006_01_ES01").size());
		try {
			cm.getEnrollmentSets("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}
	
	public void testFindEnrolledSections() throws Exception {
		// one of the two enrollment records is flagged as 'dropped'
		Assert.assertEquals(1, cm.findEnrolledSections("josh").size());
	}

	public void testGetEnrollment() throws Exception {
		Assert.assertNotNull(cm.findEnrollment("josh", "BIO101_F2006_01_ES01"));
		Assert.assertNotNull(cm.findEnrollment("josh", "CHEM101_F2006_01_ES01"));
		Assert.assertNull(cm.findEnrollment("josh", "bad eid"));
	}

	public void testGetOfficialGraders() throws Exception {
		Set graders = cm.getInstructorsOfRecordIds("BIO101_F2006_01_ES01");
		Assert.assertTrue(graders.contains("grader1"));
		Assert.assertTrue(graders.contains("grader2"));
		Assert.assertTrue( ! graders.contains("josh"));
		
		try {
			cm.getInstructorsOfRecordIds("bad eid");
			fail();
		} catch(IdNotFoundException ide) {}
	}
	
	public void testIsEnrolled() throws Exception {
		Set enrollmentSetEids = new HashSet();
		enrollmentSetEids.add("BIO101_F2006_01_ES01");
		
		// We don't care about bad EnrollmentSet eids here... we're just interested in Enrollments
		enrollmentSetEids.add("bad eid");

		Assert.assertTrue(cm.isEnrolled("josh", enrollmentSetEids));
		
		// Graders are not enrolled
		Assert.assertTrue( ! cm.isEnrolled("grader1", enrollmentSetEids));
		Assert.assertTrue( ! cm.isEnrolled("grader2", enrollmentSetEids));
	}
	
	public void testGetEnrolledEnrollmentSets() throws Exception {
		// User "josh" is enrolled in two EnrollmentSets.  One is only current in 2036.
		// The other is always current.
		Set enrSets = cm.findCurrentlyEnrolledEnrollmentSets("josh");
		Assert.assertEquals(1, enrSets.size());
	}
	
	public void testGetGradableEnrollmentSets() throws Exception {
		Set gradableEnrollmentSets = cm.findCurrentlyInstructingEnrollmentSets("grader1");
		Assert.assertEquals(1, gradableEnrollmentSets.size());
	}

	public void testFindInstructingSections() throws Exception {
		Section section = (Section)cm.getSections("BIO101_F2006_01").iterator().next();
		log.debug(section.getTitle() + " contains these instructors: " + section.getEnrollmentSet().getOfficialInstructors());
		Set sections = cm.findInstructingSections("grader1");
		Assert.assertEquals(1, sections.size());
	}

	public void testFindInstructingSectionsByAcademicSession() throws Exception {
		Set sections = cm.findInstructingSections("grader1", "F2006");
		Assert.assertEquals(1, sections.size());
	}
	
	public void testGetCourseOfferingsByCourseSetAndAcademicSession() throws Exception {
		Assert.assertEquals(1, cm.findCourseOfferings("BIO_DEPT", "F2006").size());
	}

	public void testIsCourseSetEmpty() throws Exception {
		Assert.assertTrue(cm.isEmpty("EMPTY_COURSE_SET"));
		Assert.assertFalse(cm.isEmpty("BIO_DEPT"));
		Assert.assertFalse(cm.isEmpty("BIO_CHEM_GROUP"));
	}
	
	public void testFindCourseSetByCategory() throws Exception {
		List courseSets = cm.findCourseSets("DEPT");
		Assert.assertEquals(1, courseSets.size());
		Assert.assertEquals("BIO_DEPT", ((CourseSet)courseSets.get(0)).getEid());
	}

	public void testFindSectionRoles() throws Exception {
		Map joshMap = cm.findSectionRoles("josh");
		// This user is both enrolled and has a membership.  This method only returns membership roles.
		Assert.assertEquals("student", joshMap.get("CHEM101_F2006_01_SEC01"));
		
		Map entMap = cm.findSectionRoles("AN_ENTERPRISE_USER");
		Assert.assertEquals("AN_ENTERPRISE_ROLE", entMap.get("BIO101_F2006_01_SEC01"));
	}
	
	public void testFindCourseOfferingRoles() throws Exception {
		Map coUserMap = cm.findCourseOfferingRoles("coUser");
		Assert.assertEquals("coRole1", coUserMap.get("BIO101_F2006_01"));
		Assert.assertEquals("coRole2", coUserMap.get("CHEM101_F2006_01"));
	}

	public void testFindCourseSetRoles() throws Exception {
		Map deptAdminMap = cm.findCourseSetRoles("user1");
		Assert.assertEquals("departmentAdmin", deptAdminMap.get("BIO_DEPT"));
	}
	
	public void testFindCategories() throws Exception {
		List categories = cm.getSectionCategories();
		Assert.assertEquals(3, categories.size());
	}

	public void testFindCategoryDescription() throws Exception {
		Assert.assertEquals("Lecture", cm.getSectionCategoryDescription("lct"));
	}
	
}
