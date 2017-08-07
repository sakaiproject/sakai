package org.sakaiproject.gradebookng.business;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;

/**
 * Comparator class for sorting by extra student info like an internal id
 */
public class StudentExtraInfoComparator implements Comparator<GbStudentGradeInfo> {

	private final String studentProperty;

	public StudentExtraInfoComparator(final String studentProperty) {
		this.studentProperty = studentProperty;
	}

	@Override
	public int compare(final GbStudentGradeInfo g1, final GbStudentGradeInfo g2) {
		final Map<String, String> m1 = g1.getStudentExtraProperties();
		final Map<String, String> m2 = g2.getStudentExtraProperties();

		final String s1 = m1.containsKey(studentProperty) ? m1.get(studentProperty) : null;
		final String s2 = m2.containsKey(studentProperty) ? m2.get(studentProperty) : null;

		return new CompareToBuilder()
				.append(s1, s2)
				.toComparison();
	}

}
