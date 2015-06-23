package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

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
		assignmentVisibility = new HashMap<Long, Boolean>();
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

	private Map<Long, Boolean> assignmentVisibility;

	public boolean isAssignmentVisible(Long assignmentId) {
		if (assignmentVisibility.containsKey(assignmentId)) {
			return assignmentVisibility.get(assignmentId);
		}
	
		return true;
	}

	public void setAssignmentVisibility(Long assignmentId, Boolean visible) {
		assignmentVisibility.put(assignmentId, visible);
	}
}
