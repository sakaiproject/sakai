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

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseGroup;

/**
 * A detachable CourseSection for persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseGroupImpl extends LearningContextImpl implements CourseGroup, Comparable, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected Course course;
    protected String description;

    public CourseGroupImpl() {
    	// Default constructor needed by hibernate
    }

    public CourseGroupImpl(String uuid, Course course, String title, String description) {
		this.course = course;
		this.title = title;
		this.uuid = uuid;
		this.description = description;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int compareTo(Object o) {
		CourseGroup other = (CourseGroup)o;
		return title.compareTo(other.getTitle());
	}
}
