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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradingType;

public class GradeSummaryTablePanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	boolean isGroupedByCategory;

	public GradeSummaryTablePanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		final Map<String, Object> data = (Map<String, Object>) getDefaultModelObject();

		final Map<Long, GbGradeInfo> grades = (Map<Long, GbGradeInfo>) data.get("grades");
		final Map<String, List<Assignment>> categoryNamesToAssignments = (Map<String, List<Assignment>>) data
				.get("categoryNamesToAssignments");
		final List<String> categoryNames = (List<String>) data.get("categoryNames");
		final Map<Long, Double> categoryAverages = (Map<Long, Double>) data.get("categoryAverages");
		final boolean categoriesEnabled = (boolean) data.get("categoriesEnabled");
		final boolean isCategoryWeightEnabled = (boolean) data.get("isCategoryWeightEnabled");
		final boolean showingStudentView = (boolean) data.get("showingStudentView");
		final GradingType gradingType = (GradingType) data.get("gradingType");
		this.isGroupedByCategory = (boolean) data.get("isGroupedByCategory");

		if (getPage() instanceof GradebookPage) {
			final GradebookPage page = (GradebookPage) getPage();
			final GradebookUiSettings settings = page.getUiSettings();
			this.isGroupedByCategory = settings.isGradeSummaryGroupedByCategory();
		}

		final WebMarkupContainer toggleActions = new WebMarkupContainer("toggleActions");
		toggleActions.setVisible(categoriesEnabled);

		final GbAjaxLink toggleCategoriesLink = new GbAjaxLink("toggleCategoriesLink") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (GradeSummaryTablePanel.this.isGroupedByCategory) {
					add(new AttributeAppender("class", " on"));
				}
				add(new AttributeModifier("aria-pressed", GradeSummaryTablePanel.this.isGroupedByCategory));
			}

			@Override
			public void onClick(final AjaxRequestTarget target) {
				if (getPage() instanceof GradebookPage) {
					final GradebookPage page = (GradebookPage) getPage();
					final GradebookUiSettings settings = page.getUiSettings();
					settings.setGradeSummaryGroupedByCategory(!settings.isGradeSummaryGroupedByCategory());
				}

				GradeSummaryTablePanel.this.isGroupedByCategory = !GradeSummaryTablePanel.this.isGroupedByCategory;
				data.put("isGroupedByCategory", GradeSummaryTablePanel.this.isGroupedByCategory);

				target.add(GradeSummaryTablePanel.this);
				target.appendJavaScript(
						String.format("new GradebookGradeSummary($(\"#%s\"), %s);",
								GradeSummaryTablePanel.this.getParent().getMarkupId(),
								showingStudentView));
			}
		};
		toggleActions.add(toggleCategoriesLink);
		toggleActions.addOrReplace(new WebMarkupContainer("expandCategoriesLink").setVisible(this.isGroupedByCategory));
		toggleActions.addOrReplace(new WebMarkupContainer("collapseCategoriesLink").setVisible(this.isGroupedByCategory));
		addOrReplace(toggleActions);

		addOrReplace(new WebMarkupContainer("weightColumnHeader")
				.setVisible(categoriesEnabled && isCategoryWeightEnabled && this.isGroupedByCategory));

		addOrReplace(new WebMarkupContainer("categoryColumnHeader").setVisible(categoriesEnabled && !this.isGroupedByCategory));

		// output all of the categories
		// within each we then add the assignments in each category
		// if not grouped by category, render all assignments in one go!
		addOrReplace(new ListView<String>("categoriesList",
				this.isGroupedByCategory ? categoryNames : Arrays.asList(getString(GradebookPage.UNCATEGORISED))) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<String> categoryItem) {
				final String categoryName = categoryItem.getModelObject();

				final List<Assignment> categoryAssignments;
				if (GradeSummaryTablePanel.this.isGroupedByCategory) {
					if (categoryNamesToAssignments.containsKey(categoryName)) {
						categoryAssignments = categoryNamesToAssignments.get(categoryName);
					} else {
						categoryAssignments = new ArrayList<Assignment>();
					}
				} else {
					categoryAssignments = new ArrayList<Assignment>();
					categoryNamesToAssignments.values().forEach(categoryAssignments::addAll);
				}

				final WebMarkupContainer categoryRow = new WebMarkupContainer("categoryRow");
				categoryRow
						.setVisible(categoriesEnabled && GradeSummaryTablePanel.this.isGroupedByCategory && !categoryAssignments.isEmpty());
				categoryItem.add(categoryRow);
				categoryRow.add(new Label("category", categoryName));

				if (!categoryAssignments.isEmpty()) {
					final Double categoryAverage = categoryAverages.get(categoryAssignments.get(0).getCategoryId());
					if (categoryAverage == null) {
						categoryRow.add(new Label("categoryGrade", getString("label.nocategoryscore")));
					} else {
						categoryRow.add(new Label("categoryGrade", FormatHelper.formatDoubleAsPercentage(categoryAverage)));
					}
				} else {
					categoryRow.add(new Label("categoryGrade", getString("label.nocategoryscore")));
				}

				String categoryWeight = "";
				if (!categoryAssignments.isEmpty()) {
					final Double weight = categoryAssignments.get(0).getWeight();
					if (weight != null) {
						categoryWeight = FormatHelper.formatDoubleAsPercentage(weight * 100);
					}
				}
				categoryRow.add(new Label("categoryWeight", categoryWeight)
						.setVisible(isCategoryWeightEnabled && GradeSummaryTablePanel.this.isGroupedByCategory));

				categoryItem.add(new ListView<Assignment>("assignmentsForCategory", categoryAssignments) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(final ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();

						if (!categoriesEnabled) {
							assignmentItem.add(new AttributeAppender("class", " gb-no-categories"));
						}

						final GbGradeInfo gradeInfo = grades.get(assignment.getId());

						final String rawGrade;
						String comment;
						if (gradeInfo != null) {
							rawGrade = gradeInfo.getGrade();
							comment = gradeInfo.getGradeComment();
						} else {
							rawGrade = "";
							comment = "";
						}

						final Label title = new Label("title", assignment.getName());
						assignmentItem.add(title);

						final BasePage page = (BasePage) getPage();

						// popover flags
						final WebMarkupContainer flags = new WebMarkupContainer("flags");
						flags.add(page.buildFlagWithPopover("isExtraCredit", getString("label.gradeitem.extracredit"))
								.add(new AttributeModifier("data-trigger", "focus"))
								.add(new AttributeModifier("data-container", "#gradeSummaryTable"))
								.setVisible(assignment.getExtraCredit()));
						flags.add(page.buildFlagWithPopover("isNotCounted", getString("label.gradeitem.notcounted"))
								.add(new AttributeModifier("data-trigger", "focus"))
								.add(new AttributeModifier("data-container", "#gradeSummaryTable"))
								.setVisible(!assignment.isCounted()));
						flags.add(page.buildFlagWithPopover("isNotReleased", getString("label.gradeitem.notreleased"))
								.add(new AttributeModifier("data-trigger", "focus"))
								.add(new AttributeModifier("data-container", "#gradeSummaryTable"))
								.setVisible(!assignment.isReleased()));
						flags.add(page
								.buildFlagWithPopover("isExternal",
										new StringResourceModel("label.gradeitem.externalapplabel", null,
												new Object[] { assignment.getExternalAppName() }).getString())
								.add(new AttributeModifier("data-trigger", "focus"))
								.add(new AttributeModifier("data-container", "#gradeSummaryTable"))
								.add(new AttributeModifier("class",
										"gb-external-app-flag " + GradeSummaryTablePanel.this.businessService.getIconClass(assignment)))
								.setVisible(assignment.isExternallyMaintained()));

						assignmentItem.add(flags);

						assignmentItem.add(new WebMarkupContainer("weight")
								.setVisible(isCategoryWeightEnabled && GradeSummaryTablePanel.this.isGroupedByCategory));

						final Label dueDate = new Label("dueDate",
								FormatHelper.formatDate(assignment.getDueDate(), getString("label.studentsummary.noduedate")));
						dueDate.add(new AttributeModifier("data-sort-key",
								assignment.getDueDate() == null ? 0 : assignment.getDueDate().getTime()));
						assignmentItem.add(dueDate);

						if (GradingType.PERCENTAGE.equals(gradingType)) {
							assignmentItem.add(new Label("grade",
									new StringResourceModel("label.percentage.valued", null,
											new Object[] { FormatHelper.formatGrade(rawGrade) })) {
								@Override
								public boolean isVisible() {
									return StringUtils.isNotBlank(rawGrade);
								}
							});
							assignmentItem.add(new Label("outOf").setVisible(false));
						} else {
							assignmentItem.add(new Label("grade", FormatHelper.formatGradeForDisplay(rawGrade)));
							assignmentItem.add(new Label("outOf",
									new StringResourceModel("label.studentsummary.outof", null, new Object[] { assignment.getPoints() })) {
								@Override
								public boolean isVisible() {
									return StringUtils.isNotBlank(rawGrade);
								}
							});
						}

						assignmentItem.add(new Label("comments", comment));
						assignmentItem.add(
								new Label("category", assignment.getCategoryName())
										.setVisible(categoriesEnabled && !GradeSummaryTablePanel.this.isGroupedByCategory));
					}
				});
			}
		});

	}
}
