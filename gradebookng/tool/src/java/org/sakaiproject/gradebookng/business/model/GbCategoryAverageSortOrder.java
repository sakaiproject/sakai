/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
