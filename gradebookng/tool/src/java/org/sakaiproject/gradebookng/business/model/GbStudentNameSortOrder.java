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

/**
 * Types of sort order
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GbStudentNameSortOrder {

	LAST_NAME,
	FIRST_NAME;

	/**
	 * Get the default sort type
	 * 
	 * @return
	 */
	public GbStudentNameSortOrder getDefault() {
		return GbStudentNameSortOrder.LAST_NAME;
	}

	/**
	 * Get the next sort type
	 * 
	 * @return
	 */
	public GbStudentNameSortOrder toggle() {
		return values()[(ordinal() + 1) % values().length];
	}

}
