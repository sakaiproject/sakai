/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.AttributeModifier;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.tool.pages.AccessDeniedPage;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.MessageHelper;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * Panel extension to abstract away some common functionality that many GBNG panels share. Classes extending {@link BasePanel} do not need
 * to inject the {@link GradebookNgBusinessService} as it is in here.
 */
public abstract class BasePanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	@SpringBean(name = "org.sakaiproject.grading.api.GradingService")
	protected GradingService gradingService;

	@SpringBean(name = "org.sakaiproject.rubrics.api.RubricsService")
	protected RubricsService rubricsService;

	@SpringBean(name = "org.sakaiproject.assignment.api.AssignmentService")
	protected AssignmentService assignmentService;

	@SpringBean(name = "org.sakaiproject.authz.api.AuthzGroupService")
	protected AuthzGroupService authzGroupService;

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	protected ServerConfigurationService serverConfigService;

	@SpringBean(name = "org.sakaiproject.tool.api.ToolManager")
	protected ToolManager toolManager;

	protected static final String SAK_PROP_SHOW_COURSE_GRADE_STUDENT = "gradebookng.showDisplayCourseGradeToStudent";
	protected static final Boolean SAK_PROP_SHOW_COURSE_GRADE_STUDENT_DEFAULT = Boolean.TRUE;

	protected static final String SAK_PROP_ALLOW_COMPARE_GRADES = "gradebookng.allowStudentsToCompareGradesWithClassmates";
	protected static final Boolean SAK_PROP_ALLOW_COMPARE_GRADES_DEFAULT = Boolean.FALSE;

	protected static final String SAK_PROP_ENABLE_OSIRIS_EXPORT = "gradebookng.export.enabelOsirisExport";
	protected static final Boolean SAK_PROP_ENABLE_OSIRIS_EXPORT_DEFAULT = Boolean.FALSE;

	protected String currentGradebookUid;
	protected String currentSiteId;

	private static ResourceLoader RL = new ResourceLoader();

	public BasePanel(final String id) {
		super(id);
	}

	public BasePanel(final String id, final IModel<?> model) {
		super(id, model);
	}

	public void onInitialize() {
		super.onInitialize();
		setCurrentGradebookAndSite(getCurrentGradebookUid(), getCurrentSiteId());
	}

	/**
	 * Helper to get the user role, via the business service. Handles the Access Denied scenario.
	 *
	 * @return
	 */
	protected GbRole getUserRole() {

		GbRole role;
		try {
			role = this.businessService.getUserRole(getCurrentSiteId());
		} catch (final GbAccessDeniedException e) {
			final PageParameters params = new PageParameters();
			params.add("message", MessageHelper.getString("error.role", RL.getLocale()));
			throw new RestartResponseException(AccessDeniedPage.class, params);
		}
		return role;
	}

	/**
	 * Get the current user, via the business service
	 *
	 * @return
	 */
	protected String getCurrentUserId() {
		return this.businessService.getCurrentUser().getId();
	}

	/**
	 * Get the current siteId, via the business service
	 * 
	 * @return
	 */
	protected String getCurrentSiteId() {
		try {
			return this.toolManager.getCurrentPlacement().getContext();
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Get the Gradebook for the panel, via the business service
	 *
	 * @return
	 */
	protected Gradebook getGradebook() {
		if (currentGradebookUid == null || currentSiteId == null) {
			throw new RuntimeException("Error trying to get settings of gradebook with null value");
		}
		return this.businessService.getGradebook(currentGradebookUid, currentSiteId);
	}

	protected String getCurrentGradebookUid() {
		String gradebookUid = getCurrentSiteId();
		Placement placement = toolManager.getCurrentPlacement();
		Properties props = placement.getPlacementConfig();
		if (props.getProperty("gb-group") != null) {
			gradebookUid = props.getProperty("gb-group");
		}

		return gradebookUid;
	}

	public void setCurrentGradebookAndSite(String gUid, String siteId) {
		currentGradebookUid = gUid;
		currentSiteId = siteId;
	}

	/**
	 * Get the settings for the gradebook
	 *
	 * @return
	 */
	protected GradebookInformation getSettings() {
		if (currentGradebookUid == null || currentSiteId == null) {
			throw new RuntimeException("Error trying to get settings of gradebook with null value");
		}
		return this.businessService.getGradebookSettings(currentGradebookUid, currentSiteId);
	}

	/**
	 * Get the Rubric request parameters
	 *
	 * @return A map with key and value of those parameters
	 */
	protected Map<String, String> getRubricParameters(final String entityId) {

		final Map<String, String> map = new HashMap<>();

		String entity = RubricsConstants.RBCS_PREFIX;
		if (entityId != null && !entityId.isEmpty()) {
			entity += entityId + "-";
		}
		final String startsWith = entity;

		final IRequestParameters parameters = RequestCycle.get().getRequest().getPostParameters();
		parameters.getParameterNames().forEach((value) -> {
			if (value.startsWith(startsWith)) {
				map.put(value, parameters.getParameterValue(value).toString());
			}
		});

		return map;
	}

	/**
	 * Utility method to set up accordion behavior for settings panels
	 * 
	 * @param accordionButton The button that toggles the accordion
	 * @param accordionPanel The panel that is shown/hidden
	 * @param expandedState Reference to the panel's expanded state boolean
	 * @param expandedStateUpdater Function to update the expanded state in the panel
	 */
	protected void setupAccordionBehavior(final WebMarkupContainer accordionButton, final WebMarkupContainer accordionPanel, 
			final boolean initialExpandedState, final AccordionStateUpdater expandedStateUpdater) {
		
		// Add click event to toggle accordion
		accordionButton.add(new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(final AjaxRequestTarget target) {
				// Toggle the expanded state in the panel class
				expandedStateUpdater.updateState(!expandedStateUpdater.getState());
				
				// Update UI based on new state
				updateAccordionState(accordionButton, accordionPanel, expandedStateUpdater.getState(), target);
			}
		});
		
		// Set initial state
		updateAccordionState(accordionButton, accordionPanel, initialExpandedState, null);
		
		// Make components update via AJAX
		accordionPanel.setOutputMarkupId(true);
		accordionButton.setOutputMarkupId(true);
	}
	
	/**
	 * Interface for updating the expanded state in panel classes
	 */
	public interface AccordionStateUpdater extends java.io.Serializable {
		void updateState(boolean newState);
		boolean getState();
	}
	
	/**
	 * Updates the accordion state based on the expanded flag
	 * 
	 * @param accordionButton The button that toggles the accordion
	 * @param accordionPanel The panel that is shown/hidden
	 * @param expanded Whether the accordion is expanded
	 * @param target Optional AJAX target to add components to
	 */
	private void updateAccordionState(final WebMarkupContainer accordionButton, final WebMarkupContainer accordionPanel, 
			final boolean expanded, final AjaxRequestTarget target) {
		
		if (expanded) {
			accordionPanel.add(new AttributeModifier("class", "accordion-collapse collapse show"));
			accordionButton.add(new AttributeModifier("class", "accordion-button fw-bold"));
			accordionButton.add(new AttributeModifier("aria-expanded", "true"));
		} else {
			accordionPanel.add(new AttributeModifier("class", "accordion-collapse collapse"));
			accordionButton.add(new AttributeModifier("class", "accordion-button collapsed fw-bold"));
			accordionButton.add(new AttributeModifier("aria-expanded", "false"));
		}
		
		// Add components to AJAX response if target provided
		if (target != null) {
			target.add(accordionPanel);
			target.add(accordionButton);
		}
	}
}
