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
package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper class for the grading schema entries. It is supplied as a Map which is difficult to work with in the UI so we turn it into a list
 * of these objects
 *
 * All comparisons are based on the minPercent ONLY.
 *
 */
public class GbGradingSchemaEntry implements Serializable, Comparable<GbGradingSchemaEntry> {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String grade;

	@Getter
	@Setter
	private Double minPercent;

	public GbGradingSchemaEntry(final String grade, final Double minPercent) {
		this.grade = grade;
		this.minPercent = minPercent;
	}

	@Override
	public int compareTo(final GbGradingSchemaEntry other) {
		return new CompareToBuilder().append(this.minPercent, other.minPercent).toComparison();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		final GbGradingSchemaEntry other = (GbGradingSchemaEntry) o;
		return new EqualsBuilder().append(this.minPercent, other.minPercent).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.minPercent).toHashCode();
	}

}
