package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by chmaurer on 3/16/15.
 */
@Data
public class ProcessedGradeItemStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Data is being updated
	 */
	public static final int STATUS_UPDATE = 0;

	/**
	 * New item to be added
	 */
	public static final int STATUS_NEW = 1;

	/**
	 * To skip
	 */
	public static final int STATUS_NA = 2;

	/**
	 * To be removed. Was used as a base value...
	 * @deprecated
	 */
	@Deprecated
	public static final int STATUS_UNKNOWN = 3;

	/**
	 * External assignment
	 */
	public static final int STATUS_EXTERNAL = 4;

	/**
	 * Title/points have been modified
	 */
	public static final int STATUS_MODIFIED = 5;


	@Getter
	@Setter
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
