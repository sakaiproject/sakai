/**********************************************************************************
*
* $Id: CourseSectionImpl.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.section;

import java.io.Serializable;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;

public class CourseSectionImpl extends LearningContextImpl implements CourseSection, Serializable {

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
	private String startTime;
	private boolean startTimeAm;
	private String endTime;
	private boolean endTimeAm;

    public CourseSectionImpl() {
    	// Default constructor needed by hibernate
    }


    public CourseSectionImpl(Course course, String title, String uuid, String category,
    		Integer maxEnrollments, String location, String startTime, boolean startTimeAm,
    		String endTime, boolean endTimeAm, boolean monday, boolean tuesday,
    		boolean wednesday, boolean thursday, boolean friday, boolean saturday,
    		boolean sunday) {
		this.course = course;
		this.title = title;
		this.uuid = uuid;
		this.category = category;
		this.maxEnrollments = maxEnrollments;
		this.location = location;
		this.startTime = startTime;
		this.startTimeAm = startTimeAm;
		this.endTime = endTime;
		this.endTimeAm = endTimeAm;
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
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public boolean isEndTimeAm() {
		return endTimeAm;
	}
	public void setEndTimeAm(boolean endTimeAm) {
		this.endTimeAm = endTimeAm;
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
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public boolean isStartTimeAm() {
		return startTimeAm;
	}
	public void setStartTimeAm(boolean startTimeAm) {
		this.startTimeAm = startTimeAm;
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
}


/**********************************************************************************
 * $Id: CourseSectionImpl.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
 *********************************************************************************/
