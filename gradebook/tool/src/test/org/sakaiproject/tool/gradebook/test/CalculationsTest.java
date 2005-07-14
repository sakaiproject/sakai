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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.LetterGradeMapping;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class CalculationsTest extends TestCase {
	private static final Log log = LogFactory.getLog(CalculationsTest.class);

    private Gradebook gradebook;
    private CourseGrade courseGrade;
    private GradeMapping gradeMap;
    private Assignment homework1;
    private Assignment homework2;
    private Assignment homework3;
    private List gradeRecords;
    private Set assignments;

	protected void setUp() throws Exception {
        Date now = new Date();

        gradebook = new Gradebook("Calculation Test GB");

        gradeMap = new LetterGradeMapping();
        gradeMap.setDefaultValues();
        gradebook.setSelectedGradeMapping(gradeMap);

        homework1 = new Assignment(gradebook, "homework1", new Double(200), now);

        homework2 = new Assignment(gradebook, "homework2", new Double(300), now);

        homework3 = new Assignment(gradebook, "homework3", new Double(400), now);

        courseGrade = new CourseGrade();
        courseGrade.setGradebook(gradebook);

        assignments = new HashSet();
        assignments.add(homework1);
        assignments.add(homework2);
        assignments.add(homework3);

        // The statistics calculation should be able to deal with grade records
        // that do not belong to the assignment
        gradeRecords = generateGradeRecords(homework1, 101);
        gradeRecords.addAll(generateGradeRecords(homework2, 101));
        gradeRecords.addAll(generateGradeRecords(homework3, 101));
        gradeRecords.addAll(generateGradeRecords(courseGrade, 30));
    }

	/**
     * Tests the statistics calculations for assignments
     *
	 * @throws Exception
	 */
    public void testAssignmentStatisticsCalculation() throws Exception {
        homework1.calculateStatistics(gradeRecords, 101);

        // 101 students, each getting from 0 to 100 points out of 200 possible = 25% average
        Assert.assertTrue(homework1.getMean().equals(new Double(25)));
	}

    /**
     * Tests the statistics calculations on course grade objects
     *
     * @throws Exception
     */
    public void testCourseGradeStatisticsCalculation() throws Exception {
        courseGrade.calculateTotalPointsPossible(assignments);
        courseGrade.calculateStatistics(gradeRecords, 30); // We generated 30 course grade records

        Double firstMean = courseGrade.getMean();
        // An equal number of A's B's and C's should lead to a mean grade of B
        Assert.assertTrue(firstMean.equals(gradeMap.getValue("B")));

        // The total points in the gradebook should be 900
        Assert.assertTrue(courseGrade.getTotalPoints().doubleValue() == 900);
    }

    /**
     * Tests the course grade auto-calculation
     *
     * @throws Exception
     */
    public void testCourseGradeCalculation() throws Exception {
        //CourseGrade cg = gradebook.getCourseGrade();

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1, "studentId", null, new Double(110)));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2, "studentId", null, new Double(300)));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3, "studentId", null, new Double(400)));

        // The grade records should total 90%
        Double autoCalc = courseGrade.calculateCourseGrade("studentId", assignments, studentGradeRecords);
        Assert.assertEquals(autoCalc, new Double(90));
    }

    /**
     * Generates a list of grade records.  For assignment grade records, the
     * number of points earned is equal to the index of the grade record in the
     * list.  For course grade records, the entered grade is equal to either 75,
     * 85, or 95 percent.
     *
     * @param go The gradable object to assign to the generated records
     * @param gradeRecordsToGenerate The number of generate records to generate.
     *
     * @return A list of grade records
     */
    private List generateGradeRecords(GradableObject go, int gradeRecordsToGenerate) {
        String[] grades = {"A", "B", "C"};
        List records = new ArrayList();
        AbstractGradeRecord record;
        // Add 101 records for the gradableObject
        for(int i = 0; i < gradeRecordsToGenerate; i++) {
            if(go.isCourseGrade()) {
                record = new CourseGradeRecord();
                ((CourseGradeRecord)record).setEnteredGrade(grades[i%3]);
            } else {
                record = new AssignmentGradeRecord();
                ((AssignmentGradeRecord)record).setPointsEarned(new Double(i));
            }

            record.setGradableObject(go);
            record.setStudentId("studentId");
            records.add(record);
        }
        return records;
    }

    public void testGradeInputConversion() throws Exception {
    	String inputGrade, standardizedGrade;

    	Assert.assertEquals(gradeMap.standardizeInputGrade("b"), "B");
    	Assert.assertEquals(gradeMap.standardizeInputGrade("B"), "B");
    	Assert.assertEquals(gradeMap.standardizeInputGrade("Ba"), null);
    	Assert.assertEquals(gradeMap.standardizeInputGrade("z"), null);
    }
}


