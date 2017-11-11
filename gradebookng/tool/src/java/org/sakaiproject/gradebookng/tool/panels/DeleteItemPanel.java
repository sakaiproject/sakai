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

import java.text.MessageFormat;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Panel handling the delete of a gb item
 */
public class DeleteItemPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	public DeleteItemPanel(final String id, final IModel<Long> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Long assignmentId = (Long) getDefaultModelObject();

		final Form<Long> form = new Form("form", Model.of(assignmentId));

		final GbAjaxButton submit = new GbAjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

				final Long assignmentIdToDelete = (Long) form.getModelObject();
				final Assignment assignment = DeleteItemPanel.this.businessService.getAssignment(assignmentIdToDelete);
				final String assignmentTitle = assignment.getName();

				DeleteItemPanel.this.businessService.removeAssignment(assignmentIdToDelete);

				getSession().success(MessageFormat.format(getString("delete.success"), assignmentTitle));
				setResponsePage(GradebookPage.class);
			}

		};
		form.add(submit);

		final GbAjaxButton cancel = new GbAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				DeleteItemPanel.this.window.close(target);
			}
		};

		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		add(form);
	}
}
