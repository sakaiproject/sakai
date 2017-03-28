package org.sakaiproject.gradebookng.business;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;

/**
 * Comparator class for sorting a category by the subtotals
 *
 * Note that this must have the categoryId set into it so we can extract the appropriate grade entry from the map that each student has.
 *
 */
public class CategorySubtotalComparator implements Comparator<GbStudentGradeInfo> {

	private final long categoryId;

	public CategorySubtotalComparator(final long categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public int compare(final GbStudentGradeInfo g1, final GbStudentGradeInfo g2) {

		final Double subtotal1 = g1.getCategoryAverages().get(this.categoryId);
		final Double subtotal2 = g2.getCategoryAverages().get(this.categoryId);

		return new CompareToBuilder().append(subtotal1, subtotal2).toComparison();
	}

}
