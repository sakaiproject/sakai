package org.sakaiproject.gradebookng.tool.panels;

import java.text.MessageFormat;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * The panel for the add and edit grade item window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddOrEditGradeItemPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Long> model;

	/**
	 * How this panel is rendered
	 */
	enum Mode {
		ADD,
		EDIT;
	}

	Mode mode;

	public AddOrEditGradeItemPanel(final String id, final ModalWindow window, final IModel<Long> model) {
		super(id);
		this.model = model;

		// determine mode
		if (model != null) {
			this.mode = Mode.EDIT;
		} else {
			this.mode = Mode.ADD;
		}

		// setup the backing object
		Assignment assignment;

		if (this.mode == Mode.EDIT) {
			final Long assignmentId = this.model.getObject();
			assignment = this.businessService.getAssignment(assignmentId);

			// TODO if we are in edit mode and don't have an assignment, need to error here

		} else {
			// Mode.ADD
			assignment = new Assignment();
			// Default released to true
			assignment.setReleased(true);
			// If no categories, then default counted to true
			final Gradebook gradebook = this.businessService.getGradebook();
			assignment.setCounted(GradebookService.CATEGORY_TYPE_NO_CATEGORY == gradebook.getCategory_type());
		}

		// form model
		final Model<Assignment> formModel = new Model<Assignment>(assignment);

		// form
		final Form<Assignment> form = new Form<Assignment>("addOrEditGradeItemForm", formModel);

		final GbAjaxButton submit = new GbAjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final Assignment assignment = (Assignment) form.getModelObject();

				boolean validated = true;

				// PRE VALIDATION
				// 1. if category selected and drop/keep highest/lowest selected for that category,
				// ensure points match the already established maximum for the category.
				if (assignment.getCategoryId() != null) {
					final List<CategoryDefinition> categories = AddOrEditGradeItemPanel.this.businessService.getGradebookCategories();
					final CategoryDefinition category = categories
							.stream()
							.filter(c -> (c.getId() == assignment.getCategoryId())
									&& (c.getDropHighest() > 0 || c.getKeepHighest() > 0 || c.getDrop_lowest() > 0))
							.filter(c -> (c.getDropHighest() > 0 || c.getKeepHighest() > 0 || c.getDrop_lowest() > 0))
							.findFirst()
							.orElse(null);

					if (category != null) {
						final Assignment mismatched = category.getAssignmentList()
								.stream()
								.filter(a -> Double.compare(a.getPoints().doubleValue(), assignment.getPoints().doubleValue()) != 0)
								.findFirst()
								.orElse(null);
						if (mismatched != null) {
							validated = false;
							error(MessageFormat.format(getString("error.addeditgradeitem.categorypoints"), mismatched.getPoints()));
							target.addChildren(form, FeedbackPanel.class);
						}
					}
				}

				if (validated) {
					if (AddOrEditGradeItemPanel.this.mode == Mode.EDIT) {

						final boolean success = AddOrEditGradeItemPanel.this.businessService.updateAssignment(assignment);

						if (success) {
							getSession().success(MessageFormat.format(getString("message.edititem.success"), assignment.getName()));
							setResponsePage(getPage().getPageClass());
						} else {
							error(new ResourceModel("message.edititem.error").getObject());
							target.addChildren(form, FeedbackPanel.class);
						}

					} else {

						Long assignmentId = null;

						boolean success = true;
						try {
							assignmentId = AddOrEditGradeItemPanel.this.businessService.addAssignment(assignment);
						} catch (final AssignmentHasIllegalPointsException e) {
							error(new ResourceModel("error.addgradeitem.points").getObject());
							success = false;
						} catch (final ConflictingAssignmentNameException e) {
							error(new ResourceModel("error.addgradeitem.title").getObject());
							success = false;
						} catch (final ConflictingExternalIdException e) {
							error(new ResourceModel("error.addgradeitem.exception").getObject());
							success = false;
						} catch (final Exception e) {
							error(new ResourceModel("error.addgradeitem.exception").getObject());
							success = false;
						}
						if (success) {
							getSession()
									.success(MessageFormat.format(getString("notification.addgradeitem.success"), assignment.getName()));
							setResponsePage(getPage().getPageClass(),
									new PageParameters().add(GradebookPage.CREATED_ASSIGNMENT_ID_PARAM, assignmentId));
						} else {
							target.addChildren(form, FeedbackPanel.class);
						}
					}
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.addChildren(form, FeedbackPanel.class);
			}
		};

		// submit button label
		submit.add(new Label("submitLabel", getSubmitButtonLabel()));
		form.add(submit);

		// add the common components
		form.add(new AddOrEditGradeItemPanelContent("subComponents", formModel));

		// feedback panel
		form.add(new GbFeedbackPanel("addGradeFeedback"));

		// cancel button
		final GbAjaxButton cancel = new GbAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		add(form);
	}

	/**
	 * Helper to get the model for the button
	 *
	 * @return
	 */
	private ResourceModel getSubmitButtonLabel() {
		if (this.mode == Mode.EDIT) {
			return new ResourceModel("button.savechanges");
		} else {
			return new ResourceModel("button.create");
		}
	}
}
