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
package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

import java.io.Serializable;

public class ToggleCourseGradePoints extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public ToggleCourseGradePoints() {
	}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final GradebookPage gradebookPage = (GradebookPage) target.getPage();

		// get current settings
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		final Boolean currentSetting = settings.getShowPoints();

		// toggle it
		final Boolean nextSetting = !currentSetting;

		// set it
		settings.setShowPoints(nextSetting);

		// save settings
		gradebookPage.setUiSettings(settings);

		// refresh the page
		target.appendJavaScript("location.reload();");

		return new EmptyOkResponse();
	}
}
