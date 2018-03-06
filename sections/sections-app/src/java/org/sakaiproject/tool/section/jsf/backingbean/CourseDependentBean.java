/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.SectionManager.ExternalIntegrationConfig;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Base class for all JSF backing beans relying on knowledge of the current
 * course context.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class CourseDependentBean extends InitializableBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient CourseBean courseBean;

	private CourseBean getCourseBean() {
		if(courseBean == null) {
			courseBean = (CourseBean)JsfUtil.resolveVariable("courseBean");
		}
		return courseBean;
	}

	/**
	 * Gets the categories that are currently being used in this site context.
	 * 
	 * @param categories
	 * @param sections
	 * @return
	 */
	protected Set<String> getUsedCategories() {
		Set<String> used = new HashSet<String>();
		List sections = getAllSiteSections();
		List categories = getSectionManager().getSectionCategories(getSiteContext());
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			String cat = section.getCategory();
			if(categories.contains(cat)) {
				used.add(cat);
			}
		}
		return used;
	}

	protected SectionManager getSectionManager() {
		return getCourseBean().getSectionManager();
	}
	
	protected String getUserUid() {
		return getCourseBean().authn.getUserUid(FacesContext.getCurrentInstance().getExternalContext().getRequest());
	}
	
	protected ExternalIntegrationConfig getApplicationConfiguration() {
		ExternalIntegrationConfig config = getCourseBean().sectionManager.getConfiguration(FacesContext.getCurrentInstance().getExternalContext().getRequest());
		if(log.isDebugEnabled()) log.debug("Application configuration = " + config);
		return config;
	}
	
	protected String getSiteContext() {
		return getCourseBean().context.getContext(null);
	}
	
	protected Course getCourse() {
		return getCourseBean().sectionManager.getCourse(getSiteContext());
	}
	
	protected List<CourseSection> getAllSiteSections() {
		return getCourseBean().sectionManager.getSections(getSiteContext());
	}

	protected Set getEnrolledSections(String userUid) {
		return getCourseBean().sectionManager.getSectionEnrollments(userUid, getCourse().getUuid());
	}

	protected String getCategoryName(String categoryId) {
		Locale locale = LocaleUtil.getLocale(FacesContext.getCurrentInstance());
		return getCourseBean().sectionManager.getCategoryName(categoryId, locale);
	}
	
	protected List<String> getSectionCategories() {
		return getCourseBean().sectionManager.getSectionCategories(getSiteContext());
	}

	public boolean isSectionManagementEnabled() {
		return getCourseBean().authz.isSectionManagementAllowed(getUserUid(), getSiteContext());
	}
	public boolean isSectionOptionsManagementEnabled() {
		return getCourseBean().authz.isSectionOptionsManagementAllowed(getUserUid(), getSiteContext());
	}
	public boolean isSectionEnrollmentMangementEnabled() {
		return getCourseBean().authz.isSectionEnrollmentMangementAllowed(getUserUid(), getSiteContext());
	}
	public boolean isSectionTaManagementEnabled() {
		return getCourseBean().authz.isSectionTaManagementAllowed(getUserUid(), getSiteContext());
	}
	public boolean isViewOwnSectionsEnabled() {
		return getCourseBean().authz.isViewOwnSectionsAllowed(getUserUid(), getSiteContext());
	}
	public boolean isViewAllSectionsEnabled() {
		return getCourseBean().authz.isViewAllSectionsAllowed(getUserUid(), getSiteContext());
	}
	public boolean isSectionAssignable() {
		return getCourseBean().authz.isSectionAssignable(getUserUid(), getSiteContext());
	}
	public PreferencesBean getPrefs() {
		return getCourseBean().getPrefs();
	}
	public String getSiteRole() {
		return getCourseBean().authz.getRoleDescription(getUserUid(), getSiteContext());
	}
}
