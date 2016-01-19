package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by chmaurer on 3/16/15.
 */
@Data
public class ProcessedGradeItemStatus implements Serializable {

	public static final int STATUS_UPDATE = 0;
	public static final int STATUS_NEW = 1;
	public static final int STATUS_NA = 2;
	public static final int STATUS_UNKNOWN = 3;
	public static final int STATUS_EXTERNAL = 4;

	private int statusCode;
	private String statusValue;

	public ProcessedGradeItemStatus(final int statusCode) {
		this.statusCode = statusCode;
	}

	public ProcessedGradeItemStatus(final int statusCode, final String statusValue) {
		this.statusCode = statusCode;
		this.statusValue = statusValue;
	}
}
