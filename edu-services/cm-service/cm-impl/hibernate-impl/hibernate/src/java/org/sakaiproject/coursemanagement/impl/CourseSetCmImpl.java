/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;

public class CourseSetCmImpl extends AbstractMembershipContainerCmImpl
	implements CourseSet, Serializable {

	private static final long serialVersionUID = 1L;

	private CourseSet parent;
	private String category;
	private Set<CourseOffering> courseOfferings;
	private Set<CanonicalCourse> canonicalCourses;

	public CourseSetCmImpl() {}
	
	public CourseSetCmImpl(String eid, String title, String description, String category, CourseSet parent) {
		this.eid = eid;
		this.title = title;
		this.description = description;
		this.category = category;
		this.parent = parent;
	}
	
	public CourseSet getParent() {
		return parent;
	}
	public void setParent(CourseSet parent) {
		this.parent = parent;
	}
	

	public Set<CanonicalCourse> getCanonicalCourses() {
		return canonicalCourses;
	}

	public void setCanonicalCourses(Set<CanonicalCourse> canonicalCourses) {
		this.canonicalCourses = canonicalCourses;
	}

	public Set<CourseOffering> getCourseOfferings() {
		return courseOfferings;
	}

	public void setCourseOfferings(Set<CourseOffering> courseOfferings) {
		this.courseOfferings = courseOfferings;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
}
