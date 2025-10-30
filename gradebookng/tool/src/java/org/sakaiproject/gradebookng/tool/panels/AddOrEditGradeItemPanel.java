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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.AssignmentHasIllegalPointsException;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.ConflictingExternalIdException;
import org.sakaiproject.grading.api.GradebookHelper;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.InvalidGradeItemNameException;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.rubrics.api.RubricsConstants;

/**
 * The panel for the add and edit grade item window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddOrEditGradeItemPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private GbModalWindow window;

	IModel<Long> model;

	private UiMode mode;

	private boolean createAnotherChecked = false;

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
   }

	public AddOrEditGradeItemPanel(final String id, final GbModalWindow window, final IModel<Long> model, final boolean createAnotherChecked) {
		super(id);
		this.model = model;
		this.window = window;
		this.createAnotherChecked = createAnotherChecked;

		// determine mode
		if (model != null) {
			this.mode = UiMode.EDIT;
		} else {
			this.mode = UiMode.ADD;
		}
   }

    @Override
    public void onInitialize() {
		super.onInitialize();

		// setup the backing object
		Assignment assignment;

		if (this.mode == UiMode.EDIT) {
			final Long assignmentId = this.model.getObject();
			assignment = this.businessService.getAssignment(currentGradebookUid, currentSiteId, assignmentId);

			// TODO if we are in edit mode and don't have an assignment, need to error here

		} else {
			// Mode.ADD
			assignment = new Assignment();
			// Default released to true
			assignment.setReleased(true);
			// If no categories, then default counted to true
			final Gradebook gradebook = this.businessService.getGradebook(currentGradebookUid, currentSiteId);
			assignment.setCounted(Objects.equals(GradingConstants.CATEGORY_TYPE_NO_CATEGORY, gradebook.getCategoryType()));
		}

		// form model
		final Model<Assignment> formModel = new Model<Assignment>(assignment);

		// form
		final Form<Assignment> form = new Form<Assignment>("addOrEditGradeItemForm", formModel);

		// create another container - only visible in ADD mode
		WebMarkupContainer createAnotherContainer = new WebMarkupContainer("createAnotherContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return AddOrEditGradeItemPanel.this.mode == UiMode.ADD;
			}
		};
		form.add(createAnotherContainer);

		// create another checkbox
		final CheckBox createAnother = new CheckBox("createAnother", Model.of(this.createAnotherChecked));
		createAnotherContainer.add(createAnother);

		// modify the submit button to check the checkbox value
		final GbAjaxButton submit = new GbAjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {
				// Get the checkbox value
				createGradeItem(target, form, createAnother.getModelObject());
			}

			@Override
			protected void onError(final AjaxRequestTarget target) {
				target.addChildren(form, FeedbackPanel.class);
			}
		};

		// submit button label
		submit.add(new Label("submitLabel", getSubmitButtonLabel()));
		form.add(submit);

		// add the common components
		AddOrEditGradeItemPanelContent aegipc = new AddOrEditGradeItemPanelContent("subComponents", formModel, this.mode);
		form.add(aegipc);

		// feedback panel
		form.add(new GbFeedbackPanel("addGradeFeedback"));

		// cancel button
		final GbAjaxButton cancel = new GbAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {
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
	 * @return ResourceModel
	 */
	private ResourceModel getSubmitButtonLabel() {
		if (this.mode == UiMode.EDIT) {
			return new ResourceModel("button.savechanges");
		} else {
			return new ResourceModel("button.create");
		}
	}

	private void createGradeItem(final AjaxRequestTarget target, final Form<?> form, final boolean createAnother) {
		final Assignment assignment = (Assignment) form.getModelObject();

		boolean validated = true;

		// PRE VALIDATION
		// 1. if category selected and drop/keep highest/lowest selected for that category,
		// ensure points match the already established maximum for the category.
		if (assignment.getCategoryId() != null) {
			final List<CategoryDefinition> categories = AddOrEditGradeItemPanel.this.businessService.getGradebookCategories(currentGradebookUid, currentSiteId);
			final CategoryDefinition category = categories
					.stream()
					.filter(c -> (c.getId().equals(assignment.getCategoryId()))
							&& (c.getDropHighest() > 0 || c.getKeepHighest() > 0 || c.getDropLowest() > 0))
					.findFirst()
					.orElse(null);

			if (category != null && !category.getEqualWeight()) {
				final Assignment mismatched = category.getAssignmentList()
						.stream()
						.filter(a -> Double.compare(a.getPoints(), assignment.getPoints()) != 0)
						.findFirst()
						.orElse(null);
				if (mismatched != null) {
					validated = false;
					error(MessageFormat.format(getString("error.addeditgradeitem.categorypoints"), mismatched.getPoints()));
					if (assignment.getExternallyMaintained()) {
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

			Long assignmentId = null;
			boolean success = true;

			try {
				if (AddOrEditGradeItemPanel.this.mode == UiMode.EDIT) {
					assignmentId = assignment.getId();
					AddOrEditGradeItemPanel.this.businessService.updateAssignment(currentGradebookUid, currentSiteId, assignment);
				} 
				else {
					assignmentId = AddOrEditGradeItemPanel.this.businessService.addAssignment(currentGradebookUid, currentSiteId, assignment);
				}
				Map<String, String> rubricParams = getRubricParameters("");
				if (!rubricParams.isEmpty()) {
					rubricsService.saveRubricAssociation(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, assignmentId.toString(), rubricParams);
				}
			}
			catch (final AssignmentHasIllegalPointsException e) {
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
				
			if (AddOrEditGradeItemPanel.this.mode == UiMode.EDIT) {
				if (success) {
					getSession().success(MessageFormat.format(getString("message.edititem.success"), assignment.getName()));
					setResponsePage(getPage().getPageClass(),
							new PageParameters().add(GradebookPage.FOCUS_ASSIGNMENT_ID_PARAM, assignment.getId()));
				} else {
					error(new ResourceModel("message.edititem.error").getObject());
					target.addChildren(form, FeedbackPanel.class);
				}

			} else {
				if (success) {
					final String successMessage = MessageFormat.format(getString("notification.addgradeitem.success"), assignment.getName());
					getSession()
							.success(successMessage);

					if (createAnother) {
						final Component newFormPanel = new AddOrEditGradeItemPanel(this.window.getContentId(), this.window, null, true);
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
