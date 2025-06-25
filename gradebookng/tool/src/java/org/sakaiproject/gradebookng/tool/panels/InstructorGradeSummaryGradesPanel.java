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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;

public class InstructorGradeSummaryGradesPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	Integer configuredCategoryType;

	boolean isGroupedByCategory = false;
	boolean categoriesEnabled = false;

	public InstructorGradeSummaryGradesPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Gradebook gradebook = this.businessService.getGradebook(currentGradebookUid, currentSiteId);

		setOutputMarkupId(true);

		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final boolean groupedByCategoryByDefault = (Boolean) modelData.get("groupedByCategoryByDefault");

		this.configuredCategoryType = gradebook.getCategoryType();
		this.isGroupedByCategory = groupedByCategoryByDefault && !Objects.equals(this.configuredCategoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
		this.categoriesEnabled = !Objects.equals(this.configuredCategoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final String userId = (String) modelData.get("studentUuid");

		final GradebookPage gradebookPage = (GradebookPage) getPage();

		// build the grade matrix for the user
		final Gradebook gradebook = getGradebook();
		final SortType sortedBy = this.isGroupedByCategory ? SortType.SORT_BY_CATEGORY : SortType.SORT_BY_SORTING;
		final List<Assignment> assignments = this.businessService.getGradebookAssignmentsForStudent(currentGradebookUid, currentSiteId, userId, sortedBy);

		final boolean isCourseGradeVisible = this.businessService.isCourseGradeVisible(currentGradebookUid, currentSiteId, this.businessService.getCurrentUser().getId());
		final GbRole userRole = gradebookPage.getCurrentRole();

		final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
				gradebook,
				userRole,
				isCourseGradeVisible,
				gradebook.getCoursePointsDisplayed(),
				true,
				this.businessService.getShowCalculatedGrade());

		// TODO catch if this is null, the get(0) will throw an exception
		// TODO also catch the GbException
		final GbStudentGradeInfo studentGradeInfo = this.businessService
				.buildGradeMatrix(currentGradebookUid, currentSiteId, 
						assignments,
						new ArrayList<>(Arrays.asList(userId)), // needs to support #remove
						gradebookPage.getUiSettings())
				.get(0);
		final Map<Long, Double> categoryAverages = studentGradeInfo.getCategoryAverages();
		final Map<Long, GbGradeInfo> grades = studentGradeInfo.getGrades();

		// setup
		final List<String> categoryNames = new ArrayList<>();
		final Map<String, List<Assignment>> categoryNamesToAssignments = new HashMap<>();

		// iterate over assignments and build map of categoryname to list of assignments
		for (final Assignment assignment : assignments) {

			final String categoryName = getCategoryName(assignment);

			if (!categoryNamesToAssignments.containsKey(categoryName)) {
				categoryNames.add(categoryName);
				categoryNamesToAssignments.put(categoryName, new ArrayList<>());
			}

			categoryNamesToAssignments.get(categoryName).add(assignment);
		}
		Map<String, CategoryDefinition> categoriesMap = businessService.getGradebookCategories(currentGradebookUid, currentSiteId).stream()
				.collect(Collectors.toMap(cat -> cat.getName(), cat -> cat));

		// build the model for table
		final Map<String, Object> tableModel = new HashMap<>();
		tableModel.put("grades", grades);
		tableModel.put("categoryNamesToAssignments", categoryNamesToAssignments);
		tableModel.put("categoryNames", categoryNames);
		tableModel.put("categoryAverages", categoryAverages);
		tableModel.put("categoriesEnabled", this.categoriesEnabled);
		tableModel.put("isCategoryWeightEnabled", isCategoryWeightEnabled());
		tableModel.put("isGroupedByCategory", this.isGroupedByCategory);
		tableModel.put("showingStudentView", false);
		tableModel.put("gradeType", gradebook.getGradeType());
		tableModel.put("categoriesMap", categoriesMap);
		tableModel.put("studentUuid", userId);

		GradeSummaryTablePanel gstp = new GradeSummaryTablePanel("gradeSummaryTable", new LoadableDetachableModel<Map<String, Object>>() {
			@Override
			public Map<String, Object> load() {
				return tableModel;
			}
		});
		gstp.setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
		addOrReplace(gstp);

		// course grade, via the formatter
		final CourseGradeTransferBean courseGrade = this.businessService.getCourseGrade(currentGradebookUid, currentSiteId, userId);

		addOrReplace(new Label("courseGrade", courseGradeFormatter.format(courseGrade)).setEscapeModelStrings(false));

		addOrReplace(new Label("courseGradeNotReleasedFlag", getString("label.studentsummary.coursegradenotreleasedflag")) {
			@Override
			public boolean isVisible() {
				return showDisplayCourseGradeToStudent(gradebook, userRole, isCourseGradeVisible);
			}
		});

		addOrReplace(new Label("courseGradeNotReleasedMessage", getString("label.studentsummary.coursegradenotreleasedmessage")) {
			@Override
			public boolean isVisible() {
				return showDisplayCourseGradeToStudent(gradebook, userRole, isCourseGradeVisible);
			}
		});

		add(new AttributeModifier("data-studentid", userId));
	}

	/**
	 * Determine if the 'Display Course Grade to Student' setting should be visible for the user.
	 * @param gradebook
	 * @param userRole
	 * @param isCourseGradeVisible
	 * @return
	 */
	private boolean showDisplayCourseGradeToStudent(Gradebook gradebook,GbRole userRole, boolean isCourseGradeVisible) {
		return !gradebook.getCourseGradeDisplayed()
						&& (GbRole.INSTRUCTOR.equals(userRole) || GbRole.TA.equals(userRole) && isCourseGradeVisible)
						&& serverConfigService.getBoolean(SAK_PROP_SHOW_COURSE_GRADE_STUDENT, SAK_PROP_SHOW_COURSE_GRADE_STUDENT_DEFAULT);
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
		return Objects.equals(this.configuredCategoryType, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY);
	}
}
