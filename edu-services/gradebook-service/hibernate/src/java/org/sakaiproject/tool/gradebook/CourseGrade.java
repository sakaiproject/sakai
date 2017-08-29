/**
 * Copyright (c) 2003-2012 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook;

import java.math.BigDecimal;
import java.util.Collection;

import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * A CourseGrade is a GradableObject that represents the overall course grade
 * in a gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class CourseGrade extends GradableObject {
	// Should only be used to fill in the DB column.
    private static final String COURSE_GRADE_NAME = "Course Grade";

    public static String SORT_BY_OVERRIDE_GRADE = "override";
    public static String SORT_BY_CALCULATED_GRADE = "autoCalc";
    public static String SORT_BY_POINTS_EARNED = "pointsEarned";
    
    private Double averageScore;

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
     * @see org.sakaiproject.tool.gradebook.GradableObject#isAssignment()
     */
    public boolean isAssignment() {
        return false;
    }
    
    /**
     * @see org.sakaiproject.tool.gradebook.GradableObject#isCategory()
     */
    public boolean getIsCategory() {
        return false;
    }

	//// Bean getters and setters ////

    /**
	 * Calculate the mean course grade (whether entered or calulated) as a
	 * percentage for all enrollments, leaving
	 * students who've explicitly been given non-percentage-valued manual-only
	 * course grades (such as "I" for incomplete) or null scores out of the calculation.
	 */
    public void calculateStatistics(Collection<CourseGradeRecord> gradeRecords, int numEnrollments) {
        // Ungraded but enrolled students count as if they have 0% in the course.
        int numScored = numEnrollments - gradeRecords.size();
        BigDecimal total = new BigDecimal("0");
        BigDecimal average = new BigDecimal("0");

        for (CourseGradeRecord record : gradeRecords) {
            Double score = record.getGradeAsPercentage();

            // Skip manual-only course grades.
        	if ((record.getEnteredGrade() != null) && (score == null)) {
        		continue;
        	}
        	
        	if (score != null && record.getPointsEarned() != null) {
        		average = average.add(new BigDecimal(record.getPointsEarned().toString()));
        		total = total.add(new BigDecimal(score.toString()));
          	numScored++;
        	}
//        	numScored++;
        }
        if (numScored == 0) {
        	mean = null;
        	averageScore = null;
        } else {
        	mean = Double.valueOf(total.divide(new BigDecimal(numScored), GradebookService.MATH_CONTEXT).doubleValue());
        	averageScore = Double.valueOf(average.divide(new BigDecimal(numScored), GradebookService.MATH_CONTEXT).doubleValue());
        }
    }

		public Double getAverageScore()
		{
			return averageScore;
		}

		public void setAverageScore(Double averageScore)
		{
			this.averageScore = averageScore;
		}
}
