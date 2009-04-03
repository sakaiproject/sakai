/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2008 The Sakai Foundation
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
import java.util.Set;

import junit.framework.Assert;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Site;

/**
 * Test the default configuration of the Course Management Group provider.
 */
public class DefaultConfigurationTest extends ConfigurationTestBase {
	private static String officialInstructorOfA = "officialInstructorOfA";
	private static String unofficialInstructorOfA = "unofficialInstructorOfA";
	private static String adminOfOfferingA = "adminOfOfferingA";
	private static String instructorOfB = "instructorOfB";
	private static String deptAdminOfADept = "deptAdminOfADept";
	private static String deptAdminAndGsi = "deptAdminAndGsi";
	private static String deptAdminOfBDept = "deptAdminOfBDept";
	private static String waitListedStudentALec1 = "waitListedStudentALec1";
	private static String studentADis1 = "studentADis1";
	private static String enrolledStudentBLec1 = "enrolledStudentBLec1";
	private static String droppedStudentALec1 = "droppedStudentALec1";
	private static String expelledStudentALec1 = "expelledStudentALec1";
	private static String unofficialStudentALec1 = "unofficialStudentALec1";
	private static String gsiALec1 = "gsiALec1";
	
	private static String term1 = "term1";
	private static String courseOfferingA = "courseOfferingA";
	private static String courseOfferingB = "courseOfferingB";
	private static String deptA = "deptA";
	private static String deptB = "deptB";
	private static String canonicalA = "canonicalA";
	private static String canonicalB = "canonicalB";
	private static String enrollmentALec1 = "enrollmentALec1";
	private static String sectionALec1 = "sectionALec1";
	private static String sectionADis1 = "sectionADis1";
	private static String enrollmentBLec1 = "enrollmentBLec1";
	private static String sectionBLec1 = "sectionBLec1";
	private static String sectionCanceled = "sectionCanceled";

	static {
		setSakaiHome("default");
	}

	protected void onSetUp() throws Exception {
		super.onSetUp();
		
		/*
			What we need to test:
			
			The Sakai authz side will be interested in three types of roles: Instructor, Teaching Assistant, and Student.
			
			From the CM side, a section's official instructor will get the "Instructor" role.
			
			Users with an enrollment status of "enrolled" or "wait" will get the "Student" role. Other enrollment statuses
			("expelled", etc.,) will result in non-membership. A dropped status will result in non-membership.
			
			Remaining users who are members of the section will be mapped "I" to "Instructor", "S" to "Student", and
			"GSI" to "Teaching Assistant". Other types of membership will be discarded.
			
			At the CourseOffering level, "CourseAdmin" or "I" members will get the "Instructor" role.
			
			At the CourseSet level, "DeptAdmin" members will get the "Instructor" role. No one else gets anything.
			
			When a user is mapped to multiple roles, they're preferred in the order: "Instructor", "Teaching Assistant", "Student".
		*/

		addUser(officialInstructorOfA);
		addUser(unofficialInstructorOfA);
		addUser(adminOfOfferingA);
		addUser(instructorOfB);
		addUser(deptAdminOfADept);
		addUser(deptAdminAndGsi);
		addUser(deptAdminOfBDept);
		addUser(waitListedStudentALec1);
		addUser(studentADis1);
		addUser(enrolledStudentBLec1);
		addUser(droppedStudentALec1);
		addUser(expelledStudentALec1);
		addUser(unofficialStudentALec1);
		addUser(gsiALec1);
		
		courseManagementAdmin.createCourseSet(deptA, deptA, deptA, "DEPT", null);
		courseManagementAdmin.addOrUpdateCourseSetMembership(deptAdminOfADept, "DeptAdmin", deptA, "active");
		courseManagementAdmin.addOrUpdateCourseSetMembership(deptAdminAndGsi, "DeptAdmin", deptA, "active");
		courseManagementAdmin.createCourseSet(deptB, deptB, deptB, "DEPT", null);
		courseManagementAdmin.addOrUpdateCourseSetMembership(deptAdminOfBDept, "DeptAdmin", deptB, "active");
		courseManagementAdmin.createAcademicSession(term1, term1, term1, null, null);
		courseManagementAdmin.createCanonicalCourse(canonicalA, canonicalA, canonicalA);
		courseManagementAdmin.createCanonicalCourse(canonicalB, canonicalB, canonicalB);
		courseManagementAdmin.createCourseOffering(courseOfferingA, courseOfferingA, courseOfferingA, "open", term1, canonicalA, null, null);
		courseManagementAdmin.addOrUpdateCourseOfferingMembership(adminOfOfferingA, "CourseAdmin", courseOfferingA, null);
		courseManagementAdmin.createCourseOffering(courseOfferingB, courseOfferingB, courseOfferingB, "open", term1, canonicalB, null, null);
		courseManagementAdmin.addCourseOfferingToCourseSet(deptA, courseOfferingA);
		courseManagementAdmin.addCourseOfferingToCourseSet(deptB, courseOfferingB);
		
		Set<String> officialInstructors = new HashSet<String>();
		officialInstructors.add(officialInstructorOfA);
		courseManagementAdmin.createEnrollmentSet(enrollmentALec1, enrollmentALec1, enrollmentALec1, "lecture", "4", courseOfferingA, officialInstructors);
		courseManagementAdmin.createEnrollmentSet(enrollmentBLec1, enrollmentBLec1, enrollmentBLec1, "lecture", "4", courseOfferingB, null);
		
		courseManagementAdmin.createSection(sectionCanceled, sectionCanceled, sectionCanceled, "lecture", null, courseOfferingA, null);
		courseManagementAdmin.createSection(sectionALec1, sectionALec1, sectionALec1, "lecture", null, courseOfferingA, enrollmentALec1);
		courseManagementAdmin.createSection(sectionADis1, sectionADis1, sectionADis1, "discussion", null, courseOfferingA, null);
		courseManagementAdmin.createSection(sectionBLec1, sectionBLec1, sectionBLec1, "lecture", null, courseOfferingB, enrollmentBLec1);
		
		courseManagementAdmin.addOrUpdateCourseOfferingMembership(unofficialInstructorOfA, "I", courseOfferingA, "active");
		
		courseManagementAdmin.addOrUpdateEnrollment(waitListedStudentALec1, enrollmentALec1, "wait", "4", "letter");
		courseManagementAdmin.addOrUpdateEnrollment(enrolledStudentBLec1, enrollmentBLec1, "enrolled", "4", "letter");
		courseManagementAdmin.addOrUpdateEnrollment(droppedStudentALec1, enrollmentALec1, "enrolled", "4", "letter");
		courseManagementAdmin.removeEnrollment(droppedStudentALec1, enrollmentALec1);
		courseManagementAdmin.addOrUpdateEnrollment(expelledStudentALec1, enrollmentALec1, "expelled", "4", "letter");
		
		courseManagementAdmin.addOrUpdateSectionMembership(studentADis1, "S", sectionADis1, "active");
		courseManagementAdmin.addOrUpdateSectionMembership(gsiALec1, "GSI", sectionALec1, "active");
		courseManagementAdmin.addOrUpdateSectionMembership(deptAdminAndGsi, "GSI", sectionALec1, "active");
		courseManagementAdmin.addOrUpdateSectionMembership(instructorOfB, "I", sectionBLec1, "active");
		
		// Now let's make some course sites.
		Site site = siteService.addSite(courseOfferingA, "course");
		site.setProviderGroupId(groupProvider.packId(new String[] {sectionCanceled, sectionALec1, sectionADis1}));
		siteService.save(site);
		
		// As icing on the cake, remove one of the sections. SAK-14713
		courseManagementAdmin.removeSection(sectionCanceled);

		// Add an unofficial student to the site. The save will wipe out the provided users!
		AuthzGroup authzGroup = authzGroupService.getAuthzGroup(site.getReference());
		authzGroup.addMember(unofficialStudentALec1, "Student", true, false);
		authzGroupService.save(authzGroup);

		site = siteService.addSite(sectionALec1, "course");
		site.setProviderGroupId(sectionALec1);
		siteService.save(site);

		site = siteService.addSite(courseOfferingB, "course");
		site.setProviderGroupId(sectionBLec1);
		siteService.save(site);
	}

	public void testSiteRoles() throws Exception {
		actAsUserEid(officialInstructorOfA);
		Site site = siteService.getSite(courseOfferingA);
		
		// Check for the officially enrolled (but waitlisted) student.
		Member member = site.getMember(waitListedStudentALec1);
		Assert.assertNotNull(member);
		Assert.assertEquals("Student", member.getRole().getId());		

		// Check for the student who was added directly to the site.
		member = site.getMember(unofficialStudentALec1);
		Assert.assertNotNull(member);
		Assert.assertEquals("Student", member.getRole().getId());

		// Check for the expelled student.
		member = site.getMember(expelledStudentALec1);
		Assert.assertNull(member);
		
		// Check for the dropped student.
		member = site.getMember(droppedStudentALec1);
		Assert.assertNull(member);
		
		// Check for the student in a discussion section.
		member = site.getMember(studentADis1);
		Assert.assertNotNull(member);
		Assert.assertEquals("Student", member.getRole().getId());
		
		// Check for a student in another course site.
		member = site.getMember(enrolledStudentBLec1);
		Assert.assertNull(member);
		
		// Check for the TA.
		member = site.getMember(gsiALec1);
		Assert.assertNotNull(member);
		Assert.assertEquals("Teaching Assistant", member.getRole().getId());
		
		// Check for the TA who also happens to be a DeptAdmin.
		member = site.getMember(deptAdminAndGsi);
		Assert.assertNotNull(member);
		Assert.assertEquals("Instructor", member.getRole().getId());
		
		// Check for the official instructor of the enrollment set.
		member = site.getMember(officialInstructorOfA);
		Assert.assertNotNull(member);
		Assert.assertEquals("Instructor", member.getRole().getId());
		
		// Check for the course offering member in the instructor role.
		member = site.getMember(unofficialInstructorOfA);
		Assert.assertNotNull(member);
		Assert.assertEquals("Instructor", member.getRole().getId());
		
		// Check for the department administrator.
		member = site.getMember(deptAdminOfADept);
		Assert.assertNotNull(member);
		Assert.assertEquals("Instructor", member.getRole().getId());
		
		// Check for the course admin.
		member = site.getMember(adminOfOfferingA);
		Assert.assertNotNull(member);
		Assert.assertEquals("Instructor", member.getRole().getId());
		
		// Check for an instructor in another course site.
		member = site.getMember(instructorOfB);
		Assert.assertNull(member);
		
		// Check for another department's administrator.
		member = site.getMember(deptAdminOfBDept);
		Assert.assertNull(member);
	}

}
