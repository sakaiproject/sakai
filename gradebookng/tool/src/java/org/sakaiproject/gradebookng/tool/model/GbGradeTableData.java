package org.sakaiproject.gradebookng.tool.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.tool.gradebook.Gradebook;

public class GbGradeTableData {
	private List<Assignment> assignments;
	private List<GbStudentGradeInfo> grades;
	private List<CategoryDefinition> categories;
	private GradebookInformation gradebookInformation;
	private GradebookUiSettings uiSettings;
	private GbRole role;
	private Map<String, String> toolNameToIconCSS;
	private String defaultIconCSS;
	private Map<String, Double> courseGradeMap;

	public GbGradeTableData(final GradebookNgBusinessService businessService,
			final GradebookUiSettings settings) {
		final GbStopWatch stopwatch = new GbStopWatch();
		stopwatch.time("GbGradeTableData init", stopwatch.getTime());

		uiSettings = settings;

		SortType sortBy = SortType.SORT_BY_SORTING;
		if (settings.isCategoriesEnabled() && settings.isGroupedByCategory()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
		}

		try {
			role = businessService.getUserRole();
		} catch (GbAccessDeniedException e) {
			throw new RuntimeException(e);
		}

		assignments = businessService.getGradebookAssignments(sortBy);
		stopwatch.time("getGradebookAssignments", stopwatch.getTime());

		grades = businessService.buildGradeMatrix(
				assignments,
				settings);
		stopwatch.time("buildGradeMatrix", stopwatch.getTime());

		categories = businessService.getGradebookCategories();
		stopwatch.time("getGradebookCategories", stopwatch.getTime());

		gradebookInformation = businessService.getGradebookSettings();
		stopwatch.time("getGradebookSettings", stopwatch.getTime());

		toolNameToIconCSS = businessService.getIconClassMap();
		defaultIconCSS = businessService.getDefaultIconClass();
		stopwatch.time("toolNameToIconCSS", stopwatch.getTime());

		final Gradebook gradebook = businessService.getGradebook();
		courseGradeMap = gradebook.getSelectedGradeMapping().getGradeMap();
	}

	public List<Assignment> getAssignments() {
		return assignments;
	}

	public List<GbStudentGradeInfo> getGrades() {
		return grades;
	}

	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public GradebookInformation getGradebookInformation() {
		return gradebookInformation;
	}

	public GradebookUiSettings getUiSettings() {
		return uiSettings;
	}

	public GbRole getRole() {
		return role;
	}

	public Map<String, String> getToolNameToIconCSS() {
		return toolNameToIconCSS;
	}

	public String getDefaultIconCSS() {
		return defaultIconCSS;
	}

	public Map<String, Double> getCourseGradeMap() {
		return courseGradeMap;
	}
}
