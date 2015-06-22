package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;

/**
 * DTO for storing data in the session so that state is preserved between requests.
 * Things like filters and ordering go in here and are persisted whenever something is set
 * They are then retrieved on the Gradebookpage load and passed around
 *
 */
public class GradebookUiSettings implements Serializable {

	private static final long serialVersionUID = 1L;

	public GradebookUiSettings() {
		this.categoriesEnabled = false;
	}

	@Getter @Setter
	private String groupFilter;
	
	/**
	 * For sorting based on assignment grades
	 */
	@Getter @Setter
	private GbAssignmentGradeSortOrder assignmentSortOrder;
	
	@Getter @Setter
	private boolean categoriesEnabled;
}
