package org.sakaiproject.gradebookng.tool.panels;

import java.text.MessageFormat;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

public class DeleteItemPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

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

		final AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

				final Long assignmentIdToDelete = (Long) form.getModelObject();
				final Assignment assignment = DeleteItemPanel.this.businessService.getAssignment(assignmentIdToDelete);
				final String assignmentTitle = assignment.getName();

				DeleteItemPanel.this.businessService.removeAssignment(assignmentIdToDelete);

				getSession().info(MessageFormat.format(getString("delete.success"), assignmentTitle));
				setResponsePage(new GradebookPage());
			}

		};
		form.add(submit);

		final AjaxButton cancel = new AjaxButton("cancel") {
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
