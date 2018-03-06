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
package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * Holds the data about a grade item that is imported from the spreadsheet as well as any edits that happen through the wizard
 *
 * TODO rename to ProcessedColumn
 */
@ToString
public class ProcessedGradeItem implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	/**
	 * Indicate the type of column
	 */
	@Getter
	@Setter
	private Type type;

	public enum Type {

		// order here is important and used for comparisons. gb items come before comments

		/**
		 * A gradebook item
		 */
		GB_ITEM,

		/**
		 * Comments attached to a gradebook item
		 */
		COMMENT
	}

	public enum Status {

		/**
		 * Data is being updated
		 */
		UPDATE,

		/**
		 * New item to be added
		 */
		NEW,

		/**
		 * To skip
		 */
		SKIP,

		/**
		 * External assignment
		 */
		EXTERNAL,

		/**
		 * Title/points have been modified
		 */
		MODIFIED
	}

	@Getter
	@Setter
	private String itemTitle;

	@Getter
	@Setter
	private Long itemId;

	@Getter
	@Setter
	private String itemPointValue;

	@Getter
	@Setter
	private Status status;

	@Getter
	@Setter
	private List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<ProcessedGradeItemDetail>();

	/**
	 * Flag set in the selection screen for whether or not this item is selected
	 */
	@Getter
	@Setter
	private boolean selected;

	/**
	 * Helper to determine if an items is in a state that can be selected
	 * 
	 * @return
	 */
	public boolean isSelectable() {
		return this.status == Status.NEW || this.status == Status.UPDATE || this.status == Status.MODIFIED;
	}

	@Override
	public int compareTo(final Object o) {
		// sort by title then type order as above
		final ProcessedGradeItem other = (ProcessedGradeItem) o;
		return new CompareToBuilder()
				.append(this.itemTitle, other.itemTitle)
				.append(this.type.ordinal(), other.type.ordinal())
				.toComparison();
	}

	/**
	 * Collection of fields from the edited assignment. These may differ to the imported fields and need to be used on the confirmation screen.
	 */
	@Getter
	@Setter
	private String assignmentTitle;

	@Getter
	@Setter
	private Double assignmentPoints;
}
