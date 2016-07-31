package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Status of an imported item
 * TODO these values should be an enum rather than an ints
 */
public class ProcessedGradeItemStatus implements Serializable {

	private static final long serialVersionUID = 1L;


	public static final int STATUS_UPDATE = 0;
	public static final int STATUS_NEW = 1;
	public static final int STATUS_NA = 2;
	public static final int STATUS_UNKNOWN = 3;
	public static final int STATUS_EXTERNAL = 4;

	@Getter
	@Setter
	private int statusCode;

	@Getter
	@Setter
	private String statusValue;

	public ProcessedGradeItemStatus(final int statusCode) {
		this.statusCode = statusCode;
	}

	public ProcessedGradeItemStatus(final int statusCode, final String statusValue) {
		this.statusCode = statusCode;
		this.statusValue = statusValue;
	}
}
