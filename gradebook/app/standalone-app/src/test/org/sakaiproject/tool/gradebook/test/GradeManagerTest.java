/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007 Sakai Foundation, the MIT Corporation
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvents;

/**
 * Tests the grade manager.
 *
 */
public class GradeManagerTest extends GradebookTestBase {
    protected Gradebook gradebook;

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();

        String gradebookName = this.getClass().getName();
        gradebookFrameworkService.addGradebook(gradebookName, gradebookName);

        // Set up a holder for enrollments, teaching assignments, and sections.
        integrationSupport.createCourse(gradebookName, gradebookName, false, false, false);

        // Grab the gradebook for use in the tests
        gradebook = gradebookManager.getGradebook(gradebookName);
    }

    public void testGradeManager() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
			"testStudentUserUid2",
			"testStudentUserUid3",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        gradebookManager.createAssignment(gradebook.getId(), "Assignment #1", new Double(20), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment persistentAssignment = (Assignment)gradebookManager.
            getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

        Map studentAssignmentScoreMap = new HashMap();
        studentAssignmentScoreMap.put(studentUidsList.get(0), new Double(18));
        studentAssignmentScoreMap.put(studentUidsList.get(1), new Double(19));
        studentAssignmentScoreMap.put(studentUidsList.get(2), new Double(20));
        Map gradeRecordMap = new HashMap();
        gradeRecordMap.put(studentUidsList.get(0), new AssignmentGradeRecord(persistentAssignment, (String)studentUidsList.get(0), (Double)studentAssignmentScoreMap.get(studentUidsList.get(0))));
        gradeRecordMap.put(studentUidsList.get(1), new AssignmentGradeRecord(persistentAssignment, (String)studentUidsList.get(1), (Double)studentAssignmentScoreMap.get(studentUidsList.get(1))));
        gradeRecordMap.put(studentUidsList.get(2), new AssignmentGradeRecord(persistentAssignment, (String)studentUidsList.get(2), (Double)studentAssignmentScoreMap.get(studentUidsList.get(2))));

        gradebookManager.updateAssignmentGradeRecords(persistentAssignment, gradeRecordMap.values());

        // Fetch the grade records
        List records = gradebookManager.getAssignmentGradeRecords(persistentAssignment, studentUidsList);

        // Ensure that each of the students in the map have a grade record, and
        // that their grade is correct
        Set students = new HashSet(studentUidsList);
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
            double tmpScore = ((AssignmentGradeRecord)gradeRecordMap.get(agr.getStudentId())).getPointsEarned().doubleValue();
            double persistentScore = agr.getPointsEarned().doubleValue();
            Assert.assertTrue(tmpScore == persistentScore);
        }

        // Add overrides to the course grades
        CourseGrade courseGrade = gradebookManager.getCourseGrade(gradebook.getId());
        records = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, students);

        gradeRecordMap = new HashMap();
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            CourseGradeRecord record = (CourseGradeRecord)iter.next();
            if(record.getStudentId().equals(studentUidsList.get(0))) {
                record.setEnteredGrade("C-");
            } else if(record.getStudentId().equals(studentUidsList.get(1))) {
                record.setEnteredGrade("D+");
            } else if(record.getStudentId().equals(studentUidsList.get(2))) {
                record.setEnteredGrade("F");
            }
            gradeRecordMap.put(record.getStudentId(), record);
        }

        gradebookManager.updateCourseGradeRecords(courseGrade, gradeRecordMap.values());

        GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();

        // Ensure that the sort grades have been updated to reflect the overridden grades
        List courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, studentUidsList);
        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
            Double sortGrade = cgr.getGradeAsPercentage();
            String studentId = cgr.getStudentId();
            String tmpGrade = ((CourseGradeRecord)gradeRecordMap.get(studentId)).getEnteredGrade();
            if (logger.isDebugEnabled()) logger.debug("sortGrade=" + sortGrade + ", gradeMapping=" + gradeMapping + ", tmpGrade=" + tmpGrade);
            
            Assert.assertTrue(sortGrade.equals(gradeMapping.getValue(tmpGrade)));
            Assert.assertTrue(gradeMapping.getGrade(cgr.getGradeAsPercentage()).equals(tmpGrade));
        }

        // Remove the overrides
        gradeRecordMap = new HashMap();
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            CourseGradeRecord record = (CourseGradeRecord)iter.next();
            record.setEnteredGrade(null);
            gradeRecordMap.put(record.getStudentId(), record);
        }

        gradebookManager.updateCourseGradeRecords(courseGrade, gradeRecordMap.values());

        // Ensure that the sort grades have been updated
        courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, studentUidsList);
        double totalPoints = gradebookManager.getTotalPoints(gradebook.getId());

        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
            double percent = ((Double)studentAssignmentScoreMap.get(cgr.getStudentId())).doubleValue() / totalPoints * 100.0;
            Double sortGrade = cgr.getGradeAsPercentage();
            Assert.assertTrue(sortGrade.doubleValue() - percent < .001);
        }
    }

    public void testNewExcessiveScores() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"normalStudent",
			"goodStudent",
			"excessiveStudent",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        gradebookManager.createAssignment(gradebook.getId(), "Excessive Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn = (Assignment)gradebookManager.getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

        // Create a grade record set
        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn, "normalStudent", new Double(9)));
        gradeRecords.add(new AssignmentGradeRecord(asn, "goodStudent", new Double(10)));
        gradeRecords.add(new AssignmentGradeRecord(asn, "excessiveStudent", new Double(11)));

        Set excessives = gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);
        Assert.assertTrue(excessives.size() == 1);
        Assert.assertTrue(excessives.contains("excessiveStudent"));
    }

    public void testAssignmentScoresEntered() throws Exception {
        Set students = new HashSet();
        students.add("entered1");

        Long asgId = gradebookManager.createAssignment(gradebook.getId(), "Scores Entered Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE
        );
        Assignment asn = (Assignment)gradebookManager.getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

        Assert.assertTrue(!gradebookManager.isEnteredAssignmentScores(asgId));

        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn, "entered1", new Double(9)));

        gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);
        Assert.assertTrue(gradebookManager.isEnteredAssignmentScores(asgId));

        List persistentGradeRecords = gradebookManager.getAssignmentGradeRecords(asn, students);

        gradeRecords = new ArrayList();
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(null);
        gradeRecords.add(gradeRecord);

        gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);
        Assert.assertTrue(!gradebookManager.isEnteredAssignmentScores(asgId));
    }

    public void testGradeEvents() throws Exception {
        String studentId = "student1";
		List studentUidsList = Arrays.asList(new String[] {
			studentId,
		});
        gradebookManager.createAssignment(gradebook.getId(), "GradingEvent Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment assignment = (Assignment)gradebookManager.getAssignments(gradebook.getId()).get(0);

        // Create a map of studentUserUids to grades
        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(assignment, studentId, new Double(9)));

        // Save the grades
        gradebookManager.updateAssignmentGradeRecords(assignment, gradeRecords);

        // Update the grades (make sure we grab them from the db, first)
        Set students = new HashSet();
        students.add(studentId);
        List persistentGradeRecords = gradebookManager.getAssignmentGradeRecords(assignment, students);

        gradeRecords = new ArrayList();
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(new Double(10));
        gradeRecords.add(gradeRecord);

        gradebookManager.updateAssignmentGradeRecords(assignment, gradeRecords);

        // Ensure that there are two grading events for this student
        GradingEvents events = gradebookManager.getGradingEvents(assignment, studentUidsList);
        Assert.assertEquals(events.getEvents(studentId).size(), 2);
    }

    public void testDroppedStudents() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(this.getClass().getName());
        Long asgId = gradebookManager.createAssignment(gradebook.getId(), "Dropped Students Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn = gradebookManager.getAssignment(asgId);

        // We need to operate on whatever grade records already exist in the db
		List studentUidsList = Arrays.asList(new String[] {
			"realStudent1",
			"realStudent2",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        // Create a grade record set
        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn, "realStudent1", new Double(10)));
        gradeRecords.add(new AssignmentGradeRecord(asn, "realStudent2", new Double(10)));
        gradeRecords.add(new AssignmentGradeRecord(asn, "droppedStudent", new Double(1)));

        gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);

        asn = gradebookManager.getAssignmentWithStats(asgId);

        // Make sure the dropped student wasn't included in the average.
        Assert.assertTrue(asn.getMean().doubleValue() == 100.0);

		// Now add the dropped student.
		studentUidsList = Arrays.asList(new String[] {
			"droppedStudent",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        asn = gradebookManager.getAssignmentWithStats(asgId);
        Assert.assertTrue(asn.getMean().doubleValue() < 100.0);

		// Make sure that dropped students can't prevent changing final grade types.
        CourseGrade courseGrade = getCourseGradeWithStats(gradebook.getId());
        gradeRecords = new ArrayList();
        CourseGradeRecord courseGradeRecord = new CourseGradeRecord(courseGrade, "noSuchStudent");
        courseGradeRecord.setEnteredGrade("C-");
        gradeRecords.add(courseGradeRecord);
		gradebookManager.updateCourseGradeRecords(courseGrade, gradeRecords);
		Assert.assertFalse(gradebookManager.isExplicitlyEnteredCourseGradeRecords(gradebook.getId()));
    }

    public void testNotCountedAssignments() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        Long id1 = gradebookManager.createAssignment(gradebook.getId(), "asn1", new Double(10), null, Boolean.FALSE,Boolean.TRUE);
        Long id2 = gradebookManager.createAssignment(gradebook.getId(), "asn2", new Double(20), new Date(10), Boolean.FALSE,Boolean.TRUE);

        Assignment asn1 = gradebookManager.getAssignmentWithStats(id1);
        Assignment asn2 = gradebookManager.getAssignmentWithStats(id2);

		// Add some scores to the assignments.
        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn1, (String)studentUidsList.get(0), new Double(8)));
		gradebookManager.updateAssignmentGradeRecords(asn1, gradeRecords);
		gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn2, (String)studentUidsList.get(0), new Double(18)));
		gradebookManager.updateAssignmentGradeRecords(asn2, gradeRecords);

		// Make sure that the Course Grade total points includes both.
		CourseGrade courseGrade = getCourseGradeWithStats(gradebook.getId());
        List courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, studentUidsList);
        CourseGradeRecord cgr = (CourseGradeRecord)courseGradeRecords.get(0);
        Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 26.0);

        // Don't count one assignment.
        asn2.setNotCounted(true);
        gradebookManager.updateAssignment(asn2);

		// Get what the student will see.
		CourseGradeRecord scgr = gradebookManager.getStudentCourseGradeRecord(gradebook, (String)studentUidsList.get(0));

        // Make sure it's not counted.
        courseGrade = getCourseGradeWithStats(gradebook.getId());
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, studentUidsList);
		cgr = (CourseGradeRecord)courseGradeRecords.get(0);
//		Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 8.0);

		// Make sure there's no disconnect between what the instructor
		// will see and what the student will see.
		Assert.assertTrue(cgr.getNonNullAutoCalculatedGrade().equals(scgr.getGradeAsPercentage()));

		// Test what is now (unfortunately) a different code path.
        List persistentGradeRecords = gradebookManager.getAssignmentGradeRecords(asn1, studentUidsList);
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(new Double(7));
        gradeRecords = new ArrayList();
        gradeRecords.add(gradeRecord);
		gradebookManager.updateAssignmentGradeRecords(asn1, gradeRecords);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, studentUidsList);
		cgr = (CourseGradeRecord)courseGradeRecords.get(0);
//		Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 7.0);
    }

    /**
     * This tests an edge case responsible for an earlier bug: If an unscored assignment
     * changes whether it's counted towards the course grade, all course grades need
     * to be recalculated because the total points possible have changed.
     */
    public void testNotCountedUnscoredAssignments() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        Long id1 = gradebookManager.createAssignment(gradebook.getId(), "asn1", new Double(10), null, Boolean.FALSE,Boolean.FALSE);
        Long id2 = gradebookManager.createAssignment(gradebook.getId(), "asn2", new Double(20), new Date(10), Boolean.FALSE,Boolean.FALSE);

        Assignment asn1 = gradebookManager.getAssignmentWithStats(id1);
        Assignment asn2 = gradebookManager.getAssignmentWithStats(id2);

		// Only score the first assignment.
        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn1, (String)studentUidsList.get(0), new Double(8)));
		gradebookManager.updateAssignmentGradeRecords(asn1, gradeRecords);

        // Don't count the unscored assignment.
        asn2.setNotCounted(true);
        gradebookManager.updateAssignment(asn2);

		// Get what the student will see.
		CourseGradeRecord scgr = gradebookManager.getStudentCourseGradeRecord(gradebook, (String)studentUidsList.get(0));

        // Make sure it's not counted.
        CourseGrade courseGrade = getCourseGradeWithStats(gradebook.getId());
		List courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, studentUidsList);
		CourseGradeRecord cgr = (CourseGradeRecord)courseGradeRecords.get(0);
		Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 8.0);

		// Make sure there's no disconnect between what the instructor
		// will see and what the student will see.
		Assert.assertTrue(cgr.getNonNullAutoCalculatedGrade().equals(scgr.getGradeAsPercentage()));
	}

}
