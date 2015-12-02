package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.sakaiproject.tool.gradebook.GradingEvent;

/**
 * DTO for the grade log
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private Date dateGraded;
	
	@Getter
	private String graderUuid;
	
	@Getter
	private String grade;
	
	public GbGradeLog(GradingEvent ge){
		this.dateGraded = ge.getDateGraded();
		this.graderUuid = ge.getGraderId();
		this.grade = ge.getGrade();
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	
}
