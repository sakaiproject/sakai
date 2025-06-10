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
import java.util.Objects;

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
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GraderPermission;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.PermissionDefinition;
import org.sakaiproject.util.NumberUtil;
import org.sakaiproject.util.api.FormattedText;

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

		final Assignment assignment = this.businessService.getAssignment(currentGradebookUid, currentSiteId, assignmentId);

		final GradeType gradeType = this.getGradebook().getGradeType();

		// form model
		final GradeOverride override = new GradeOverride();
		override.setGrade(",".equals(ComponentManager.get(FormattedText.class).getDecimalSeparator()) ? "0,0" : "0.0");
		final CompoundPropertyModel<GradeOverride> formModel = new CompoundPropertyModel<GradeOverride>(override);

		// build form
		// modal window forms must be submitted via AJAX so we do not specify an onSubmit here
		final Form<GradeOverride> form = new Form<GradeOverride>("form", formModel);

		final GbAjaxButton submit = new GbAjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {

				final GradeOverride override = (GradeOverride) form.getModelObject();

				final Assignment assignment = UpdateUngradedItemsPanel.this.businessService.getAssignment(currentGradebookUid, currentSiteId, assignmentId);

				try {
					if(!NumberUtil.isValidLocaleDouble(override.getGrade())){
						throw new NumberFormatException();
					}
					final Double overrideValue = FormatHelper.validateDouble(override.getGrade());
					final GbGroup group = override.getGroup();

					if (getExtraCredit(overrideValue, assignment, gradeType)) {
						target.addChildren(form, FeedbackPanel.class);
					}

					final boolean success = businessService.updateUngradedItems(currentGradebookUid, currentSiteId, assignmentId, override.getGrade(), group.getId());

					if (success) {
						UpdateUngradedItemsPanel.this.window.close(target);
						setResponsePage(GradebookPage.class);
					} else {
						// InvalidGradeException
						error(getString("grade.notifications.invalid"));
						target.addChildren(form, FeedbackPanel.class);
						target.appendJavaScript("new GradebookUpdateUngraded(document.getElementById(\"" + getParent().getMarkupId() + "\"), /* enableInputs = */ true);");
					}
				} catch (final NumberFormatException e) {
					// InvalidGradeException
					error(getString("grade.notifications.invalid"));
					target.addChildren(form, FeedbackPanel.class);
					target.appendJavaScript("new GradebookUpdateUngraded(document.getElementById(\"" + getParent().getMarkupId() + "\"), /* enableInputs = */ true);");
				}
			}
		};
		form.add(submit);

		final GbAjaxButton cancel = new GbAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {
				UpdateUngradedItemsPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		form.add(new TextField<Double>("grade").setRequired(true));

		if (gradeType == GradeType.PERCENTAGE) {
			form.add(new Label("points", getString("label.percentage.plain")));
		} else {
			form.add(new Label("points",
					new StringResourceModel("label.studentsummary.outof").setParameters(assignment.getPoints())));
		}

		final WebMarkupContainer hiddenGradePoints = new WebMarkupContainer("gradePoints");
		if (gradeType == GradeType.PERCENTAGE) {
			hiddenGradePoints.add(new AttributeModifier("value", 100));
		} else {
			hiddenGradePoints.add(new AttributeModifier("value", assignment.getPoints()));
		}
		form.add(hiddenGradePoints);

		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups(currentGradebookUid, currentSiteId);
		groups.add(0, new GbGroup(null, getString("groups.all"), null, GbGroup.Type.ALL));

		if (getUserRole() == GbRole.TA) {
			final boolean categoriesEnabled = this.businessService.categoriesAreEnabled(currentGradebookUid, currentSiteId);
			final List<PermissionDefinition> permissions = this.businessService.getPermissionsForUser(getCurrentUserId(), currentGradebookUid, currentSiteId);

			final List<String> gradableGroupIds = new ArrayList<>();
			boolean canGradeAllGroups = false;

			for (final PermissionDefinition permission : permissions) {
				if (permission.getFunctionName().equals(GraderPermission.GRADE.toString())) {
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
                groups.removeIf(group -> !gradableGroupIds.contains(group.getReference()));
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
						return g.getId() != null ? g.getId() : "";
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
						"label.updateungradeditems.confirmation.general")
						.setParameters("${score}", "${group}")).setEscapeModelStrings(false));
	}

	private boolean getExtraCredit(Double grade, Assignment assignment, GradeType gradeType) {

		return (gradeType == GradeType.PERCENTAGE && grade > 100)
				|| (gradeType == GradeType.POINTS && grade > assignment.getPoints());
	}

	/**
	 * Model for this form
	 */
    @Getter
	@Setter
    class GradeOverride implements Serializable {

		private static final long serialVersionUID = 1L;

		private String grade;

		private GbGroup group;
	}

}
