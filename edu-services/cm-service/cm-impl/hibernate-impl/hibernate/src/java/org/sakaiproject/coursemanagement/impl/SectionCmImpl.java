/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
import org.sakaiproject.coursemanagement.api.Section;

public class SectionCmImpl extends AbstractMembershipContainerCmImpl
	implements Section, Serializable {

	private static final long serialVersionUID = 1L;

	private String category;
	private Set meetings;
	private CourseOffering courseOffering;
	private String courseOfferingEid; // We keep this here to avoid lazy loading of the courseOffering
	private Section parent;
	private EnrollmentSet enrollmentSet;
        private Integer maxSize;
	
	public SectionCmImpl() {}
	
    public SectionCmImpl(String eid, String title, String description, String category, Section parent, CourseOffering courseOffering, EnrollmentSet enrollmentSet, Integer maxSize) {
		this.eid = eid;
		this.title = title;
		this.description = description;
		this.category = category;
		this.parent = parent;
		this.courseOffering = courseOffering;
		if(courseOffering != null) {
			this.courseOfferingEid = courseOffering.getEid();
		}
		this.enrollmentSet = enrollmentSet;
                this.maxSize = maxSize;
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
	public String getCourseOfferingEid() {
		return courseOfferingEid;
	}
	public void setCourseOffering(CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
		if(courseOffering == null) {
			this.courseOfferingEid = null;
		} else {
			this.courseOfferingEid = courseOffering.getEid(); // Make sure we update the cached eid
		}
	}
	public Section getParent() {
		return parent;
	}
	public void setParent(Section parent) {
		this.parent = parent;
	}
	public EnrollmentSet getEnrollmentSet() {
		return enrollmentSet;
	}
	public void setEnrollmentSet(EnrollmentSet enrollmentSet) {
		this.enrollmentSet = enrollmentSet;
	}
	public Set getMeetings() {
		return meetings;
	}
	public void setMeetings(Set meetings) {
		this.meetings = meetings;
	}
	public Integer getMaxSize() {
		return maxSize;
	}
        public void setMaxSize(Integer maxSize) {
	    this.maxSize = maxSize;
	}

	@Override
	public String getTitle() {
		if (isTitleEmpty() && enrollmentSet != null) {
			return enrollmentSet.getTitle();
		}

		return title;
	}

	@Override
	public String getDescription() {
		if (isDescriptionEmpty() && enrollmentSet != null) {
			return enrollmentSet.getDescription();
		}

		return description;
	}
}
