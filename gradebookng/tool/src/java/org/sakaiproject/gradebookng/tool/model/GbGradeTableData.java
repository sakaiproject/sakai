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
package org.sakaiproject.gradebookng.tool.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.grading.api.model.Gradebook;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode @Getter
public class GbGradeTableData {
	private List<Assignment> assignments;
	private List<GbStudentGradeInfo> grades;
	private List<CategoryDefinition> categories;
	private GradebookInformation gradebookInformation;
	private GradebookUiSettings uiSettings;
	private GbRole role;
	private Map<String, String> toolNameToIconCSS;
	private String defaultIconCSS;
	private boolean isUserAbleToEditAssessments;
	private Map<String, Double> courseGradeMap;
	private Map<String, Boolean> hasAssociatedRubricMap;
	private Long courseGradeId;
	private Long gradebookId;
	private boolean isStudentNumberVisible;
	private boolean isSectionsVisible;
	private String gradebookUid;

	public GbGradeTableData(final String currentGradebookUid, final String currentSiteId, final GradebookNgBusinessService businessService,
			final GradebookUiSettings settings, final ToolManager toolManager, final RubricsService rubricsService) {
		final GbStopWatch stopwatch = new GbStopWatch();
		stopwatch.time("GbGradeTableData init", stopwatch.getTime());

		this.gradebookUid = currentGradebookUid;
		uiSettings = settings;

		SortType sortBy = SortType.SORT_BY_SORTING;
		if (settings.isCategoriesEnabled() && settings.isGroupedByCategory()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
		}

		try {
			role = businessService.getUserRole(currentSiteId);
		} catch (GbAccessDeniedException e) {
			throw new RuntimeException(e);
		}

		isUserAbleToEditAssessments = businessService.isUserAbleToEditAssessments(currentSiteId);
		assignments = businessService.getGradebookAssignments(currentGradebookUid, currentSiteId, sortBy);
		assignments.stream()
			.filter(assignment -> assignment.getExternallyMaintained())
			.forEach(assignment -> assignment.setExternalToolTitle(businessService.getExternalAppName(assignment.getExternalAppName()))
		);
		stopwatch.time("getGradebookAssignments", stopwatch.getTime());

		String groupFilter = uiSettings.getGroupFilter() != null ? uiSettings.getGroupFilter().getId() : null;
		grades = businessService.buildGradeMatrix(currentGradebookUid, currentSiteId, 
				assignments,
				businessService.getGradeableUsers(currentGradebookUid, currentSiteId, groupFilter),
				settings);
		stopwatch.time("buildGradeMatrix", stopwatch.getTime());

		categories = businessService.getGradebookCategories(currentGradebookUid, currentSiteId);
		stopwatch.time("getGradebookCategories", stopwatch.getTime());

		gradebookInformation = businessService.getGradebookSettings(currentGradebookUid, currentSiteId);
		stopwatch.time("getGradebookSettings", stopwatch.getTime());

		toolNameToIconCSS = businessService.getIconClassMap();
		defaultIconCSS = businessService.getDefaultIconClass();
		stopwatch.time("toolNameToIconCSS", stopwatch.getTime());

		final Gradebook gradebook = businessService.getGradebook(currentGradebookUid, currentSiteId);
		courseGradeMap = gradebook.getSelectedGradeMapping().getGradeMap();

		hasAssociatedRubricMap = buildHasAssociatedRubricMap(assignments, toolManager, rubricsService);
		gradebookId = gradebook.getId();
		courseGradeId = businessService.getCourseGradeId(gradebookId);
		isStudentNumberVisible = businessService.isStudentNumberVisible(currentSiteId);

		isSectionsVisible = businessService.isSectionsVisible(currentSiteId);

	}

	public HashMap<String, Boolean> buildHasAssociatedRubricMap(final List<Assignment> assignments, final ToolManager toolManager, final RubricsService rubricsService) {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		for (Assignment assignment : assignments) {
			String externalAppName = assignment.getExternalAppName();
			if(assignment.getExternallyMaintained()) {
				String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignment.getExternalId()).reckon().getId();
				boolean hasAssociatedRubric = StringUtils.equals(externalAppName, AssignmentConstants.TOOL_ID) ? rubricsService.hasAssociatedRubric(externalAppName, assignmentId) : false;
				map.put(assignmentId, hasAssociatedRubric);
			} else {
				Long assignmentId = assignment.getId();
				boolean hasAssociatedRubric = rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, assignmentId.toString());
				map.put(assignmentId.toString(), hasAssociatedRubric);
			}
		}
		return map;
	}
}
