/**********************************************************************************
*
* $Id$
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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.facades.standalone.EnrollmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;

/**
 * Tests the grade manager.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradeManagerTest extends GradebookTestBase {

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
    }

    /**
     * Save the gradebook.  This is a hack to get around hibernate
     * collections problems.
     * 
     * @throws Exception
     */
    public void testCreateGradebookForTesting() throws Exception {
        String className = this.getClass().getName();
        gradebookService.addGradebook(className, className);
        setComplete();
    }
    
    public void testGradeManager() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(this.getClass().getName());

        // Get a gradebook and an assignment
        Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
        gradeManager.createAssignment(persistentGradebook.getId(), "Assignment #1", new Double(20), new Date());
        Assignment persistentAssignment = (Assignment)gradeManager.
            getAssignmentsWithStats(persistentGradebook.getId()).get(0);

        GradeRecordSet gradeRecordSet = new GradeRecordSet(persistentAssignment);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(persistentAssignment, "testStudentUserUid1", "teacher1", new Double(18)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(persistentAssignment, "testStudentUserUid2", "teacher1", new Double(19)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(persistentAssignment, "testStudentUserUid3", "teacher1", new Double(20)));

        gradeManager.updateAssignmentGradeRecords(gradeRecordSet);

        // Fetch the grade records
        List records = gradeManager.getPointsEarnedSortedGradeRecords(persistentAssignment);

        // Ensure that each of the students in the map have a grade record, and
        // that their grade is correct
        Set students = gradeRecordSet.getAllStudentIds();
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
            double tmpScore = gradeRecordSet.getGradeRecord(agr.getStudentId()).getPointsEarned().doubleValue();
            double persistentScore = agr.getPointsEarned().doubleValue();
            Assert.assertTrue(tmpScore == persistentScore);
        }

        // Add overrides to the course grades
        CourseGrade courseGrade = gradeManager.getCourseGradeWithStats(persistentGradebook.getId());
        records = gradeManager.getPointsEarnedSortedGradeRecords(courseGrade);

        gradeRecordSet = new GradeRecordSet(courseGrade);
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            CourseGradeRecord record = (CourseGradeRecord)iter.next();
            if(record.getStudentId().equals("testStudentUserUid1")) {
                record.setEnteredGrade("C-");
            } else if(record.getStudentId().equals("testStudentUserUid2")) {
                record.setEnteredGrade("D+");
            } else if(record.getStudentId().equals("testStudentUserUid3")) {
                record.setEnteredGrade("F");
            }
            gradeRecordSet.addGradeRecord(record);
        }

        
        gradeManager.updateCourseGradeRecords(gradeRecordSet);

        GradeMapping gradeMapping = persistentGradebook.getSelectedGradeMapping();

        // Ensure that the sort grades have been updated to reflect the overridden grades
        List courseGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(courseGrade);
        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
            Double sortGrade = cgr.getSortGrade();
            String studentId = cgr.getStudentId();
            String tmpGrade = ((CourseGradeRecord)gradeRecordSet.getGradeRecord(studentId)).getEnteredGrade();
            Assert.assertTrue(sortGrade.equals(gradeMapping.getValue(tmpGrade)));
            Assert.assertTrue(gradeMapping.getGrade(cgr.getSortGrade()).equals(tmpGrade));
        }

        // Remove the overrides
        gradeRecordSet = new GradeRecordSet(courseGrade);
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            CourseGradeRecord record = (CourseGradeRecord)iter.next();
            record.setEnteredGrade(null);
            gradeRecordSet.addGradeRecord(record);
        }

        gradeManager.updateCourseGradeRecords(gradeRecordSet);

        // Ensure that the sort grades have been updated
        courseGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(courseGrade);
        double totalPoints = gradeManager.getTotalPoints(persistentGradebook.getId());

        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
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

    public void testNewExcessiveScores() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(this.getClass().getName());
        Long asgId = gradeManager.createAssignment(gradebook.getId(), "Excessive Test", new Double(10), new Date());
        Assignment asn = (Assignment)gradeManager.getAssignmentsWithStats(gradebook.getId()).get(0);

        // Create a grade record set
        GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "normalStudent", "testId", new Double(9)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "goodStudent", "testId", new Double(10)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "excessiveStudent", "testId", new Double(11)));

        Set excessives = gradeManager.updateAssignmentGradeRecords(gradeRecordSet);
        Assert.assertTrue(excessives.size() == 1);
        Assert.assertTrue(excessives.contains("excessiveStudent"));

        
        
        
        
        // We need to operate on whatever grade records already exist in the db
        Set studentIds = new HashSet();
        studentIds.add("normalStudent");
        studentIds.add("goodStudent");
        studentIds.add("excessiveStudent");
        
        List persistentGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(asn, studentIds);
        gradeRecordSet = new GradeRecordSet(asn);

        for(Iterator iter = persistentGradeRecords.iterator(); iter.hasNext();) {
            AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)iter.next();
            if(gradeRecord.getStudentId().equals("goodStudent")) {
                gradeRecord.setPointsEarned(new Double(12));
            }
            // Always add the grade record to ensure that records that have not changed are not updated in the db
            gradeRecordSet.addGradeRecord(gradeRecord);
        }

        // Only updates should be reported.
        excessives = gradeManager.updateAssignmentGradeRecords(gradeRecordSet);
        
        Assert.assertTrue(excessives.contains("goodStudent"));
        Assert.assertEquals(1, excessives.size());
    }

    public void testAssignmentScoresEntered() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(this.getClass().getName());
        Long asgId = gradeManager.createAssignment(gradebook.getId(), "Scores Entered Test", new Double(10), new Date());
        Assignment asn = (Assignment)gradeManager.getAssignmentsWithStats(gradebook.getId()).get(0);

        Set enrollments = new HashSet();
        enrollments.add(new EnrollmentStandalone(new UserStandalone("entered1", null, null, null), gradebook));

        Assert.assertTrue(!gradeManager.isEnteredAssignmentScores(asgId));

        GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "entered1", "testId", new Double(9)));

        gradeManager.updateAssignmentGradeRecords(gradeRecordSet);
        Assert.assertTrue(gradeManager.isEnteredAssignmentScores(asgId));

        Set students = new HashSet();
        students.add("entered1");

        List persistentGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(asn, students);
        
        gradeRecordSet = new GradeRecordSet(asn);
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(null);
        gradeRecordSet.addGradeRecord(gradeRecord);

        gradeManager.updateAssignmentGradeRecords(gradeRecordSet);
        Assert.assertTrue(!gradeManager.isEnteredAssignmentScores(asgId));
    }

    public void testGradeEvents() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(this.getClass().getName());
        Long asgId = gradeManager.createAssignment(gradebook.getId(), "GradingEvent Test", new Double(10), new Date());
        Assignment assignment = (Assignment)gradeManager.getAssignments(gradebook.getId()).get(0);

        String studentId = "student1";
        Set enrollments = new HashSet();
        enrollments.add(new EnrollmentStandalone(new UserStandalone(studentId, null, null, null), gradebook));

        // Create a map of studentUserUids to grades
        GradeRecordSet gradeRecordSet = new GradeRecordSet(assignment);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(assignment, studentId, "teacherId", new Double(9)));

        // Save the grades
        gradeManager.updateAssignmentGradeRecords(gradeRecordSet);

        // Update the grades (make sure we grab them from the db, first)
        Set students = new HashSet();
        students.add(studentId);
        List persistentGradeRecords = gradeManager.getPointsEarnedSortedGradeRecords(assignment, students);
        
        gradeRecordSet = new GradeRecordSet(assignment);
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(new Double(10));
        gradeRecordSet.addGradeRecord(gradeRecord);

        gradeManager.updateAssignmentGradeRecords(gradeRecordSet);

        

        // Ensure that there are two grading events for this student
        GradingEvents events = gradeManager.getGradingEvents(assignment, enrollments);
        Assert.assertEquals(events.getEvents(studentId).size(), 2);
    }
    
}


/**********************************************************************************
 * $Id$
 *********************************************************************************/
