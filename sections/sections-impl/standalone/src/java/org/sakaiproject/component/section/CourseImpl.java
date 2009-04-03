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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.section.api.coursemanagement.Course;

/**
 * A detachable Course for persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */public class CourseImpl extends LearningContextImpl implements Course, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String siteContext;
	protected boolean externallyManaged;
	protected boolean selfRegistrationAllowed;
	protected boolean selfSwitchingAllowed;
	
	public boolean isSelfSwitchingAllowed() {
		return selfSwitchingAllowed;
	}
	public void setSelfSwitchingAllowed(boolean selfSwitchingAllowed) {
		this.selfSwitchingAllowed = selfSwitchingAllowed;
	}
	public boolean isSelfRegistrationAllowed() {
		return selfRegistrationAllowed;
	}
	public void setSelfRegistrationAllowed(boolean selfRegistrationAllowed) {
		this.selfRegistrationAllowed = selfRegistrationAllowed;
	}
	public String getSiteContext() {
		return siteContext;
	}
	public void setSiteContext(String siteContext) {
		this.siteContext = siteContext;
	}
	public boolean isExternallyManaged() {
		return externallyManaged;
	}
	public void setExternallyManaged(boolean externallyManaged) {
		this.externallyManaged = externallyManaged;
	}
	
	public String toString() {
		return new ToStringBuilder(this)
		.append(title)
		.append(siteContext)
		.append(uuid)
		.toString();
	}
}

