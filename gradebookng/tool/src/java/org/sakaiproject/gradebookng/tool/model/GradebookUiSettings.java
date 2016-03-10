package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.business.model.GbCategoryAverageSortOrder;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for storing data in the session so that state is preserved between requests. Things like filters and ordering go in here and are
 * persisted whenever something is set.
 *
 * They are then retrieved on the GradebookPage load and passed around.
 *
 */
public class GradebookUiSettings implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Stores the selected group/section
	 */
	@Getter
	@Setter
	private GbGroup groupFilter;

	/**
	 * For sorting based on assignment grades
	 */
	@Getter
	@Setter
	private GbAssignmentGradeSortOrder assignmentSortOrder;

	@Getter
	@Setter
	private boolean categoriesEnabled;

	private final Map<Long, Boolean> assignmentVisibility;
	private final Map<String, Boolean> categoryScoreVisibility;
	private final Map<String, String> categoryColors;

	/**
	 * For sorting based on first name / last name
	 */
	@Getter
	@Setter
	private GbStudentNameSortOrder nameSortOrder;

	/**
	 * For sorting based on category
	 */
	@Getter
	@Setter
	private GbCategoryAverageSortOrder categorySortOrder;

	/**
	 * For showing/hiding the points
	 */
	@Getter
	@Setter
	private Boolean showPoints;

	public GradebookUiSettings() {
		// defaults. Note there is no default for assignmentSortOrder as that requires an assignmentId which will differ between gradebooks
		this.categoriesEnabled = false;
		this.assignmentVisibility = new HashMap<Long, Boolean>();
		this.categoryScoreVisibility = new HashMap<String, Boolean>();
		this.nameSortOrder = GbStudentNameSortOrder.LAST_NAME;
		this.categoryColors = new HashMap<String, String>();
		this.showPoints = false;
	}

	public boolean isAssignmentVisible(final Long assignmentId) {
		return (this.assignmentVisibility.containsKey(assignmentId)) ? this.assignmentVisibility.get(assignmentId) : true;
	}

	public void setAssignmentVisibility(final Long assignmentId, final Boolean visible) {
		this.assignmentVisibility.put(assignmentId, visible);
	}

	public boolean isCategoryScoreVisible(final String category) {
		return (this.categoryScoreVisibility.containsKey(category)) ? this.categoryScoreVisibility.get(category) : true;
	}

	public void setCategoryScoreVisibility(final String category, final Boolean visible) {
		this.categoryScoreVisibility.put(category, visible);
	}

	public void setCategoryColor(final String categoryName, final String rgbColorString) {
		this.categoryColors.put(categoryName, rgbColorString);
	}

	public String getCategoryColor(final String categoryName) {
		return this.categoryColors.get(categoryName);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
