/**********************************************************************************
*
* $Id: CourseSectionImpl.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;

public class CourseSectionImpl extends LearningContextImpl implements CourseSection {

	protected Course course;
	protected String category;
    protected String meetingTimes;
    protected String location;

    public CourseSectionImpl() {
    	// Default constructor needed by hibernate
    }

    public CourseSectionImpl(Course course, String title, String category, String meetingTimes, String location, String uuid) {
		this.course = course;
		this.title = title;
		this.category = category;
		this.meetingTimes = meetingTimes;
		this.location = location;
		this.uuid = uuid;
	}

	public String getMeetingTimes() {
        return meetingTimes;
    }
    public void setMeetingTimes(String meetingTimes) {
        this.meetingTimes = meetingTimes;
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}


/**********************************************************************************
 * $Id: CourseSectionImpl.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
 *********************************************************************************/
