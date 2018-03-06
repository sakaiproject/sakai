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
package org.sakaiproject.gradebookng.business;

/**
 * Represents the permissions used in the gradebook. The original String constants are not accessible so they are provided here for
 * convenience.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GbPortalPermission {

	GRADE_ALL("gradebook.gradeAll"),
	GRADE_SECTION("gradebook.gradeSection"),
	EDIT_ASSIGNMENTS("gradebook.editAssignments"),
	VIEW_OWN_GRADES("gradebook.viewOwnGrades");

	private String value;

	GbPortalPermission(final String value) {
		this.value = value;
	}

	/**
	 * Get the actual name of the permission
	 * 
	 * @return
	 */
	public String getValue() {
		return this.value;
	}

}
