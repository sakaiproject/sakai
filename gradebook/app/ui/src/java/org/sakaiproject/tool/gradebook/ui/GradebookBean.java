/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook.ui;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.business.GradebookScoringAgentManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.extern.slf4j.Slf4j;

/**
 * Provide a UI handle to the selected gradebook.
 *
 * Since all application-specific backing beans (a group that doesn't include
 * authentication handlers) require a gradebook ID to use with any of the
 * business or facade services, this bean is also a reasonable place to centralize
 * configuration of and access to those services.
 */
@Slf4j
public class GradebookBean extends InitializableBean {
    private Long gradebookId;
    private String gradebookUid;

    // These interfaces are defined application-wide (through Spring, although the
    // UI classes don't know that).
    private GradebookManager gradebookManager;
    private SectionAwareness sectionAwareness;
    private UserDirectoryService userDirectoryService;
    private Authn authnService;
    private Authz authzService;
    private EventTrackingService eventTrackingService;
    private ConfigurationBean configurationBean;
    private GradebookPermissionService gradebookPermissionService;
    private GradebookExternalAssessmentService gradebookExternalAssessmentService;
    private GradebookScoringAgentManager scoringAgentManager;
    private GradebookFrameworkService gradebookFrameworkService;

    /**
     * @return Returns the gradebookId.
     */
    public final Long getGradebookId() {
        refreshFromRequest();
        return this.gradebookId;
    }

    private final void setGradebookId(final Long gradebookId) {
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
    		} catch (final GradebookNotFoundException gnfe1) {
    			log.debug("Request made for inaccessible, adding gradebookUid=" + newGradebookUid);
    			getGradebookFrameworkService().addGradebook(newGradebookUid,newGradebookUid);
    			try {
    				gradebook = getGradebookManager().getGradebook(newGradebookUid);
    			} catch (final GradebookNotFoundException gnfe2) {
    				log.error("Request made and could not add inaccessible gradebookUid=" + newGradebookUid);
    				newGradebookUid = null;
    			}
    		}
    		if(gradebook == null) {
				throw new IllegalStateException("Gradebook gradebook == null!");
			}
    		newGradebookId = gradebook.getId();
    		if (log.isDebugEnabled()) {
				log.debug("setGradebookUid gradebookUid=" + newGradebookUid + ", gradebookId=" + newGradebookId);
			}
    	}
    	this.gradebookUid = newGradebookUid;
    	setGradebookId(newGradebookId);
    }

    private final void refreshFromRequest() {
		final String requestUid = getGradebookUid(FacesContext.getCurrentInstance().getExternalContext().getRequest());
        if ((requestUid != null) && (!requestUid.equals(this.gradebookUid))) {
            if (log.isDebugEnabled()) {
				log.debug("resetting gradebookUid from " + this.gradebookUid);
			}
            setGradebookUid(requestUid);
        }
    }

    /**
     * Static method to pick up the gradebook UID, if any, held by the current GradebookBean, if any.
     * Meant to be called from a servlet filter.
     */
    public static String getGradebookUidFromRequest(final ServletRequest request) {
        String gradebookUid = null;
        final HttpSession session = ((HttpServletRequest)request).getSession();
        final GradebookBean gradebookBean = (GradebookBean)session.getAttribute("gradebookBean");
        if (gradebookBean != null) {
            gradebookUid = gradebookBean.gradebookUid;
        }
        return gradebookUid;
    }

	/**
	 * Migrated from GradebookService
	 */
	public static String getGradebookUid(final Object notNeededInSakai) {
		// get the Tool Placement, and return the tool's context if available
		final Placement placement = ToolManager.getCurrentPlacement();
		if (placement == null) {
			log.error("Placement is null");
			return "QA_6"; // migrated from GradebookService unsure what this is all about
		}

		return placement.getContext();
	}

	public void postEvent(final String event, final String objectReference, final boolean modify) {
		this.eventTrackingService.post(this.eventTrackingService.newEvent(event, objectReference, modify));
	}

	public String getUserDisplayName(final String userUid) throws UnknownUserException {
		try {
			final User sakaiUser = this.userDirectoryService.getUser(userUid);
			return sakaiUser.getDisplayName();
		} catch (final UserNotDefinedException e) {
			throw new UnknownUserException("Unknown uid: " + userUid);
		}
	}

	public String getUserEmailAddress(final String userUid) throws UnknownUserException {
		try {
			final User sakaiUser = this.userDirectoryService.getUser(userUid);
			return sakaiUser.getEmail();
		} catch (final UserNotDefinedException e) {
			throw new UnknownUserException("Unknown uid: " + userUid);
		}
	}

	/**
	 * End Gradebook Service migration
	 */


    // The following getters are used by other backing beans. The setters are used only by
    // the bean factory.

    /**
     * @return Returns the gradebookManager.
     */
    public GradebookManager getGradebookManager() {
        return this.gradebookManager;
    }

    /**
     * @return Returns the gradebookFrameworkService
     */
    public GradebookFrameworkService getGradebookFrameworkService() {
        return this.gradebookFrameworkService;
    }

    /**
     * @param gradebookManager The gradebookManager to set.
     */
    public void setGradebookManager(final GradebookManager gradebookManager) {
        this.gradebookManager = gradebookManager;
    }

    /**
     * @param gradebookFrameworkServicee The gradebookFrameworkService to set.
     */

    public void setGradebookFrameworkService(final GradebookFrameworkService gradebookFrameworkService) {
        this.gradebookFrameworkService = gradebookFrameworkService;
    }

    public SectionAwareness getSectionAwareness() {
        return this.sectionAwareness;
    }
    public void setSectionAwareness(final SectionAwareness sectionAwareness) {
        this.sectionAwareness = sectionAwareness;
    }

    /**
     * @return Returns the userDirectoryService.
     */
    public UserDirectoryService getUserDirectoryService() {
        return this.userDirectoryService;
    }
    /**
     * @param userDirectoryService The userDirectoryService to set.
     */
    public void setUserDirectoryService(final UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }
    /**
     * @return Returns the authnService.
     */
    public Authn getAuthnService() {
        return this.authnService;
    }
    /**
     * @param authnService The authnService to set.
     */
    public void setAuthnService(final Authn authnService) {
        this.authnService = authnService;
    }

    public Authz getAuthzService() {
        return this.authzService;
    }
    public void setAuthzService(final Authz authzService) {
        this.authzService = authzService;
    }

    public EventTrackingService getEventTrackingService() {
        return this.eventTrackingService;
    }

    public void setEventTrackingService(final EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public ConfigurationBean getConfigurationBean() {
        return this.configurationBean;
    }
    public void setConfigurationBean(final ConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
    }

    public GradebookPermissionService getGradebookPermissionService() {
        return this.gradebookPermissionService;
    }
    public void setGradebookPermissionService(final GradebookPermissionService gradebookPermissionService) {
        this.gradebookPermissionService = gradebookPermissionService;
    }

    public GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
        return this.gradebookExternalAssessmentService;
    }
    public void setGradebookExternalAssessmentService(final GradebookExternalAssessmentService gradebookExternalAssessmentService) {
        this.gradebookExternalAssessmentService = gradebookExternalAssessmentService;
    }

    public GradebookScoringAgentManager getScoringAgentManager() {
        return this.scoringAgentManager;
    }
    public void setScoringAgentManager(final GradebookScoringAgentManager scoringAgentManager) {
        this.scoringAgentManager = scoringAgentManager;
    }
}
