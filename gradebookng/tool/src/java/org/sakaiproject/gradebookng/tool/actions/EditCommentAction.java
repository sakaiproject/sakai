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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.EditGradeCommentPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EditCommentAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public EditCommentAction() {
	}

	private class EmptyOkResponse implements ActionResponse {
		public EmptyOkResponse() {
		}

		public String getStatus() {
			return "OK";
		}

		public String toJson() {
			return "{}";
		}
	}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final String assignmentId = params.get("assignmentId").asText();
		final String studentUuid = params.get("studentId").asText();

		final Map<String, Object> model = new HashMap<>();
		model.put("assignmentId", Long.valueOf(assignmentId));
		model.put("studentUuid", studentUuid);

		final GradebookPage gradebookPage = (GradebookPage) target.getPage();
		final GbModalWindow window = gradebookPage.getGradeCommentWindow();

		final EditGradeCommentPanel panel = new EditGradeCommentPanel(
				window.getContentId(),
				Model.ofMap(model),
				window);

		window.setContent(panel);
		window.showUnloadConfirmation(false);
		window.clearWindowClosedCallbacks();
		window.setAssignmentToReturnFocusTo(assignmentId);
		window.setStudentToReturnFocusTo(studentUuid);
		window.addWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClose(final AjaxRequestTarget target) {
				final String comment = panel.getComment();

				target.appendJavaScript(
						String.format("GbGradeTable.updateComment('%s', '%s', '%s');",
								assignmentId,
								studentUuid,
								comment == null ? "" : comment));
			}
		});
		window.show(target);

		return new EmptyOkResponse();
	}
}
