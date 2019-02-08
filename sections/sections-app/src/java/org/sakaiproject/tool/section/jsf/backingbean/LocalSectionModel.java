/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.Meeting;

@Slf4j
public class LocalSectionModel implements CourseSection, Serializable {
	private static final long serialVersionUID = 1L;

	private Course course;
	private String uuid;
	private String title;
	private String category;
	private Integer maxEnrollments;
	private boolean isLocked;

	// We need a string to represent size limit due to this JSF bug: http://issues.apache.org/jira/browse/MYFACES-570
	private String limitSize;

	private List<Meeting> meetings;

	public LocalSectionModel() {}
	
	public LocalSectionModel(Course course, String title, String category, String uuid) {
		this.uuid = uuid;
		this.title = title;
		this.category = category;
		limitSize = Boolean.FALSE.toString();
	}
	
	public LocalSectionModel(CourseSection section) {
		this.course = section.getCourse();
		this.uuid = section.getUuid();
		this.title = section.getTitle();
		this.category = section.getCategory();
		this.maxEnrollments = section.getMaxEnrollments();
		if(maxEnrollments == null) {
			limitSize = Boolean.FALSE.toString();
		} else {
			limitSize = Boolean.TRUE.toString();
		}
		this.meetings = new ArrayList<Meeting>();
		for(Iterator iter = section.getMeetings().iterator(); iter.hasNext();) {
			Meeting meeting = (Meeting)iter.next();
			meetings.add(new LocalMeetingModel(meeting));
		}
		this.isLocked = section.isLocked();
	}
    
    public Integer getMaxEnrollments() {
		return maxEnrollments;
	}
	public void setMaxEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<Meeting> getMeetings() {
		if(meetings == null) {
			// Keep this out of the constructor to avoid it in deserialization
			this.meetings = new ArrayList<Meeting>();
		}
		return meetings;
	}

	public void setMeetings(List<Meeting> meetings) {
		this.meetings = meetings;
	}

    public boolean isLocked() {
		return isLocked;
	}

	public String toString() {
		return new ToStringBuilder(this).append(title).append(maxEnrollments).toString();
	}

	public String getLimitSize() {
		return limitSize;
	}

	public void setLimitSize(String limitSize) {
		this.limitSize = limitSize;
	}

	public String getCategory() {
		return category;
	}

	public Course getCourse() {
		return course;
	}

	public String getUuid() {
		return uuid;
	}

	/**
	 * Enterprise ID is not needed in this app.
	 */
	public String getEid() {
		return null;
	}
}
