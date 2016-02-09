package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 *
 * Panel for the modal window that allows an instructor to zero the ungraded scores for all gradebook items 
 *
 */
public class ZeroUngradedItemsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private final ModalWindow window;

	private static final double ZERO_GRADE = 0;

	public ZeroUngradedItemsPanel(final String id, final ModalWindow window) {
		super(id);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

				// fetch all assignments
				List<Assignment> assignments = businessService.getGradebookAssignments();

				for (Assignment assignment : assignments) {
					final long assignmentId = assignment.getId().longValue();
					ZeroUngradedItemsPanel.this.businessService.updateUngradedItems(assignmentId, ZERO_GRADE);
				}

				ZeroUngradedItemsPanel.this.window.close(target);
				setResponsePage(new GradebookPage());
			}
		};
		add(submit);

		final AjaxButton cancel = new AjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				ZeroUngradedItemsPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		add(cancel);
	}
}
