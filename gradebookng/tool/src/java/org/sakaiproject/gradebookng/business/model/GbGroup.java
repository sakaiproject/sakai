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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import lombok.Getter;
import lombok.ToString;

/**
 * Represents a group or section
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@ToString
public class GbGroup implements Comparable<GbGroup>, Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String id;

	@Getter
	private final String title;

	@Getter
	private final String reference;

	@Getter
	private final Type type;

	/**
	 * Type of group
	 */
	public enum Type {
		SECTION,
		GROUP,
		ALL;
	}

	public GbGroup(final String id, final String title, final String reference, final Type type) {
		this.id = id;
		this.title = title;
		this.reference = reference;
		this.type = type;
	}

	@Override
	public int compareTo(final GbGroup other) {
		return new CompareToBuilder()
				.append(this.title, other.getTitle())
				.append(this.type, other.getType())
				.toComparison();

	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o.getClass() != getClass()) {
			return false;
		}
		final GbGroup other = (GbGroup) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(this.id, other.id)
				.append(this.title, other.title)
				.append(this.reference, other.reference)
				.append(this.type, other.type)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.id)
				.append(this.title)
				.append(this.reference)
				.append(this.type)
				.toHashCode();
	}

}
