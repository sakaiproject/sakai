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

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.pages.AccessDeniedPage;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.rubrics.logic.RubricsService;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Panel extension to abstract away some common functionality that many GBNG panels share. Classes extending {@link BasePanel} do not need
 * to inject the {@link GradebookNgBusinessService} as it is in here.
 */
public abstract class BasePanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	@SpringBean(name = "org.sakaiproject.rubrics.logic.RubricsService")
	protected RubricsService rubricsService;

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	protected ServerConfigurationService serverConfigService;

	protected static final String SAK_PROP_SHOW_COURSE_GRADE_STUDENT = "gradebookng.showDisplayCourseGradeToStudent";
	protected static final Boolean SAK_PROP_SHOW_COURSE_GRADE_STUDENT_DEFAULT = Boolean.TRUE;

	public BasePanel(final String id) {
		super(id);
	}

	public BasePanel(final String id, final IModel<?> model) {
		super(id, model);
	}

	/**
	 * Helper to get the user role, via the business service. Handles the Access Denied scenario.
	 *
	 * @return
	 */
	protected GbRole getUserRole() {

		GbRole role;
		try {
			role = this.businessService.getUserRole();
		} catch (final GbAccessDeniedException e) {
			final PageParameters params = new PageParameters();
			params.add("message", MessageHelper.getString("error.role"));
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
		return this.businessService.getCurrentSiteId();
	}

	/**
	 * Get the Gradebook for the panel, via the business service
	 *
	 * @return
	 */
	protected Gradebook getGradebook() {
		return this.businessService.getGradebook();
	}

	/**
	 * Get the settings for the gradebook
	 *
	 * @return
	 */
	protected GradebookInformation getSettings() {
		return this.businessService.getGradebookSettings();
	}

	/**
	 * Get the Rubric request parameters
	 *
	 * @return A map with key and value of those parameters
	 */
	protected HashMap<String, String> getRubricParameters(final String entityId) {
		final HashMap<String, String> list = new HashMap<String, String>();

		String entity = RubricsConstants.RBCS_PREFIX;
		if (entityId != null && !entityId.isEmpty()) {
			entity += entityId + "-";
		}
		final String startsWith = entity;

		final IRequestParameters parameters = RequestCycle.get().getRequest().getPostParameters();
		parameters.getParameterNames().forEach((value) -> {
			if (value.startsWith(startsWith)) {
				list.put(value, parameters.getParameterValue(value).toString());
			}
		});

		return list;
	}
}
