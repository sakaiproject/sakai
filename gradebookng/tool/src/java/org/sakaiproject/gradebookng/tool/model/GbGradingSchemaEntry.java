package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper class for the grading schema entries. It is supplied as a Map which is difficult to work with in the UI so we turn it into a list
 * of these objects
 *
 */
public class GbGradingSchemaEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String grade;

	@Getter
	@Setter
	private Double minPercent;

	public GbGradingSchemaEntry(final String grade, final Double minPercent) {
		this.grade = grade;
		this.minPercent = minPercent;
	}
}
