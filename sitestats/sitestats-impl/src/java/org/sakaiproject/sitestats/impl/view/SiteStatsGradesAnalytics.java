/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_SUCCESS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_WARNING;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.GROUP_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.ITEM_ALL;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsFilterOption;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;

@Slf4j
public class SiteStatsGradesAnalytics {

	private static final String COL_USER = "user";
	private static final String COL_ITEM = "item";
	private static final String COL_ITEM_TYPE = "itemType";
	private static final String COL_SCORE = "score";
	private static final String COL_POSSIBLE = "possible";
	private static final String COL_COMPLETE = "complete";
	private static final String COL_GRADED = "graded";
	private static final String COL_BELOW = "belowThreshold";
	private static final String COL_AVERAGE = "average";
	private static final String ITEM_KEY_PREFIX = "gradebook:";
	private static final String GRADEBOOK_TOOL_ID = "sakai.gradebookng";

	@Setter private GradingService gradingService;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetMetricSupport metricSupport;

	SiteStatsReportView byStudentReport(String siteId, SiteStatsReportRequest request, String userId) {
		GradeSnapshot snapshot = snapshot(siteId, request, userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_grades");
		if (request.isIncludeTable()) {
			view.setTable(StringUtils.isNotBlank(userId) ? ownItemTable(snapshot, request) : studentTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			if (StringUtils.isNotBlank(userId)) {
				view.setChart(percentChart(ownItemRows(snapshot), COL_ITEM, COL_COMPLETE, view.getTitle()));
			} else if (singleItemSelected(request)) {
				view.setChart(thresholdPie(snapshot, view.getTitle()));
			} else {
				view.setChart(percentChart(studentRows(snapshot), COL_USER, COL_COMPLETE, view.getTitle()));
			}
		}
		return view;
	}

	SiteStatsReportView byItemReport(String siteId, SiteStatsReportRequest request, String userId) {
		GradeSnapshot snapshot = snapshot(siteId, request, userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_grades");
		if (request.isIncludeTable()) {
			view.setTable(StringUtils.isNotBlank(userId) ? ownItemTable(snapshot, request) : itemTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			if (StringUtils.isNotBlank(userId)) {
				view.setChart(percentChart(ownItemRows(snapshot), COL_ITEM, COL_COMPLETE, view.getTitle()));
			} else {
				view.setChart(percentChart(itemRows(snapshot), COL_ITEM, COL_AVERAGE, view.getTitle()));
			}
		}
		return view;
	}

	SiteStatsReportView belowThresholdReport(String siteId, SiteStatsReportRequest request, String userId) {
		GradeSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_grades_below_threshold");
		if (request.isIncludeTable()) {
			view.setTable(belowTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			view.setChart(thresholdPie(snapshot, view.getTitle()));
		}
		return view;
	}

	SiteStatsReportView completeReport(String siteId, SiteStatsReportRequest request, String userId) {
		return StringUtils.isNotBlank(userId) ? byItemReport(siteId, allTime(request), userId)
				: byStudentReport(siteId, allTime(request), userId);
	}

	SiteStatsReportView gradedReport(String siteId, SiteStatsReportRequest request, String userId) {
		GradeSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_grades_graded");
		if (request.isIncludeTable()) {
			view.setTable(StringUtils.isNotBlank(userId) ? ownItemTable(snapshot, request) : itemTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			view.setChart(percentChart(itemRows(snapshot), COL_ITEM, COL_GRADED, view.getTitle()));
		}
		return view;
	}

	WidgetMetricValue belowThresholdValue(String siteId, String userId, SiteStatsReportRequest request) {
		GradeSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (StringUtils.isNotBlank(userId)) {
			UserTotals totals = snapshot.byUser.get(userId);
			if (totals == null || totals.graded == 0) {
				return WidgetMetricValue.of("-");
			}
			boolean below = totals.percent() < snapshot.threshold;
			return WidgetMetricValue.of(below ? message("overview_status_below_threshold")
					: message("overview_status_meeting_threshold"));
		}
		return WidgetMetricValue.withPercentage(snapshot.belowUsers + " / " + snapshot.gradedUsers,
				snapshot.belowPercent());
	}

	WidgetMetricValue completeValue(String siteId, String userId, SiteStatsReportRequest request) {
		GradeSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (StringUtils.isNotBlank(userId)) {
			UserTotals totals = snapshot.byUser.get(userId);
			int percent = totals == null ? 0 : totals.percent();
			return WidgetMetricValue.withPercentage(ratio(totals == null ? 0d : totals.earned,
					totals == null ? 0d : totals.gradedPossible), percent);
		}
		return WidgetMetricValue.withPercentage(snapshot.completeUsers + " / " + snapshot.byUser.size(),
				snapshot.completePercent());
	}

	WidgetMetricValue classAverageValue(String siteId, String userId, SiteStatsReportRequest request) {
		GradeSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (snapshot.gradedUsers == 0) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.of(metricSupport.formatPercent(snapshot.averagePercent()));
	}

	WidgetMetricValue gradedValue(String siteId, String userId, SiteStatsReportRequest request) {
		GradeSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (StringUtils.isNotBlank(userId)) {
			UserTotals totals = snapshot.byUser.get(userId);
			int graded = totals == null ? 0 : totals.graded;
			int expected = totals == null ? 0 : totals.expected;
			return WidgetMetricValue.withPercentage(graded + " / " + expected, percent(graded, expected));
		}
		return WidgetMetricValue.withPercentage(snapshot.gradedCells + " / " + snapshot.expectedCells,
				snapshot.gradedPercent());
	}

	SiteStatsChart gradingFunnelChart(String siteId, String userId, SiteStatsReportRequest request) {
		GradeSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (StringUtils.isNotBlank(userId)) {
			return ownGradingFunnel(snapshot, userId);
		}
		return siteGradingFunnel(snapshot);
	}

	private SiteStatsChart siteGradingFunnel(GradeSnapshot snapshot) {
		int enrolled = snapshot.byUser.size();
		if (enrolled == 0) {
			return null;
		}
		int complete = snapshot.completeUsers;
		int partial = Math.max(0, snapshot.gradedUsers - complete);
		String enrolledLabel = message("overview_funnel_enrolled");
		String withGradesLabel = message("overview_funnel_with_grades");
		String meetingLabel = message("overview_funnel_meeting_threshold");
		SiteStatsChart chart = funnelChart(message("overview_title_grades_funnel"),
				enrolledLabel, enrolled,
				withGradesLabel, complete,
				meetingLabel, snapshot.meetingUsers);
		chart.setStacked(true);
		SiteStatsChartDataset completeDataset = chart.getDatasets().get(0);
		completeDataset.setKey("complete");
		completeDataset.setLabel(message("overview_funnel_fully_graded"));
		SiteStatsChartDataset partialDataset = dataset("partial", message("overview_funnel_not_fully_graded"),
				CHART_COLOR_WARNING);
		partialDataset.getPoints().add(point(enrolledLabel, Integer.valueOf(0)));
		partialDataset.getPoints().add(point(withGradesLabel, Integer.valueOf(partial)));
		partialDataset.getPoints().add(point(meetingLabel, Integer.valueOf(0)));
		chart.getDatasets().add(partialDataset);
		return chart;
	}

	private SiteStatsChart ownGradingFunnel(GradeSnapshot snapshot, String userId) {
		UserTotals totals = snapshot.byUser.get(userId);
		if (totals == null || totals.expected == 0) {
			return null;
		}
		int meeting = 0;
		for (GradeCell cell : snapshot.cells) {
			if (userId.equals(cell.userId) && !cell.excused && cell.graded
					&& cell.percent() >= snapshot.threshold) {
				meeting++;
			}
		}
		return funnelChart(message("overview_title_grades_funnel_own"),
				message("overview_funnel_your_items"), totals.expected,
				message("overview_title_grades_graded"), totals.graded,
				message("overview_funnel_meeting_threshold"), meeting);
	}

	private SiteStatsChart funnelChart(String title, String firstLabel, int first, String secondLabel, int second,
			String thirdLabel, int third) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey("stage");
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		chart.setItemLabelsVisible(false);
		chart.setCompact(true);
		chart.setHorizontal(true);
		SiteStatsChartDataset dataset = dataset("funnel", title);
		dataset.getPoints().add(point(firstLabel, Integer.valueOf(first)));
		dataset.getPoints().add(point(secondLabel, Integer.valueOf(second)));
		dataset.getPoints().add(point(thirdLabel, Integer.valueOf(third), CHART_COLOR_SUCCESS));
		chart.getDatasets().add(dataset);
		return chart;
	}

	List<SiteStatsFilterOption> itemFilterOptions(String siteId, boolean studentView) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		for (ItemChoice choice : itemChoices(siteId, studentView)) {
			options.add(new SiteStatsFilterOption(choice.key, choice.label));
		}
		return options;
	}

	Set<String> presentItemTypes(String siteId, boolean studentView) {
		Set<String> types = new LinkedHashSet<String>();
		String userId = studentView ? context.currentUserId() : null;
		for (Assignment assignment : loadGradebookItems(siteId, studentView)) {
			if (!countsTowardGrades(siteId, assignment, userId)) {
				continue;
			}
			types.add(itemType(assignment));
		}
		return types;
	}

	private GradeSnapshot snapshot(String siteId, SiteStatsReportRequest request, String userId) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequest.normalized(request);
		GradeSnapshot snapshot = new GradeSnapshot(threshold(siteId));
		if (filterCatalog.isIncompleteCustomRange(safeRequest)) {
			return snapshot;
		}
		List<Assignment> items = loadItems(siteId, safeRequest, userId);
		Set<String> users = roster(siteId, safeRequest, userId);
		if (items.isEmpty() || users.isEmpty()) {
			return snapshot;
		}
		Map<Long, Map<String, GradeDefinition>> grades = gradesByItem(siteId, items, new ArrayList<String>(users));
		Map<String, Map<String, Double>> letterScales = new HashMap<String, Map<String, Double>>();
		for (Assignment item : items) {
			Set<String> expectedUsers = expectedUsersForItem(siteId, item, users);
			if (expectedUsers.isEmpty()) {
				continue;
			}
			ItemTotals itemTotals = snapshot.item(itemKey(item), itemDisplayTitle(siteId, item), itemType(item));
			for (String expectedUser : expectedUsers) {
				GradeDefinition grade = gradeFor(grades, item.getId(), expectedUser);
				GradeCell cell = cell(siteId, item, expectedUser, grade, letterScales);
				snapshot.add(cell);
				itemTotals.add(cell, snapshot.threshold);
				snapshot.user(expectedUser).add(cell);
			}
		}
		snapshot.summarize();
		return snapshot;
	}

	private List<Assignment> loadItems(String siteId, SiteStatsReportRequest request, String userId) {
		List<Assignment> assignments = loadGradebookItems(siteId, StringUtils.isNotBlank(userId));
		if (assignments.isEmpty()) {
			return Collections.emptyList();
		}
		List<Assignment> included = new ArrayList<Assignment>();
		for (Assignment assignment : assignments) {
			if (!includeAssignment(siteId, assignment, request, userId)) {
				continue;
			}
			included.add(assignment);
		}
		return included;
	}

	private List<Assignment> loadGradebookItems(String siteId, boolean studentView) {
		List<Assignment> assignments = new ArrayList<Assignment>();
		for (String gradebookUid : gradebookUids(siteId)) {
			try {
				List<Assignment> loaded = studentView
						? gradingService.getViewableAssignmentsForCurrentUser(gradebookUid, siteId, SortType.SORT_BY_NONE)
						: gradingService.getAssignments(gradebookUid, siteId, SortType.SORT_BY_NONE);
				if (loaded != null) {
					assignments.addAll(loaded);
				}
			} catch (RuntimeException e) {
				log.warn("Unable to load gradebook assignments for site {} gradebook {}", siteId, gradebookUid, e);
			}
		}
		return assignments;
	}

	private boolean includeAssignment(String siteId, Assignment assignment, SiteStatsReportRequest request, String userId) {
		if (!countsTowardGrades(siteId, assignment, userId)) {
			return false;
		}
		if (!filterCatalog.includesItemType(request, itemType(assignment))) {
			return false;
		}
		if (!includeItem(request, assignment)) {
			return false;
		}
		Instant due = assignment.getDueDate() == null ? null : assignment.getDueDate().toInstant();
		return due == null || filterCatalog.isDueInRange(due, request);
	}

	private boolean countsTowardGrades(String siteId, Assignment assignment, String userId) {
		if (assignment == null || assignment.getId() == null || Boolean.TRUE.equals(assignment.getUngraded())
				|| Boolean.TRUE.equals(assignment.getExtraCredit()) || !Boolean.TRUE.equals(assignment.getCounted())) {
			return false;
		}
		if (assignment.getPoints() == null || assignment.getPoints().doubleValue() <= 0) {
			return false;
		}
		if (StringUtils.isNotBlank(userId) && !Boolean.TRUE.equals(assignment.getReleased())) {
			return false;
		}
		if (StringUtils.isNotBlank(userId) && !userHasGradebook(siteId, userId, gradebookUid(siteId, assignment))) {
			return false;
		}
		return true;
	}

	private Map<Long, Map<String, GradeDefinition>> gradesByItem(String siteId, List<Assignment> items, List<String> users) {
		Map<String, List<Long>> idsByGradebook = new LinkedHashMap<String, List<Long>>();
		for (Assignment item : items) {
			String uid = gradebookUid(siteId, item);
			List<Long> ids = idsByGradebook.get(uid);
			if (ids == null) {
				ids = new ArrayList<Long>();
				idsByGradebook.put(uid, ids);
			}
			ids.add(item.getId());
		}
		Map<Long, Map<String, GradeDefinition>> byItem = new HashMap<Long, Map<String, GradeDefinition>>();
		for (Map.Entry<String, List<Long>> entry : idsByGradebook.entrySet()) {
			Map<Long, List<GradeDefinition>> loaded;
			try {
				loaded = gradingService.getGradesWithoutCommentsForStudentsForItems(entry.getKey(), siteId,
						entry.getValue(), users);
			} catch (RuntimeException e) {
				log.warn("Unable to load grades for site {} gradebook {}", siteId, entry.getKey(), e);
				continue;
			}
			if (loaded == null || loaded.isEmpty()) {
				continue;
			}
			for (Map.Entry<Long, List<GradeDefinition>> grades : loaded.entrySet()) {
				Map<String, GradeDefinition> byUser = new HashMap<String, GradeDefinition>();
				if (grades.getValue() != null) {
					for (GradeDefinition grade : grades.getValue()) {
						if (grade != null && StringUtils.isNotBlank(grade.getStudentUid())) {
							byUser.put(grade.getStudentUid(), grade);
						}
					}
				}
				byItem.put(grades.getKey(), byUser);
			}
		}
		return byItem;
	}

	private GradeDefinition gradeFor(Map<Long, Map<String, GradeDefinition>> grades, Long itemId, String userId) {
		Map<String, GradeDefinition> byUser = grades.get(itemId);
		return byUser == null ? null : byUser.get(userId);
	}

	private GradeCell cell(String siteId, Assignment item, String userId, GradeDefinition grade,
			Map<String, Map<String, Double>> letterScales) {
		boolean excused = grade != null && grade.isExcused();
		Double score = excused ? null : parseScore(siteId, item, grade, letterScales);
		boolean graded = !excused && score != null;
		return new GradeCell(userId, itemKey(item), item.getName(), itemType(item), score, item.getPoints(), graded, excused);
	}

	private Double parseScore(String siteId, Assignment item, GradeDefinition grade,
			Map<String, Map<String, Double>> letterScales) {
		if (grade == null || StringUtils.isBlank(grade.getGrade()) || item.getPoints() == null) {
			return null;
		}
		if (GradingConstants.GRADE_TYPE_LETTER.equals(grade.getGradeEntryType())) {
			Double percent = letterPercent(siteId, gradebookUid(siteId, item), grade.getGrade(), letterScales);
			return percent == null ? null : Double.valueOf(percent.doubleValue() / 100d * item.getPoints().doubleValue());
		}
		Double parsed = metricSupport.parseNumber(grade.getGrade());
		if (parsed == null) {
			return null;
		}
		if (GradingConstants.GRADE_TYPE_PERCENTAGE.equals(grade.getGradeEntryType())) {
			return Double.valueOf(parsed.doubleValue() / 100d * item.getPoints().doubleValue());
		}
		return parsed;
	}

	private Double letterPercent(String siteId, String gradebookUid, String letter,
			Map<String, Map<String, Double>> letterScales) {
		Map<String, Double> scale = letterScales.get(gradebookUid);
		if (scale == null) {
			scale = loadLetterScale(siteId, gradebookUid);
			letterScales.put(gradebookUid, scale);
		}
		Double percent = scale.get(letter);
		if (percent != null) {
			return percent;
		}
		for (Map.Entry<String, Double> entry : scale.entrySet()) {
			if (letter.equalsIgnoreCase(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	private Map<String, Double> loadLetterScale(String siteId, String gradebookUid) {
		try {
			GradebookInformation information = gradingService.getGradebookInformation(gradebookUid, siteId);
			if (information == null || information.getSelectedGradingScaleBottomPercents() == null) {
				return Collections.emptyMap();
			}
			return information.getSelectedGradingScaleBottomPercents();
		} catch (RuntimeException e) {
			log.warn("Unable to load letter-grade scale for site {} gradebook {}", siteId, gradebookUid, e);
			return Collections.emptyMap();
		}
	}

	private Set<String> roster(String siteId, SiteStatsReportRequest request, String userId) {
		return filterUsers(siteId, filterCatalog.gradeableUsers(siteId), request, userId);
	}

	private Set<String> expectedUsersForItem(String siteId, Assignment item, Set<String> roster) {
		Set<String> expected = new HashSet<String>(roster);
		String uid = gradebookUid(siteId, item);
		if (StringUtils.isNotBlank(uid) && !siteId.equals(uid)) {
			expected.retainAll(groupMembers(siteId, uid));
		}
		return expected;
	}

	private Set<String> filterUsers(String siteId, Set<String> users, SiteStatsReportRequest request, String userId) {
		Set<String> remaining = new HashSet<String>(users);
		if (StringUtils.isNotBlank(userId)) {
			return remaining.contains(userId) ? Collections.singleton(userId) : Collections.<String>emptySet();
		}
		String role = filterCatalog.roleFilter(request);
		if (!ReportManager.WHO_ALL.equals(role) && StringUtils.isNotBlank(role)) {
			Site site = site(siteId);
			Set<String> matchingRole = new HashSet<String>();
			for (String candidate : remaining) {
				if (role.equals(memberRoleFor(site, candidate))) {
					matchingRole.add(candidate);
				}
			}
			remaining = matchingRole;
		}
		String groupId = filterCatalog.groupFilter(request);
		if (!GROUP_ALL.equals(groupId) && StringUtils.isNotBlank(groupId)) {
			remaining.retainAll(groupMembers(siteId, groupId));
		}
		return remaining;
	}

	private Set<String> groupMembers(String siteId, String groupId) {
		try {
			Site site = context.getSiteService().getSite(siteId);
			Group group = site.getGroup(groupId);
			if (group == null || group.getUsers() == null) {
				return Collections.emptySet();
			}
			return new HashSet<String>(group.getUsers());
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
			return Collections.emptySet();
		}
	}

	private Site site(String siteId) {
		try {
			return context.getSiteService().getSite(siteId);
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
			return null;
		}
	}

	private String memberRoleFor(Site site, String userId) {
		if (site == null) {
			return null;
		}
		Member member = site.getMember(userId);
		if (member != null && member.getRole() != null) {
			return member.getRole().getId();
		}
		return null;
	}

	private boolean includeItem(SiteStatsReportRequest request, Assignment assignment) {
		if (!singleItemSelected(request)) {
			return true;
		}
		String selected = filterCatalog.itemFilter(request);
		String key = itemKey(assignment);
		return selected.equals(key) || selected.equals(String.valueOf(assignment.getId()));
	}

	private boolean singleItemSelected(SiteStatsReportRequest request) {
		String selected = filterCatalog.itemFilter(request);
		return StringUtils.isNotBlank(selected) && !ITEM_ALL.equals(selected);
	}

	private double threshold(String siteId) {
		return context.getStatsManager().getGradesThreshold(siteId);
	}

	private List<ItemChoice> itemChoices(String siteId, boolean studentView) {
		List<ItemChoice> choices = new ArrayList<ItemChoice>();
		String userId = studentView ? context.currentUserId() : null;
		for (Assignment assignment : loadGradebookItems(siteId, studentView)) {
			if (assignment == null || assignment.getId() == null || Boolean.TRUE.equals(assignment.getUngraded())
					|| (studentView && !Boolean.TRUE.equals(assignment.getReleased()))) {
				continue;
			}
			if (studentView && !userHasGradebook(siteId, userId, gradebookUid(siteId, assignment))) {
				continue;
			}
			choices.add(new ItemChoice(itemKey(assignment), itemChoiceLabel(siteId, assignment)));
		}
		Collections.sort(choices, Comparator.comparing((ItemChoice choice) -> choice.label, String.CASE_INSENSITIVE_ORDER));
		return choices;
	}

	private String itemChoiceLabel(String siteId, Assignment assignment) {
		return itemDisplayTitle(siteId, assignment) + " (" + itemTypeLabel(itemType(assignment)) + ")";
	}

	private String itemDisplayTitle(String siteId, Assignment assignment) {
		String title = StringUtils.defaultIfBlank(assignment.getName(), itemTypeLabel(itemType(assignment)));
		String groupTitle = gradebookGroupTitle(siteId, gradebookUid(siteId, assignment));
		if (StringUtils.isBlank(groupTitle)) {
			return title;
		}
		return title + " — " + groupTitle;
	}

	private String itemKey(Assignment assignment) {
		return ITEM_KEY_PREFIX + assignment.getId();
	}

	private String itemType(Assignment assignment) {
		if (!Boolean.TRUE.equals(assignment.getExternallyMaintained())) {
			return GRADEBOOK_TOOL_ID;
		}
		return StringUtils.defaultIfBlank(assignment.getExternalAppName(), GRADEBOOK_TOOL_ID);
	}

	private String itemTypeLabel(String itemType) {
		if (StringUtils.isBlank(itemType)) {
			return context.toolName(GRADEBOOK_TOOL_ID);
		}
		return context.toolName(itemType);
	}

	private List<String> gradebookUids(String siteId) {
		if (!gradingService.isGradebookGroupEnabled(siteId)) {
			return Collections.singletonList(siteId);
		}
		List<String> uids = gradingService.getGradebookGroupInstancesIds(siteId);
		if (uids == null || uids.isEmpty()) {
			return Collections.singletonList(siteId);
		}
		return uids;
	}

	private String gradebookUid(String siteId, Assignment assignment) {
		String uid = StringUtils.firstNonBlank(assignment.getGradebookUid(), assignment.getContext());
		return StringUtils.defaultIfBlank(uid, siteId);
	}

	private boolean userHasGradebook(String siteId, String userId, String gradebookUid) {
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(gradebookUid) || siteId.equals(gradebookUid)
				|| !gradingService.isGradebookGroupEnabled(siteId)) {
			return true;
		}
		List<String> instances = gradingService.getGradebookInstancesForUser(siteId, userId);
		return instances != null && instances.contains(gradebookUid);
	}

	private String gradebookGroupTitle(String siteId, String gradebookUid) {
		if (StringUtils.isBlank(gradebookUid) || siteId.equals(gradebookUid)) {
			return null;
		}
		try {
			Group group = context.getSiteService().getSite(siteId).getGroup(gradebookUid);
			return group == null ? null : StringUtils.trimToNull(group.getTitle());
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
			return null;
		}
	}

	private SiteStatsTable studentTable(GradeSnapshot snapshot, SiteStatsReportRequest request) {
		return pagedTable(studentColumns(), studentRows(snapshot), request);
	}

	private SiteStatsTable itemTable(GradeSnapshot snapshot, SiteStatsReportRequest request) {
		return pagedTable(itemColumns(), itemRows(snapshot), request);
	}

	private SiteStatsTable ownItemTable(GradeSnapshot snapshot, SiteStatsReportRequest request) {
		return pagedTable(ownItemColumns(), ownItemRows(snapshot), request);
	}

	private SiteStatsTable belowTable(GradeSnapshot snapshot, SiteStatsReportRequest request) {
		List<SiteStatsTableRow> rows = new ArrayList<SiteStatsTableRow>();
		for (SiteStatsTableRow row : studentRows(snapshot)) {
			if (Boolean.TRUE.equals(rawBoolean(row, COL_BELOW))) {
				rows.add(row);
			}
		}
		return pagedTable(studentColumns(), rows, request);
	}

	private List<SiteStatsTableColumn> studentColumns() {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_USER, "th_user", "string", "start"));
		columns.add(column(COL_SCORE, "th_score", "number", "end"));
		columns.add(column(COL_POSSIBLE, "th_possible", "number", "end"));
		columns.add(column(COL_COMPLETE, "th_complete", "number", "end"));
		columns.add(column(COL_GRADED, "th_graded", "number", "end"));
		columns.add(column(COL_BELOW, "th_below_threshold", "string", "start"));
		return columns;
	}

	private List<SiteStatsTableColumn> itemColumns() {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_ITEM, "th_item", "string", "start"));
		columns.add(column(COL_ITEM_TYPE, "th_item_type", "string", "start"));
		columns.add(column(COL_AVERAGE, "th_average", "number", "end"));
		columns.add(column(COL_GRADED, "th_graded", "number", "end"));
		columns.add(column(COL_BELOW, "th_below_threshold", "number", "end"));
		return columns;
	}

	private List<SiteStatsTableColumn> ownItemColumns() {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_ITEM, "th_item", "string", "start"));
		columns.add(column(COL_ITEM_TYPE, "th_item_type", "string", "start"));
		columns.add(column(COL_SCORE, "th_score", "number", "end"));
		columns.add(column(COL_POSSIBLE, "th_possible", "number", "end"));
		columns.add(column(COL_COMPLETE, "th_complete", "number", "end"));
		return columns;
	}

	private List<SiteStatsTableRow> studentRows(GradeSnapshot snapshot) {
		List<UserTotals> rows = new ArrayList<UserTotals>(snapshot.byUser.values());
		Collections.sort(rows, Comparator.comparing((UserTotals row) -> displayUser(row.userId), String.CASE_INSENSITIVE_ORDER));
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (UserTotals row : rows) {
			SiteStatsTableRow tableRow = new SiteStatsTableRow();
			boolean graded = row.graded > 0;
			boolean below = graded && row.percent() < snapshot.threshold;
			tableRow.getCells().put(COL_USER, cell(displayUser(row.userId), row.userId));
			tableRow.getCells().put(COL_SCORE, numberCell(row.earned));
			tableRow.getCells().put(COL_POSSIBLE, numberCell(row.possible));
			tableRow.getCells().put(COL_COMPLETE, graded ? percentCell(row.percent()) : cell("-", null));
			tableRow.getCells().put(COL_GRADED, numberCell(row.graded));
			tableRow.getCells().put(COL_BELOW, graded
					? cell(below ? message("overview_status_below_threshold")
							: message("overview_status_meeting_threshold"), Boolean.valueOf(below))
					: cell("-", null));
			tableRows.add(tableRow);
		}
		return tableRows;
	}

	private List<SiteStatsTableRow> itemRows(GradeSnapshot snapshot) {
		List<ItemTotals> rows = new ArrayList<ItemTotals>(snapshot.byItem.values());
		Collections.sort(rows, Comparator.comparing((ItemTotals row) -> row.title, String.CASE_INSENSITIVE_ORDER));
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (ItemTotals row : rows) {
			SiteStatsTableRow tableRow = new SiteStatsTableRow();
			tableRow.getCells().put(COL_ITEM, cell(row.title, row.key));
			tableRow.getCells().put(COL_ITEM_TYPE, cell(itemTypeLabel(row.itemType), row.itemType));
			tableRow.getCells().put(COL_AVERAGE, percentCell(row.averagePercent()));
			tableRow.getCells().put(COL_GRADED, numberCell(row.graded));
			tableRow.getCells().put(COL_BELOW, numberCell(row.below));
			tableRows.add(tableRow);
		}
		return tableRows;
	}

	private List<SiteStatsTableRow> ownItemRows(GradeSnapshot snapshot) {
		List<GradeCell> cells = new ArrayList<GradeCell>(snapshot.cells);
		Collections.sort(cells, Comparator.comparing((GradeCell cell) -> cell.itemTitle, String.CASE_INSENSITIVE_ORDER));
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (GradeCell gradeCell : cells) {
			if (gradeCell.excused) {
				continue;
			}
			SiteStatsTableRow tableRow = new SiteStatsTableRow();
			tableRow.getCells().put(COL_ITEM, cell(gradeCell.itemTitle, gradeCell.itemKey));
			tableRow.getCells().put(COL_ITEM_TYPE, cell(itemTypeLabel(gradeCell.itemType), gradeCell.itemType));
			tableRow.getCells().put(COL_SCORE, gradeCell.graded ? numberCell(gradeCell.score.doubleValue()) : cell("-", null));
			tableRow.getCells().put(COL_POSSIBLE, numberCell(gradeCell.possible.doubleValue()));
			tableRow.getCells().put(COL_COMPLETE, gradeCell.graded ? percentCell(gradeCell.percent()) : cell("-", null));
			tableRows.add(tableRow);
		}
		return tableRows;
	}

	private SiteStatsTable pagedTable(List<SiteStatsTableColumn> columns, List<SiteStatsTableRow> rows,
			SiteStatsReportRequest request) {
		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(message("overview_title_grades"));
		table.setColumns(columns);
		table.setPage(request.getPage());
		table.setPageSize(request.getPageSize());
		table.setTotalRows(rows.size());
		table.setRows(page(rows, request));
		return table;
	}

	private List<SiteStatsTableRow> page(List<SiteStatsTableRow> rows, SiteStatsReportRequest request) {
		int from = (request.getPage() - 1) * request.getPageSize();
		if (from >= rows.size()) {
			return Collections.emptyList();
		}
		int to = Math.min(from + request.getPageSize(), rows.size());
		return new ArrayList<SiteStatsTableRow>(rows.subList(from, to));
	}

	private SiteStatsChart percentChart(List<SiteStatsTableRow> rows, String labelKey, String valueKey, String title) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey(labelKey);
		chart.setYKey(valueKey);
		chart.setEmptyMessage(message("no_data"));
		if (rows.isEmpty()) {
			return chart;
		}
		SiteStatsChartDataset dataset = dataset(valueKey, message(valueKey.equals(COL_GRADED) ? "th_graded" : "th_complete"));
		for (SiteStatsTableRow row : rows) {
			dataset.getPoints().add(point(display(row, labelKey), number(row, valueKey)));
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsChart thresholdPie(GradeSnapshot snapshot, String title) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_PIE);
		chart.setXKey(COL_BELOW);
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		if (snapshot.byUser.isEmpty()) {
			return chart;
		}
		SiteStatsChartDataset dataset = dataset("threshold", title);
		dataset.getPoints().add(point(message("overview_status_below_threshold"), Integer.valueOf(snapshot.belowUsers)));
		dataset.getPoints().add(point(message("overview_status_meeting_threshold"),
				Integer.valueOf(snapshot.meetingUsers)));
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsChartDataset dataset(String key, String label) {
		return dataset(key, label, null);
	}

	private SiteStatsChartDataset dataset(String key, String label, String color) {
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(key);
		dataset.setLabel(label);
		dataset.setColor(color);
		return dataset;
	}

	private SiteStatsChartPoint point(String label, Number y) {
		return point(label, y, null);
	}

	private SiteStatsChartPoint point(String label, Number y, String color) {
		SiteStatsChartPoint point = new SiteStatsChartPoint();
		point.setX(label);
		point.setLabel(label);
		point.setY(y);
		point.setColor(color);
		return point;
	}

	private SiteStatsTableColumn column(String key, String labelKey, String type, String align) {
		SiteStatsTableColumn column = new SiteStatsTableColumn();
		column.setKey(key);
		column.setLabel(message(labelKey));
		column.setType(type);
		column.setAlign(align);
		column.setSortable(true);
		column.setSortKey(key);
		return column;
	}

	private SiteStatsTableCell cell(String display, Object raw) {
		SiteStatsTableCell cell = new SiteStatsTableCell();
		cell.setDisplay(display);
		cell.setRaw(raw);
		cell.setSort(raw);
		return cell;
	}

	private SiteStatsTableCell numberCell(double value) {
		return cell(metricSupport.formatNumber(value), Double.valueOf(value));
	}

	private SiteStatsTableCell numberCell(int value) {
		return cell(String.valueOf(value), Integer.valueOf(value));
	}

	private SiteStatsTableCell percentCell(int value) {
		return cell(metricSupport.formatPercent(value), Integer.valueOf(value));
	}

	private String ratio(double partial, double total) {
		return metricSupport.formatNumber(partial) + " / " + metricSupport.formatNumber(total);
	}

	private String displayUser(String userId) {
		return StringUtils.defaultIfBlank(metricSupport.userTooltip(userId), userId);
	}

	private String display(SiteStatsTableRow row, String key) {
		SiteStatsTableCell cell = row.getCells().get(key);
		return cell == null ? "" : StringUtils.defaultString(cell.getDisplay());
	}

	private Number number(SiteStatsTableRow row, String key) {
		SiteStatsTableCell cell = row.getCells().get(key);
		if (cell == null || !(cell.getRaw() instanceof Number)) {
			return Integer.valueOf(0);
		}
		return (Number) cell.getRaw();
	}

	private Boolean rawBoolean(SiteStatsTableRow row, String key) {
		SiteStatsTableCell cell = row.getCells().get(key);
		if (cell == null || !(cell.getRaw() instanceof Boolean)) {
			return Boolean.FALSE;
		}
		return (Boolean) cell.getRaw();
	}

	private int percent(int partial, int total) {
		return total == 0 ? 0 : (int) Math.round(100d * partial / total);
	}

	private SiteStatsReportView reportShell(String siteId, SiteStatsReportRequest request, String titleKey) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message(titleKey));
		view.setPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		return view;
	}

	private SiteStatsReportRequest allTime(SiteStatsReportRequest request) {
		SiteStatsReportRequest allTime = SiteStatsReportRequest.normalized(request);
		allTime.setDate(ReportManager.WHEN_ALL);
		allTime.setWhenFrom(null);
		allTime.setWhenTo(null);
		return allTime;
	}

	private String message(String key) {
		return context.message(key);
	}

	private static class GradeCell {
		private final String userId;
		private final String itemKey;
		private final String itemTitle;
		private final String itemType;
		private final Double score;
		private final Double possible;
		private final boolean graded;
		private final boolean excused;

		GradeCell(String userId, String itemKey, String itemTitle, String itemType, Double score, Double possible,
				boolean graded, boolean excused) {
			this.userId = userId;
			this.itemKey = itemKey;
			this.itemTitle = itemTitle;
			this.itemType = itemType;
			this.score = score;
			this.possible = possible;
			this.graded = graded;
			this.excused = excused;
		}

		int percent() {
			if (possible == null || possible.doubleValue() <= 0) {
				return 0;
			}
			double earned = score == null ? 0d : score.doubleValue();
			return (int) Math.round(100d * earned / possible.doubleValue());
		}
	}

	private static class UserTotals {
		private final String userId;
		private double earned;
		private double possible;
		private double gradedPossible;
		private int graded;
		private int expected;

		UserTotals(String userId) {
			this.userId = userId;
		}

		void add(GradeCell cell) {
			if (cell.excused) {
				return;
			}
			expected++;
			possible += cell.possible.doubleValue();
			if (cell.graded) {
				graded++;
				earned += cell.score.doubleValue();
				gradedPossible += cell.possible.doubleValue();
			}
		}

		int percent() {
			return gradedPossible <= 0 ? 0 : (int) Math.round(100d * earned / gradedPossible);
		}

		double percentExact() {
			return gradedPossible <= 0 ? 0d : 100d * earned / gradedPossible;
		}
	}

	private static class ItemTotals {
		private final String key;
		private final String title;
		private final String itemType;
		private double earned;
		private double possible;
		private double gradedPossible;
		private int graded;
		private int expected;
		private int below;

		ItemTotals(String key, String title, String itemType) {
			this.key = key;
			this.title = title;
			this.itemType = itemType;
		}

		void add(GradeCell cell, double threshold) {
			if (cell.excused) {
				return;
			}
			expected++;
			possible += cell.possible.doubleValue();
			if (cell.graded) {
				graded++;
				earned += cell.score.doubleValue();
				gradedPossible += cell.possible.doubleValue();
				if (cell.percent() < threshold) {
					below++;
				}
			}
		}

		int averagePercent() {
			return gradedPossible <= 0 ? 0 : (int) Math.round(100d * earned / gradedPossible);
		}
	}

	private static class GradeSnapshot {
		private final double threshold;
		private final List<GradeCell> cells = new ArrayList<GradeCell>();
		private final Map<String, UserTotals> byUser = new LinkedHashMap<String, UserTotals>();
		private final Map<String, ItemTotals> byItem = new LinkedHashMap<String, ItemTotals>();
		private int belowUsers;
		private int meetingUsers;
		private int gradedUsers;
		private int completeUsers;
		private int gradedCells;
		private int expectedCells;
		private double percentSum;

		GradeSnapshot(double threshold) {
			this.threshold = threshold;
		}

		void add(GradeCell cell) {
			cells.add(cell);
		}

		UserTotals user(String userId) {
			UserTotals totals = byUser.get(userId);
			if (totals == null) {
				totals = new UserTotals(userId);
				byUser.put(userId, totals);
			}
			return totals;
		}

		ItemTotals item(String key, String title, String itemType) {
			ItemTotals totals = byItem.get(key);
			if (totals == null) {
				totals = new ItemTotals(key, title, itemType);
				byItem.put(key, totals);
			}
			return totals;
		}

		void summarize() {
			for (UserTotals totals : byUser.values()) {
				expectedCells += totals.expected;
				gradedCells += totals.graded;
				if (totals.expected > 0 && totals.graded == totals.expected) {
					completeUsers++;
				}
				if (totals.graded > 0) {
					gradedUsers++;
					percentSum += totals.percentExact();
					if (totals.percent() < threshold) {
						belowUsers++;
					} else {
						meetingUsers++;
					}
				}
			}
		}

		int belowPercent() {
			return gradedUsers == 0 ? 0 : (int) Math.round(100d * belowUsers / gradedUsers);
		}

		int completePercent() {
			return byUser.isEmpty() ? 0 : (int) Math.round(100d * completeUsers / byUser.size());
		}

		double averagePercent() {
			return gradedUsers == 0 ? 0d : percentSum / gradedUsers;
		}

		int gradedPercent() {
			return expectedCells == 0 ? 0 : (int) Math.round(100d * gradedCells / expectedCells);
		}
	}

	private static class ItemChoice {
		private final String key;
		private final String label;

		ItemChoice(String key, String label) {
			this.key = key;
			this.label = label;
		}
	}
}
