package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * Panel for the modal window that allows an instructor to update the ungraded items
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class UpdateUngradedItemsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private final ModalWindow window;

	private final IModel<Long> model;

	private static final double DEFAULT_GRADE = 0;

	public UpdateUngradedItemsPanel(final String id, final IModel<Long> model, final ModalWindow window) {
		super(id);
		this.model = model;
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Long assignmentId = this.model.getObject();

		// form model
		final GradeOverride override = new GradeOverride();
		override.setGrade(String.valueOf(DEFAULT_GRADE));
		final CompoundPropertyModel<GradeOverride> formModel = new CompoundPropertyModel<GradeOverride>(override);

		// build form
		// modal window forms must be submitted via AJAX so we do not specify an onSubmit here
		final Form<GradeOverride> form = new Form<GradeOverride>("form", formModel);

		final AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

				final GradeOverride override = (GradeOverride) form.getModelObject();

				final Assignment assignment = UpdateUngradedItemsPanel.this.businessService.getAssignment(assignmentId);

				try {
					final Double overrideValue = Double.valueOf(override.getGrade());

					if (overrideValue > assignment.getPoints()) {
						target.addChildren(form, FeedbackPanel.class);
					}

					final boolean success = UpdateUngradedItemsPanel.this.businessService.updateUngradedItems(assignmentId, overrideValue);

					if (success) {
						UpdateUngradedItemsPanel.this.window.close(target);
						setResponsePage(new GradebookPage());
					} else {
						// InvalidGradeException
						error(getString("grade.notifications.invalid"));
						target.addChildren(form, FeedbackPanel.class);
						target.appendJavaScript("new GradebookUpdateUngraded($(\"#" + getParent().getMarkupId() + "\"));");
					}
				} catch (final NumberFormatException e) {
					// InvalidGradeException
					error(getString("grade.notifications.invalid"));
					target.addChildren(form, FeedbackPanel.class);
					target.appendJavaScript("new GradebookUpdateUngraded($(\"#" + getParent().getMarkupId() + "\"));");
				}
			}
		};
		form.add(submit);

		final AjaxButton cancel = new AjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				UpdateUngradedItemsPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		form.add(new TextField<Double>("grade").setRequired(true));

		add(form);

		// feedback panel
		form.add(new GbFeedbackPanel("updateGradeFeedback"));

		final Assignment assignment = this.businessService.getAssignment(assignmentId);
		final WebMarkupContainer hiddenGradePoints = new WebMarkupContainer("gradePoints");
		hiddenGradePoints.add(new AttributeModifier("value", assignment.getPoints()));
		form.add(hiddenGradePoints);
	}

	/**
	 * Model for this form
	 */
	class GradeOverride implements Serializable {

		private static final long serialVersionUID = 1L;

		@Getter
		@Setter
		private String grade;

	}

}
