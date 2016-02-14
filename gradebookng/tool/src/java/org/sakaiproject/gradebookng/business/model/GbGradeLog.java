package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.sakaiproject.tool.gradebook.GradingEvent;

import lombok.Getter;

/**
 * DTO for grade log events.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final Date dateGraded;

	@Getter
	private final String graderUuid;

	@Getter
	private final String grade;

	public GbGradeLog(final GradingEvent ge) {
		this.dateGraded = ge.getDateGraded();
		this.graderUuid = ge.getGraderId();
		this.grade = ge.getGrade();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
