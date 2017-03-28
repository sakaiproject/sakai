package org.sakaiproject.gradebookng.business;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

/**
 * Comparator class for sorting by course grade, first by the letter grade's index in the gradebook's grading scale and then by the
 * number of points the student has earned.
 */
public class CourseGradeComparator implements Comparator<GbStudentGradeInfo> {

	private List<String> ascendingGrades;

	public CourseGradeComparator(final GradebookInformation gradebookInformation) {
		final Map<String, Double> gradeMap = gradebookInformation.getSelectedGradingScaleBottomPercents();
		this.ascendingGrades = new ArrayList<>(gradeMap.keySet());
		this.ascendingGrades.sort(new Comparator<String>() {
			@Override
			public int compare(final String a, final String b) {
				return new CompareToBuilder()
						.append(gradeMap.get(a), gradeMap.get(b))
						.toComparison();
			}
		});
	}

	@Override
	public int compare(final GbStudentGradeInfo g1, final GbStudentGradeInfo g2) {
		final CourseGrade cg1 = g1.getCourseGrade().getCourseGrade();
		final CourseGrade cg2 = g2.getCourseGrade().getCourseGrade();

		String letterGrade1 = cg1.getMappedGrade();
		if (cg1.getEnteredGrade() != null) {
			letterGrade1 = cg1.getEnteredGrade();
		}
		String letterGrade2 = cg2.getMappedGrade();
		if (cg2.getEnteredGrade() != null) {
			letterGrade2 = cg2.getEnteredGrade();
		}

		final int gradeIndex1 = this.ascendingGrades.indexOf(letterGrade1);
		final int gradeIndex2 = this.ascendingGrades.indexOf(letterGrade2);

		final Double calculatedGrade1 = cg1.getCalculatedGrade() == null ? null : Double.valueOf(cg1.getCalculatedGrade());
		final Double calculatedGrade2 = cg2.getCalculatedGrade() == null ? null : Double.valueOf(cg2.getCalculatedGrade());

		return new CompareToBuilder()
				.append(gradeIndex1, gradeIndex2)
				.append(calculatedGrade1, calculatedGrade2)
				.toComparison();
	}

}
