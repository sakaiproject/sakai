/**********************************************************************************
*
* $Header: $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.facades.standalone.EnrollmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;

/**
 * TODO Document org.sakaiproject.tool.gradebook.test.GradableObjectManagerTest
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
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
        gradebookService.addGradebook(className, className);
        
        // Grab the gradebook for use in the tests
        gradebook = gradebookManager.getGradebook(className);
    }

    public void testCreateAndUpdateAssignment() throws Exception {
        Long asnId = gradableObjectManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), new Date());;
        gradableObjectManager.updateAssignment(asnId, ASN1_NAME, new Double(20), new Date());

        // Fetch the updated assignment with statistics
        Assignment persistentAssignment = (Assignment)gradableObjectManager.getGradableObjectWithStats(asnId);
        // Ensure the DB update was successful
        Assert.assertEquals(persistentAssignment.getPointsPossible(), new Double(20));

        // Try to save a new assignment with the same name
        boolean errorThrown = false;
        try {
            Long dupId = gradableObjectManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(20), new Date());;
        } catch (ConflictingAssignmentNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);

        // Save a second assignment
        Long secondId = gradableObjectManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(10), new Date());;

        // Try to update its name to that of the first
        errorThrown = false;
        try {
            gradableObjectManager.updateAssignment(secondId, ASN1_NAME, new Double(10), new Date());
        } catch (ConflictingAssignmentNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);
    }

    public void testGradableObjectSorting() throws Exception {
        // Create an assignment with a null date
        Long id1 = gradableObjectManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null);

        // Create an assignment with an early date (in 1970)
        Long id2 = gradableObjectManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10));

        // Create an assignment with a date of now
        Long id3 = gradableObjectManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date());

        // Get lists of assignments with different sort orders
        List ascDateOrderedAssignments = gradableObjectManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_DATE, true);
        List descDateOrderedAssignments = gradableObjectManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_DATE, false);

        List ascNameOrderedAssignments = gradableObjectManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_NAME, true);
        List descNameOrderedAssignments = gradableObjectManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_NAME, false);

        List ascPointsOrderedAssignments = gradableObjectManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_POINTS, true);
        List descPointsOrderedAssignments = gradableObjectManager.getAssignments(gradebook.getId(), Assignment.SORT_BY_POINTS, false);

        Assignment asn1 = (Assignment)gradableObjectManager.getGradableObject(id1);
        Assignment asn2 = (Assignment)gradableObjectManager.getGradableObject(id2);
        Assignment asn3 = (Assignment)gradableObjectManager.getGradableObject(id3);

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
        Long id1 = gradableObjectManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null);
        Long id2 = gradableObjectManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10));
        Long id3 = gradableObjectManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date());

        List assignments = gradableObjectManager.getAssignments(gradebook.getId());
        Assignment asn = null;

        // Get the assignment to delete
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment tmp = (Assignment)iter.next();
            if(tmp.getName().equals(ASN1_NAME)) {
                asn = tmp;
                break;
            }
        }

        // Remove the assignment
        gradebookManager.removeAssignment(id1);

        // Get the list of assignments again, and make sure it's missing the removed assignment
        assignments = gradableObjectManager.getAssignments(gradebook.getId());
        Assert.assertTrue(!assignments.contains(asn));

        // And again, this time calculating statistics
        assignments = gradableObjectManager.getAssignmentsWithStats(gradebook.getId());
        Assert.assertTrue(!assignments.contains(asn));

        // Get the grade records for this gradebook, and make sure none of them
        // belong to a removed assignment
        Set enrollments = new HashSet();
        enrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid1", null, null, null), gradebook));
        enrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid2", null, null, null), gradebook));
        enrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid3", null, null, null), gradebook));
        List gradeRecords = gradeManager.getPointsEarnedSortedAllGradeRecords(gradebook.getId(), FacadeUtils.getStudentUids(enrollments));
        assertNoneFromRemovedAssignments(gradeRecords);

        // Get the grade records for this assignment.  None should be returned, since
        // it has been removed.
        gradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(asn);
        assertNoneFromRemovedAssignments(gradeRecords);
        Assert.assertTrue(gradeRecords.size() == 0);

        // Make sure we can add a new assignment with the same name as the removed one.
        // This will throw an exception if it doesn't like the assignment name.
        gradableObjectManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), new Date());
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
        Long id1 = gradableObjectManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(10), null);
        Long id2 = gradableObjectManager.createAssignment(gradebook.getId(), ASN2_NAME, new Double(20), new Date(10));
        Long id3 = gradableObjectManager.createAssignment(gradebook.getId(), ASN3_NAME, new Double(30), new Date());
        
        double totalPointsPossible = gradableObjectManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 60);

        gradebookManager.removeAssignment(id1);
        
        totalPointsPossible = gradableObjectManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 50);

        gradableObjectManager.createAssignment(gradebook.getId(), ASN1_NAME, new Double(50), null);

        totalPointsPossible = gradableObjectManager.getTotalPoints(gradebook.getId());
        Assert.assertTrue(totalPointsPossible == 100);
    }
}


/**********************************************************************************
 * $Header: $
 *********************************************************************************/
