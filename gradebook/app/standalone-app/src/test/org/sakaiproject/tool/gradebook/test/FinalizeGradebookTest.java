/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Sakai Foundation
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

package org.sakaiproject.tool.gradebook.test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 *
 */
public class FinalizeGradebookTest extends GradebookTestBase {
	private static final Log log = LogFactory.getLog(FinalizeGradebookTest.class);

	private static final String GRADEBOOK_UID = "gradebookFinalize";
	private static final String INSTRUCTOR_UID = "Inst-1";
	private static final String TA_UID = "TA-1";
	private static final String SECTION_NAME = "Lab 01";
	private static final String STUDENT_ON_TIME = "StudentOnTime";
	private static final String STUDENT_TARDY = "StudentTardy";
	private static final String STUDENT_VERY_TARDY = "StudentVeryTardy";
	private static final String ASN_TITLE1 = "Assignment #1";
	private static final String ASN_TITLE2 = "Assignment #2";
	private static final String ASN_NOT_COUNTED = "Survey";
	private static final Double ASN_POINTS1 = new Double(40.0);
	private static final Double ASN_POINTS2 = new Double(60.0);
	private static final Double ASN_POINTS_NOT_COUNTED = new Double(300.0);
	private static final List<String> STUDENT_UIDS = Arrays.asList(new String[] {STUDENT_ON_TIME, STUDENT_TARDY, STUDENT_VERY_TARDY});
	private static Long gradebookId;
	private static Long asnId1 ;
	private static Long asnId2;
	private static Long asnIdNotCounted;


	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		
		userManager.createUser(INSTRUCTOR_UID, null, null, null);
		userManager.createUser(TA_UID, null, null, null);
		
		gradebookFrameworkService.addGradebook(GRADEBOOK_UID, GRADEBOOK_UID);
		Gradebook gradebook = gradebookManager.getGradebook(GRADEBOOK_UID);
		gradebookId = gradebook.getId();
		
		Course courseSite1 = integrationSupport.createCourse(GRADEBOOK_UID, GRADEBOOK_UID, false, false, false);
		addUsersEnrollments(gradebook, STUDENT_UIDS);
		
		integrationSupport.addSiteMembership(INSTRUCTOR_UID, GRADEBOOK_UID, Role.INSTRUCTOR);
		
		integrationSupport.addSiteMembership(TA_UID, GRADEBOOK_UID, Role.TA);
		List<String> sectionCategories1 = sectionAwareness.getSectionCategories(GRADEBOOK_UID);
		CourseSection section1 = integrationSupport.createSection(courseSite1.getUuid(), SECTION_NAME,
				(String)sectionCategories1.get(0),
				new Integer(40), null, null, null, true, false, true,  false, false, false, false);
		integrationSupport.addSectionMembership(STUDENT_ON_TIME, section1.getUuid(), Role.STUDENT);
		integrationSupport.addSectionMembership(TA_UID, section1.getUuid(), Role.TA);

		// Add internal assignments
		asnId1 = gradebookManager.createAssignment(gradebook.getId(), ASN_TITLE1, ASN_POINTS1, new Date(), Boolean.FALSE, Boolean.FALSE);
		asnId2 = gradebookManager.createAssignment(gradebook.getId(), ASN_TITLE2, ASN_POINTS2, new Date(), Boolean.FALSE, Boolean.TRUE);
		asnIdNotCounted = gradebookManager.createAssignment(gradebook.getId(), ASN_NOT_COUNTED, ASN_POINTS_NOT_COUNTED, new Date(), Boolean.TRUE, Boolean.TRUE);
		
	}
	
	public void testFinalizeGradebook() throws Exception {
		setAuthnId(INSTRUCTOR_UID);
		
		// One student gets scores of 0, 60, and 300, resulting in a D+.
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnId1, STUDENT_ON_TIME, "0", "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnId2, STUDENT_ON_TIME, "60", "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnIdNotCounted, STUDENT_ON_TIME, "300", "");
		
		// Another gets no score on the 40-point assignment, and 60 and 300 on the other two, resulting in an A+.
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnId2, STUDENT_TARDY, "60", "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnIdNotCounted, STUDENT_TARDY, "300", "");
		
		// A third gets explicit null scores (as they'd have if an erroneous score was reverted).
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnId1, STUDENT_VERY_TARDY, "40", "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnId2, STUDENT_VERY_TARDY, "60", "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnIdNotCounted, STUDENT_VERY_TARDY, "300", "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnId1, STUDENT_VERY_TARDY, null, "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnId2, STUDENT_VERY_TARDY, null, "");
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID, asnIdNotCounted, STUDENT_VERY_TARDY, null, "");
		
		Map<String, String> courseGradeMap = getCourseGradeMap(gradebookId, STUDENT_UIDS);
		assertEquals("D-", courseGradeMap.get(STUDENT_ON_TIME));
		assertEquals("A+", courseGradeMap.get(STUDENT_TARDY));
		assertNull(courseGradeMap.get(STUDENT_VERY_TARDY));
		
		// Make sure TAs can't finalize grades.
		setAuthnId(TA_UID);
		try {
			if (log.isInfoEnabled()) log.info("Ignore the upcoming authorization error...");
			gradebookService.finalizeGrades(GRADEBOOK_UID);
			fail();
		} catch (SecurityException e) {
		}
		
		// Finalize grades and make sure the tardy students have paid for their sins.
		setAuthnId(INSTRUCTOR_UID);
		gradebookService.finalizeGrades(GRADEBOOK_UID);
		courseGradeMap = getCourseGradeMap(gradebookId, STUDENT_UIDS);
		assertEquals("D-", courseGradeMap.get(STUDENT_ON_TIME));
		assertEquals("D-", courseGradeMap.get(STUDENT_TARDY));
		assertEquals("F", courseGradeMap.get(STUDENT_VERY_TARDY));
	}
	
	/**
	 * The GradebookService "getCalculatedCourseGrade" currently does not show the same letter grades as
	 * the application's Course Grade page. For now, we need to mimic the latter's logic.
	 * @param gradebookUid
	 * @return map from student UID to letter grade (or null)
	 */
	private Map<String, String> getCourseGradeMap(Long gradebookId, List<String> studentUids) {
		Map<String, String> courseGradeMap = new HashMap<String, String>();
		List<CourseGradeRecord> courseGradeRecords = 
			gradebookManager.getPointsEarnedCourseGradeRecords(gradebookManager.getCourseGrade(gradebookId), studentUids);
		Gradebook gradebook = gradebookManager.getGradebookWithGradeMappings(gradebookId);
		GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
		for (CourseGradeRecord courseGradeRecord : courseGradeRecords) {
			courseGradeMap.put(courseGradeRecord.getStudentId(), gradeMapping.getGrade(courseGradeRecord.getAutoCalculatedGrade()));
		}
		return courseGradeMap;
	}
}
