package org.sakaiproject.gradebookng.tool.stats;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;

/**
 * Implementation of {@link BaseStatistics} to render assignment stats
 */
public class AssignmentStatistics extends BaseStatistics {

	private static final long serialVersionUID = 1L;
	private final List<GbStudentGradeInfo> gradeInfo;
	private final Long assignmentId;

	public AssignmentStatistics(final String id, final IModel<?> model) {
		super(id, model);

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		this.gradeInfo = (List<GbStudentGradeInfo>) modelData.get("gradeInfo");
		this.assignmentId = (Long) modelData.get("assignmentId");
	}

	/**
	 * Calculates stats based on the calculated course grade values, excluding any empty grades
	 *
	 * @return {@link DescriptiveStatistics}
	 */
	@Override
	protected DescriptiveStatistics calculateStatistics() {

		final DescriptiveStatistics stats = new DescriptiveStatistics();

		for (int i = 0; i < this.gradeInfo.size(); i++) {
			final GbStudentGradeInfo studentGradeInfo = this.gradeInfo.get(i);

			final Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();
			final GbGradeInfo grade = studentGrades.get(this.assignmentId);

			if (grade != null && grade.getGrade() != null) {
				stats.addValue(Double.valueOf(grade.getGrade()));
			}
		}

		return stats;
	}

}
