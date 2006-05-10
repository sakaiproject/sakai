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
package org.sakaiproject.api.section.coursemanagement;

import java.sql.Time;

/**
 * A subset of a Course that may meet at specific times during the week.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface CourseSection extends LearningContext {
	/**
	 * Gets the Course that this CourseSection belongs to
	 * 
	 * @return
	 */
	public Course getCourse();

	/**
	 * Gets the location where this CourseSection meets.
	 * @return
	 */
    public String getLocation();
    
    /**
     * Gets the category ID of this CourseSection.  Students may be enrolled in
     * only one section of a given category per Course.
     * 
     * @return
     */
    public String getCategory();
    
    /**
     * Gets the maximum number of enrollments allowed in this CourseSection.
     * Instructors and TAs may assign more than the maximum number of enrollments,
     * but students may not self enroll in a section at or above the maximum
     * number of enrollments.
     * 
     * @return
     */
    public Integer getMaxEnrollments();
    
	/**
	 * Whether the CourseSection meets on Mondays.
	 * 
	 * @return
	 */
    public boolean isMonday();

	/**
	 * Whether the CourseSection meets on Tuesdays.
	 * 
	 * @return
	 */
    public boolean isTuesday();
    
	/**
	 * Whether the CourseSection meets on Wednesdays.
	 * 
	 * @return
	 */
	public boolean isWednesday();

	/**
	 * Whether the CourseSection meets on Thursdays.
	 * 
	 * @return
	 */
	public boolean isThursday();

	/**
	 * Whether the CourseSection meets on Fridays.
	 * 
	 * @return
	 */
	public boolean isFriday();

	/**
	 * Whether the CourseSection meets on Saturdays.
	 * 
	 * @return
	 */
	public boolean isSaturday();

	/**
	 * Whether the CourseSection meets on Sundays.
	 * 
	 * @return
	 */
	public boolean isSunday();
	
	/**
	 * Gets the time of day that this CourseSection's meeting(s) start.
	 * 
	 * @return
	 */
	public Time getStartTime();

	/**
	 * Gets the time of day that this CourseSection's meeting(s) end.
	 * 
	 * @return
	 */
	public Time getEndTime();
}
