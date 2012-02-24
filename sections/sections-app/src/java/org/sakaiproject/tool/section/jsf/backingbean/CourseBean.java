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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;

import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.facade.manager.Authn;
import org.sakaiproject.section.api.facade.manager.Authz;
import org.sakaiproject.section.api.facade.manager.Context;

/**
 * Provides the current course uuid for a given user session.  This is also the
 * integration point for JSF backing beans and Spring-manages services.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String courseUuid;

	protected SectionManager sectionManager;
    protected Authn authn;
    protected Authz authz;
    protected Context context;
    protected PreferencesBean prefs;

	protected String getCourseUuid() {
		Course course = sectionManager.getCourse(context.getContext(null));
		courseUuid = course.getUuid();
		return courseUuid;
	}
	
	public SectionManager getSectionManager() {
		return sectionManager;
	}
	
    //// Setters for dep. injection
    public void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }
    
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }

    public void setAuthz(Authz authz) {
        this.authz = authz;
    }

	public void setContext(Context context) {
		this.context = context;
	}

	public PreferencesBean getPrefs() {
		return prefs;
	}

	public void setPrefs(PreferencesBean prefs) {
		this.prefs = prefs;
	}
	
}

