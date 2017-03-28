package org.sakaiproject.gradebookng.business;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;

/**
 * Comparator class for sorting an assignment by the grades.
 *
 * Note that this must have the assignmentId set into it so we can extract the appropriate grade entry from the map that each student
 * has.
 *
 */
public class AssignmentGradeComparator implements Comparator<GbStudentGradeInfo> {

	private final long assignmentId;

	public AssignmentGradeComparator(final long assignmentId) {
		this.assignmentId = assignmentId;
	}

	@Override
	public int compare(final GbStudentGradeInfo g1, final GbStudentGradeInfo g2) {

		final GbGradeInfo info1 = g1.getGrades().get(this.assignmentId);
		final GbGradeInfo info2 = g2.getGrades().get(this.assignmentId);

		// for proper number ordering, these have to be numerical
		final Double grade1 = (info1 != null) ? NumberUtils.toDouble(info1.getGrade()) : null;
		final Double grade2 = (info2 != null) ? NumberUtils.toDouble(info2.getGrade()) : null;

		return new CompareToBuilder().append(grade1, grade2).toComparison();

	}

}
