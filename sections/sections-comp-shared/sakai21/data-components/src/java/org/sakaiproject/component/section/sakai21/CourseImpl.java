/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.section.sakai21;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.site.Site;

public class CourseImpl implements Course, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String EXTERNALLY_MAINTAINED = "sections_externally_maintained";
	public static final String STUDENT_REGISTRATION_ALLOWED = "sections_student_registration_allowed";
	public static final String STUDENT_SWITCHING_ALLOWED = "sections_student_switching_allowed";
	
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
	
	public void decorateSite(Site site) {
		ResourceProperties props = site.getProperties();
		props.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.toString(externallyManaged));
		props.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.toString(selfRegistrationAllowed));
		props.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.toString(selfSwitchingAllowed));
	}

	protected String siteContext;
	protected String uuid;
	protected String title;
	protected boolean externallyManaged;
	protected boolean selfRegistrationAllowed;
	protected boolean selfSwitchingAllowed;

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



/**********************************************************************************
 * $Id$
 *********************************************************************************/
