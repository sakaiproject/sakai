/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;

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
    protected String location;
    protected Integer maxEnrollments;
    
    // FIXME Replace this with a scheduling service
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	private Time startTime;
	private Time endTime;

    public CourseSectionImpl() {
    	// Default constructor needed by hibernate
    }


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
		this.location = location;
		this.startTime = startTime;
		this.endTime = endTime;
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
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
	public Time getEndTime() {
		return endTime;
	}
	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}
	public boolean isFriday() {
		return friday;
	}
	public void setFriday(boolean friday) {
		this.friday = friday;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Integer getMaxEnrollments() {
		return maxEnrollments;
	}
	public void setMaxEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}
	public boolean isMonday() {
		return monday;
	}
	public void setMonday(boolean monday) {
		this.monday = monday;
	}
	public boolean isSaturday() {
		return saturday;
	}
	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}
	public Time getStartTime() {
		return startTime;
	}
	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}
	public boolean isSunday() {
		return sunday;
	}
	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}
	public boolean isThursday() {
		return thursday;
	}
	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}
	public boolean isTuesday() {
		return tuesday;
	}
	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}
	public boolean isWednesday() {
		return wednesday;
	}
	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
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

}
