/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestManagers.java,v 1.6 2005/05/26 18:53:18 josh.media.berkeley.edu Exp $
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.business.GradableObjectManager;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.standalone.EnrollmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class TestManagers extends SpringEnabledTestCase {
	private static final Log log = LogFactory.getLog(TestManagers.class);

    static String GB_SERVICE = "org_sakaiproject_service_gradebook_GradebookService";
    static String GB_MANAGER = "org_sakaiproject_tool_gradebook_business_GradebookManager";
    static String GO_MANAGER = "org_sakaiproject_tool_gradebook_business_GradableObjectManager";
    static String GR_MANAGER = "org_sakaiproject_tool_gradebook_business_GradeManager";

    static String GB_NAME = "josh's gradebook";
    static String ASN1_NAME = "First Homework Assignment";
    static String ASN2_NAME = "Second Homework Assignment";

    GradebookManager gradebookManager;
    GradableObjectManager gradableObjectManager;
    GradeManager gradeManager;
    GradebookService gradebookService;

	protected void setUp() throws Exception {
		log.info("Attempting to obtain spring-managed services.");
		initialize("components.xml,components-test.xml");
        gradebookService = (GradebookService)getBean(GB_SERVICE);
        gradebookManager = (GradebookManager)getBean(GB_MANAGER);
        gradableObjectManager = (GradableObjectManager)getBean(GO_MANAGER);
        gradeManager = (GradeManager)getBean(GR_MANAGER);
	}

	public void testGradebookManager() throws Exception {
        // Create a gradebook
        String gradebookUid = "TestManagersGB";
        gradebookService.addGradebook(gradebookUid, GB_NAME);

        // Fetch the gradebook
        Gradebook persistentGradebook = gradebookManager.getGradebook(gradebookUid);

        // Modify the gradebook (including the grade mapping)
        persistentGradebook.setAllAssignmentsEntered(true);
        persistentGradebook.getSelectedGradeMapping().getGradeMap().put("A", new Double(99));

        // Update the gradebook
        gradebookManager.updateGradebook(persistentGradebook);

        // Ensure that the DB update was successful
        persistentGradebook = gradebookManager.getGradebook(gradebookUid);
        Assert.assertTrue(persistentGradebook.isAllAssignmentsEntered());
        Assert.assertTrue(persistentGradebook.getSelectedGradeMapping().getGradeMap().get("A").equals(new Double(99)));

        // Reset the grade mapping in the db
        persistentGradebook.getSelectedGradeMapping().setDefaultValues();
        gradebookManager.updateGradebook(persistentGradebook);
	}

    public void testGradableObjectManager() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(new Long(1));

        // Create an assignment
        Long asnId = gradableObjectManager.createAssignment(gb.getId(), ASN1_NAME, new Double(10), new Date());;

        // Update the assignment
        gradableObjectManager.updateAssignment(asnId, ASN1_NAME, new Double(20), new Date());

        // Fetch the updated assignment with statistics
        Assignment persistentAssignment = (Assignment)gradableObjectManager.getGradableObjectWithStats(asnId);

        // Ensure the DB update was successful
        Assert.assertEquals(persistentAssignment.getPointsPossible(), new Double(20));

        // Try to save a new assignment with the same name
        boolean errorThrown = false;
        try {
            Long dupId = gradableObjectManager.createAssignment(gb.getId(), ASN1_NAME, new Double(20), new Date());;
        } catch (ConflictingAssignmentNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);

        // Save a second assignment
        Long secondId = gradableObjectManager.createAssignment(gb.getId(), ASN2_NAME, new Double(10), new Date());;

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
        Gradebook gb = gradebookManager.getGradebook(new Long(1));

        // Create an assignment with a null date
        Long id1 = gradableObjectManager.createAssignment(gb.getId(), "HW 1", new Double(10), null);

        // Create an assignment with an early date (in 1970)
        Long id2 = gradableObjectManager.createAssignment(gb.getId(), "hw 2", new Double(20), new Date(10));

        // Create an assignment with a date of now
        Long id3 = gradableObjectManager.createAssignment(gb.getId(), "lab1", new Double(30), new Date());

        // Get lists of assignments with different sort orders
        List ascDateOrderedAssignments = gradableObjectManager.getAssignments(new Long(1), Assignment.SORT_BY_DATE, true);
        List descDateOrderedAssignments = gradableObjectManager.getAssignments(new Long(1), Assignment.SORT_BY_DATE, false);

        List ascNameOrderedAssignments = gradableObjectManager.getAssignments(new Long(1), Assignment.SORT_BY_NAME, true);
        List descNameOrderedAssignments = gradableObjectManager.getAssignments(new Long(1), Assignment.SORT_BY_NAME, false);

        List ascPointsOrderedAssignments = gradableObjectManager.getAssignments(new Long(1), Assignment.SORT_BY_POINTS, true);
        List descPointsOrderedAssignments = gradableObjectManager.getAssignments(new Long(1), Assignment.SORT_BY_POINTS, false);

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

    public void testGradeManager() throws Exception {
        // Get a gradebook and an assignment
        Gradebook persistentGradebook = gradebookManager.getGradebook(new Long(1));
        Assignment persistentAssignment = (Assignment)gradableObjectManager.
            getAssignmentsWithStats(persistentGradebook.getId()).get(0);

        // Create a map of studentUserUids to grades
        Map map = new HashMap();
        map.put("testStudentUserUid1", new Double(18));
        map.put("testStudentUserUid2", new Double(19));
        map.put("testStudentUserUid3", new Double(20));

        gradeManager.updateAssignmentGradeRecords(persistentAssignment.getId(), map);

        // Fetch the grade records
        List records = gradeManager.getPointsEarnedSortedGradeRecords(persistentAssignment);

        // Ensure that each of the students in the map have a grade record, and
        // that their grade is correct
        Set students = map.keySet();
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
            double tmpScore = ((Double)map.get(agr.getStudentId())).doubleValue();
            double persistentScore = agr.getPointsEarned().doubleValue();
            Assert.assertTrue(tmpScore == persistentScore);
        }

        // Filter

        // Add overrides to the course grades
        map.clear();
        map.put("testStudentUserUid1", "C-");
        map.put("testStudentUserUid2", "D+");
        map.put("testStudentUserUid3", "F");

        CourseGrade courseGrade = gradableObjectManager.getCourseGradeWithStats(persistentGradebook.getId());
        gradeManager.updateCourseGradeRecords(persistentGradebook.getId(), map);

        GradeMapping gradeMapping = persistentGradebook.getSelectedGradeMapping();

        // Ensure that the sort grades have been updated to reflect the overridden grades
        List courseGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(courseGrade);
        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
            Double sortGrade = cgr.getSortGrade();
            String studentId = cgr.getStudentId();
            String tmpGrade = (String)map.get(cgr.getStudentId());
            Assert.assertTrue(sortGrade.equals(gradeMapping.getValue(tmpGrade)));
            Assert.assertTrue(gradeMapping.getGrade(cgr.getSortGrade()).equals(tmpGrade));
        }

        // Change the grade mapping
        gradeMapping.getGradeMap().put("D+", new Double(65));
        gradebookManager.updateGradebook(persistentGradebook);

        // Ensure that the grade mapping change triggered an update to the sort grades
        List gradeRecords = gradeManager.getStudentGradeRecords(persistentGradebook.getId(), "testStudentUserUid2");
        CourseGradeRecord cgr = null;
        for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
            AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
            if(agr.getGradableObject().isCourseGrade()) {
                cgr = (CourseGradeRecord)agr;
            }
        }
        Assert.assertTrue(cgr.getSortGrade().equals(new Double(65)));

        // Reset the grade mapping
        persistentGradebook.getSelectedGradeMapping().setDefaultValues();
        gradebookManager.updateGradebook(persistentGradebook);

        // Remove the overrides
        map.clear();
        map.put("testStudentUserUid1", null);
        map.put("testStudentUserUid2", null);
        map.put("testStudentUserUid3", null);
        gradeManager.updateCourseGradeRecords(persistentGradebook.getId(), map);

        // Ensure that the sort grades have been updated
        courseGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(courseGrade);
        double totalPoints = gradableObjectManager.getTotalPoints(persistentGradebook.getId());

        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            cgr = (CourseGradeRecord)iter.next();
            Double percent = cgr.calculatePercent(totalPoints);
            Double sortGrade = cgr.getSortGrade();
            String studentId = cgr.getStudentId();
            Assert.assertTrue(sortGrade.doubleValue() - percent.doubleValue() < .001);
        }

        List allGradeRecords = gradeManager.getPointsEarnedSortedAllGradeRecords(persistentGradebook.getId());
        // There should be six grade records for these students
        Assert.assertTrue(allGradeRecords.size() == 6);

        // Create a new, smaller set of enrollments and ensure the smaller set of grade records are selected correctly
        Set filteredEnrollments = new HashSet();
        filteredEnrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid1", null, null, null), persistentGradebook));
        filteredEnrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid2", null, null, null), persistentGradebook));
        List filteredGradeRecords = gradeManager.getPointsEarnedSortedAllGradeRecords(persistentGradebook.getId(), FacadeUtils.getStudentUids(filteredEnrollments));

        // There should be four grade records for these students
        Assert.assertTrue(filteredGradeRecords.size() == 4);

        // There should be two grade records for these students and for this assignment
        filteredGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(persistentAssignment, FacadeUtils.getStudentUids(filteredEnrollments));
        Assert.assertTrue(filteredGradeRecords.size() == 2);

    }

    public void testDeletedAssignments() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(new Long(1));

        List assignments = gradableObjectManager.getAssignments(gb.getId());
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
        gradebookManager.removeAssignment(asn.getId());

        // Get the list of assignments again, and make sure it's missing the removed assignment
        assignments = gradableObjectManager.getAssignments(gb.getId());
        Assert.assertTrue(!assignments.contains(asn));

        // And again, this time calculating statistics
        assignments = gradableObjectManager.getAssignmentsWithStats(gb.getId());
        Assert.assertTrue(!assignments.contains(asn));

        // Get the grade records for this gradebook, and make sure none of them
        // belong to a removed assignment
        Set enrollments = new HashSet();
        enrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid1", null, null, null), gb));
        enrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid2", null, null, null), gb));
        enrollments.add(new EnrollmentStandalone(new UserStandalone("testStudentUserUid3", null, null, null), gb));
        List gradeRecords = gradeManager.getPointsEarnedSortedAllGradeRecords(gb.getId(), FacadeUtils.getStudentUids(enrollments));
        assertNoneFromRemovedAssignments(gradeRecords);

        // Get the grade records for this assignment.  None should be returned, since
        // it has been removed.
        gradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(asn);
        assertNoneFromRemovedAssignments(gradeRecords);
        Assert.assertTrue(gradeRecords.size() == 0);

        // Make sure we can add a new assignment with the same name as the removed one.
        // This will throw an exception if it doesn't like the assignment name.
        gradableObjectManager.createAssignment(gb.getId(), ASN1_NAME, new Double(10), new Date());
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

    public void testTotalPointsInGradebook() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(new Long(1));

//        ASN1_NAME = 20 (removed)
//        ASN1_NAME = 10 (re-added)
//        ASN2_NAME = 10
//        HW1 = 10
//        HW2 = 20
//        LAB1 = 30
//        TOTAL = 80

        double totalPointsPossible = gradableObjectManager.getTotalPoints(gb.getId());
        Assert.assertTrue(totalPointsPossible == 80);
    }

	public void testExcessiveScores() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(new Long(1));

        Long asgId = gradableObjectManager.createAssignment(gb.getId(), "Excessive Test", new Double(10), new Date());

        Set enrollments = new HashSet();
        enrollments.add(new EnrollmentStandalone(new UserStandalone("normalStudent", null, null, null), gb));
        enrollments.add(new EnrollmentStandalone(new UserStandalone("goodStudent", null, null, null), gb));
        enrollments.add(new EnrollmentStandalone(new UserStandalone("excessiveStudent", null, null, null), gb));

        // Create a map of studentUserUids to grades
        Map map = new HashMap();
        map.put("normalStudent", new Double(9));
        map.put("goodStudent", new Double(10));
        map.put("excessiveStudent", new Double(11));

        Set excessives = gradeManager.updateAssignmentGradeRecords(asgId, map);
        Assert.assertTrue(excessives.size() == 1);
        Assert.assertTrue(excessives.contains("excessiveStudent"));

        map = new HashMap();
        map.put("goodStudent", new Double(11));
        map.put("excessiveStudent", new Double(11));

        // Only updates should be reported.
        excessives = gradeManager.updateAssignmentGradeRecords(asgId, map);
        Assert.assertTrue(excessives.size() == 1);
        Assert.assertTrue(excessives.contains("goodStudent"));
	}

	public void testAssignmentScoresEntered() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(new Long(1));

        Long asgId = gradableObjectManager.createAssignment(gb.getId(), "Scores Entered Test", new Double(10), new Date());

        Set enrollments = new HashSet();
        enrollments.add(new EnrollmentStandalone(new UserStandalone("entered1", null, null, null), gb));

        Assert.assertTrue(!gradeManager.isEnteredAssignmentScores(asgId));

        // Create a map of studentUserUids to grades
        Map map = new HashMap();
        map.put("entered1", new Double(9));

        gradeManager.updateAssignmentGradeRecords(asgId, map);
        Assert.assertTrue(gradeManager.isEnteredAssignmentScores(asgId));

        map = new HashMap();
        map.put("entered1", null);

        gradeManager.updateAssignmentGradeRecords(asgId, map);
        Assert.assertTrue(!gradeManager.isEnteredAssignmentScores(asgId));
	}
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestManagers.java,v 1.6 2005/05/26 18:53:18 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
