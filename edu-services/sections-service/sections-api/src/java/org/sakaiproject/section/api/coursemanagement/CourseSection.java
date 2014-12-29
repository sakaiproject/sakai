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
package org.sakaiproject.section.api.coursemanagement;

import java.util.List;

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
	 * The enterprise ID of this CourseSection.  CourseSections that model Sections from
	 * the CM service have enterprise IDs.  Manually created CourseSections have an
	 * EID of null.
	 * 
	 * @return The enterprise ID of the Section that this CourseSection models.
	 */
	public String getEid();

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
     * Gets the meetings for this CourseSection.
     * 
     * @return
     */
    public List<Meeting> getMeetings();
}
