/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import org.sakaiproject.api.section.coursemanagement.Course;

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
	private static org.sakaiproject.api.section.CourseManager instance;

	public static final boolean courseExists(String siteContext) {
		return getInstance().courseExists(siteContext);
	}
	
	public static final Course createCourse(String siteContext, String title, boolean selfRegAllowed, boolean selfSwitchingAllowed, boolean externallyManaged) {
		return getInstance().createCourse(siteContext, title, selfRegAllowed, selfSwitchingAllowed, externallyManaged);
	}

	private static org.sakaiproject.api.section.CourseManager getInstance() {
		if(instance == null) {
			instance = (org.sakaiproject.api.section.CourseManager)ComponentManager.get(
					org.sakaiproject.api.section.CourseManager.class);
		}
		return instance;
	}

}
