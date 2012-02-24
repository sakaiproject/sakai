/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

/**
 * Models a sectionable "class" in higher education.  What a Course actually represents
 * is intentionally ambiguous.  In Sakai 2.1, where multiple sections from a
 * variety of courses may be associated with a site, a Course simply represents the
 * site along with the metadata needed by the Section Manager Tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface Course extends LearningContext {
	/**
	 * The site associated with this course.
	 * 
	 * @return
	 */
	public String getSiteContext();
	
}
