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
import java.util.Objects;

import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.tool.model.GbChartData;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.MessageHelper;
import org.sakaiproject.util.ResourceLoader;

/**
 * Panel that renders the individual assignment grade charts
 */
public class AssignmentGradeChart extends BaseChart {

	private static final long serialVersionUID = 1L;

	private final long assignmentId;

	private final String studentGrade;

	@SuppressWarnings("unchecked")
	private static ResourceLoader RL = new ResourceLoader();

	//public AssignmentGradeChart(final String id, final IModel<?> model) {
	public AssignmentGradeChart(String id, long assignmentId, String studentGrade) {
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

			GradebookInformation info = businessService.getGradebookSettings(currentGradebookUid, currentSiteId);
			final GradeType gradingType = info.getGradeType();
			final Assignment assignment = businessService.getAssignment(currentGradebookUid, currentSiteId, this.assignmentId);
			final List<GbStudentGradeInfo> gradeInfo = businessService.buildGradeMatrix(currentGradebookUid, currentSiteId, Arrays.asList(assignment), this.businessService.getGradeableUsers(currentGradebookUid, currentSiteId, null), null);
        	final Map<String, Double> gradeMap = info.getSelectedGradingScaleBottomPercents();

			// get all grades for this assignment
			final List allGrades = new ArrayList();
            for (final GbStudentGradeInfo studentGradeInfo : gradeInfo) {
                final Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();
                final GbGradeInfo grade = studentGrades.get(this.assignmentId);

                if (grade == null || grade.getGrade() == null) {
                    continue;
                }

                allGrades.add(gradingType == GradeType.LETTER ? grade.getGrade() : Double.valueOf(grade.getGrade()));
            }
			Collections.sort(allGrades);

			final GbChartData data = new GbChartData();

			if (gradingType != GradeType.LETTER) {
				// Add 0-50% range
				data.addZeroed(buildRangeLabel(0, 50));

				// Add all ranges from 50 up to 100 in increments of 10.
				final int range = 10;
				for (int start = 50; start < 100; start = start + range) {
					data.addZeroed(buildRangeLabel(start, start + range));
				}

				for (final Double grade : (List<Double>) allGrades) {
					if (getExtraCredit(grade, assignment, gradingType)) {
						data.add(getString("label.statistics.chart.extracredit"));
						continue;
					}

					final double percentage = this.getPercentage(grade, assignment, gradingType);
					data.add(determineKeyForGrade(percentage, range));
				}

				if (this.studentGrade != null) {
					data.setStudentGradeRange(determineKeyForGrade(
							getPercentage(Double.valueOf(this.studentGrade), assignment, gradingType), range));
				}
				data.setXAxisLabel(MessageHelper.getString("label.statistics.chart.xaxis", RL.getLocale()));
			} else {
				gradeMap.keySet().forEach(data::addZeroed);
				((List<String>) allGrades).forEach(data::add);
				data.setXAxisLabel(MessageHelper.getString("label.statistics.chart.letter_xaxis", RL.getLocale()));

				if (this.studentGrade != null) {
					data.setStudentGradeRange(this.studentGrade);
				}
			}

			data.setChartTitle(MessageHelper.getString("label.statistics.chart.title", RL.getLocale()));
			data.setYAxisLabel(MessageHelper.getString("label.statistics.chart.yaxis", RL.getLocale()));
			data.setChartType("bar");
			data.setChartId(this.getMarkupId());
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
		return new StringResourceModel("label.statistics.chart.range").setParameters(start, end).getString();
	}

	/**
	 * Check if a grade is considered extra credit
	 *
	 * @param grade
	 * @param assignment
	 * @param gradingType
	 * @return
	 */
	private boolean getExtraCredit(Double grade, Assignment assignment, GradeType gradingType) {
		return (gradingType == GradeType.PERCENTAGE && grade > 100)
				|| (gradingType == GradeType.POINTS && grade > assignment.getPoints());
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

	private double getPercentage(Double grade, Assignment assignment, GradeType gradingType) {
		if (gradingType == GradeType.PERCENTAGE) {
			return grade;
		} else {
			return grade / assignment.getPoints() * 100;
		}
	}

}
