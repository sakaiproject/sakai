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
 * Defines a group of students who are somehow associated with a CourseOffering
 * or a Section for credit.  Defines who is allowed to submit the final grade for this
 * student, and what the grade is for.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface EnrollmentSet {

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
	 * A category
	 * @return
	 */
	public String getCategory();
	public void setCategory(String category);
	
	/**
	 * The default credits an Enrollment should have, if not specified by the
	 * Enrollment itself.
	 * @return
	 */
	public String getDefaultEnrollmentCredits();
	public void setDefaultEnrollmentCredits(String defaultEnrollmentCredits);
	
	/**
	 * The official grader(s) for this EnrollmentSet.
	 * @return
	 */
	public Set<String> getOfficialInstructors();
	public void setOfficialInstructors(Set<String> officialInstructors);
}
