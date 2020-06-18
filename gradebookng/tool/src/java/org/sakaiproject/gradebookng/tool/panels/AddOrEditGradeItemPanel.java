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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.UiMode;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookHelper;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeItemNameException;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.util.DateFormatterUtil;

/**
 * The panel for the add and edit grade item window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddOrEditGradeItemPanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	private static String HIDDEN_DUEDATE_ISO8601 = "duedate_iso8601";

	private Date dueDate;
	private GbModalWindow window;

	IModel<Long> model;

	private UiMode mode;

	public AddOrEditGradeItemPanel(final String id, final GbModalWindow window, final IModel<Long> model) {
		super(id);
		this.model = model;
		this.window = window;

		// determine mode
		if (model != null) {
			this.mode = UiMode.EDIT;
		} else {
			this.mode = UiMode.ADD;
		}

		// setup the backing object
		Assignment assignment;

		if (this.mode == UiMode.EDIT) {
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
				createGradeItem(target, form, false);
			}

			@Override
			protected void onError(final AjaxRequestTarget target, final Form<?> form) {
				target.addChildren(form, FeedbackPanel.class);
			}
		};

		// submit button label
		submit.add(new Label("submitLabel", getSubmitButtonLabel()));
		form.add(submit);

		final GbAjaxButton createAnother = new GbAjaxButton("createAnother", form) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				createGradeItem(target, form, true);
			}

			@Override
			protected void onError(final AjaxRequestTarget target, final Form<?> form) {
				target.addChildren(form, FeedbackPanel.class);
			}

			@Override
			public boolean isVisible() {
				return AddOrEditGradeItemPanel.this.mode == UiMode.ADD;
			}
		};
		form.add(createAnother);

		// add the common components
		form.add(new AddOrEditGradeItemPanelContent("subComponents", formModel, this.mode));

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
		if (this.mode == UiMode.EDIT) {
			return new ResourceModel("button.savechanges");
		} else {
			return new ResourceModel("button.create");
		}
	}

	private void setISODates() {
		final String dueDateString = StringUtils.trimToNull(
				getRequest().getRequestParameters().getParameterValue(HIDDEN_DUEDATE_ISO8601).toString(""));
		//Allow for clearing the due date

		if (dueDateString == null) {
			this.dueDate = null;
		}
		else if (DateFormatterUtil.isValidISODate(dueDateString)) {
			this.dueDate = DateFormatterUtil.parseISODate(dueDateString);
		}
		else {
			error(new ResourceModel("error.addgradeitem.duedate").getObject());
		}
	}

	private void createGradeItem(final AjaxRequestTarget target, final Form<?> form, final boolean createAnother) {
		final Assignment assignment = (Assignment) form.getModelObject();

		setISODates();
		assignment.setDueDate(AddOrEditGradeItemPanel.this.dueDate);

		boolean validated = true;

		// PRE VALIDATION
		// 1. if category selected and drop/keep highest/lowest selected for that category,
		// ensure points match the already established maximum for the category.
		if (assignment.getCategoryId() != null) {
			final List<CategoryDefinition> categories = AddOrEditGradeItemPanel.this.businessService.getGradebookCategories();
			final CategoryDefinition category = categories
					.stream()
					.filter(c -> (c.getId().equals(assignment.getCategoryId()))
							&& (c.getDropHighest() > 0 || c.getKeepHighest() > 0 || c.getDropLowest() > 0))
					.findFirst()
					.orElse(null);

			if (category != null && !category.getEqualWeight()) {
				final Assignment mismatched = category.getAssignmentList()
						.stream()
						.filter(a -> Double.compare(a.getPoints().doubleValue(), assignment.getPoints().doubleValue()) != 0)
						.findFirst()
						.orElse(null);
				if (mismatched != null) {
					validated = false;
					error(MessageFormat.format(getString("error.addeditgradeitem.categorypoints"), mismatched.getPoints()));
					if (assignment.isExternallyMaintained()) {
						error(MessageFormat.format(getString("info.edit_assignment_external_items"), assignment.getExternalAppName()));
					}
					target.addChildren(form, FeedbackPanel.class);
				}
			}
		}

		// 2. names cannot contain these special chars
		if (validated) {
			try {
				GradebookHelper.validateGradeItemName(assignment.getName());
			} catch (final InvalidGradeItemNameException e) {
				validated = false;
				error(getString("error.addeditgradeitem.titlecharacters"));
				target.addChildren(form, FeedbackPanel.class);
			}
		}

		// OK
		if (validated) {
			if (AddOrEditGradeItemPanel.this.mode == UiMode.EDIT) {

				final boolean success = AddOrEditGradeItemPanel.this.businessService.updateAssignment(assignment);

				if (success) {
					rubricsService.saveRubricAssociation(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, assignment.getId().toString(), getRubricParameters(""));
					getSession().success(MessageFormat.format(getString("message.edititem.success"), assignment.getName()));
					setResponsePage(getPage().getPageClass(),
							new PageParameters().add(GradebookPage.FOCUS_ASSIGNMENT_ID_PARAM, assignment.getId()));
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
					rubricsService.saveRubricAssociation(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, assignmentId.toString(), getRubricParameters(""));
					final String successMessage = MessageFormat.format(getString("notification.addgradeitem.success"), assignment.getName());
					getSession()
							.success(successMessage);

					if (createAnother) {
						final Component newFormPanel = new AddOrEditGradeItemPanel(this.window.getContentId(), this.window, null);
						AddOrEditGradeItemPanel.this.replaceWith(newFormPanel);
						this.window.setAssignmentToReturnFocusTo(String.valueOf(assignmentId));
						this.window.clearWindowClosedCallbacks();
						this.window.addWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
							@Override
							public void onClose(final AjaxRequestTarget ajaxRequestTarget) {
								setResponsePage(AddOrEditGradeItemPanel.this.window.getPage().getPageClass(),
										new PageParameters().add(GradebookPage.FOCUS_ASSIGNMENT_ID_PARAM,
												AddOrEditGradeItemPanel.this.window.getAssignmentToReturnFocusTo()));
							}
						});
						target.add(newFormPanel);
					} else {
						PageParameters params = new PageParameters();
						params.add(GradebookPage.FOCUS_ASSIGNMENT_ID_PARAM, assignmentId);
						params.add(GradebookPage.NEW_GBITEM_POPOVER_PARAM, true);
						setResponsePage(getPage().getPageClass(),params);
					}
				} else {
					target.addChildren(form, FeedbackPanel.class);
				}
			}
		}
	}
}
