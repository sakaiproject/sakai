package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import org.sakaiproject.gradebookng.business.SortDirection;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for the fields we need to know when sorting an assignment by its grades
 */
public class GbAssignmentGradeSortOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private long assignmentId;

	@Getter
	@Setter
	private SortDirection direction;

	/**
	 * Constructor to set the values meaning this cannot be an empty object
	 * 
	 * @param assignmentId
	 * @param direction
	 */
	public GbAssignmentGradeSortOrder(final long assignmentId, final SortDirection direction) {
		this.assignmentId = assignmentId;
		this.direction = direction;
	}

}
