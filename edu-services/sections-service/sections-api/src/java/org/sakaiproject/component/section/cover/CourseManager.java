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
package org.sakaiproject.component.section.cover;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.section.api.coursemanagement.Course;

/**
 * A static cover over the section info project's CourseManager.  Note that, since
 * some of CourseManager's interface methods are not implemented in Sakai, and so
 * are not available here.
 * 
 * TODO Move methods not available in sakai into another interface, or implement
 * them via legacy services.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManager {
	private static org.sakaiproject.section.api.CourseManager instance;

	public static final boolean courseExists(String siteContext) {
		return getInstance().courseExists(siteContext);
	}
	
	public static final Course createCourse(String siteContext, String title, boolean selfRegAllowed, boolean selfSwitchingAllowed, boolean externallyManaged) {
		return getInstance().createCourse(siteContext, title, selfRegAllowed, selfSwitchingAllowed, externallyManaged);
	}

	private static org.sakaiproject.section.api.CourseManager getInstance() {
		if(instance == null) {
			instance = (org.sakaiproject.section.api.CourseManager)ComponentManager.get(
					org.sakaiproject.section.api.CourseManager.class);
		}
		return instance;
	}

}
