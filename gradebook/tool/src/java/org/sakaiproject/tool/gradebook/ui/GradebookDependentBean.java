/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.util.*;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.facade.Role;

import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public abstract class GradebookDependentBean extends InitializableBean {
	private static final Log logger = LogFactory.getLog(GradebookDependentBean.class);

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
	String getGradebookUid() {
		return getGradebookManager().getGradebookUid(getGradebookId());
	}

	/**
	 * Convenience method to hide the Authn context object.
	 */
	public String getUserUid() {
		return FacadeUtils.getUserUid(getAuthnService());
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

	public GradeManager getGradeManager() {
		return getGradebookBean().getGradeManager();
	}

	public Authn getAuthnService() {
		return getGradebookBean().getAuthnService();
	}

	public boolean isUserAbleToEdit() {
		return getGradebookBean().getAuthzService().isUserAbleToEdit(getGradebookUid(), getUserUid());
	}

	public List getEnrollments() {
		return getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.STUDENT);
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
}
