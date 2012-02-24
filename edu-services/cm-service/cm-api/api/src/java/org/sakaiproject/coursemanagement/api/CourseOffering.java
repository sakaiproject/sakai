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

import java.util.Date;
import java.util.Set;

/**
 * An instance of a course.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface CourseOffering {

	/**
	 * Gets the unique enterprise id of this CourseOffering.
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
	 * Gets the title of this CourseOffering.
	 * @return
	 */
	public String getTitle();
	public void setTitle(String title);
	
	/**
	 * Gets the description of this CourseOffering.
	 * @return
	 */
	public String getDescription();
	public void setDescription(String description);

	/**
	 * Gets the status of this CourseOffering.  This might be open, closed, planned, or discontinued, for example.
	 * @return
	 */
	public String getStatus();
	public void setStatus(String status);

	/**
	 * The AcademicSession for this course offering
	 * @return
	 */
	public AcademicSession getAcademicSession();
	public void setAcademicSession(AcademicSession academicSession);

	/**
	 * The date this CourseOffering starts (if any).  Typically, a CourseOffering
	 * starts when its AcademicSession starts.  Since this isn't necessarily true
	 * for every CourseOffering, the startDate can be set explicitly here.
	 * 
	 * @return
	 */
	public Date getStartDate();
	public void setStartDate(Date startDate);
	
	/**
	 * The date this CourseOffering ends (if any).  Typically, a CourseOffering
	 * ends when its AcademicSession ends.  Since this isn't necessarily true
	 * for every CourseOffering, the endDate can be set explicitly here.
	 * 
	 * @return
	 */
	public Date getEndDate();	
	public void setEndDate(Date endDate);
	
	/**
	 * Gets the enterprise ID of the CourseOffering's CanonicalCourse.
	 * @return
	 */
	public String getCanonicalCourseEid();

	/**
	 * Gets the Set <String> of course set EIDs that contain this canonical course.
	 * @return
	 */
	public Set<String> getCourseSetEids();

}
