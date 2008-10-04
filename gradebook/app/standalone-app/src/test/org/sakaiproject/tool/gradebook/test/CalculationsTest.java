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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.LetterGradeMapping;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class CalculationsTest extends TestCase {
	private Gradebook gradebook;
    private CourseGrade courseGrade;
    private GradeMapping gradeMap;
    private Assignment homework1;
    private Assignment homework2;
    private Assignment homework3;
    private List<AssignmentGradeRecord> gradeRecords;
    private List<CourseGradeRecord> courseGradeRecords;
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
        courseGradeRecords = generateCourseGradeRecords(courseGrade, 30);
    }
	
	public static double getTotalPointsPossible(Collection assignments) {
		double total = 0;
		for (Iterator iter = assignments.iterator(); iter.hasNext();) {
			total += ((Assignment)iter.next()).getPointsPossible();
		}
		return total;
	}
	
	public static Double getTotalPointsEarned(Collection gradeRecords) {
		double total = 0;
		boolean hasScores = false;
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
			total += ((AssignmentGradeRecord)iter.next()).getPointsEarned();
			hasScores = true;
		}
		return hasScores ? new Double(total) : null;
	}

	/**
     * Tests the statistics calculations for assignments
     *
	 * @throws Exception
	 */
    public void testAssignmentStatisticsCalculation() throws Exception {
        homework1.calculateStatistics(gradeRecords);

        // 101 students, each getting from 0 to 100 points out of 200 possible = 25% average
        Assert.assertTrue(homework1.getMean().equals(new Double(25)));
	}

    /**
     * Tests the statistics calculations on course grade objects
     *
     * @throws Exception
     */
    public void testCourseGradeStatisticsCalculation() throws Exception {
        courseGrade.calculateStatistics(courseGradeRecords, 30); // We generated 30 course grade records

        Double firstMean = courseGrade.getMean();
        // An equal number of A's B's and C's should lead to a mean grade of B
        if(firstMean != null)
        	Assert.assertTrue(firstMean.equals(gradeMap.getValue("B")));

        // The total points in the gradebook should be 900
        Assert.assertTrue(getTotalPointsPossible(assignments) == 900);
    }

    /**
     * Tests the course grade auto-calculation
     *
     * @throws Exception
     */
    public void testCourseGradeCalculation() throws Exception {
        //CourseGrade cg = gradebook.getCourseGrade();

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1, "studentId", new Double(110)));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2, "studentId", new Double(300)));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3, "studentId", new Double(400)));

        // The grade records should total 90%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.initNonpersistentFields(getTotalPointsPossible(assignments), getTotalPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(90), cgr.getAutoCalculatedGrade());
    }

    /**
     * Generates a list of assignment grade records. The
     * number of points earned is equal to the index of the grade record in the
     * list.
     *
     * @param go The gradable object to assign to the generated records
     * @param gradeRecordsToGenerate The number of generate records to generate.
     *
     * @return A list of grade records
     */
    private List<AssignmentGradeRecord> generateGradeRecords(Assignment go, int gradeRecordsToGenerate) {
        List<AssignmentGradeRecord> records = new ArrayList<AssignmentGradeRecord>();
        AssignmentGradeRecord record;
        // Add 101 records for the gradableObject
        for(int i = 0; i < gradeRecordsToGenerate; i++) {
        	record = new AssignmentGradeRecord();
        	record.setPointsEarned(new Double(i));
        	record.setGradableObject(go);
        	record.setStudentId("studentId");
        	records.add(record);
        }
        return records;
    }

    /**
     * Generates a list of course grade records. The entered grade is equal to 
     * either 75, 85, or 95 percent.
     *
     * @param go The gradable object to assign to the generated records
     * @param gradeRecordsToGenerate The number of generate records to generate.
     *
     * @return A list of grade records
     */
    private List<CourseGradeRecord> generateCourseGradeRecords(CourseGrade go, int gradeRecordsToGenerate) {
        String[] grades = {"A", "B", "C"};
        List<CourseGradeRecord> records = new ArrayList<CourseGradeRecord>();
        CourseGradeRecord record;
        // Add 101 records for the gradableObject
        for(int i = 0; i < gradeRecordsToGenerate; i++) {
        	record = new CourseGradeRecord();
        	record.setEnteredGrade(grades[i%3]);
        	record.setGradableObject(go);
        	record.setStudentId("studentId");
        	records.add(record);
        }
        return records;
    }

    public void testGradeInputConversion() throws Exception {
    	Assert.assertEquals(gradeMap.standardizeInputGrade("b"), "B");
    	Assert.assertEquals(gradeMap.standardizeInputGrade("B"), "B");
    	Assert.assertEquals(gradeMap.standardizeInputGrade("Ba"), null);
    	Assert.assertEquals(gradeMap.standardizeInputGrade("z"), null);
    }

    public void testRoundDown() throws Exception {
    	Assert.assertTrue(FacesUtil.getRoundDown(17.99, 2) == 17.99);
    	Assert.assertTrue(FacesUtil.getRoundDown(17.999, 2) == 17.99);
	}

	public void testNullScoresNotAveraged() throws Exception {
		Assignment asn = new Assignment(gradebook, "homework1", new Double(10), new Date());
		int numEnrollments = 10;
		List<AssignmentGradeRecord> records = new ArrayList<AssignmentGradeRecord>();
		List<CourseGradeRecord> courseRecords = new ArrayList<CourseGradeRecord>();
		for (int i = 0; i < numEnrollments; i++) {
			Double score = (i == 0) ? asn.getPointsPossible() : null;
			records.add(new AssignmentGradeRecord(asn, "student" + i, score));
			
			CourseGradeRecord cgr = new CourseGradeRecord();
			cgr.setGradableObject(courseGrade);
			cgr.setStudentId("student" + i);
			double scoreVal = (score != null) ? score.doubleValue() : 0.0; 
			cgr.initNonpersistentFields(asn.getPointsPossible(), scoreVal);
			courseRecords.add(cgr);
		}
		asn.calculateStatistics(records);
		Double mean = asn.getMean();
		Assert.assertEquals(new Double(100), mean);
		courseGrade.calculateStatistics(courseRecords, numEnrollments);
		mean = courseGrade.getMean();
		Assert.assertEquals(new Double(10), mean);

		records = new ArrayList<AssignmentGradeRecord>();
		for (int i = 0; i < numEnrollments; i++) {
			records.add(new AssignmentGradeRecord(asn, "student" + i, null));
		}
		asn.calculateStatistics(records);
		mean = asn.getMean();
		Assert.assertEquals(null, mean);
	}
}
