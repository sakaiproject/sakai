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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Describes the type of column imported
 */
@NoArgsConstructor
@AllArgsConstructor
public class ImportedColumn implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String columnTitle;

	@Getter
	@Setter
	private String points;

	@Getter
	@Setter
	private Type type;

	public enum Type {
		GB_ITEM_WITH_POINTS,
		GB_ITEM_WITHOUT_POINTS,
		COMMENTS,
		USER_ID,
		USER_NAME,
		IGNORE;
	}

	/**
	 * Helper to determine if the type of column can be ignored
	 * 
	 * @return
	 */
	public boolean isIgnorable() {
		if (this.type == Type.USER_ID || this.type == Type.USER_NAME || this.type == Type.IGNORE) {
			return true;
		}
		return false;
	}

	/**
	 * Helper to determine if the type of column is a gradeItem
	 * 
	 * @return
	 */
	public boolean isGradeItem() {
		if (this.type == Type.GB_ITEM_WITH_POINTS || this.type == Type.GB_ITEM_WITHOUT_POINTS) {
			return true;
		}
		return false;
	}

	/**
	 * Helper to determine if the type of column is a comment column - purely for convenience
	 * 
	 * @return
	 */
	public boolean isComment() {
		if (this.type == Type.COMMENTS) {
			return true;
		}
		return false;
	}

	/**
	 * Column titles are the only thing we care about for comparisons so that we can filter out duplicates. Must also match type and exclude
	 * IGNORE
	 */
	@Override
	public boolean equals(final Object o) {
		final ImportedColumn other = (ImportedColumn) o;
		if (StringUtils.isBlank(this.columnTitle) || StringUtils.isBlank(other.columnTitle)) {
			return false;
		}
		if (this.type == Type.IGNORE || other.type == Type.IGNORE) {
			return false;
		}

		// we allow columns names to be the same but of different cases (eg "Assignment 1" and "assignment 1" are both valid and unique)
		if (StringUtils.equals(this.columnTitle, other.getColumnTitle()) && this.type == other.getType()) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.columnTitle)
				.append(this.points)
				.append(this.type)
				.toHashCode();
	}
}
