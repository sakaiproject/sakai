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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CategoryScoreData;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradingType;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * The panel that is rendered for students for both their own grades view, and also when viewing it from the instructor review tab
 */
public class StudentGradeSummaryGradesPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	GbCategoryType configuredCategoryType;

	// used as a visibility flag. if any are released, show the table
	boolean someAssignmentsReleased = false;
	boolean isGroupedByCategory = false;
	boolean categoriesEnabled = false;
	boolean isAssignmentsDisplayed = false;
	private boolean courseGradeStatsEnabled;

	public StudentGradeSummaryGradesPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Gradebook gradebook = this.businessService.getGradebook();

		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final boolean groupedByCategoryByDefault = (Boolean) modelData.get("groupedByCategoryByDefault");

		this.configuredCategoryType = GbCategoryType.valueOf(gradebook.getCategory_type());
		this.isGroupedByCategory = groupedByCategoryByDefault && this.configuredCategoryType != GbCategoryType.NO_CATEGORY;
		this.categoriesEnabled = this.configuredCategoryType != GbCategoryType.NO_CATEGORY;
		this.isAssignmentsDisplayed = gradebook.isAssignmentsDisplayed();

		final GradebookInformation settings = getSettings();
		this.courseGradeStatsEnabled = settings.isCourseGradeStatsDisplayed();

		setOutputMarkupId(true);
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final String userId = (String) modelData.get("studentUuid");

		final Gradebook gradebook = getGradebook();
		final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
				gradebook,
				GbRole.STUDENT,
				gradebook.isCourseGradeDisplayed(),
				gradebook.isCoursePointsDisplayed(),
				true);

		// build up table data
		final Map<Long, GbGradeInfo> grades = this.businessService.getGradesForStudent(userId);
		final SortType sortedBy = this.isGroupedByCategory ? SortType.SORT_BY_CATEGORY : SortType.SORT_BY_SORTING;
		final List<Assignment> assignments = this.businessService.getGradebookAssignmentsForStudent(userId, sortedBy);

		final List<String> categoryNames = new ArrayList<>();
		final Map<String, List<Assignment>> categoryNamesToAssignments = new HashMap<>();
		final Map<Long, Double> categoryAverages = new HashMap<>();
		Map<String, CategoryDefinition> categoriesMap = Collections.emptyMap();
		final ModalWindow statsWindow = new ModalWindow("statsWindow");
		add(statsWindow);

		// if gradebook release setting disabled, no work to do
		if (this.isAssignmentsDisplayed) {
			// iterate over assignments and build map of categoryname to list of assignments as well as category averages
			for (final Assignment assignment : assignments) {
				// if an assignment is released, update the flag (but don't set it false again)
				// then build the category map. we don't do any of this for unreleased gradebook items
				if (assignment.isReleased()) {
					this.someAssignmentsReleased = true;
					final String categoryName = getCategoryName(assignment);

					if (!categoryNamesToAssignments.containsKey(categoryName)) {
						categoryNames.add(categoryName);
						categoryNamesToAssignments.put(categoryName, new ArrayList<>());
					}

					categoryNamesToAssignments.get(categoryName).add(assignment);
				}
			}
			// get the category scores and mark any dropped items
			for (final String catName : categoryNamesToAssignments.keySet()) {
				if (catName.equals(getString(GradebookPage.UNCATEGORISED))) {
					continue;
				}

				final List<Assignment> catItems = categoryNamesToAssignments.get(catName);
				if (!catItems.isEmpty()) {
					final Long catId = catItems.get(0).getCategoryId();
					if (catId != null) {
						this.businessService.getCategoryScoreForStudent(catId, userId, false) // Dont include non-released items in the category calc
							.ifPresent(avg -> storeAvgAndMarkIfDropped(avg, catId, categoryAverages, grades));
					}
				}
			}
			categoriesMap = this.businessService.getGradebookCategoriesForStudent(userId).stream()
				.collect(Collectors.toMap(cat -> cat.getName(), cat -> cat));
			Collections.sort(categoryNames);
		}

		// build the model for table
		final Map<String, Object> tableModel = new HashMap<>();
		tableModel.put("grades", grades);
		tableModel.put("categoryNamesToAssignments", categoryNamesToAssignments);
		tableModel.put("categoryNames", categoryNames);
		tableModel.put("categoryAverages", categoryAverages);
		tableModel.put("categoriesEnabled", this.categoriesEnabled);
		tableModel.put("isCategoryWeightEnabled", isCategoryWeightEnabled());
		tableModel.put("isGroupedByCategory", this.isGroupedByCategory);
		tableModel.put("showingStudentView", true);
		tableModel.put("gradingType", GradingType.valueOf(gradebook.getGrade_type()));
		tableModel.put("categoriesMap", categoriesMap);
		tableModel.put("studentUuid", userId);

		addOrReplace(new GradeSummaryTablePanel("gradeSummaryTable", new LoadableDetachableModel<Map<String, Object>>() {
			@Override
			public Map<String, Object> load() {
				return tableModel;
			}
		}).setVisible(this.isAssignmentsDisplayed && this.someAssignmentsReleased));

		// no assignments message
		final WebMarkupContainer noAssignments = new WebMarkupContainer("noAssignments") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !StudentGradeSummaryGradesPanel.this.someAssignmentsReleased;
			}
		};
		addOrReplace(noAssignments);

		// course grade panel
		final WebMarkupContainer courseGradePanel = new WebMarkupContainer("course-grade-panel") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return StudentGradeSummaryGradesPanel.this.serverConfigService.getBoolean(SAK_PROP_SHOW_COURSE_GRADE_STUDENT, SAK_PROP_SHOW_COURSE_GRADE_STUDENT_DEFAULT);
			}
		};
		addOrReplace(courseGradePanel);

		// course grade, via the formatter
		final CourseGrade courseGrade = this.businessService.getCourseGrade(userId);

		courseGradePanel.addOrReplace(new Label("courseGrade", courseGradeFormatter.format(courseGrade)).setEscapeModelStrings(false));

		final GbAjaxLink courseGradeStatsLink = new GbAjaxLink(
				"courseGradeStatsLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				statsWindow.setContent(new StudentCourseGradeStatisticsPanel(
						statsWindow.getContentId(),
						Model.of(StudentGradeSummaryGradesPanel.this
								.getCurrentSiteId()),
						statsWindow, courseGrade));
				statsWindow.show(target);
			}

			@Override
			public boolean isVisible() {
				return StudentGradeSummaryGradesPanel.this.courseGradeStatsEnabled
						&& courseGrade != null;
			}
		};

		courseGradePanel.add(courseGradeStatsLink);

		add(new AttributeModifier("data-studentid", userId));
	}

	/**
	 * Helper to get the category name. Looks at settings as well.
	 *
	 * @param assignment
	 * @return
	 */
	private String getCategoryName(final Assignment assignment) {
		if (!this.categoriesEnabled) {
			return getString(GradebookPage.UNCATEGORISED);
		}
		return StringUtils.isBlank(assignment.getCategoryName()) ? getString(GradebookPage.UNCATEGORISED) : assignment.getCategoryName();
	}

	/**
	 * Helper to determine if weightings are enabled
	 *
	 * @return
	 */
	private boolean isCategoryWeightEnabled() {
		return this.configuredCategoryType == GbCategoryType.WEIGHTED_CATEGORY;
	}

	private void storeAvgAndMarkIfDropped(final CategoryScoreData avg, final Long catId, final Map<Long, Double> categoryAverages,
		final Map<Long, GbGradeInfo> grades) {

		categoryAverages.put(catId, avg.score);

		grades.entrySet().stream().filter(e -> avg.droppedItems.contains(e.getKey()))
			.forEach(entry -> entry.getValue().setDroppedFromCategoryScore(true));
	}
}
