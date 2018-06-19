package org.sakaiproject.gradebookng.tool.component;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.model.IModel;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link GbBaseStatisticsPanel} to render course grade stats
 */
@Slf4j
public class GbCourseGradeStatisticsPanel extends GbBaseStatisticsPanel {

	private static final long serialVersionUID = 1L;
	private Map<String, CourseGrade> courseGradeMap;
	private String gradingSchemaName;

	public GbCourseGradeStatisticsPanel(final String id, final IModel<?> model) {
		super(id, model);
		// TODO Auto-generated constructor stub
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

		grades.forEach(g -> {
			stats.addValue(g);
		});

		return stats;
	}

	/**
	 * Calculates the average GPA for the course
	 *
	 * @return String average GPA
	 */
	private String getAverageGPA() {

		if (this.totalGrades < 1 && StringUtils.equals(this.gradingSchemaName, "Grade Points")) {
			return "-";
		} else if (StringUtils.equals(this.gradingSchemaName, "Grade Points")) {
			final Map<String, Double> gpaScoresMap = getGPAScoresMap();

			// get all of the non null mapped grades
			// mapped grades will be null if the student doesn't have a course grade yet.
			final List<String> mappedGrades = this.courseGradeMap.values().stream().filter(c -> c.getMappedGrade() != null)
					.map(c -> (c.getMappedGrade())).collect(Collectors.toList());
			Double averageGPA = 0.0;
			for (final String mappedGrade : mappedGrades) {
				// Note to developers. If you changed GradePointsMapping without changing gpaScoresMap, the average will be incorrect.
				// As per GradePointsMapping, both must be kept in sync
				final Double grade = gpaScoresMap.get(mappedGrade);
				if (grade != null) {
					averageGPA += grade;
				} else {
					log.debug(
							"Grade skipped when calculating course average GPA: " + mappedGrade + ". Calculated value will be incorrect.");
				}
			}
			averageGPA /= mappedGrades.size();

			return String.format("%.2f", averageGPA);
		} else {
			return null;
		}
	}


}

