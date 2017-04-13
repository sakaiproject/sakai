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
