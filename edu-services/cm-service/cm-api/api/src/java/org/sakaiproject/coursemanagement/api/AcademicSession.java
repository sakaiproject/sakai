/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

/**
 * <p>
 * An institutional context for CourseOfferings, distinguishing one instance of
 * a CanonicalCourse from another.  In higher educational institutions, it almost always
 * includes a time range. However, self-paced "sessions" are apparently also
 * possible.
 * </p>
 * 
 * <p>
 * AcademicSession includes a notion of ordering and currency to support queries
 * such as "Find all current course offerings" and "Sort past course offerings
 * in reverse session order".
 * </p>
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface AcademicSession {
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
	 * The date this AcademicSession starts (if any).
	 * @return
	 */
	public Date getStartDate();
	public void setStartDate(Date startDate);
	
	/**
	 * The date this AcademicSession ends (if any).
	 * @return
	 */
	public Date getEndDate();
	public void setEndDate(Date endDate);
}
