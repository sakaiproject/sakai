/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook;

import java.util.Collection;
import java.util.Iterator;

/**
 * A CourseGrade is a GradableObject that represents the overall course grade
 * in a gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class CourseGrade extends GradableObject {
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

	//// Bean getters and setters ////

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



