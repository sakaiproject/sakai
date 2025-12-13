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
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.util.string.StringValue;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CategoryScoreData;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradebookInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;

/**
 * The panel that is rendered for students for both their own grades view, and also when viewing it from the instructor review tab
 */
public class StudentGradeSummaryGradesPanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(StudentGradeSummaryGradesPanel.class);

	Integer configuredCategoryType;

	// used as a visibility flag. if any are released, show the table
	boolean someAssignmentsReleased = false;
	boolean isGroupedByCategory = false;
	boolean categoriesEnabled = false;
	boolean isAssignmentsDisplayed = false;
	private boolean courseGradeStatsEnabled;
	private AbstractDefaultAjaxBehavior whatIfCalculationBehavior;
	private String studentUuid;

	public StudentGradeSummaryGradesPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		this.whatIfCalculationBehavior = new AbstractDefaultAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(final AjaxRequestTarget target) {
				handleWhatIfRequest();
			}
		};
		add(this.whatIfCalculationBehavior);

		final Gradebook gradebook = this.businessService.getGradebook(currentGradebookUid, currentSiteId);

		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final boolean groupedByCategoryByDefault = (Boolean) modelData.get("groupedByCategoryByDefault");

		this.configuredCategoryType = gradebook.getCategoryType();
		this.isGroupedByCategory = groupedByCategoryByDefault && !Objects.equals(this.configuredCategoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
		this.categoriesEnabled = !Objects.equals(this.configuredCategoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
		this.isAssignmentsDisplayed = gradebook.getAssignmentsDisplayed();

		final GradebookInformation settings = getSettings();
		this.courseGradeStatsEnabled = settings.getCourseGradeStatsDisplayed();

		setOutputMarkupId(true);
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final String userId = (String) modelData.get("studentUuid");
		this.studentUuid = userId;

		final Gradebook gradebook = getGradebook();
		String studentCourseGradeComment = this.businessService.getAssignmentGradeComment(getCurrentSiteId(), this.businessService.getCourseGradeId(gradebook.getId()), userId);
		if (StringUtils.isEmpty(studentCourseGradeComment)){
			studentCourseGradeComment = " -";
		}
		final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
				gradebook,
				GbRole.STUDENT,
				gradebook.getCourseGradeDisplayed(),
				gradebook.getCoursePointsDisplayed(),
				true,
				this.businessService.getShowCalculatedGrade());

		// build up table data
		final Map<Long, GbGradeInfo> grades = this.businessService.getGradesForStudent(currentGradebookUid, currentSiteId, userId);
		final SortType sortedBy = this.isGroupedByCategory ? SortType.SORT_BY_CATEGORY : SortType.SORT_BY_SORTING;
		final List<Assignment> assignments = this.businessService.getGradebookAssignmentsForStudent(currentGradebookUid, currentSiteId, userId, sortedBy);

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
				if (assignment.getReleased()) {
					this.someAssignmentsReleased = true;
					final String categoryName = getCategoryName(assignment);

					if (!categoryNamesToAssignments.containsKey(categoryName)) {
						categoryNames.add(categoryName);
						categoryNamesToAssignments.put(categoryName, new ArrayList<>());
					}

					categoryNamesToAssignments.get(categoryName).add(assignment);
				}
			}
			// get all category scores in one efficient operation and mark any dropped items
			final Map<Long, CategoryScoreData> allCategoryScores = this.businessService.getAllCategoryScoresForStudent(currentGradebookUid, currentSiteId, userId, false); // Dont include non-released items in the category calc
			
			for (final String catName : categoryNamesToAssignments.keySet()) {
				if (catName.equals(getString(GradebookPage.UNCATEGORISED))) {
					continue;
				}

				final List<Assignment> catItems = categoryNamesToAssignments.get(catName);
				if (!catItems.isEmpty()) {
					final Long catId = catItems.get(0).getCategoryId();
					if (catId != null) {
						final CategoryScoreData categoryScore = allCategoryScores.get(catId);
						if (categoryScore != null) {
							storeAvgAndMarkIfDropped(categoryScore, catId, categoryAverages, grades);
						}
					}
				}
			}
			categoriesMap = this.businessService.getGradebookCategoriesForStudent(currentGradebookUid, currentSiteId, userId).stream()
				.collect(Collectors.toMap(cat -> cat.getName(), cat -> cat));
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
		tableModel.put("gradeType", gradebook.getGradeType());
		tableModel.put("categoriesMap", categoriesMap);
		tableModel.put("studentUuid", userId);

		GradeSummaryTablePanel gstp = new GradeSummaryTablePanel("gradeSummaryTable", new LoadableDetachableModel<Map<String, Object>>() {
			@Override
			public Map<String, Object> load() {
				return tableModel;
			}
		});
		addOrReplace(gstp.setVisible(this.isAssignmentsDisplayed && this.someAssignmentsReleased));

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
		final CourseGradeTransferBean courseGrade = this.businessService.getCourseGrade(currentGradebookUid, currentSiteId, userId);

		courseGradePanel.addOrReplace(new Label("courseGrade", courseGradeFormatter.format(courseGrade)).setEscapeModelStrings(false));

		final GbAjaxLink courseGradeStatsLink = new GbAjaxLink(
				"courseGradeStatsLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				StudentCourseGradeStatisticsPanel scgsp = new StudentCourseGradeStatisticsPanel(statsWindow.getContentId(), statsWindow, courseGrade);
				statsWindow.setContent(scgsp);
				statsWindow.show(target);
			}

			@Override
			public boolean isVisible() {
				return StudentGradeSummaryGradesPanel.this.courseGradeStatsEnabled
						&& courseGrade != null;
			}
		};

		courseGradePanel.add(courseGradeStatsLink);
		courseGradePanel.addOrReplace(new Label("studentCourseGradeComment", studentCourseGradeComment));
		add(new AttributeModifier("data-studentid", userId));
		add(new AttributeModifier("data-whatif-url", this.whatIfCalculationBehavior.getCallbackUrl().toString()));
		add(new AttributeModifier("data-whatif-error", getString("whatif.error.generic")));
		add(new AttributeModifier("data-whatif-label-prefix", getString("whatif.label.prefix")));

		final boolean whatIfEnabled = this.someAssignmentsReleased && courseGrade != null;
		add(new AttributeModifier("data-whatif-enabled", String.valueOf(whatIfEnabled)));
	}

	/**
	 * Handle a what-if calculation request from the client.
	 */
	private void handleWhatIfRequest() {
		final RequestCycle requestCycle = RequestCycle.get();
		final StringValue payload = requestCycle.getRequest().getRequestParameters().getParameterValue("whatIf");

		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode result = mapper.createObjectNode();

		try {
			final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
			final String modelStudentId = (String) modelData.get("studentUuid");
			if (StringUtils.isBlank(modelStudentId)) {
				result.put("error", getString("whatif.error.generic"));
				requestCycle.scheduleRequestHandlerAfterCurrent(new TextRequestHandler("application/json", "UTF-8", result.toString()));
				return;
			}
			Map<Long, String> whatIfMap = Collections.emptyMap();
			final String payloadString = payload != null ? payload.toOptionalString() : null;
			if (StringUtils.isNotBlank(payloadString)) {
				final Map<String, String> raw = mapper.readValue(payloadString, new TypeReference<Map<String, String>>() {});
				if (raw != null) {
					final Map<Long, String> parsed = new HashMap<>();
					for (final Map.Entry<String, String> entry : raw.entrySet()) {
						if (StringUtils.isBlank(entry.getKey())) {
							continue;
						}
						try {
							parsed.put(Long.valueOf(entry.getKey()), entry.getValue());
						} catch (NumberFormatException nfe) {
							log.warn("Ignoring what-if entry with invalid assignment id {}", entry.getKey());
						}
					}
					whatIfMap = parsed;
				}
			}

			final CourseGradeTransferBean preview = this.businessService.calculateWhatIfCourseGrade(currentGradebookUid, currentSiteId,
					modelStudentId, whatIfMap, false);

			final Gradebook gradebook = this.businessService.getGradebook(currentGradebookUid, currentSiteId);
			final CourseGradeFormatter formatter = new CourseGradeFormatter(
					gradebook,
					GbRole.STUDENT,
					gradebook.getCourseGradeDisplayed(),
					gradebook.getCoursePointsDisplayed(),
					true,
					this.businessService.getShowCalculatedGrade());

			if (preview != null) {
				result.put("courseGrade", formatter.format(preview));
				if (preview.getMappedGrade() != null) {
					result.put("mappedGrade", preview.getMappedGrade());
				}
				if (preview.getCalculatedGrade() != null) {
					result.put("calculatedGrade", preview.getCalculatedGrade());
				}
				if (preview.getPointsEarned() != null) {
					result.put("pointsEarned", preview.getPointsEarned());
				}
				if (preview.getTotalPointsPossible() != null) {
					result.put("totalPointsPossible", preview.getTotalPointsPossible());
				}
			} else {
				result.put("courseGrade", formatter.format(null));
			}
		} catch (IllegalArgumentException e) {
			if ("invalidGrade".equals(e.getMessage())) {
				result.put("error", getString("whatif.error.invalid"));
			} else {
				result.put("error", getString("whatif.error.generic"));
			}
		} catch (Exception e) {
			log.error("Error calculating what-if course grade preview", e);
			result.put("error", getString("whatif.error.generic"));
		}

		requestCycle.scheduleRequestHandlerAfterCurrent(new TextRequestHandler("application/json", "UTF-8", result.toString()));
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

	private void storeAvgAndMarkIfDropped(final CategoryScoreData avg, final Long catId, final Map<Long, Double> categoryAverages,
		final Map<Long, GbGradeInfo> grades) {

		categoryAverages.put(catId, avg.score);

		grades.entrySet().stream().filter(e -> avg.droppedItems.contains(e.getKey()))
			.forEach(entry -> entry.getValue().setDroppedFromCategoryScore(true));
	}
}
