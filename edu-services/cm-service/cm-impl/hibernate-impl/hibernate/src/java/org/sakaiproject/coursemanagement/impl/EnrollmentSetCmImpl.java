/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.impl;

import java.io.Serializable;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;

public class EnrollmentSetCmImpl extends AbstractNamedCourseManagementObjectCmImpl
	implements EnrollmentSet, Serializable {

	private static final long serialVersionUID = 1L;

	private String category;
	private String defaultEnrollmentCredits;
	private CourseOffering courseOffering;
	private Set officialInstructors;
	
	public EnrollmentSetCmImpl () {}
	
	public EnrollmentSetCmImpl(String eid, String title, String description, String category,
			String defaultEnrollmentCredits, CourseOffering courseOffering, Set officialInstructors) {
		this.eid = eid;
		this.title = title;
		this.description = description;
		this.category = category;
		this.defaultEnrollmentCredits = defaultEnrollmentCredits;
		this.courseOffering = courseOffering;
		this.officialInstructors = officialInstructors;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public CourseOffering getCourseOffering() {
		return courseOffering;
	}

	public void setCourseOffering(CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
	}

	public String getDefaultEnrollmentCredits() {
		return defaultEnrollmentCredits;
	}

	public void setDefaultEnrollmentCredits(String defaultEnrollmentCredits) {
		this.defaultEnrollmentCredits = defaultEnrollmentCredits;
	}

	public Set getOfficialInstructors() {
		return officialInstructors;
	}
	public void setOfficialInstructors(Set officialInstructors) {
		this.officialInstructors = officialInstructors;
	}
	
	@Override
	public String getTitle() {
		if (isTitleEmpty() && courseOffering != null) {
			return courseOffering.getTitle();
		}

		return title;
	}

	@Override
	public String getDescription() {
		if (isDescriptionEmpty() && courseOffering != null) {
			return courseOffering.getDescription();
		}

		return description;
	}

}
