/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public abstract class GradebookDependentBean extends InitializableBean {
	private String pageName;

	/**
	 * Marked transient to allow serializable subclasses.
	 */
	private transient GradebookBean gradebookBean;
    private transient PreferencesBean preferencesBean;

	/**
	 * Convenience method, for use in calling locally implemented services
	 * that assume the gradebook ID is an integer.
	 */
	Long getGradebookId() {
		return getGradebookBean().getGradebookId();
	}

	/**
	 * Convenience method, for use in calling external facades
	 * that assume the gradebook ID is an string.
	 */
	private transient String gradebookUid;
	String getGradebookUid() {
		if (gradebookUid == null) {
			gradebookUid = getGradebookManager().getGradebookUid(getGradebookId());
		}
		return gradebookUid;
	}

	/**
	 * Convenience method to hide the Authn context object.
	 */
	public String getUserUid() {
		return getAuthnService().getUserUid();
	}

	/**
	 * Convenience method to load the current gradebook object.
	 */
	Gradebook getGradebook() {
		return getGradebookManager().getGradebook(getGradebookId());
	}

    /**
     * Gets a localized message string based on the locale determined by the
     * FacesContext.  Useful for adding localized FacesMessages from a backing bean.
     *
     * TODO Replace with direct calls to FacesUtil.
     *
     * @param key The key to look up the localized string
     */
    public String getLocalizedString(String key) {
    	return FacesUtil.getLocalizedString(key);
    }

    /**
     * Gets a localized message string based on the locale determined by the
     * FacesContext.  Useful for adding localized FacesMessages from a backing bean.
     *
     * TODO Replace with direct calls to FacesUtil.
     *
     * @param key The key to look up the localized string
     * @param params The array of strings to use in replacing the placeholders
     * in the localized string
     */
    public String getLocalizedString(String key, String[] params) {
    	return FacesUtil.getLocalizedString(key, params);
    }

    // Still more convenience methods, hiding the bean configuration details.

	public GradebookManager getGradebookManager() {
		return getGradebookBean().getGradebookManager();
	}

	public SectionAwareness getSectionAwareness() {
		return getGradebookBean().getSectionAwareness();
	}

	public UserDirectoryService getUserDirectoryService() {
		return getGradebookBean().getUserDirectoryService();
	}

	public Authn getAuthnService() {
		return getGradebookBean().getAuthnService();
	}

	// Because these methods are referred to inside "rendered" tag attributes,
	// JSF will call them multiple times in every request. To cut back on
	// business logic traffic, cache them in request scope. They need to be
	// declared transient, however, so that they aren't copied between
	// requests (which would prevent changes in a user's authz status).
	private transient Boolean userAbleToEditAssessments;
	public boolean isUserAbleToEditAssessments() {
		if (userAbleToEditAssessments == null) {
			userAbleToEditAssessments = new Boolean(getGradebookBean().getAuthzService().isUserAbleToEditAssessments(getGradebookUid()));
		}
		return userAbleToEditAssessments.booleanValue();
	}
	private transient Boolean userAbleToGradeAll;
	public boolean isUserAbleToGradeAll() {
		if (userAbleToGradeAll == null) {
			userAbleToGradeAll = new Boolean(getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid()));
		}
		return userAbleToGradeAll.booleanValue();
	}
	private transient Map userAbleToGradeSectionMap;
	public boolean isUserAbleToGradeSection(String sectionUid) {
		if (userAbleToGradeSectionMap == null) {
			userAbleToGradeSectionMap = new HashMap();
		}
		Boolean isAble = (Boolean)userAbleToGradeSectionMap.get(sectionUid);
		if (isAble == null) {
			isAble = new Boolean(getGradebookBean().getAuthzService().isUserAbleToGradeSection(sectionUid));
			userAbleToGradeSectionMap.put(sectionUid, isAble);
		}
		return isAble.booleanValue();
	}

	public List getAvailableEnrollments() {
		return getGradebookBean().getAuthzService().getAvailableEnrollments(getGradebookUid());
	}

	public List getAvailableSections() {
		return getGradebookBean().getAuthzService().getAvailableSections(getGradebookUid());
	}

	public List getSectionEnrollments(String sectionUid) {
		return getGradebookBean().getAuthzService().getSectionEnrollments(getGradebookUid(), sectionUid);
	}

	public List findMatchingEnrollments(String searchString, String optionalSectionUid) {
		return getGradebookBean().getAuthzService().findMatchingEnrollments(getGradebookUid(), searchString, optionalSectionUid);
	}

	/**
	 * Get the gradebook context.
	 */
	public GradebookBean getGradebookBean() {
		if (gradebookBean == null) {
			// This probably happened because gradebookBean is transient.
			// Just restore it from the session context.
			setGradebookBean((GradebookBean)FacesUtil.resolveVariable("gradebookBean"));
		}
		return gradebookBean;
	}

	/**
	 * Set the gradebook context.
	 */
	public void setGradebookBean(GradebookBean gradebookBean) {
		this.gradebookBean = gradebookBean;
	}

    /**
     * @return Returns the preferencesBean.
     */
    public PreferencesBean getPreferencesBean() {
        if (preferencesBean == null) {
            setPreferencesBean((PreferencesBean)FacesUtil.resolveVariable("preferencesBean"));
        }
        return preferencesBean;
    }
    /**
     * @param preferencesBean The preferencesBean to set.
     */
    public void setPreferencesBean(PreferencesBean preferencesBean) {
        this.preferencesBean = preferencesBean;
    }

    /**
     * Set up close relations with page and action names for easier control
     * of menus.
     */
    public String getPageName() {
    	return pageName;
    }
    public void setPageName(String pageName) {
    	this.pageName = pageName;
    }


   	/**
     * Generates a default filename (minus the extension) for a download from this Gradebook. 
     *
	 * @param   prefix for filename
	 * @return The appropriate filename for the export
	 */
    public String getDownloadFileName(String prefix) {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat(getLocalizedString("export_filename_date_format"));
		StringBuffer fileName = new StringBuffer(prefix);
        String gbName = getGradebook().getName();
        if(StringUtils.trimToNull(gbName) != null) {
            gbName = gbName.replaceAll("\\s", "_"); // replace whitespace with '_'
            fileName.append("-");
            fileName.append(gbName);
        }
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}


    /**
     * 
     * @return
     */
    public String getAuthzLevel(){
         return (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ?"instructor" : "TA";
    }

}
