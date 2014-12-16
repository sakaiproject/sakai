package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;

import lombok.Getter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;

/**
 * Similar to GradeDefinition but serialisable
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradeInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private String grade;
	
	@Getter
	private String gradeComment;
	
	public GradeInfo(GradeDefinition gd) {
		this.grade = gd.getGrade();
		this.gradeComment = gd.getGradeComment();
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
