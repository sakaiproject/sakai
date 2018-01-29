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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.test;

import java.sql.Time;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdExistsException;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.coursemanagement.impl.EnrollmentCmImpl;

public class CourseManagementAdministrationTest extends CourseManagementTestBase {
	@Autowired
	private CourseManagementService cm;
	
	@Autowired
	private CourseManagementAdministration cmAdmin;
	
	@Test
	public void testCreateAcademicSession() throws Exception {
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		Assert.assertTrue(cm.getAcademicSession("as1").getTitle().equals("academic session 1"));
		
		try {
			cmAdmin.createAcademicSession("as1", "foo", "foo", null, null);
			Assert.fail();
		} catch (IdExistsException ide) {}
	}

	@Test
	public void testCreateCanonicalCourse() throws Exception {
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		Assert.assertTrue(cm.getCanonicalCourse("cc1").getTitle().equals("cc 1"));
		
		try {
			cmAdmin.createCanonicalCourse("cc1", "another canon course", "another canonical course");
			Assert.fail();
		} catch (IdExistsException ide) {}
	}

	@Test
	public void testCreateCourseOffering() throws Exception {
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "co 1", "a course offering", "open", "as1","cc1",  null, null);
		Assert.assertTrue(cm.getCourseOffering("co1").getTitle().equals("co 1"));
		
		try {
			cmAdmin.createCourseOffering("co1", "another course", "another course", "open", "as1", "cc1", null, null);
			Assert.fail();
		} catch (IdExistsException ide) {}
	}

	@Test
	public void testCreateCourseSet() throws Exception {
		cmAdmin.createCourseSet("cs1", "set 1", "a course set", null, null);
		Assert.assertTrue(cm.getCourseSet("cs1").getTitle().equals("set 1"));
		
		try {
			cmAdmin.createCourseSet("cs1", "another set 1", "another cset", null, null);
			Assert.fail();
		} catch (IdExistsException ide) {}
	}

	@Test
	public void testAddCanonicalCourseToCourseSet() throws Exception {
		cmAdmin.createCourseSet("cs1", "course set", "course set", null, null);
		cmAdmin.createCanonicalCourse("cc1", "canon course 1", "canon course");
		cmAdmin.addCanonicalCourseToCourseSet("cs1", "cc1");
		CanonicalCourse cc = cm.getCanonicalCourse("cc1");
		Assert.assertTrue(cm.getCanonicalCourses("cs1").contains(cc));
	}
	
	@Test
	public void testRemoveCanonicalCourseFromCourseSet() throws Exception {
		cmAdmin.createCourseSet("cs1", "course set", "course set", null, null);
		cmAdmin.createCanonicalCourse("cc1", "canon course 1", "canon course");
		cmAdmin.addCanonicalCourseToCourseSet("cs1", "cc1");
		cmAdmin.removeCanonicalCourseFromCourseSet("cs1", "cc1");
		CanonicalCourse cc = cm.getCanonicalCourse("cc1");
		Assert.assertFalse(cm.getCanonicalCourses("cs1").contains(cc));
	}

	@Test
	public void testAddCourseOfferingToCourseSet() throws Exception {
		cmAdmin.createCourseSet("cs1", "course set", "course set", null, null);
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		cmAdmin.addCourseOfferingToCourseSet("cs1", "co1");
		CourseOffering co = cm.getCourseOffering("co1");
		Assert.assertTrue(cm.getCourseOfferingsInCourseSet("cs1").contains(co));
	}
	
	@Test
	public void testRemoveCourseOfferingFromCourseSet() throws Exception {
		cmAdmin.createCourseSet("cs1", "course set", "course set",null,  null);
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		cmAdmin.addCourseOfferingToCourseSet("cs1", "co1");
		cmAdmin.removeCourseOfferingFromCourseSet("cs1", "co1");
		CourseOffering co = cm.getCourseOffering("co1");
		Assert.assertFalse(cm.getCourseOfferingsInCourseSet("cs1").contains(co));
	}

	@Test
	public void testSetEquivalentCanonicalCourses() throws Exception {
		// Create some courses
		cmAdmin.createCanonicalCourse("cc1", "cc1", "cc1");
		cmAdmin.createCanonicalCourse("cc2", "cc2", "cc2");
		cmAdmin.createCanonicalCourse("cc3", "cc3", "cc3");
		
		// Add them to a set
		Set<CanonicalCourse> courses = new HashSet<CanonicalCourse>();
		courses.add(cm.getCanonicalCourse("cc1"));
		courses.add(cm.getCanonicalCourse("cc2"));
		courses.add(cm.getCanonicalCourse("cc3"));

		// Crosslist them
		cmAdmin.setEquivalentCanonicalCourses(courses);
		
		// Ensure that CM sees them as crosslisted
		Set<CanonicalCourse> equivalents = cm.getEquivalentCanonicalCourses("cc1");
		Assert.assertTrue(equivalents.contains(cm.getCanonicalCourse("cc2")));
		Assert.assertTrue(equivalents.contains(cm.getCanonicalCourse("cc3")));
		
		// Ensure that we can remove one of the equivalents
		courses.remove(cm.getCanonicalCourse("cc3"));
		cmAdmin.setEquivalentCanonicalCourses(courses);
		equivalents = cm.getEquivalentCanonicalCourses("cc1");
		Assert.assertTrue(equivalents.contains(cm.getCanonicalCourse("cc2")));
		Assert.assertFalse(equivalents.contains(cm.getCanonicalCourse("cc3")));
	}
	
	@Test
	public void testRemoveEquivalencyCanonCourse() throws Exception {
		// Create some courses
		cmAdmin.createCanonicalCourse("cc1", "cc1", "cc1");
		cmAdmin.createCanonicalCourse("cc2", "cc2", "cc2");
		cmAdmin.createCanonicalCourse("cc3", "cc3", "cc3");
		
		// Add them to a set
		Set<CanonicalCourse> courses = new HashSet<CanonicalCourse>();
		courses.add(cm.getCanonicalCourse("cc1"));
		courses.add(cm.getCanonicalCourse("cc2"));

		// Crosslist them
		cmAdmin.setEquivalentCanonicalCourses(courses);
		
		// Remove a course that was crosslisted
		Assert.assertTrue(cmAdmin.removeEquivalency(cm.getCanonicalCourse("cc1")));
		
		// Remove one that wasn't crosslisted
		Assert.assertFalse(cmAdmin.removeEquivalency(cm.getCanonicalCourse("cc3")));
	}

	@Test
	public void testSetEquivalentCourseOfferings() throws Exception {
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createAcademicSession("as2", "academic session 2", "another academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc1", "cc1");
		cmAdmin.createCourseOffering("co1", "co1", "co1", "", "as1", "cc1", null, null);
		cmAdmin.createCourseOffering("co2", "co2", "co2", "", "as2", "cc1", null, null);
		
		// Add them to a set
		Set<CourseOffering> courses = new HashSet<CourseOffering>();
		courses.add(cm.getCourseOffering("co1"));
		courses.add(cm.getCourseOffering("co2"));

		// Crosslist them
		cmAdmin.setEquivalentCourseOfferings(courses);
		
		// Ensure that CM sees them as crosslisted
		Set<CourseOffering> equivalents = cm.getEquivalentCourseOfferings("co1");
		Assert.assertTrue(equivalents.contains(cm.getCourseOffering("co2")));
	}

	@Test
	public void testCreateEnrollmentSet() throws Exception {
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		cmAdmin.createEnrollmentSet("es1", "enr set 1", "an enr set", "lecture", "3", "co1", null);
		Assert.assertTrue(cm.getEnrollmentSet("es1").getTitle().equals("enr set 1"));
		
		try {
			cmAdmin.createEnrollmentSet("es1", "enr set 1", "an enr set", "lecture", "3", "co1", null);
			Assert.fail();
		} catch (IdExistsException ide) {}
	}

	@Test
	public void testAddEnrollment() throws Exception {
		// Create the EnrollmentSet
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		cmAdmin.createEnrollmentSet("es1", "enr set 1", "an enr set", "lecture", "3", "co1", null);
		
		// Add an enrollment
		cmAdmin.addOrUpdateEnrollment("josh", "es1", "enrolled", "4", "pass/fail");
		
		// Ensure that the enrollment exists
		Assert.assertNotNull(cm.findEnrollment("josh", "es1"));
	}

	@Test
	public void testUpdateEnrollment() throws Exception {
		// Create the EnrollmentSet
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		cmAdmin.createEnrollmentSet("es1", "enr set 1", "an enr set", "lecture", "3", "co1", null);
		
		// Add an enrollment
		cmAdmin.addOrUpdateEnrollment("josh", "es1", "enrolled", "4", "pass/fail");
		
		// Update the enrollment
		cmAdmin.addOrUpdateEnrollment("josh", "es1", "waitlisted", "3", "lettter gradel");
		
		// Ensure that the enrollment has been updated
		Assert.assertEquals("waitlisted", cm.findEnrollment("josh", "es1").getEnrollmentStatus());
	}

	@Test
	public void testDropEnrollment() throws Exception {
		// Create the EnrollmentSet
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		cmAdmin.createEnrollmentSet("es1", "enr set 1", "an enr set", "lecture", "3", "co1", null);
		
		// Add an enrollment
		cmAdmin.addOrUpdateEnrollment("josh", "es1", "enrolled", "4", "pass/fail");
		
		// Drop the enrollment
		cmAdmin.removeEnrollment("josh", "es1");
		
		// Ensure that the enrollment has been dropped
		Assert.assertTrue(((Enrollment)cm.getEnrollments("es1").iterator().next()).isDropped());
		
		// Add the same enrollment again
		cmAdmin.addOrUpdateEnrollment("josh", "es1", "enrolled", "4", "pass/fail");
		
		// Ensure that the hibernate version has been incremented
		Assert.assertNotSame(Integer.valueOf(0), ((EnrollmentCmImpl)cm.getEnrollments("es1").iterator().next()).getVersion());
	}
	
	@Test
	public void testAddCourseSetMembership() throws Exception {
		// Create a course set
		cmAdmin.createCourseSet("cs1", "cs1", "cs1", null, null);
		
		// Create a membership in the courseSet
		cmAdmin.addOrUpdateCourseSetMembership("josh", "student", "cs1", "active");
		
		// Ensure that the membership was added
		Assert.assertEquals(1, cm.getCourseSetMemberships("cs1").size());

		// Add the same username, this time with a different role
		cmAdmin.addOrUpdateCourseSetMembership("josh", "ta", "cs1","active");
		
		// Ensure that the membership was updated, not added
		Assert.assertEquals(1, cm.getCourseSetMemberships("cs1").size());
		Assert.assertEquals("ta", ((Membership)cm.getCourseSetMemberships("cs1").iterator().next()).getRole());
	}

	@Test
	public void testRemoveCourseSetMembers() throws Exception {
		// Create a course set
		cmAdmin.createCourseSet("cs1", "cs1", "cs1", null, null);
		
		// Create a membership in the courseSet
		cmAdmin.addOrUpdateCourseSetMembership("josh", "student", "cs1", "active");

		// Remove the membership (should return true)
		Assert.assertTrue(cmAdmin.removeCourseSetMembership("josh", "cs1"));
		
		// Try to remove it again (should return false)
		Assert.assertFalse(cmAdmin.removeCourseSetMembership("josh", "cs1"));
	}
	
	@Test
	public void testAddCourseOfferingMembership() throws Exception {
		// Create a course offering
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		
		// Create a membership in the courseOffering
		cmAdmin.addOrUpdateCourseOfferingMembership("josh", "student", "co1", "active");
		
		// Ensure that the membership was added
		Assert.assertEquals(1, cm.getCourseOfferingMemberships("co1").size());

		// Add the same username, this time with a different role
		cmAdmin.addOrUpdateCourseOfferingMembership("josh", "ta", "co1", "active");
		
		// Ensure that the membership was updated, not added
		Assert.assertEquals(1, cm.getCourseOfferingMemberships("co1").size());
		Assert.assertEquals("ta", ((Membership)cm.getCourseOfferingMemberships("co1").iterator().next()).getRole());
	}

	@Test
	public void testRemoveCourseOfferingMembers() throws Exception {
		// Create a course offering
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		
		// Create a membership in the courseOffering
		cmAdmin.addOrUpdateCourseOfferingMembership("josh", "student", "co1", "active");

		// Remove the membership (should return true)
		Assert.assertTrue(cmAdmin.removeCourseOfferingMembership("josh", "co1"));
		
		// Try to remove it again (should return false)
		Assert.assertFalse(cmAdmin.removeCourseOfferingMembership("josh", "co1"));
	}

	@Test
	public void testCreateSection() throws Exception {
		// Create a course offering
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);

		cmAdmin.createSection("sec1", "sec 1", "a sec", "lecture", null, "co1", null);
		Assert.assertTrue(cm.getSection("sec1").getTitle().equals("sec 1"));
		
		try {
			cmAdmin.createSection("sec1", "sec 1", "a sec", "lecture", null, null, null);
			Assert.fail();
		} catch (IdExistsException ide) {}
	}

	@Test
	public void testAddSectionMembership() throws Exception {
		// Create a course offering
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		
		// Add a section
		cmAdmin.createSection("sec1", "sec1", "sec1", "sec1", null, "co1", null);
		
		// Create a membership in the section
		cmAdmin.addOrUpdateSectionMembership("josh", "student", "sec1", "active");
		
		// Ensure that the membership was added
		Assert.assertEquals(1, cm.getSectionMemberships("sec1").size());

		// Add the same username, this time with a different role
		cmAdmin.addOrUpdateSectionMembership("josh", "ta", "sec1", "active");
		
		// Ensure that the membership was updated, not added
		Assert.assertEquals(1, cm.getSectionMemberships("sec1").size());
		Assert.assertEquals("ta", ((Membership)cm.getSectionMemberships("sec1").iterator().next()).getRole());
	}

	@Test
	public void testRemoveSectionMembers() throws Exception {
		// Create a course offering
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		
		// Add a section
		cmAdmin.createSection("sec1", "sec1", "sec1", "sec1", null, "co1", null);
		
		// Create a membership in the section
		cmAdmin.addOrUpdateSectionMembership("josh", "student", "sec1", "active");

		// Remove the membership (should return true)
		Assert.assertTrue(cmAdmin.removeSectionMembership("josh", "sec1"));
		
		// Try to remove it again (should return false)
		Assert.assertFalse(cmAdmin.removeSectionMembership("josh", "sec1"));
	}
	
	@Test
	public void testMeetingCascading() throws Exception {
		// Create a course offering
		cmAdmin.createAcademicSession("as1", "academic session 1", "an academic session", new Date(), new Date());
		cmAdmin.createCanonicalCourse("cc1", "cc 1", "a canon course");
		cmAdmin.createCourseOffering("co1", "course 1", "course", "open", "as1", "cc1", null, null);
		
		// Add a section
		Section section1 = cmAdmin.createSection("sec1", "sec1", "sec1", "sec1", null, "co1", null);
		
		// Add some meetings for the section
		Meeting meeting1 = cmAdmin.newSectionMeeting("sec1","a lecture hall", new Time(new Date().getTime()), new Time(new Date().getTime()), "If you're late, I won't let you in.");
		section1.getMeetings().add(meeting1);
		
		// Update the section
		cmAdmin.updateSection(section1);
		
		// Ensure that the section has the right meetings
		Section section2 = cm.getSection("sec1");
		Assert.assertEquals(1, section2.getMeetings().size());
		Assert.assertEquals("a lecture hall", ((Meeting)section2.getMeetings().iterator().next()).getLocation());
	}

	@Test
	public void testRemoveAcademicSession() throws Exception {
		cmAdmin.createAcademicSession("foo", "foo", "foo", null, null);
		
		// Ensure that the service can find the new AS
		Assert.assertEquals("foo", cm.getAcademicSession("foo").getTitle());
		
		// Remove the AS, and ensure that the service can no longer access it
		cmAdmin.removeAcademicSession("foo");
		try {
			cm.getAcademicSession("foo");
			Assert.fail();
		} catch (IdNotFoundException ide) {}
	}
	
	@Test
	public void testRemoveEnrollmentSet() throws Exception {
		cmAdmin.createAcademicSession("as", "as", "as", null, null);
		cmAdmin.createCanonicalCourse("cc", "cc", "cc");
		cmAdmin.createCourseOffering("co", "co", "co", "co", "as", "cc", null, null);
		cmAdmin.createEnrollmentSet("es", "es", "es", "es", "es", "co", null);
		cmAdmin.addOrUpdateEnrollment("student1","es","enrolled", "4", "letter grade");
		
		// Remove the ES
		cmAdmin.removeEnrollmentSet("es");
		
		// Ensure that the enrollment was deleted as well
		Assert.assertEquals(0, cm.getEnrollments("es").size());

		// Ensure that the CM service can no longer find the ES
		try {
			cm.getEnrollmentSet("es");
			Assert.fail();
		} catch (IdNotFoundException ide) {}
		
	}
	
	@Test
	public void testRemoveSection() throws Exception {
		cmAdmin.createAcademicSession("as", "as", "as", null, null);
		cmAdmin.createCanonicalCourse("cc", "cc", "cc");
		cmAdmin.createCourseOffering("co", "co", "co", "co", "as", "cc", null, null);
		cmAdmin.createSection("sec", "sec", "sec", "sec", null, "co", null);
		cmAdmin.addOrUpdateSectionMembership("member1", "TA", "sec", "active");
		
		// Remove the section
		cmAdmin.removeSection("sec");
		
		// Ensure that the CM service can no longer find the section
		try {
			cm.getSection("sec");
			Assert.fail();
		} catch (IdNotFoundException ide) {}
		
		// Ensure that the membership was deleted as well
		try {
			cm.getSectionMemberships("sec");
			Assert.fail();
		} catch (IdNotFoundException ide) {}
	}

	@Test
	public void testRemoveCourseOffering() throws Exception {
		cmAdmin.createAcademicSession("as", "as", "as", null, null);
		cmAdmin.createCanonicalCourse("cc", "cc", "cc");
		cmAdmin.createCourseOffering("co", "co", "co", "co", "as", "cc", null, null);
		cmAdmin.createEnrollmentSet("es", "es", "es", "es", "3", "co", null);
		cmAdmin.createSection("sec", "sec", "sec", "sec", null, "co", "es");
		
		// Remove the CO
		cmAdmin.removeCourseOffering("co");
		
		// Ensure that the CM service can no longer find the CO
		try {
			cm.getCourseOffering("co");
			Assert.fail();
		} catch (IdNotFoundException ide) {}

		// Ensure that the ES was deleted as well
		try {
			cm.getEnrollmentSet("es");
			Assert.fail();
		} catch (IdNotFoundException ide) {}

		// Ensure that the section was deleted as well
		try {
			cm.getSection("sec");
			Assert.fail();
		} catch (IdNotFoundException ide) {}
	}
	
	@Test
	public void testSetCurrentAcademicSessions() throws Exception {
		long oneWeekMs = 1000 * 60 * 60 * 24 * 7;
		long nowMs = System.currentTimeMillis();
		
		cmAdmin.createAcademicSession("previousTerm", "previous term", "", new Date(nowMs - (oneWeekMs * 2)), new Date(nowMs - oneWeekMs));
		cmAdmin.createAcademicSession("nowTerm", "now term", "", new Date(nowMs - oneWeekMs), new Date(nowMs + oneWeekMs));
		cmAdmin.createAcademicSession("nextTerm", "next term", "", new Date(nowMs + oneWeekMs), new Date(nowMs + (oneWeekMs * 2)));
		
		List<AcademicSession> academicSessions = cm.getCurrentAcademicSessions();
		Assert.assertEquals(0, academicSessions.size());		
		
		cmAdmin.setCurrentAcademicSessions(Arrays.asList(new String[] {"previousTerm"}));
		academicSessions = cm.getCurrentAcademicSessions();
		Assert.assertEquals(1, academicSessions.size());
		Assert.assertEquals("previousTerm", academicSessions.get(0).getEid());
		
		cmAdmin.setCurrentAcademicSessions(Arrays.asList(new String[] {"nowTerm", "nextTerm"}));
		academicSessions = cm.getCurrentAcademicSessions();
		Assert.assertEquals(2, academicSessions.size());
		Assert.assertEquals("nextTerm", academicSessions.get(0).getEid());
		Assert.assertEquals("nowTerm", academicSessions.get(1).getEid());
	}

}
