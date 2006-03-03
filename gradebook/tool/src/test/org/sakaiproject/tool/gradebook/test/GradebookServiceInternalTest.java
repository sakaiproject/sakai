/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://www.opensource.org/licenses/ecl1.php
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.gradebook.test;

import java.util.*;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.facade.Role;

import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Test the service methods which interact with internally maintained data.
 * These methods have more complex authorization requirements.
 */
public class GradebookServiceInternalTest extends GradebookTestBase {

    private static final Log log = LogFactory.getLog(GradebookServiceInternalTest.class);

    private static final String GRADEBOOK_UID = "gradebookServiceTest";
    private static final String ASN_TITLE = "Assignment #1";
    private static final String EXT_ID_1 = "External #1";
    private static final String EXT_TITLE_1 = "External Title #1";
    private static final String INSTRUCTOR_UID = "Inst-1";
    private static final String TA_UID = "TA-1";
    private static final String SECTION_NAME = "Lab 01";
    private static final String STUDENT_IN_SECTION_UID = "StudentInLab";
    private static final String STUDENT_NOT_IN_SECTION_UID = "StudentNotInLab";
    private static final Double ASN_POINTS = new Double(40.0);
//    private static final String  = "";

    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
 		gradebookService.addGradebook(GRADEBOOK_UID, GRADEBOOK_UID);
 		Gradebook gradebook = gradebookManager.getGradebook(GRADEBOOK_UID);

        // Set up users, enrollments, teaching assignments, and sections.
        Course courseSite = integrationSupport.createCourse(GRADEBOOK_UID, GRADEBOOK_UID, false, false, false);
		addUsersEnrollments(gradebook, Arrays.asList(new String[] {STUDENT_IN_SECTION_UID, STUDENT_NOT_IN_SECTION_UID}));
		userManager.createUser(INSTRUCTOR_UID, null, null, null);
		integrationSupport.addSiteMembership(INSTRUCTOR_UID, GRADEBOOK_UID, Role.INSTRUCTOR);
		userManager.createUser(TA_UID, null, null, null);
		integrationSupport.addSiteMembership(TA_UID, GRADEBOOK_UID, Role.TA);
		List sectionCategories = sectionAwareness.getSectionCategories(GRADEBOOK_UID);
		CourseSection section = integrationSupport.createSection(courseSite.getUuid(), SECTION_NAME,
			(String)sectionCategories.get(0),
			new Integer(40), null, null, null, true, false, true,  false, false, false, false);
		integrationSupport.addSectionMembership(STUDENT_IN_SECTION_UID, section.getUuid(), Role.STUDENT);
		integrationSupport.addSectionMembership(TA_UID, section.getUuid(), Role.TA);

        // Add an internal assignment.
        gradeManager.createAssignment(gradebook.getId(), ASN_TITLE, ASN_POINTS, new Date(), Boolean.FALSE);

        // Add an external assessment.
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, null, "Samigo");
    }

	public void testExternalClientSupport() throws Exception {
		setAuthnId(TA_UID);

		List assignments = gradebookService.getAssignments(GRADEBOOK_UID);
		log.info("assignments=" + assignments);
		Assert.assertTrue(assignments.size() == 2);

		for (Iterator iter = assignments.iterator(); iter.hasNext(); ) {
			Assignment assignment = (Assignment)iter.next();

			if (assignment.isExternallyMaintained()) {
				Assert.assertTrue(EXT_TITLE_1.equals(assignment.getName()));
				// Make sure we can't update it.
				boolean gotSecurityException = false;
				try {
					if (log.isInfoEnabled()) log.info("Ignore the upcoming authorization error...");
					gradebookService.setAssignmentScore(GRADEBOOK_UID, EXT_TITLE_1, STUDENT_IN_SECTION_UID, new Double(9), "Service Test");
				} catch (SecurityException e) {
					gotSecurityException = true;
				}
				Assert.assertTrue(gotSecurityException);
			} else {
				Assert.assertTrue(ASN_TITLE.equals(assignment.getName()));
				Assert.assertTrue(assignment.getPoints().equals(ASN_POINTS));

				Assert.assertFalse(gradebookService.isUserAbleToGradeStudent(GRADEBOOK_UID, STUDENT_NOT_IN_SECTION_UID));
				boolean gotSecurityException = false;
				try {
					if (log.isInfoEnabled()) log.info("Ignore the upcoming authorization error...");
					gradebookService.getAssignmentScore(GRADEBOOK_UID, ASN_TITLE, STUDENT_NOT_IN_SECTION_UID);
				} catch (SecurityException e) {
					gotSecurityException = true;
				}
				gotSecurityException = false;
				try {
					if (log.isInfoEnabled()) log.info("Ignore the upcoming authorization error...");
					gradebookService.setAssignmentScore(GRADEBOOK_UID, ASN_TITLE, STUDENT_NOT_IN_SECTION_UID, new Double(39), "Service Test");
				} catch (SecurityException e) {
					gotSecurityException = true;
				}
				Assert.assertTrue(gotSecurityException);

				Assert.assertTrue(gradebookService.isUserAbleToGradeStudent(GRADEBOOK_UID, STUDENT_IN_SECTION_UID));
				Double score = gradebookService.getAssignmentScore(GRADEBOOK_UID, ASN_TITLE, STUDENT_IN_SECTION_UID);
				Assert.assertTrue(score == null);
				gradebookService.setAssignmentScore(GRADEBOOK_UID, ASN_TITLE, STUDENT_IN_SECTION_UID, new Double(39), "Service Test");
				score = gradebookService.getAssignmentScore(GRADEBOOK_UID, ASN_TITLE, STUDENT_IN_SECTION_UID);
				Assert.assertTrue(score.doubleValue() == 39.0);
			}
		}
	}
}
