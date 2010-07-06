/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.roster;

import java.util.List;

import org.sakaiproject.section.api.coursemanagement.CourseSection;

/**
 * Some of our session-scoped beans make frequent and expensive calls to services.
 * We can't cache the results in those beans because of their scope.  We therefore
 * use this request-scoped bean to cache objects returned by the services, and use
 * the JSF variable resolver to ensure that we're using the same RequestCache
 * throughout a single request. 
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public class RequestCache {
	
	private boolean init;
	protected List<CourseSection> viewableSections;

	protected boolean isInitizlized() {
		return init;
	}
	
	protected void init(ServicesBean services) {
		this.viewableSections = services.rosterManager.getViewableSectionsForCurrentUser();		
		this.init = true;
	}
}
