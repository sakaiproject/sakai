package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.service.gradebook.shared.GraderPermission;

/**
 * Similar to GradeDefinition but serialisable and grader permission aware
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeInfo implements Serializable, Comparable<GbGradeInfo> {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String grade;

	@Getter
	private final String gradeComment;


	/**
	 * Functions available to the user on this grade
	 */
	@Getter
	@Setter
	private List<String> functions;

	/**
	 * Constructor. Takes a GradeDefinition or null. If null, a stub is created.
	 *
	 * @param gd GradeDefinition object. May be null
	 */
	public GbGradeInfo(final GradeDefinition gd) {

		// allows for a stub
		if (gd == null) {
			this.grade = null;
			this.gradeComment = null;
		} else {
			this.grade = gd.getGrade();
			this.gradeComment = gd.getGradeComment();
		}

		this.functions = new ArrayList<>(); 
	}

	public boolean canEdit() {
		return getFunctions().contains(GraderPermission.GRADE.toString());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Only compares grades
	 */
	@Override
	public int compareTo(final GbGradeInfo o) {
		return new CompareToBuilder()
				.append(this.grade, o.getGrade())
				.toComparison();

	}
}
