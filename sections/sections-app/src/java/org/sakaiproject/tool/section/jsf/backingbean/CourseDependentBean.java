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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Base class for all JSF backing beans relying on knowledge of the current
 * course context.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseDependentBean extends InitializableBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(CourseDependentBean.class);

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
	protected Set getUsedCategories() {
		Set used = new HashSet();
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
	
//	protected Role getSiteRole() {
//		return getCourseBean().authz.getSiteRole(getUserUid(), getSiteContext());
//	}
	
	protected String getSiteContext() {
		return getCourseBean().context.getContext(null);
	}
	
	protected Course getCourse() {
		return getCourseBean().sectionManager.getCourse(getSiteContext());
	}
	
	protected List getAllSiteSections() {
		return getCourseBean().sectionManager.getSections(getSiteContext());
	}

	protected Set getMyEnrolledSections() {
		String userUid = getUserUid();
		Course course = getCourse();
		return getCourseBean().sectionManager.getSectionEnrollments(userUid, course.getUuid());
	}

	protected String getCategoryName(String categoryId) {
		return getCourseBean().sectionManager.getCategoryName(categoryId, JsfUtil.getLocale());
	}
	
	protected List getSectionCategories() {
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

	public PreferencesBean getPrefs() {
		return getCourseBean().getPrefs();
	}
}
