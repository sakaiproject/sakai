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
	private final Map<String, Double> gpaScoresMap;
	private final String gradingSchemaName;

	public CourseGradeStatistics(final String id, final IModel<?> model) {
		super(id, model);

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		this.courseGradeMap = (Map<String, CourseGrade>) modelData.get("courseGradeMap");

		// these are optional
		// but, gpaScoresMap must be set if Grade Points is the grading schema
		this.gradingSchemaName = (String) modelData.get("gradingSchemaName");
		this.gpaScoresMap = (Map<String, Double>) modelData.get("gpaScoresMap");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new Label("average", getAverageGPA(getStatistics())));
		/// TODO hide if null via enclosure

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

		if (stats.getN() == 0) {
			return "-";
		}

		// get all of the non null mapped grades
		// mapped grades will be null if the student doesn't have a course grade yet.
		final List<String> mappedGrades = this.courseGradeMap.values().stream().filter(c -> c.getMappedGrade() != null)
				.map(c -> (c.getMappedGrade())).collect(Collectors.toList());
		Double averageGPA = 0.0;
		for (final String mappedGrade : mappedGrades) {
			// Note to developers. If you changed GradePointsMapping without changing gpaScoresMap, the average will be incorrect.
			// As per GradePointsMapping, both must be kept in sync
			final Double grade = this.gpaScoresMap.get(mappedGrade);
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
