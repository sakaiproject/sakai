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

package org.sakaiproject.tool.gradebook;

import java.math.BigDecimal;
import java.util.Collection;

import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * A CourseGrade is a GradableObject that represents the overall course grade in a gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class CourseGrade extends GradableObject {
	/**
	 *
	 */
	private static final long serialVersionUID = -7607255825842609208L;

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
	@Override
	public boolean isCourseGrade() {
		return true;
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.GradableObject#isAssignment()
	 */
	@Override
	public boolean isAssignment() {
		return false;
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.GradableObject#isCategory()
	 */
	@Override
	public boolean getIsCategory() {
		return false;
	}

	/**
	 * Calculate the mean course grade (whether entered or calulated) as a percentage for all enrollments, leaving students who've
	 * explicitly been given non-percentage-valued manual-only course grades (such as "I" for incomplete) or null scores out of the
	 * calculation.
	 */
	public void calculateStatistics(final Collection<CourseGradeRecord> gradeRecords, final int numEnrollments) {
		// Ungraded but enrolled students count as if they have 0% in the course.
		int numScored = numEnrollments - gradeRecords.size();
		BigDecimal total = new BigDecimal("0");
		BigDecimal average = new BigDecimal("0");

		for (final CourseGradeRecord record : gradeRecords) {
			final Double score = record.getGradeAsPercentage();

			// Skip manual-only course grades.
			if ((record.getEnteredGrade() != null) && (score == null)) {
				continue;
			}

			if (score != null && record.getPointsEarned() != null) {
				average = average.add(new BigDecimal(record.getPointsEarned().toString()));
				total = total.add(new BigDecimal(score.toString()));
				numScored++;
			}
		}
		if (numScored == 0) {
			this.mean = null;
			this.averageScore = null;
		} else {
			this.mean = Double.valueOf(total.divide(new BigDecimal(numScored), GradebookService.MATH_CONTEXT).doubleValue());
			this.averageScore = Double.valueOf(average.divide(new BigDecimal(numScored), GradebookService.MATH_CONTEXT).doubleValue());
		}
	}

	public Double getAverageScore() {
		return this.averageScore;
	}

	public void setAverageScore(final Double averageScore) {
		this.averageScore = averageScore;
	}
}
