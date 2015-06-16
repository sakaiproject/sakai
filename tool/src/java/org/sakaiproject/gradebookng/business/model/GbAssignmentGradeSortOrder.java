package org.sakaiproject.gradebookng.business.model;

import org.sakaiproject.gradebookng.business.SortDirection;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for the fields we need to know when sorting an assignment by its grades
 */
public class GbAssignmentGradeSortOrder {

	@Getter @Setter
	private long assignmentId;
	
	@Getter @Setter
	private SortDirection direction;
	
}
