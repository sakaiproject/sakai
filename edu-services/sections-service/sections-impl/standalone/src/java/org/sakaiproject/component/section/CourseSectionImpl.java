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
package org.sakaiproject.component.section;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;

/**
 * A detachable CourseSection for persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseSectionImpl extends LearningContextImpl implements CourseSection, Comparable, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected Course course;
	protected String category;
    protected Integer maxEnrollments;
    
    protected List meetings;

    /** Default constructor needed by hibernate */
    public CourseSectionImpl() {}

    /** Converts an arbitrary CourseSection into an instance of this class  */
    public CourseSectionImpl(CourseSection section) {
    	this.course = section.getCourse();
    	this.category = section.getCategory();
    	this.maxEnrollments = section.getMaxEnrollments();
    	this.meetings = section.getMeetings();
    	this.title = section.getTitle();
    	this.uuid = section.getUuid();
    }


    /**
     * Convenience constructor to create a CourseSection with a single meeting.
     * 
     * @param course
     * @param title
     * @param uuid
     * @param category
     * @param maxEnrollments
     * @param location
     * @param startTime
     * @param endTime
     * @param monday
     * @param tuesday
     * @param wednesday
     * @param thursday
     * @param friday
     * @param saturday
     * @param sunday
     */
    public CourseSectionImpl(Course course, String title, String uuid, String category,
    		Integer maxEnrollments, String location, Time startTime,
    		Time endTime, boolean monday, boolean tuesday,
    		boolean wednesday, boolean thursday, boolean friday, boolean saturday,
    		boolean sunday) {
    	
		this.course = course;
		this.title = title;
		this.uuid = uuid;
		this.category = category;
		this.maxEnrollments = maxEnrollments;
		this.meetings = new ArrayList();
		MeetingImpl meeting = new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
		meetings.add(meeting);
	}
    
    public CourseSectionImpl(Course course, String title, String uuid, String category,
    		Integer maxEnrollments, List meetings) {
		this.course = course;
		this.title = title;
		this.uuid = uuid;
		this.category = category;
		this.maxEnrollments = maxEnrollments;
    	this.meetings = meetings;
    }

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	public Integer getMaxEnrollments() {
		return maxEnrollments;
	}
	public void setMaxEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}
	public List getMeetings() {
		return meetings;
	}
	public void setMeetings(List meetings) {
		this.meetings = meetings;
	}

	/**
	 * Compares CourseSectionImpls based on their category ID and title.  Sections
	 * without a category are sorted last.
	 */
	public int compareTo(Object o) {
		if(o == this) {
			return 0;
		}
		if(o instanceof CourseSectionImpl) {
			CourseSectionImpl other = (CourseSectionImpl)o;
			if(this.category != null && other.category == null) {
				return -1;
			} else if(this.category == null && other.category != null) {
				return 1;
			}
			if(this.category == null && other.category == null) {
				return this.title.compareTo(other.title);
			}
			int categoryComparison = this.category.compareTo(other.category);
			if(categoryComparison == 0) {
				return this.title.compareTo(other.title);
			} else {
				return categoryComparison;
			}
		} else {
			throw new ClassCastException("Can not compare CourseSectionImpl to " + o.getClass());
		}
		
	}

	/**
	 * Standalone does not support the notion of enterprise-defined CourseSections.
	 */
	public String getEid() {
		return null;
	}
}
