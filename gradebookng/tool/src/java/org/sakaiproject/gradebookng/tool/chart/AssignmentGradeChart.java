/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.model.GbChartData;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradingType;

/**
 * Panel that renders the individual assignment grade charts
 */
public class AssignmentGradeChart extends BaseChart {

	private static final long serialVersionUID = 1L;

	private final long assignmentId;

	private final String studentGrade;

	public AssignmentGradeChart(final String id, final long assignmentId, final String studentGrade) {
		super(id);
		this.assignmentId = assignmentId;
		this.studentGrade = studentGrade;
	}

	/**
	 * Get chart data for this site
	 *
	 * @return
	 */
	@Override
	protected GbChartData getData() {

		try {
			// so students can get grade stats
			addAdvisor();

			final GradingType gradingType = GradingType.valueOf(this.businessService.getGradebook().getGrade_type());
			final Assignment assignment = this.businessService.getAssignment(this.assignmentId);
			final List<GbStudentGradeInfo> gradeInfo = this.businessService.buildGradeMatrix(Arrays.asList(assignment));

			// get all grades for this assignment
			final List<Double> allGrades = new ArrayList<>();
			for (int i = 0; i < gradeInfo.size(); i++) {
				final GbStudentGradeInfo studentGradeInfo = gradeInfo.get(i);

				final Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();
				final GbGradeInfo grade = studentGrades.get(this.assignmentId);

				if (grade == null || grade.getGrade() == null) {
					continue;
				}

				allGrades.add(Double.valueOf(grade.getGrade()));
			}
			Collections.sort(allGrades);

			final GbChartData data = new GbChartData();

			// Add 0-50% range
			data.addZeroed(buildRangeLabel(0, 50));

			// Add all ranges from 50 up to 100 in increments of 10.
			final int range = 10;
			for (int start = 50; start < 100; start = start + range) {
				data.addZeroed(buildRangeLabel(start, start + range));
			}

			for (final Double grade : allGrades) {
				if (isExtraCredit(grade, assignment, gradingType)) {
					data.add(getString("label.statistics.chart.extracredit"));
					continue;
				}

				final double percentage = this.getPercentage(grade, assignment, gradingType);
				data.add(determineKeyForGrade(percentage, range));
			}

			data.setChartTitle(MessageHelper.getString("label.statistics.chart.title"));
			data.setXAxisLabel(MessageHelper.getString("label.statistics.chart.xaxis"));
			data.setYAxisLabel(MessageHelper.getString("label.statistics.chart.yaxis"));
			data.setChartType("bar");
			data.setChartId(this.getMarkupId());
			if (this.studentGrade != null) {
				data.setStudentGradeRange(determineKeyForGrade(
						getPercentage(Double.valueOf(this.studentGrade), assignment, gradingType), range));
			}
			return data;

		} finally {
			removeAdvisor();
		}
	}

	/**
	 * Range labels are standard labels but use a translation key here
	 *
	 * @param start first number eg 0
	 * @param end second number eg 50
	 * @return eg "0-50" as a string, depending on translation
	 */
	private String buildRangeLabel(final int start, final int end) {
		return new StringResourceModel("label.statistics.chart.range", null, start, end).getString();
	}

	/**
	 * Check if a grade is considered extra credit
	 *
	 * @param grade
	 * @param assignment
	 * @param gradingType
	 * @return
	 */
	private boolean isExtraCredit(final Double grade, final Assignment assignment, final GradingType gradingType) {
		return (GradingType.PERCENTAGE.equals(gradingType) && grade > 100)
				|| (GradingType.POINTS.equals(gradingType) && grade > assignment.getPoints());
	}

	private String determineKeyForGrade(final double percentage, final int range) {
		final int total = Double.valueOf(Math.ceil(percentage) / range).intValue();

		int start = total * range;
		if (start == 100) {
			start = start - range;
		}

		if (start < 50) {
			return buildRangeLabel(0, 50);
		} else {
			return buildRangeLabel(start, start + range);
		}
	}

	private double getPercentage(final Double grade, final Assignment assignment, final GradingType gradingType) {
		if (GradingType.PERCENTAGE.equals(gradingType)) {
			return grade;
		} else {
			return grade / assignment.getPoints() * 100;
		}
	}

}