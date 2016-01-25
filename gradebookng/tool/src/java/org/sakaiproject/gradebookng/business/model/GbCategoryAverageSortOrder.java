package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import org.sakaiproject.gradebookng.business.SortDirection;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for the fields we need to know when sorting a category by the subtotals within it
 */
public class GbCategoryAverageSortOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private long categoryId;

	@Getter
	@Setter
	private SortDirection direction;

	/**
	 * Constructor to set the values meaning this cannot be an empty object
	 *
	 * @param categoryId
	 * @param direction
	 */
	public GbCategoryAverageSortOrder(final long categoryId, final SortDirection direction) {
		this.categoryId = categoryId;
		this.direction = direction;
	}

}
