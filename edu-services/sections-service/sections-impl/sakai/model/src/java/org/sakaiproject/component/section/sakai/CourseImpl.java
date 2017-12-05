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
package org.sakaiproject.component.section.sakai;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.site.api.Site;

@Slf4j
public class CourseImpl implements Course, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String EXTERNALLY_MAINTAINED = "sections_externally_maintained";
	public static final String STUDENT_REGISTRATION_ALLOWED = "sections_student_registration_allowed";
	public static final String STUDENT_SWITCHING_ALLOWED = "sections_student_switching_allowed";
	public static final String STUDENT_OPEN_DATE = "sections_student_open_date";

	/**
	 * Creates a course from a sakai Site
	 * 
	 * @param site The Sakai site
	 */
	public CourseImpl(Site site) {
		this.site = site;
		this.uuid = site.getReference();
		this.title = site.getTitle();
		this.siteContext = site.getId();
		this.externallyManaged = Boolean.valueOf(site.getProperties().getProperty(CourseImpl.EXTERNALLY_MAINTAINED)).booleanValue();
		this.selfRegistrationAllowed = Boolean.valueOf(site.getProperties().getProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED)).booleanValue();
		this.selfSwitchingAllowed = Boolean.valueOf(site.getProperties().getProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED)).booleanValue();
	}

	protected String siteContext;
	protected String uuid;
	protected String title;
	protected boolean externallyManaged;
	protected boolean selfRegistrationAllowed;
	protected boolean selfSwitchingAllowed;

	
	public void decorateSite(Site site) {
		ResourceProperties props = site.getProperties();
		if(log.isDebugEnabled()) log.debug("Decorating site " + site.getId() + " with external = " + externallyManaged);
		props.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.toString(externallyManaged));
		props.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.toString(selfRegistrationAllowed));
		props.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.toString(selfSwitchingAllowed));
	}

	// Transient reference to the site being modeled
	private transient Site site;
	
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String toString() {
		return new ToStringBuilder(this)
		.append(title)
		.append(siteContext)
		.append(uuid)
		.toString();
	}

	public Site getSite() {
		return site;
	}

}
