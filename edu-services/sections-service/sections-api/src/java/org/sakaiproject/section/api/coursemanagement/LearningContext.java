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
 * A LearningContext is an abstract grouping of users in an academic environment.
 * Examples include CourseOfferings, Sections, and could potentially include
 * departments and colleges.  For Sakai 2.1, only CourseOfferings and CourseSections
 * are LearningContexts.
 * 
 * A student can be enrolled in any learning context.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface LearningContext {
	public String getUuid();
	public String getTitle();
	public void setTitle(String title);
}

