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

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;

/**
 * Provide a UI handle to the selected gradebook.
 *
 * Since all application-specific backing beans (a group that doesn't include
 * authentication handlers) require a gradebook ID to use with any of the
 * business or facade services, this bean is also a reasonable place to centralize
 * configuration of and access to those services.
 */
public class GradebookBean extends InitializableBean {
	private static final Log logger = LogFactory.getLog(GradebookBean.class);

	// TODO Remove.
	public static final String GRADEBOOK_UID_PARAM = "gradebookUid";

	private Long gradebookId;
	private String gradebookUid;


	// These interfaces are defined application-wide (through Spring, although the
	// UI classes don't know that).
	private GradebookManager gradebookManager;
	private GradeManager gradeManager;
	private CourseManagement courseManagementService;
	private UserDirectoryService userDirectoryService;
	private Authn authnService;
	private ContextManagement contextManagementService;

	/**
	 * @return Returns the gradebookId.
	 */
	public final Long getGradebookId() {
		refreshFromRequest();
		return gradebookId;
	}

	private final void setGradebookId(Long gradebookId) {
		this.gradebookId = gradebookId;
	}

	/**
	 * @param newGradebookUid The gradebookId to set.
	 * Since this is coming from the client, the application should NOT
	 * trust that the current user actually has access to the gradebook
	 * with this UID. This design assumes that authorization will come
	 * into play on each request.
	 */
	public final void setGradebookUid(String newGradebookUid) {
		Long newGradebookId = null;
		if (newGradebookUid != null) {
            Gradebook gradebook = null;
            try {
                gradebook = getGradebookManager().getGradebook(newGradebookUid);
            } catch (GradebookNotFoundException gnfe) {
                logger.error("Request made for inaccessible gradebookUid=" + newGradebookUid);
                newGradebookUid = null;
            }
			newGradebookId = gradebook.getId();
			if (logger.isInfoEnabled()) logger.info("setGradebookUid gradebookUid=" + newGradebookUid + ", gradebookId=" + newGradebookId);
		}
		this.gradebookUid = newGradebookUid;
		setGradebookId(newGradebookId);
	}

	private final void refreshFromRequest() {
		String requestUid = contextManagementService.getGradebookUid(FacesContext.getCurrentInstance().getExternalContext().getRequest());
		if ((requestUid != null) && (!requestUid.equals(gradebookUid))) {
			if (logger.isInfoEnabled()) logger.info("resetting gradebookUid from " + gradebookUid);
			setGradebookUid(requestUid);
		}
	}

	/**
	 * Static method to pick up the gradebook UID, if any, held by the current GradebookBean, if any.
	 * Meant to be called from a servlet filter.
	 */
	public static String getGradebookUidFromRequest(ServletRequest request) {
		String gradebookUid = null;
		HttpSession session = ((HttpServletRequest)request).getSession();
		GradebookBean gradebookBean = (GradebookBean)session.getAttribute("gradebookBean");
		if (gradebookBean != null) {
			gradebookUid = gradebookBean.gradebookUid;
		}
		return gradebookUid;
	}


	// The following getters are used by other backing beans. The setters are used only by
	// the bean factory.

	/**
	 * @return Returns the gradebookManager.
	 */
	public GradebookManager getGradebookManager() {
		return gradebookManager;
	}

	/**
	 * @param gradebookManager The gradebookManager to set.
	 */
	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}

	/**
	 * @return Returns the courseManagementService.
	 */
	public CourseManagement getCourseManagementService() {
		return courseManagementService;
	}
	/**
	 * @param courseManagementService The courseManagementService to set.
	 */
	public void setCourseManagementService(CourseManagement courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

	/**
	 * @return Returns the userDirectoryService.
	 */
	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}
	/**
	 * @param userDirectoryService The userDirectoryService to set.
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	/**
	 * @return Returns the gradeManager.
	 */
	public GradeManager getGradeManager() {
		return gradeManager;
	}
	/**
	 * @param gradeManager The gradeManager to set.
	 */
	public void setGradeManager(GradeManager gradeManager) {
		this.gradeManager = gradeManager;
	}
	/**
	 * @return Returns the authnService.
	 */
	public Authn getAuthnService() {
		return authnService;
	}
	/**
	 * @param authnService The authnService to set.
	 */
	public void setAuthnService(Authn authnService) {
		this.authnService = authnService;
	}
	/**
	 * @return Returns the contextManagementService.
	 */
	public ContextManagement getContextManagementService() {
		return contextManagementService;
	}
	/**
	 * @param contextManagementService The contextManagementService to set.
	 */
	public void setContextManagementService(ContextManagement contextManagementService) {
		this.contextManagementService = contextManagementService;
	}
}



