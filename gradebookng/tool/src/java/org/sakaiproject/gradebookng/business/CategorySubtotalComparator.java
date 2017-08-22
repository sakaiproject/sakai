/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
