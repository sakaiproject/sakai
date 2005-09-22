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

import java.util.*;

import junit.framework.Assert;

import org.sakaiproject.api.section.facade.Role;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradeRecordSet;

/**
 */
public class GradableObjectManagerTest extends GradebookTestBase {
    protected static final String ASN1_NAME = "Assignment #1";
    protected static final String ASN2_NAME = "Assignment #2";
    protected static final String ASN3_NAME = "Assignment #3";

    protected Gradebook gradebook;

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();

        // Create a gradebook to work with
        String className = this.getClass().getName();
        String gradebookName = className + (new Date()).getTime();
        gradebookService.addGradebook(gradebookName, gradebookName);

        // Set up a holder for enrollments, teaching assignments, and sections.
        integrationSupport.createCourse(gradebookName, gradebookName, false, false, false);

        // Grab the gradebook for use in the tests
        gradebook = gradebookManager.getGradebook(gradebookName);
    }

    public void testCreateAndUpdateAssignment() throws Exception {
        Long asnId = gradeManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), new Date());;
        Assignment asn = (Assignment)gradeManager.getGradableObject(asnId);
        asn.setPointsPossible(new Double(20));
        gradeManager.updateAssignment(asn);

        // Fetch the updated assignment with statistics
        Assignment persistentAssignment = (Assignment)gradeManager.getGradableObjectWithStats(asnId, new HashSet());
        // Ensure the DB update was successful
        Assert.assertEquals(persistentAssignment.getPointsPossible(), new Double(20));

        // Try to save a new assignment with the same name
        boolean errorThrown = false;
        try {
            Long dupId = gradeManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(20), new Date());;
        } catch (ConflictingAssignmentNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);

        // Save a second assignment
        Long secondId = gradeManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(10), new Date());;
        Assignment asn2 = (Assignment)gradeManager.getGradableObject(secondId);

        errorThrown = false;

        // Try to update its name to that of the first
        asn2.setName(ASN1_NAME);
        try {
            gradeManager.updateAssignment(asn2);
        } catch (ConflictingAssignmentNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);
    }

    public void testGradableObjectSorting() throws Exception {
        // Create an assignment with a null date
        Long id1 = gradeManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null);

        // Create an assignment with an early date (in 1970)
        Long id2 = gradeManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10));

        // Create an assignment with a date of now
        Long id3 = gradeManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date());

        // Get lists of assignments with different sort orders
        List ascDateOrderedAssignments = gradeManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_DATE, true);
        List descDateOrderedAssignments = gradeManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_DATE, false);

        List ascNameOrderedAssignments = gradeManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_NAME, true);
        List descNameOrderedAssignments = gradeManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_NAME, false);

        List ascPointsOrderedAssignments = gradeManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_POINTS, true);
        List descPointsOrderedAssignments = gradeManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_POINTS, false);

        Assignment asn1 = (Assignment)gradeManager.getGradableObject(id1);
        Assignment asn2 = (Assignment)gradeManager.getGradableObject(id2);
        Assignment asn3 = (Assignment)gradeManager.getGradableObject(id3);

        // Ensure that the dates sort correctly
        Assert.assertTrue(ascDateOrderedAssignments.indexOf(asn2) < ascDateOrderedAssignments.indexOf(asn3));
        Assert.assertTrue(ascDateOrderedAssignments.indexOf(asn3) < ascDateOrderedAssignments.indexOf(asn1));
        Assert.assertTrue(descDateOrderedAssignments.indexOf(asn2) > descDateOrderedAssignments.indexOf(asn3));
        Assert.assertTrue(descDateOrderedAssignments.indexOf(asn3) > descDateOrderedAssignments.indexOf(asn1));

        // Ensure that the names sort correctly
        Assert.assertTrue(ascNameOrderedAssignments.indexOf(asn1) < ascNameOrderedAssignments.indexOf(asn2));
        Assert.assertTrue(ascNameOrderedAssignments.indexOf(asn2) < ascNameOrderedAssignments.indexOf(asn3));
        Assert.assertTrue(descNameOrderedAssignments.indexOf(asn1) > descNameOrderedAssignments.indexOf(asn2));
        Assert.assertTrue(descNameOrderedAssignments.indexOf(asn2) > descNameOrderedAssignments.indexOf(asn3));

        // Ensure that the points sort correctly
        Assert.assertTrue(ascPointsOrderedAssignments.indexOf(asn1) < ascPointsOrderedAssignments.indexOf(asn2));
        Assert.assertTrue(ascPointsOrderedAssignments.indexOf(asn2) < ascPointsOrderedAssignments.indexOf(asn3));
        Assert.assertTrue(descPointsOrderedAssignments.indexOf(asn1) > descPointsOrderedAssignments.indexOf(asn2));
        Assert.assertTrue(descPointsOrderedAssignments.indexOf(asn2) > descPointsOrderedAssignments.indexOf(asn3));
    }

    public void testDeletedAssignments() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
			"testStudentUserUid2",
			"testStudentUserUid3",
		});
		addUsersEnrollments(gradebook, studentUidsList);
		Set studentUids = new HashSet(studentUidsList);

        Long id1 = gradeManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null);
        Long id2 = gradeManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10));
        Long id3 = gradeManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date());

        List assignments = gradeManager.getAssignments(gradebook.getId());
        Assignment asn = (Assignment)gradeManager.getGradableObjectWithStats(id1, studentUids);

		// Add some scores to the interesting assignment, leaving one student unscored.
		GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
		gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, (String)studentUidsList.get(0), new Double(8)));
		gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, (String)studentUidsList.get(1), new Double(9)));
		gradeManager.updateAssignmentGradeRecords(gradeRecordSet);

		// Do what the Overview page does.
		assignments = gradeManager.getAssignmentsWithStats(gradebook.getId(), studentUids, Assignment.SORT_BY_NAME, true);
		CourseGrade courseGrade = gradeManager.getCourseGradeWithStats(gradebook.getId(), studentUids);

        // Remove the assignments.
        // (We remove all of them to make sure that the calculated course grade can be emptied.)
        gradebookManager.removeAssignment(id2);
        gradebookManager.removeAssignment(id3);
        gradebookManager.removeAssignment(id1);

        // Get the list of assignments again, and make sure it's missing the removed assignment
        assignments = gradeManager.getAssignments(gradebook.getId());
        Assert.assertTrue(!assignments.contains(asn));

        // And again, this time calculating statistics
        assignments = gradeManager.getAssignmentsWithStats(gradebook.getId(), studentUids);
        Assert.assertTrue(!assignments.contains(asn));

        // Get the grade records for this gradebook, and make sure none of them
        // belong to a removed assignment
        List gradeRecords = gradeManager.getPointsEarnedSortedAllGradeRecords(gradebook.getId(), studentUids);
        assertNoneFromRemovedAssignments(gradeRecords);

        // Get the grade records for this assignment.  None should be returned, since
        // it has been removed.
        gradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(asn);
        assertNoneFromRemovedAssignments(gradeRecords);
        Assert.assertTrue(gradeRecords.size() == 0);

        // Make sure we can add a new assignment with the same name as the removed one.
        // This will throw an exception if it doesn't like the assignment name.
        gradeManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), new Date());
    }

    /**
     * Ensures that none of the grade records passed in belong to a removed assignment.
     *
     * @param gradeRecords
     */
    private void assertNoneFromRemovedAssignments(Collection gradeRecords) {
        for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
            AbstractGradeRecord gr = (AbstractGradeRecord)iter.next();
            Assert.assertTrue(!gr.getGradableObject().isRemoved());
        }
    }

    /**
     * Create, remove, and re-add some assignments to ensure that the total
     * points calculation remains accurate.
     *
     * @throws Exception
     */
    public void testTotalPointsInGradebook() throws Exception {
        Long id1 = gradeManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null);
        Long id2 = gradeManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10));
        Long id3 = gradeManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date());

        double totalPointsPossible = gradeManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 60);

        gradebookManager.removeAssignment(id1);

        totalPointsPossible = gradeManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 50);

        gradeManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(50), null);

        totalPointsPossible = gradeManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 100);
    }
}
