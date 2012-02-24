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
 * A CanonicalCourse represents the aspects of a course that stay the same across
 * instances of a course. A CanonicalCourse exists whether there are any instances
 * of the course or not.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface CanonicalCourse {

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
	 * Gets the Set <String> of course set EIDs that contain this canonical course.
	 * @return
	 */
	public Set<String> getCourseSetEids();
}
