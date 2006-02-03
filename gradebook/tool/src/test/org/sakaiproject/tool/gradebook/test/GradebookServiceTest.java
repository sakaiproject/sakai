/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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
package org.sakaiproject.tool.gradebook.test;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;

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

        // Add an internal assignment
        Long gbId = gradebookManager.getGradebook(GRADEBOOK_UID).getId();
        asn_1Id = gradeManager.createAssignment(gbId, ASN_1, new Double(10), null, Boolean.FALSE);

        // Add a score for the internal assignment
        List assignments = gradeManager.getAssignments(gbId);
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
        gradeManager.updateAssignmentGradeRecords(gradeRecordSet);
    }

    /**
     * Tests the gradebook service.
     *
     * @throws Exception
     */
    public void testCreateExternalAssessment() throws Exception {
        Assert.assertTrue(gradebookService.gradebookExists(GRADEBOOK_UID));
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Make sure that internal name conflicts are detected
        boolean exceptionThrown = false;
        try {
            gradebookService.addExternalAssessment(GRADEBOOK_UID, "A unique external id", null, ASN_1, 10, new Date(), "Samigo");
        } catch (ConflictingAssignmentNameException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Make sure that external name conflicts are detected
        exceptionThrown = false;
        try {
            gradebookService.addExternalAssessment(GRADEBOOK_UID, "Another unique external id", null, EXT_TITLE_1, 10, new Date(), "Samigo");
        } catch (ConflictingAssignmentNameException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Make sure that external id conflicts are detected
        exceptionThrown = false;
        try {
            gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, "A unique title", 10, new Date(), "Samigo");
        } catch (ConflictingExternalIdException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Test a floating value.
        double floatingPoints = 10.66666;
        String floatingExtId = "Just another external ID";
        gradebookService.addExternalAssessment(GRADEBOOK_UID, floatingExtId, null, "AFractionalAssessment", floatingPoints, new Date(), "Samigo");

        // Find the assessment and ensure that it has been updated
        Long gbId = gradebookManager.getGradebook(GRADEBOOK_UID).getId();
        Assignment asn = null;
        List assignments = gradeManager.getAssignments(gbId);
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
        Assert.assertTrue(gradebookService.gradebookExists(GRADEBOOK_UID));
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");
        gradebookService.updateExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 20, null);

        // Find the assessment and ensure that it has been updated
        Long gbId = gradebookManager.getGradebook(GRADEBOOK_UID).getId();
        Assignment asn = null;
        List assignments = gradeManager.getAssignments(gbId);
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment tmp = (Assignment)iter.next();
            if(tmp.getExternalId() != null && tmp.getExternalId().equals(EXT_ID_1)) {
                asn = tmp;
                break;
            }
        }
        Assert.assertEquals(asn.getPointsPossible(), new Double(20));

        // Ensure that the total points possible in the gradebook reflects the updated assessment's points
        Assert.assertTrue(gradeManager.getTotalPoints(gbId) == 30);
    }

    public void testCreateExternalGradeRecords() throws Exception {
    	// Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Add the external assessment score
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID);
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXT_ID_1, "student1", new Double(5));

        // Ensure that the course grade record for student1 has been updated
        CourseGradeRecord cgr = gradeManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(15))); // 10 points on internal, 5 points on external
    }

    public void testModifyExternalGradeRecords() throws Exception {
        // Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Add the external assessment score
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID);
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXT_ID_1, "student1", new Double(2));

        // Ensure that the course grade record for student1 has been updated
        CourseGradeRecord cgr = gradeManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(12))); // 10 points on internal, 2 points on external
    }

    public void testRemoveExternalAssignment() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID);

        // Add an external assessment
        gradebookService.addExternalAssessment(GRADEBOOK_UID, EXT_ID_1, null, EXT_TITLE_1, 10, new Date(), "Samigo");

        // Add the external assessment score
        gradebookService.updateExternalAssessmentScore(GRADEBOOK_UID, EXT_ID_1, "student1", new Double(5));
        CourseGradeRecord cgr = gradeManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(15)));// 10 points on internal, 10 points on external

        // Remove the external assessment
        gradebookService.removeExternalAssessment(GRADEBOOK_UID, EXT_ID_1);

        // Ensure that the course grade record for student1 has been updated
        cgr = gradeManager.getStudentCourseGradeRecord(gb, "student1");
        Assert.assertTrue(cgr.getPointsEarned().equals(new Double(10)));// 10 points on internal, 0 points on external
    }

    public void testDeleteGradebook() throws Exception {
    	gradebookService.deleteGradebook(GRADEBOOK_UID);
    	Assert.assertFalse(gradebookService.gradebookExists(GRADEBOOK_UID));
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
}
