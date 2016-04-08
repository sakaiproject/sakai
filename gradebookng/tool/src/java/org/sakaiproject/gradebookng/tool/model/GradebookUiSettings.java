package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.business.model.GbCategoryAverageSortOrder;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

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
		if (!this.categoryColors.containsKey(categoryName)) {
			setCategoryColor(categoryName, generateRandomRGBColorString());
		}

		return this.categoryColors.get(categoryName);
	}

	public void setCategoryColors(final List<CategoryDefinition> categories) {
		if (categories != null) {
			for (CategoryDefinition category : categories) {
				if (!this.categoryColors.containsKey(category.getName())) {
					setCategoryColor(category.getName(), generateRandomRGBColorString());
				}
			}
		}
	}

	/**
	 * Helper to generate a RGB CSS color string with values between 180-250 to ensure a lighter color e.g. rgb(181,222,199)
	 */
	private String generateRandomRGBColorString() {
		final Random rand = new Random();
		final int min = 180;
		final int max = 250;

		final int r = rand.nextInt((max - min) + 1) + min;
		final int g = rand.nextInt((max - min) + 1) + min;
		final int b = rand.nextInt((max - min) + 1) + min;

		return String.format("rgb(%d,%d,%d)", r, g, b);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
