/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

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
        gradebookFrameworkService.addGradebook(gradebookName, gradebookName);

        // Set up a holder for enrollments, teaching assignments, and sections.
        integrationSupport.createCourse(gradebookName, gradebookName, false, false, false);

        // Grab the gradebook for use in the tests
        gradebook = gradebookManager.getGradebook(gradebookName);
    }

    public void testCreateAndUpdateAssignment() throws Exception {
        Long asnId = gradebookManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn = gradebookManager.getAssignment(asnId);
        asn.setPointsPossible(new Double(20));
        gradebookManager.updateAssignment(asn);

        // Fetch the updated assignment with statistics
        Assignment persistentAssignment = gradebookManager.getAssignmentWithStats(asnId);
        // Ensure the DB update was successful
        Assert.assertEquals(persistentAssignment.getPointsPossible(), new Double(20));

        // Try to save a new assignment with the same name
        boolean errorThrown = false;
        try {
            gradebookManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(20), new Date(), Boolean.FALSE,Boolean.FALSE);
        } catch (ConflictingAssignmentNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);

        // Save a second assignment
        Long secondId = gradebookManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn2 = gradebookManager.getAssignment(secondId);

        errorThrown = false;

        // Try to update its name to that of the first
        asn2.setName(ASN1_NAME);
        try {
            gradebookManager.updateAssignment(asn2);
        } catch (ConflictingAssignmentNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);
    }


    public void testGradableObjectSorting() throws Exception {
        // Create an assignment with a null date
        Long id1 = gradebookManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null, Boolean.FALSE,Boolean.FALSE);

        // Create an assignment with an early date (in 1970)
        Long id2 = gradebookManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10), Boolean.FALSE,Boolean.FALSE);

        // Create an assignment with a date of now
        Long id3 = gradebookManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date(), Boolean.FALSE,Boolean.FALSE);

        // Get lists of assignments with different sort orders
        List ascDateOrderedAssignments = gradebookManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_DATE, true);
        List descDateOrderedAssignments = gradebookManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_DATE, false);

        List ascNameOrderedAssignments = gradebookManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_NAME, true);
        List descNameOrderedAssignments = gradebookManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_NAME, false);

        List ascPointsOrderedAssignments = gradebookManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_POINTS, true);
        List descPointsOrderedAssignments = gradebookManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_POINTS, false);

        Assignment asn1 = gradebookManager.getAssignment(id1);
        Assignment asn2 = gradebookManager.getAssignment(id2);
        Assignment asn3 = gradebookManager.getAssignment(id3);

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
    	// Make sure nothing awful happens when we ask for CourseGrade
    	// total points for an empty Gradebook
		CourseGrade courseGrade = getCourseGradeWithStats(gradebook.getId());
		Assert.assertTrue(courseGrade.getMean() == null);

		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
			"testStudentUserUid2",
			"testStudentUserUid3",
		});
		addUsersEnrollments(gradebook, studentUidsList);
		Set studentUids = new HashSet(studentUidsList);

        Long id1 = gradebookManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null, Boolean.FALSE,Boolean.FALSE);
        Long id2 = gradebookManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10), Boolean.FALSE,Boolean.FALSE);
        Long id3 = gradebookManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date(), Boolean.FALSE,Boolean.FALSE);

        List assignments = gradebookManager.getAssignments(gradebook.getId());
        Assignment asn = gradebookManager.getAssignmentWithStats(id1);

		// Add some scores to the interesting assignment, leaving one student unscored.
        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn, (String)studentUidsList.get(0), new Double(8)));
        gradeRecords.add(new AssignmentGradeRecord(asn, (String)studentUidsList.get(1), new Double(9)));
		gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);

        // Remove the assignments.
        // (We remove all of them to make sure that the calculated course grade can be emptied.)
        gradebookManager.removeAssignment(id2);
        gradebookManager.removeAssignment(id3);
        gradebookManager.removeAssignment(id1);

        // Get the list of assignments again, and make sure it's missing the removed assignment
        assignments = gradebookManager.getAssignments(gradebook.getId());
        Assert.assertTrue(!assignments.contains(asn));

        // And again, this time calculating statistics
        assignments = gradebookManager.getAssignmentsWithStats(gradebook.getId(), Assignment.SORT_BY_NAME, true);
        Assert.assertTrue(!assignments.contains(asn));

        // Get the grade records for this gradebook, and make sure none of them
        // belong to a removed assignment
        gradeRecords = gradebookManager.getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
        assertNoneFromRemovedAssignments(gradeRecords);

        // Get the grade records for this assignment.  None should be returned, since
        // it has been removed.
        gradeRecords = gradebookManager.getAssignmentGradeRecords(asn, studentUids);
        assertNoneFromRemovedAssignments(gradeRecords);
        Assert.assertTrue(gradeRecords.size() == 0);

        // Make sure we can add a new assignment with the same name as the removed one.
        // This will throw an exception if it doesn't like the assignment name.
        gradebookManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
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
        Long id1 = gradebookManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null, Boolean.FALSE,Boolean.FALSE);
        gradebookManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10), Boolean.FALSE,Boolean.FALSE);
        gradebookManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date(), Boolean.FALSE,Boolean.FALSE);

        double totalPointsPossible = gradebookManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 60);

        gradebookManager.removeAssignment(id1);

        totalPointsPossible = gradebookManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 50);

        gradebookManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(50), null, Boolean.FALSE,Boolean.FALSE);

        totalPointsPossible = gradebookManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 100);
    }
}
