/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.stats;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link BaseStatistics} to render course grade stats
 */
@Slf4j
public class CourseGradeStatistics extends BaseStatistics {

	private static final long serialVersionUID = 1L;
	private final Map<String, CourseGrade> courseGradeMap;
	private final Map<String, Double> bottomPercents;
	private final String gradingSchemaName;

	public CourseGradeStatistics(final String id, final IModel<?> model) {
		super(id, model);

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		this.courseGradeMap = (Map<String, CourseGrade>) modelData.get("courseGradeMap");

		// these are optional. currently only used for gpa stats
		this.gradingSchemaName = (String) modelData.get("gradingSchemaName");
		this.bottomPercents = (Map<String, Double>) modelData.get("bottomPercents");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// add average GPA but hidden if none
		add(new Label("averageGpa", getAverageGPA(getStatistics())) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return StringUtils.isNotBlank((String) getDefaultModelObject());
			}
		});

	}

	/**
	 * Calculates stats based on the calculated course grade values, excluding any empty grades
	 *
	 * @return {@link DescriptiveStatistics}
	 */
	@Override
	protected DescriptiveStatistics calculateStatistics() {

		final List<Double> grades = this.courseGradeMap.values().stream().filter(c -> StringUtils.isNotBlank(c.getMappedGrade()))
				.map(c -> NumberUtils.toDouble(c.getCalculatedGrade()))
				.collect(Collectors.toList());

		final DescriptiveStatistics stats = new DescriptiveStatistics();

		grades.forEach(g -> stats.addValue(g));

		return stats;
	}

	/**
	 * Calculates the average GPA for the course
	 *
	 * This is only applicable if Grade Points is the grading schema in use. However this is not currently checked.
	 *
	 * @return String average GPA
	 */
	private String getAverageGPA(final DescriptiveStatistics stats) {

		if (!StringUtils.equals(this.gradingSchemaName, "Grade Points")) {
			return null;
		}

		if (this.bottomPercents == null || this.bottomPercents.isEmpty()) {
			// cannot display averageGpa without bottomPercents
			return null;
		}

		if (stats.getN() == 0) {
			return "-";
		}

		// get all of the non null mapped grades
		// mapped grades will be null if the student doesn't have a course grade yet.
		final List<String> mappedGrades = this.courseGradeMap.values().stream().filter(c -> c.getMappedGrade() != null)
				.map(c -> (c.getMappedGrade())).collect(Collectors.toList());
		Double averageGPA = 0.0;
		for (final String mappedGrade : mappedGrades) {
			final Double grade = this.bottomPercents.get(mappedGrade);
			if (grade != null) {
				averageGPA += grade;
			} else {
				log.debug("Grade skipped when calculating course average GPA: {}. Calculated value will be incorrect.", mappedGrade);
			}
		}
		averageGPA /= mappedGrades.size();

		return String.format("%.2f", averageGPA);
	}

}
