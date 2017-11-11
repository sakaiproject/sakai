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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
import org.sakaiproject.service.gradebook.shared.GradingType;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;
import org.sakaiproject.util.FormattedText;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * Panel for the modal window that allows an instructor to update the ungraded items
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class UpdateUngradedItemsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	public UpdateUngradedItemsPanel(final String id, final IModel<Long> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Long assignmentId = (Long) getDefaultModelObject();

		final Assignment assignment = this.businessService.getAssignment(assignmentId);

		final GradingType gradingType = GradingType.valueOf(this.businessService.getGradebook().getGrade_type());

		// form model
		final GradeOverride override = new GradeOverride();
		override.setGrade(",".equals(FormattedText.getDecimalSeparator()) ? "0,0" : "0.0");
		final CompoundPropertyModel<GradeOverride> formModel = new CompoundPropertyModel<GradeOverride>(override);

		// build form
		// modal window forms must be submitted via AJAX so we do not specify an onSubmit here
		final Form<GradeOverride> form = new Form<GradeOverride>("form", formModel);

		final GbAjaxButton submit = new GbAjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

				final GradeOverride override = (GradeOverride) form.getModelObject();

				final Assignment assignment = UpdateUngradedItemsPanel.this.businessService.getAssignment(assignmentId);

				try {
					if(!FormatHelper.isValidDouble(override.getGrade())){
						throw new NumberFormatException();
					}
					final Double overrideValue = FormatHelper.validateDouble(override.getGrade());
					final GbGroup group = override.getGroup();

					if (isExtraCredit(overrideValue, assignment, gradingType)) {
						target.addChildren(form, FeedbackPanel.class);
					}

					final boolean success = UpdateUngradedItemsPanel.this.businessService.updateUngradedItems(assignmentId, overrideValue,
							group);

					if (success) {
						UpdateUngradedItemsPanel.this.window.close(target);
						setResponsePage(GradebookPage.class);
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

		final GbAjaxButton cancel = new GbAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				UpdateUngradedItemsPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		form.add(new TextField<Double>("grade").setRequired(true));

		if (GradingType.PERCENTAGE.equals(gradingType)) {
			form.add(new Label("points", getString("label.percentage.plain")));
		} else {
			form.add(new Label("points",
					new StringResourceModel("label.studentsummary.outof", null,
							new Object[] { assignment.getPoints() })));
		}

		final WebMarkupContainer hiddenGradePoints = new WebMarkupContainer("gradePoints");
		if (GradingType.PERCENTAGE.equals(gradingType)) {
			hiddenGradePoints.add(new AttributeModifier("value", 100));
		} else {
			hiddenGradePoints.add(new AttributeModifier("value", assignment.getPoints()));
		}
		form.add(hiddenGradePoints);

		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
		groups.add(0, new GbGroup(null, getString("groups.all"), null, GbGroup.Type.ALL));

		if (getUserRole() == GbRole.TA) {
			final boolean categoriesEnabled = this.businessService.categoriesAreEnabled();
			final List<PermissionDefinition> permissions = this.businessService.getPermissionsForUser(
					this.businessService.getCurrentUser().getId());

			final List<String> gradableGroupIds = new ArrayList<>();
			boolean canGradeAllGroups = false;

			for (final PermissionDefinition permission : permissions) {
				if (permission.getFunction().equals(GraderPermission.GRADE.toString())) {
					if (categoriesEnabled && permission.getCategoryId() != null) {
						if (permission.getCategoryId().equals(assignment.getCategoryId())) {
							if (permission.getGroupReference() == null) {
								canGradeAllGroups = true;
								break;
							} else {
								gradableGroupIds.add(permission.getGroupReference());
							}
						}
					} else if (!categoriesEnabled && permission.getGroupReference() == null) {
						canGradeAllGroups = true;
						break;
					} else {
						gradableGroupIds.add(permission.getGroupReference());
					}
				}
			}
			if (!canGradeAllGroups) {
				// remove the ones that the user can't view
				final Iterator<GbGroup> iter = groups.iterator();
				while (iter.hasNext()) {
					final GbGroup group = iter.next();
					if (!gradableGroupIds.contains(group.getReference())) {
						iter.remove();
					}
				}
			}
		}

		final GradebookUiSettings settings = ((GradebookPage) getPage()).getUiSettings();

		final DropDownChoice<GbGroup> groupAndSectionFilter = new DropDownChoice<GbGroup>(
				"group",
				new PropertyModel<GbGroup>(override, "group"),
				groups,
				new ChoiceRenderer<GbGroup>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final GbGroup g) {
						return g.getTitle();
					}

					@Override
					public String getIdValue(final GbGroup g, final int index) {
						return g.getId();
					}
				});

		groupAndSectionFilter.setNullValid(false);
		if (!groups.isEmpty()) {
			groupAndSectionFilter.setModelObject(
					(settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
		}
		form.add(groupAndSectionFilter);

		add(form);

		// feedback panel
		form.add(new GbFeedbackPanel("updateGradeFeedback"));

		// confirmation dialog
		add(new Label("confirmationMessage",
				new StringResourceModel(
						"label.updateungradeditems.confirmation.general", null,
						new Object[] { "${score}", "${group}" })).setEscapeModelStrings(false));
	}

	private boolean isExtraCredit(final Double grade, final Assignment assignment, final GradingType gradingType) {
		return (GradingType.PERCENTAGE.equals(gradingType) && grade > 100) ||
				(GradingType.POINTS.equals(gradingType) && grade > assignment.getPoints());
	}

	/**
	 * Model for this form
	 */
	class GradeOverride implements Serializable {

		private static final long serialVersionUID = 1L;

		@Getter
		@Setter
		private String grade;

		@Getter
		@Setter
		private GbGroup group;
	}

}
