package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbGradingType;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GradeSummaryTablePanel extends Panel {

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

		Map<String, Object> data = (Map<String, Object>) getDefaultModelObject();

		final Map<Long, GbGradeInfo> grades  = (Map<Long, GbGradeInfo>) data.get("grades");
		final Map<String, List<Assignment>> categoryNamesToAssignments = (Map<String, List<Assignment>>) data.get("categoryNamesToAssignments");
		final List<String> categoryNames = (List<String>) data.get("categoryNames");
		final Map<Long, Double> categoryAverages = (Map<Long, Double>) data.get("categoryAverages");
		final boolean categoriesEnabled = (boolean) data.get("categoriesEnabled");
		final boolean isCategoryWeightEnabled = (boolean) data.get("isCategoryWeightEnabled");
		final boolean showingStudentView = (boolean) data.get("showingStudentView");
		final GbGradingType gradingType = (GbGradingType) data.get("gradingType");
		isGroupedByCategory = (boolean) data.get("isGroupedByCategory");

		if (getPage() instanceof GradebookPage) {
			GradebookPage page = (GradebookPage) getPage();
			GradebookUiSettings settings = page.getUiSettings();
			isGroupedByCategory = settings.isGradeSummaryGroupedByCategory();
		}

		final WebMarkupContainer toggleActions = new WebMarkupContainer("toggleActions");
		toggleActions.setVisible(categoriesEnabled);

		final GbAjaxLink toggleCategoriesLink = new GbAjaxLink("toggleCategoriesLink") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (isGroupedByCategory) {
					add(new AttributeAppender("class", " on"));
				}
				add(new AttributeModifier("aria-pressed", isGroupedByCategory));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (getPage() instanceof GradebookPage) {
					final GradebookPage page = (GradebookPage) getPage();
					final GradebookUiSettings settings = page.getUiSettings();
					settings.setGradeSummaryGroupedByCategory(!settings.isGradeSummaryGroupedByCategory());
				}

				isGroupedByCategory = !isGroupedByCategory;
				data.put("isGroupedByCategory", isGroupedByCategory);

				target.add(GradeSummaryTablePanel.this);
				target.appendJavaScript(
					String.format("new GradebookGradeSummary($(\"#%s\"), %s);",
						GradeSummaryTablePanel.this.getParent().getMarkupId(),
						showingStudentView));
			}
		};
		toggleActions.add(toggleCategoriesLink);
		toggleActions.addOrReplace(new WebMarkupContainer("expandCategoriesLink").setVisible(isGroupedByCategory));
		toggleActions.addOrReplace(new WebMarkupContainer("collapseCategoriesLink").setVisible(isGroupedByCategory));
		addOrReplace(toggleActions);

		addOrReplace(new WebMarkupContainer("categoryColumnHeader").
			setVisible(categoriesEnabled && !isGroupedByCategory));

		// output all of the categories
		// within each we then add the assignments in each category
		// if not grouped by category, render all assignments in one go!
		addOrReplace(new ListView<String>("categoriesList", isGroupedByCategory ? categoryNames : Arrays.asList(getString(GradebookPage.UNCATEGORISED))) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<String> categoryItem) {
				final String categoryName = categoryItem.getModelObject();

				final List<Assignment> categoryAssignments;
				if (isGroupedByCategory) {
					categoryAssignments = categoryNamesToAssignments.get(categoryName);
				} else {
					categoryAssignments = new ArrayList<Assignment>();
					categoryNamesToAssignments.values().forEach(categoryAssignments::addAll);
				}

				final WebMarkupContainer categoryRow = new WebMarkupContainer("categoryRow");
				categoryRow.setVisible(categoriesEnabled && isGroupedByCategory);
				categoryItem.add(categoryRow);
				categoryRow.add(new Label("category", categoryName));

				Double categoryAverage = categoryAverages.get(categoryAssignments.get(0).getCategoryId());
				if (categoryAverage == null) {
					categoryRow.add(new Label("categoryGrade", getString("label.nocategoryscore")));
				} else {
					categoryRow.add(new Label("categoryGrade", FormatHelper.formatDoubleAsPercentage(categoryAverage)));
				}

				String categoryWeight = "";
				if (!categoryAssignments.isEmpty()) {
					final Double weight = categoryAssignments.get(0).getWeight();
					if (weight != null) {
						categoryWeight = FormatHelper.formatDoubleAsPercentage(weight * 100);
					}
				}
				categoryRow.add(new Label("categoryWeight", categoryWeight));

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
						assignmentItem.add(flags);

						Label dueDate = new Label("dueDate",
							FormatHelper.formatDate(assignment.getDueDate(), getString("label.studentsummary.noduedate")));
						dueDate.add(new AttributeModifier("data-sort-key",
							assignment.getDueDate() == null ? 0 : assignment.getDueDate().getTime()));
						assignmentItem.add(dueDate);

						if (GbGradingType.PERCENTAGE.equals(gradingType)) {
							assignmentItem.add(new Label("grade",
								new StringResourceModel("label.percentage.valued", null,
									new Object[]{FormatHelper.formatGrade(rawGrade)})) {
								@Override
								public boolean isVisible() {
									return StringUtils.isNotBlank(rawGrade);
								}
							});
							assignmentItem.add(new Label("outOf").setVisible(false));
						} else {
							assignmentItem.add(new Label("grade", FormatHelper.formatGrade(rawGrade)));
							assignmentItem.add(new Label("outOf",
								new StringResourceModel("label.studentsummary.outof", null, new Object[]{assignment.getPoints()})) {
								@Override
								public boolean isVisible() {
									return StringUtils.isNotBlank(rawGrade);
								}
							});
						}

						assignmentItem.add(new Label("comments", comment));
						assignmentItem.add(
							new Label("category", assignment.getCategoryName()).
								setVisible(categoriesEnabled && !isGroupedByCategory));
					}
				});
			}
		});

	}
}
