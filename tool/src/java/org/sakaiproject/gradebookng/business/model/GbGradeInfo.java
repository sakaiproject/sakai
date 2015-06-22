package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;

/**
 * Similar to GradeDefinition but serialisable
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeInfo implements Serializable, Comparable<GbGradeInfo> {

	private static final long serialVersionUID = 1L;

	@Getter
	private String grade;
	
	@Getter
	private String gradeComment;
	
	public GbGradeInfo(GradeDefinition gd) {
		this.grade = gd.getGrade();
		this.gradeComment = gd.getGradeComment();
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Only compares grades
	 */
	@Override
	public int compareTo(GbGradeInfo o) {
		return new CompareToBuilder()
			.append(this.grade, o.getGrade())
			.toComparison();
	
	}
	
}
