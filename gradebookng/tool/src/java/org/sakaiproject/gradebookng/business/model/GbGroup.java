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
import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a group or section
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class GbGroup implements Comparable<GbGroup>, Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;
	private final String title;
	private final String reference;
	private final Type type;

	/**
	 * Type of group
	 */
	public enum Type {
		GROUP,
		ALL;
	}

	@Override
	public int compareTo(final @NonNull GbGroup other) {
		return Comparator.comparing(GbGroup::getTitle)
				.thenComparing(GbGroup::getType)
				.compare(this, other);
	}

}
