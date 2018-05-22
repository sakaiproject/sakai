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

/**
 * Represents the roles used in the gradebook. Users are categorised to one of these.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public enum GbRole {

	STUDENT("section.role.student"),
	TA("section.role.ta"),
	INSTRUCTOR("section.role.instructor"),
	NONE("section.role.none");

	private String value;

	GbRole(final String value) {
		this.value = value;
	}

	/**
	 * Get the actual name of the role
	 *
	 * @return
	 */
	public String getValue() {
		return this.value;
	}
}
