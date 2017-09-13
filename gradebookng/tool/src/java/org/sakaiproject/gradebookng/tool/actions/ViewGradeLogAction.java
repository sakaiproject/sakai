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
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.GradeLogPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ViewGradeLogAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public ViewGradeLogAction() {
	}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final String assignmentId = params.get("assignmentId").asText();
		final String studentUuid = params.get("studentId").asText();

		final Map<String, Object> model = new HashMap<>();
		model.put("assignmentId", Long.valueOf(assignmentId));
		model.put("studentUuid", studentUuid);

		final GradebookPage gradebookPage = (GradebookPage) target.getPage();
		final GbModalWindow window = gradebookPage.getGradeLogWindow();

		window.setAssignmentToReturnFocusTo(assignmentId);
		window.setStudentToReturnFocusTo(studentUuid);
		window.setContent(new GradeLogPanel(window.getContentId(), Model.ofMap(model), window));
		window.show(target);

		return new EmptyOkResponse();
	}
}
