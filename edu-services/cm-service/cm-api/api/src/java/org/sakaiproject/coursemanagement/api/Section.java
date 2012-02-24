/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.api;

import java.util.Set;

/**
 * Models a "cohort" (a stable group which enrolls in multiple courses as a unit)
 * as well as officially delimited course "groups" and "sections".
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface Section {

	/**
	 * A unique enterprise id
	 * @return
	 */
	public String getEid();
	public void setEid(String eid);

	/**
	 * What authority defines this object?
	 * @return 
	 */
	public String getAuthority();
	public void setAuthority(String authority);

	/**
	 * The title
	 * @return
	 */
	public String getTitle();
	public void setTitle(String title);

	/**
	 * A description
	 * @return
	 */
	public String getDescription();
	public void setDescription(String description);

	/**
	 * A category for this Section.  A category might be lecture, lab, discussion, or some
	 * other kind of classification.
	 * @return
	 */
	public String getCategory();
	public void setCategory(String category);
	
	/**
	 * The meeting time for this section.
	 * @return
	 */
	public Set<Meeting> getMeetings();
	public void setMeetings(Set<Meeting> meetingTimes);

	/**
	 * Gets the parent Section for this Section, or null if this is not a subSection.
	 * @return
	 */
	public Section getParent();
	public void setParent(Section parent);

	/**
	 * Gets the EnrollmentSet associated with this Section, if any.
	 * @return
	 */
	public EnrollmentSet getEnrollmentSet();
	public void setEnrollmentSet(EnrollmentSet enrollmentSet);
	
	/**
	 * Gets the enterprise ID of the Section's containing CourseOffering.
	 * @return
	 */
	public String getCourseOfferingEid();
	
	/**
	 * Gets maximum size allowed in a Section
	 * @return
	 */
	public Integer getMaxSize();
	public void setMaxSize(Integer maxSize);
}
