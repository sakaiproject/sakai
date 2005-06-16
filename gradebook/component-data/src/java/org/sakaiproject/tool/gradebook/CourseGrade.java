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

package org.sakaiproject.tool.gradebook;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * A CourseGrade is a GradableObject that represents the overall course grade
 * in a gradebook.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class CourseGrade extends GradableObject {
    private Double totalPoints;  // Not persisted
    
    public static final String COURSE_GRADE_NAME = "Course Grade";
    
    public static String SORT_BY_OVERRIDE_GRADE = "override";
    public static String SORT_BY_CALCULATED_GRADE = "autoCalc";
    public static String SORT_BY_POINTS_EARNED = "pointsEarned";

    public CourseGrade() {
    	setName(COURSE_GRADE_NAME);
    }
    
    /**
     * @see org.sakaiproject.tool.gradebook.GradableObject#isCourseGrade()
     */
    public boolean isCourseGrade() {
        return true;
    }

    /**
     * The course grade can not return the total number of points possible until
     * this value is set explicitly.  This should be done while gathering all of
     * the assignments from the database and calling calculateTotalPoints(Collection assignments).
     * 
     * TODO Try to figure out a better approach
     * 
     * @see org.sakaiproject.tool.gradebook.GradableObject#getPointsForDisplay()
     */
    public Double getPointsForDisplay() {
        if(totalPoints == null) {
            throw new IllegalStateException("Total points is null.  Make sure " +
                    "that total points has been calculated prior to display.");
        }
        return totalPoints;
    }

    /**
     * A CourseGrade is never "due", so it will always return null.
     * 
	 * @see org.sakaiproject.tool.gradebook.GradableObject#getDateForDisplay()
	 */
	public Date getDateForDisplay() {
        return null;
	}
        
	/**
     * Determines the auto-calculated value for an individual student's course
     * grade record, based on the given assignments and grade records.
     * 
     * @param assignments The assignments in the gradebook that count toward the
     * course grade
	 * @param gradeRecords  The collection of grade records that apply toward the
     * course grade.
     * 
	 * @return The calculated grade
	 */
	public Double calculateCourseGrade(String studentId, Collection assignments, Collection gradeRecords) {
        // Ensure that the total points field has been populated
		calculateTotalPointsPossible(assignments);
        
        // Determine the total points earned for all assignments
        double totalEarned = 0;
        // Loop through each assignment
        
        for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
            Assignment asn = (Assignment)asnIter.next();
            // Find the grade record for this assignment
            for(Iterator gradeRecordIter = gradeRecords.iterator(); gradeRecordIter.hasNext();) {
                AbstractGradeRecord gr = (AbstractGradeRecord)gradeRecordIter.next();
                // Apply this grade record only if it is for this assignment and for the given student
                if(gr.getGradableObject().equals(asn) && gr.getStudentId().equals(studentId)) {
                    if(((AssignmentGradeRecord)gr).getPointsEarned() != null) {
                        totalEarned += ((AssignmentGradeRecord)gr).getPointsEarned().doubleValue();
                    }
                    break;
                }
            }
        }
        return new Double(totalEarned / this.totalPoints.doubleValue() * 100);
	}
    
    /**
     * Calculates the total points possible based on a collection of assignments
     * 
     * Note:  This calculation assumes that the collection of assignments passed
     * is an accurate collection of the assignments in this course grade's gradebook.
     * 
     * @param assignments The assignments in this gradebook
     */
    public void calculateTotalPointsPossible(Collection assignments) {
        double totalPoints = 0;
        for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
            GradableObject go = (GradableObject)asnIter.next();
            if(go.isCourseGrade()) {
                continue;
            }
            Assignment asn = (Assignment)go;
            totalPoints+=asn.getPointsPossible().doubleValue();
        }
        this.totalPoints = new Double(totalPoints);
    }

    //// Bean getters and setters ////

    /**
     * @return Returns the totalPoints.
     */
    public Double getTotalPoints() {
        return totalPoints;
    }
    /**
     * @param totalPoints The totalPoints to set.
     */
    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

	/**
     * Calculate the mean for all enrollments, counting null grades as zero.
     * 
	 * @see org.sakaiproject.tool.gradebook.GradableObject#calculateMean(java.util.Collection, int)
	 */
	protected Double calculateMean(Collection grades, int numEnrollments) {
		for (int i = 0; i < (numEnrollments - grades.size()); i++) {
			grades.add(new Double(0));
		}

        if (grades == null || grades.size() == 0) {
			return null;
		}

		double total = 0;
		for (Iterator iter = grades.iterator(); iter.hasNext();) {
			Double grade = (Double) iter.next();
			if (grade == null) {
                grade = new Double(0);
			}
			total += grade.doubleValue();
		}
		return new Double(total / grades.size());
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
