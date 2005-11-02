/**********************************************************************************
*
* $Id: CourseSection.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
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
