/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.tool.gradebook.test;

import java.util.*;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.test.AuthnTestImpl;

/**
 * Uses spring's mock-objects to test the gradebook service without modifying the database
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradebookServiceTest extends GradebookTestBase {

    private static final Log log = LogFactory.getLog(GradebookServiceTest.class);

    private static final String GRADEBOOK_UID = "gradebookServiceTest";
    private static final String ASN_1 = "Assignment #1";
    private static final String EXT_ID_1 = "External #1";
    private static final String EXT_TITLE_1 = "External Title #1";

    private Long asn_1Id;

    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();

        gradebookService.addGradebook(GRADEBOOK_UID, GRADEBOOK_UID);
        Gradebook gradebook = gradebookManager.getGradebook(GRADEBOOK_UID);

        // Set up a holder for enrollments, teaching assignments, and sections.
        integrationSupport.createCourse(GRADEBOOK_UID, GRADEBOOK_UID, false, false, false);

		List studentUidsList = Arrays.asList(new String[] {
			"student1",
			"student2",
			"student3",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        // Add an internal assignment
        Long gbId = gradebook.getId();
        asn_1Id = gradebookManager.createAssignment(gbId, ASN_1, new Double(10), null, Boolean.FALSE);

        // Add a score for the internal assignment
        List assignments = gradebookManager.getAssignments(gbId);
        Assignment asn = null;
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment tmp = (Assignment)iter.next();
            if(tmp.getName().equals(ASN_1)) {
                asn = tmp;
                break;
            }
        }
        GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "student1", new Double(10)));
        gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);
    }

    /**
     * Tests the gradebook service.
     *
     * @throws Exception
     */
    public void testCreateExternalAssessment() throws Exception {
        Assert.assertTrue(gradebookService.isGradebookDefined(GRADEBOOK_UID));
        
        // Make sure the service knows that the external id has not been defined
        Assert.assertFalse(gradebookService.isExternalAssignmentDefined(GRADEBOOK_UID, EXT_ID_1));
        
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Make sure the service knows that the external id has been defined
        Assert.assertTrue(gradebookService.isExternalAssignmentDefined(GRADEBOOK_UID, EXT_ID_1));

        // Make sure that internal name conflicts are detected
        try {
            gradebookService.addExternalAssessment(GRADEBOOK_UID, "A unique external id", null, ASN_1, 10, new Date(), "Samigo");
            fail();
        } catch (ConflictingAssignmentNameException e) {}

        // Make sure that external name conflicts are detected
        try {
            gradebookService.addExternalAssessment(GRADEBOOK_UID, "Another unique external id", null, EXT_TITLE_1, 10, new Date(), "Samigo");
            fail();
        } catch (ConflictingAssignmentNameException e) {}

        // Make sure that external id conflicts are detected
        try {
            gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, "A unique title", 10, new Date(), "Samigo");
            fail();
        } catch (ConflictingExternalIdException e) {}

        // Test a floating value.
        double floatingPoints = 10.66666;
        String floatingExtId = "Just another external ID";
        gradebookService.addExternalAssessment(GRADEBOOK_UID, floatingExtId, null, "AFractionalAssessment", floatingPoints, new Date(), "Samigo");

        // Find the assessment and ensure that it has been updated
        Long gbId = gradebookManager.getGradebook(GRADEBOOK_UID).getId();
        Assignment asn = null;
        List assignments = gradebookManager.getAssignments(gbId);
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment tmp = (Assignment)iter.next();
            if(tmp.getExternalId() != null && tmp.getExternalId().equals(floatingExtId)) {
                asn = tmp;
                break;
            }
        }
        Assert.assertEquals(asn.getPointsPossible(), new Double(floatingPoints));
    }

    public void testModifyExternalAssessment() throws Exception {
        Assert.assertTrue(gradebookService.isGradebookDefined(GRADEBOOK_UID));
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");
        gradebookService.updateExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 20, null);

        // Find the assessment and ensure that it has been updated
        Long gbId = gradebookManager.getGradebook(GRADEBOOK_UID).getId();
        Assignment asn = null;
        List assignments = gradebookManager.getAssignments(gbId);
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment tmp = (Assignment)iter.next();
            if(tmp.getExternalId() != null && tmp.getExternalId().equals(EXT_ID_1)) {
                asn = tmp;
                break;
            }
        }
        Assert.assertEquals(asn.getPointsPossible(), new Double(20));

        // Ensure that the total points possible in the gradebook reflects the updated assessment's points
        Assert.assertTrue(gradebookManager.getTotalPoints(gbId) == 30);
    }

    public void testCreateExternalGradeRecords() throws Exception {

        // Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Add the external assessment score
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID);
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXT_ID_1, "student1", new Double(5));

        // Ensure that the course grade record for student1 has been updated
        CourseGradeRecord cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(15))); // 10 points on internal, 5 points on external
    }

    public void testModifyExternalGradeRecords() throws Exception {
        // Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Add the external assessment score
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID);
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXT_ID_1, "student1", new Double(2));

        // Ensure that the course grade record for student1 has been updated
        CourseGradeRecord cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(12))); // 10 points on internal, 2 points on external
        
        // Update the score with null points
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXT_ID_1, "student1", null);

        // Ensure that the course grade record for student1 has been updated
        cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertEquals(new Double(10), cgr.getPointsEarned()); // 10 points on internal, 0 points on external
    }

    public void testUpdateMultipleScores() throws Exception {
        // Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Add the external assessment score
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID);
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXT_ID_1, "student1", new Double(2));

        // Ensure that the course grade record for student1 has been updated
        CourseGradeRecord cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(12))); // 10 points on internal, 2 points on external

        // Update multiple scores at once.
        Map studentUidsToScores = new HashMap();
        studentUidsToScores.put("student1", null);
        studentUidsToScores.put("student2", new Double(4));
        studentUidsToScores.put("student3", new Double(5));
        gradebookService.updateExternalAssessmentScores(gb.getUid(), EXT_ID_1, studentUidsToScores);
        cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(10)));
        cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student2");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(4)));

        // Do a bogus update of a null collection of scores, a la Assignments.
        gradebookService.updateExternalAssessmentScores(gb.getUid(), EXT_ID_1, new HashMap());
        cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student2");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(4)));
    }

    public void testRemoveExternalAssignment() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID);

        // Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Add the external assessment score
        gradebookService.updateExternalAssessmentScore(GRADEBOOK_UID, EXT_ID_1, "student1", new Double(5));
        CourseGradeRecord cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(15)));// 10 points on internal, 10 points on external

        // Remove the external assessment
        gradebookService.removeExternalAssessment(GRADEBOOK_UID, EXT_ID_1);

        // Ensure that the course grade record for student1 has been updated
        cgr = gradebookManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(10)));// 10 points on internal, 0 points on external
        
        // Try to add another external assessment with the same external ID as the recently deleted external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, "some other unique title", 10, new Date(), "Samigo");        
    }

    public void testDeleteGradebook() throws Exception {
    	gradebookService.deleteGradebook(GRADEBOOK_UID);
    	Assert.assertFalse(gradebookService.isGradebookDefined(GRADEBOOK_UID));
	}

	public void testIsAssignmentDefined() throws Exception {
		String assignmentTitle = "Is Assignment Defined Quiz";
		Assert.assertFalse(gradebookService.isAssignmentDefined(GRADEBOOK_UID, assignmentTitle));
		gradebookService.addExternalAssessment(GRADEBOOK_UID, "Is Assignment Defined ID", null, assignmentTitle, 10, new Date(), "Assignments");
		Assert.assertTrue(gradebookService.isAssignmentDefined(GRADEBOOK_UID, assignmentTitle));

        // Now test conflicts with an internally defined assignment.
        Assert.assertTrue(gradebookService.isAssignmentDefined(GRADEBOOK_UID, ASN_1));
        gradebookManager.removeAssignment(asn_1Id);
        Assert.assertFalse(gradebookService.isAssignmentDefined(GRADEBOOK_UID, ASN_1));
    }

    public void testExternalAssignmentWithZeroPoints() throws Exception {
        //add assignment to grade book
        try{
            gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 0, new Date(), "Samigo");
            fail();
        }catch(AssignmentHasIllegalPointsException e){}

        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");
        try{
            gradebookService.updateExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1,0, null);
            fail();
        } catch(AssignmentHasIllegalPointsException e) {}
    }

    public void testDuplicateExternalIds() throws Exception {
        // Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Try to add another external assessment with a duplicate external ID
        try {
            gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, "some unique title", 10, new Date(), "Samigo");
            fail();
        } catch (ConflictingExternalIdException e) {}

    }
}
