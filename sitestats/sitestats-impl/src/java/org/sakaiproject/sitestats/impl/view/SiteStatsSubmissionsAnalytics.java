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

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_DANGER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_SUCCESS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_WARNING;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.GROUP_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.ITEM_ALL;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.samigo.util.SamigoConstants;
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
public class SiteStatsSubmissionsAnalytics {

	private static final String COL_USER = "user";
	private static final String COL_ITEM = "item";
	private static final String COL_ITEM_TYPE = "itemType";
	private static final String COL_ON_TIME = "onTime";
	private static final String COL_LATE = "late";
	private static final String COL_MISSED = "missed";
	private static final String COL_NEEDS_GRADING = "needsGrading";
	private static final String COL_AT_RISK = "atRisk";
	private static final String COL_NEVER = "neverSubmitted";
	private static final String COL_AVG_DELAY = "avgDelay";
	private static final String COL_STATUS = "status";
	private static final String COL_DUE = "dueDate";
	private static final String COL_SUBMITTED = "submittedDate";

	@Setter private AssignmentService assignmentService;
	@Setter private SiteStatsSamigoLookup samigoLookup;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetMetricSupport metricSupport;

	SiteStatsReportView byStudentReport(String siteId, SiteStatsReportRequest request, String userId) {
		SubmissionSnapshot snapshot = snapshot(siteId, request, userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_submissions");
		if (request.isIncludeTable()) {
			view.setTable(studentTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			view.setChart(singleItemSelected(request)
					? statusPie(snapshot, view.getTitle())
					: countChart(studentRows(snapshot), COL_USER, view.getTitle()));
		}
		return view;
	}

	SiteStatsReportView byItemReport(String siteId, SiteStatsReportRequest request, String userId) {
		SubmissionSnapshot snapshot = snapshot(siteId, request, userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_submissions");
		if (request.isIncludeTable()) {
			view.setTable(StringUtils.isNotBlank(userId) ? ownItemTable(snapshot, request) : itemTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			if (StringUtils.isNotBlank(userId)) {
				view.setChart(statusPie(snapshot, view.getTitle()));
			} else {
				view.setChart(countChart(itemRows(snapshot), COL_ITEM, view.getTitle()));
			}
		}
		return view;
	}

	SiteStatsReportView statusDetailReport(String siteId, SiteStatsReportRequest request, String userId, SubmissionStatus status) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		SiteStatsReportView view = reportShell(siteId, request, statusTitleKey(status));
		if (request.isIncludeTable()) {
			view.setTable(detailTable(snapshot, request, status));
		}
		if (request.isIncludeChart()) {
			view.setChart(statusPie(snapshot, view.getTitle()));
		}
		return view;
	}

	SiteStatsReportView atRiskReport(String siteId, SiteStatsReportRequest request, String userId) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_submissions_at_risk");
		if (request.isIncludeTable()) {
			view.setTable(atRiskTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			view.setChart(countChart(atRiskRows(snapshot), COL_USER, view.getTitle()));
		}
		return view;
	}

	WidgetMetricValue onTimeValue(String siteId, String userId, SiteStatsReportRequest request) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.withPercentage(String.valueOf(snapshot.onTime), snapshot.onTimePercent());
	}

	WidgetMetricValue lateValue(String siteId, String userId, SiteStatsReportRequest request) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.withPercentage(String.valueOf(snapshot.late), snapshot.latePercent());
	}

	WidgetMetricValue missedValue(String siteId, String userId, SiteStatsReportRequest request) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.withPercentage(String.valueOf(snapshot.missed), snapshot.missedPercent());
	}

	WidgetMetricValue atRiskValue(String siteId, String userId, SiteStatsReportRequest request) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.withPercentage(snapshot.atRiskUsers + " / " + snapshot.byUser.size(),
				snapshot.atRiskPercent());
	}

	WidgetMetricValue avgDelayValue(String siteId, String userId, SiteStatsReportRequest request) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (snapshot.delays.isEmpty()) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.of(metricSupport.msToDelayString(median(snapshot.delays)));
	}

	WidgetMetricValue needsGradingValue(String siteId, String userId, SiteStatsReportRequest request) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.withPercentage(snapshot.needsGrading + " / " + snapshot.submitted(),
				snapshot.needsGradingPercent());
	}

	SiteStatsReportView needsGradingReport(String siteId, SiteStatsReportRequest request, String userId) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_submissions_needs_grading");
		if (request.isIncludeTable()) {
			view.setTable(pagedTable(detailColumns(), detailRows(snapshot, null, true), request));
		}
		return view;
	}

	SiteStatsChart statusShareChart(String siteId, String userId, SiteStatsReportRequest request) {
		SubmissionSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (snapshot.closed() == 0) {
			return null;
		}
		String category = message("overview_title_submissions_status_share");
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(category);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey(COL_STATUS);
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		chart.setItemLabelsVisible(false);
		chart.setCompact(true);
		chart.setStacked(true);
		chart.setHorizontal(true);
		chart.getDatasets().add(shareDataset("onTime", message("overview_status_on_time"), CHART_COLOR_SUCCESS,
				category, snapshot.onTime));
		chart.getDatasets().add(shareDataset("late", message("overview_status_late"), CHART_COLOR_WARNING,
				category, snapshot.late));
		chart.getDatasets().add(shareDataset("missed", message("overview_status_missed"), CHART_COLOR_DANGER,
				category, snapshot.missed));
		return chart;
	}

	List<SiteStatsFilterOption> itemFilterOptions(String siteId, boolean studentView) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		for (ItemChoice choice : itemChoices(siteId, studentView)) {
			options.add(new SiteStatsFilterOption(choice.key, choice.label));
		}
		return options;
	}

	private SubmissionSnapshot snapshot(String siteId, SiteStatsReportRequest request, String userId) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequest.normalized(request);
		SubmissionSnapshot snapshot = new SubmissionSnapshot();
		if (filterCatalog.isIncompleteCustomRange(safeRequest)) {
			return snapshot;
		}
		if (filterCatalog.includesItemType(safeRequest, AssignmentServiceConstants.ASSIGNMENT_TOOL_ID)) {
			collectAssignments(snapshot, siteId, safeRequest, userId);
		}
		if (filterCatalog.includesItemType(safeRequest, SamigoConstants.TOOL_ID)) {
			collectQuizzes(snapshot, siteId, safeRequest, userId);
		}
		snapshot.summarize();
		return snapshot;
	}

	private void collectAssignments(SubmissionSnapshot snapshot, String siteId, SiteStatsReportRequest request, String userId) {
		Map<Assignment, List<String>> assignments;
		try {
			assignments = assignmentService.getSubmittableAssignmentsForContext(siteId);
		} catch (RuntimeException e) {
			log.warn("Unable to load assignments for site {}", siteId, e);
			return;
		}
		if (assignments == null || assignments.isEmpty()) {
			return;
		}
		for (Map.Entry<Assignment, List<String>> entry : assignments.entrySet()) {
			Assignment assignment = entry.getKey();
			if (assignment == null || Boolean.TRUE.equals(assignment.getDraft()) || Boolean.TRUE.equals(assignment.getDeleted())) {
				continue;
			}
			if (!includeItem(request, AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, assignment.getId())) {
				continue;
			}
			Instant due = assignment.getDueDate();
			Instant close = assignment.getCloseDate();
			if (!filterCatalog.isDueInRange(due != null ? due : close, request)) {
				continue;
			}
			Set<String> expectedUsers = expectedUsers(siteId, entry.getValue(), request, userId);
			if (expectedUsers.isEmpty()) {
				continue;
			}
			Map<String, AssignmentSubmission> byUser = submissionsByUser(assignment);
			for (String expectedUser : expectedUsers) {
				AssignmentSubmission submission = byUser.get(expectedUser);
				Instant submittedDate = submission == null ? null : submission.getDateSubmitted();
				boolean submitted = isSubmitted(submission);
				boolean needsGrading = submitted && !Boolean.TRUE.equals(submission.getGraded());
				snapshot.add(expected(expectedUser, assignment.getId(), assignment.getTitle(), AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, due, close,
						submittedDate, submitted, false, needsGrading));
			}
		}
	}

	private void collectQuizzes(SubmissionSnapshot snapshot, String siteId, SiteStatsReportRequest request, String userId) {
		List<SiteStatsSamigoQuiz> quizzes = samigoLookup.publishedQuizzes(siteId);
		if (quizzes == null || quizzes.isEmpty()) {
			return;
		}
		Map<String, Set<String>> groupsByUser = groupIdsByUser(siteId);
		for (SiteStatsSamigoQuiz quiz : quizzes) {
			if (!includeItem(request, SamigoConstants.TOOL_ID, quiz.getId())) {
				continue;
			}
			Set<String> expectedUsers = expectedUsersForQuiz(siteId, quiz, request, userId);
			if (expectedUsers.isEmpty()) {
				continue;
			}
			Map<String, SiteStatsSamigoAttempt> byUser = attemptsByUser(quiz.getId());
			for (String expectedUser : expectedUsers) {
				SiteStatsSamigoWindow window = quiz.windowFor(expectedUser, groupsByUser.get(expectedUser));
				Instant due = window.getDueDate();
				Instant close = window.getCloseDate();
				if (!filterCatalog.isDueInRange(due != null ? due : close, request)) {
					continue;
				}
				SiteStatsSamigoAttempt attempt = byUser.get(expectedUser);
				Instant submittedDate = attempt == null ? null : attempt.getSubmittedDate();
				boolean submitted = attempt != null && attempt.isSubmitted();
				boolean lateFlag = attempt != null && attempt.isLate();
				boolean needsGrading = submitted && quiz.isManualGradingRequired() && attempt != null
						&& !attempt.isGraded();
				snapshot.add(expected(expectedUser, quiz.getId(), quiz.getTitle(), SamigoConstants.TOOL_ID, due, close,
						submittedDate, submitted, lateFlag, needsGrading));
			}
		}
	}

	private Map<String, AssignmentSubmission> submissionsByUser(Assignment assignment) {
		Set<AssignmentSubmission> submissions;
		try {
			submissions = assignmentService.getSubmissions(assignment);
		} catch (RuntimeException e) {
			log.warn("Unable to load submissions for assignment {}", assignment.getId(), e);
			return Collections.emptyMap();
		}
		if (submissions == null || submissions.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, AssignmentSubmission> byUser = new HashMap<String, AssignmentSubmission>();
		for (AssignmentSubmission submission : submissions) {
			if (submission == null || submission.getSubmitters() == null) {
				continue;
			}
			for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
				if (submitter != null && StringUtils.isNotBlank(submitter.getSubmitter())) {
					byUser.put(submitter.getSubmitter(), submission);
				}
			}
		}
		return byUser;
	}

	private Map<String, SiteStatsSamigoAttempt> attemptsByUser(String publishedAssessmentId) {
		List<SiteStatsSamigoAttempt> attempts = samigoLookup.submittedAttempts(publishedAssessmentId);
		if (attempts == null || attempts.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, SiteStatsSamigoAttempt> byUser = new HashMap<String, SiteStatsSamigoAttempt>();
		for (SiteStatsSamigoAttempt attempt : attempts) {
			if (attempt != null && StringUtils.isNotBlank(attempt.getUserId())) {
				byUser.put(attempt.getUserId(), attempt);
			}
		}
		return byUser;
	}

	private boolean isSubmitted(AssignmentSubmission submission) {
		return submission != null && Boolean.TRUE.equals(submission.getSubmitted())
				&& Boolean.TRUE.equals(submission.getUserSubmission());
	}

	private ExpectedSubmission expected(String userId, String itemId, String itemTitle, String itemType, Instant due,
			Instant close, Instant submittedDate, boolean submitted, boolean lateFlag, boolean needsGrading) {
		SubmissionStatus status = classify(due, close, submittedDate, submitted, lateFlag);
		long delayMs = 0;
		if (status == SubmissionStatus.LATE && due != null && submittedDate != null && submittedDate.isAfter(due)) {
			delayMs = Duration.between(due, submittedDate).toMillis();
		}
		return new ExpectedSubmission(userId, itemId, itemTitle, itemType, due, submittedDate, status, delayMs,
				needsGrading);
	}

	private SubmissionStatus classify(Instant due, Instant close, Instant submittedDate, boolean submitted,
			boolean lateFlag) {
		if (submitted) {
			if (lateFlag || (due != null && submittedDate != null && submittedDate.isAfter(due))) {
				return SubmissionStatus.LATE;
			}
			return SubmissionStatus.ON_TIME;
		}
		Instant deadline = close != null ? close : due;
		if (deadline != null && Instant.now().isAfter(deadline)) {
			return SubmissionStatus.MISSED;
		}
		return SubmissionStatus.OPEN;
	}

	private Set<String> expectedUsers(String siteId, List<String> submitters, SiteStatsReportRequest request, String userId) {
		Set<String> users = new HashSet<String>();
		if (submitters != null) {
			users.addAll(submitters);
		}
		users.retainAll(filterCatalog.assignmentSubmitters(siteId));
		return filterUsers(siteId, users, request, userId);
	}

	private Set<String> expectedUsersForQuiz(String siteId, SiteStatsSamigoQuiz quiz, SiteStatsReportRequest request,
			String userId) {
		Set<String> takers = filterCatalog.quizTakers(siteId);
		Set<String> users = new HashSet<String>();
		List<String> groupIds = quiz.getGroupIds();
		boolean hasGroupRestriction = false;
		if (groupIds != null) {
			for (String groupId : groupIds) {
				if (StringUtils.isBlank(groupId) || siteId.equals(groupId)) {
					continue;
				}
				hasGroupRestriction = true;
				users.addAll(groupMembers(siteId, groupId));
			}
		}
		if (!hasGroupRestriction) {
			users.addAll(takers);
		} else {
			users.retainAll(takers);
		}
		return filterUsers(siteId, users, request, userId);
	}

	private Set<String> filterUsers(String siteId, Set<String> users, SiteStatsReportRequest request, String userId) {
		Set<String> remaining = new HashSet<String>(users);
		if (StringUtils.isNotBlank(userId)) {
			return remaining.contains(userId) ? Collections.singleton(userId) : Collections.<String>emptySet();
		}
		String role = filterCatalog.roleFilter(request);
		if (!ReportManager.WHO_ALL.equals(role) && StringUtils.isNotBlank(role)) {
			Set<String> matchingRole = new HashSet<String>();
			for (String candidate : remaining) {
				if (role.equals(memberRoleFor(siteId, candidate))) {
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

	private boolean includeItem(SiteStatsReportRequest request, String itemType, String itemId) {
		if (!singleItemSelected(request)) {
			return true;
		}
		String selected = filterCatalog.itemFilter(request);
		return selected.equals(itemKey(itemType, itemId)) || selected.equals(itemId);
	}

	private boolean singleItemSelected(SiteStatsReportRequest request) {
		String selected = filterCatalog.itemFilter(request);
		return StringUtils.isNotBlank(selected) && !ITEM_ALL.equals(selected);
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

	private Map<String, Set<String>> groupIdsByUser(String siteId) {
		Map<String, Set<String>> groupsByUser = new HashMap<String, Set<String>>();
		try {
			Site site = context.getSiteService().getSite(siteId);
			if (site.getGroups() == null) {
				return groupsByUser;
			}
			for (Group group : site.getGroups()) {
				if (group == null || StringUtils.isBlank(group.getId()) || group.getUsers() == null) {
					continue;
				}
				for (String memberId : group.getUsers()) {
					if (StringUtils.isBlank(memberId)) {
						continue;
					}
					groupsByUser.computeIfAbsent(memberId, key -> new HashSet<String>()).add(group.getId());
				}
			}
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
		}
		return groupsByUser;
	}

	private List<ItemChoice> itemChoices(String siteId, boolean studentView) {
		List<ItemChoice> choices = new ArrayList<ItemChoice>();
		Map<Assignment, List<String>> assignments;
		try {
			assignments = assignmentService.getSubmittableAssignmentsForContext(siteId);
		} catch (RuntimeException e) {
			log.warn("Unable to load assignments for site {}", siteId, e);
			assignments = Collections.emptyMap();
		}
		if (assignments != null) {
			for (Map.Entry<Assignment, List<String>> entry : assignments.entrySet()) {
				Assignment assignment = entry.getKey();
				if (assignment == null || Boolean.TRUE.equals(assignment.getDraft()) || Boolean.TRUE.equals(assignment.getDeleted())
						|| StringUtils.isBlank(assignment.getId())) {
					continue;
				}
				if (studentView && !containsCurrentUser(entry.getValue())) {
					continue;
				}
				choices.add(new ItemChoice(itemKey(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, assignment.getId()),
						itemChoiceLabel(assignment.getTitle(), AssignmentServiceConstants.ASSIGNMENT_TOOL_ID)));
			}
		}
		List<SiteStatsSamigoQuiz> quizzes = samigoLookup.publishedQuizzes(siteId);
		if (quizzes != null) {
			for (SiteStatsSamigoQuiz quiz : quizzes) {
				if (quiz == null || StringUtils.isBlank(quiz.getId())) {
					continue;
				}
				if (studentView && !quizAvailableToCurrentUser(siteId, quiz)) {
					continue;
				}
				choices.add(new ItemChoice(itemKey(SamigoConstants.TOOL_ID, quiz.getId()), itemChoiceLabel(quiz.getTitle(), SamigoConstants.TOOL_ID)));
			}
		}
		Collections.sort(choices, Comparator.comparing((ItemChoice choice) -> choice.label, String.CASE_INSENSITIVE_ORDER));
		return choices;
	}

	private boolean containsCurrentUser(List<String> submitters) {
		String userId = context.currentUserId();
		return StringUtils.isNotBlank(userId) && submitters != null && submitters.contains(userId);
	}

	private boolean quizAvailableToCurrentUser(String siteId, SiteStatsSamigoQuiz quiz) {
		String userId = context.currentUserId();
		if (StringUtils.isBlank(userId)) {
			return false;
		}
		return expectedUsersForQuiz(siteId, quiz, new SiteStatsReportRequest(), userId).contains(userId);
	}

	private String itemChoiceLabel(String title, String itemType) {
		String displayTitle = StringUtils.defaultIfBlank(title, itemType);
		return displayTitle + " (" + itemTypeLabel(itemType) + ")";
	}

	private String itemKey(String itemType, String itemId) {
		return itemType + ":" + itemId;
	}

	private SiteStatsReportView reportShell(String siteId, SiteStatsReportRequest request, String titleKey) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message(titleKey));
		view.setPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		return view;
	}

	private SiteStatsTable studentTable(SubmissionSnapshot snapshot, SiteStatsReportRequest request) {
		return pagedTable(studentColumns(), studentRows(snapshot), request);
	}

	private SiteStatsTable itemTable(SubmissionSnapshot snapshot, SiteStatsReportRequest request) {
		return pagedTable(itemColumns(), itemRows(snapshot), request);
	}

	private SiteStatsTable ownItemTable(SubmissionSnapshot snapshot, SiteStatsReportRequest request) {
		return pagedTable(detailColumns(), detailRows(snapshot, null, false), request);
	}

	private SiteStatsTable detailTable(SubmissionSnapshot snapshot, SiteStatsReportRequest request, SubmissionStatus status) {
		List<SiteStatsTableRow> rows = detailRows(snapshot, status, false);
		return pagedTable(detailColumns(), rows, request);
	}

	private SiteStatsTable atRiskTable(SubmissionSnapshot snapshot, SiteStatsReportRequest request) {
		List<SiteStatsTableRow> rows = atRiskRows(snapshot);
		return pagedTable(studentColumns(), rows, request);
	}

	private List<SiteStatsTableColumn> studentColumns() {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_USER, "th_user", "string", "start"));
		columns.add(column(COL_ON_TIME, "th_on_time", "number", "end"));
		columns.add(column(COL_LATE, "th_late", "number", "end"));
		columns.add(column(COL_MISSED, "th_missed", "number", "end"));
		columns.add(column(COL_AT_RISK, "th_at_risk", "string", "start"));
		columns.add(column(COL_NEEDS_GRADING, "th_needs_grading", "number", "end"));
		columns.add(column(COL_AVG_DELAY, "th_avg_delay", "string", "end"));
		return columns;
	}

	private List<SiteStatsTableColumn> itemColumns() {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_ITEM, "th_item", "string", "start"));
		columns.add(column(COL_ITEM_TYPE, "th_item_type", "string", "start"));
		columns.add(column(COL_ON_TIME, "th_on_time", "number", "end"));
		columns.add(column(COL_LATE, "th_late", "number", "end"));
		columns.add(column(COL_MISSED, "th_missed", "number", "end"));
		columns.add(column(COL_NEEDS_GRADING, "th_needs_grading", "number", "end"));
		columns.add(column(COL_NEVER, "th_never_submitted", "number", "end"));
		return columns;
	}

	private List<SiteStatsTableColumn> detailColumns() {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_USER, "th_user", "string", "start"));
		columns.add(column(COL_ITEM, "th_item", "string", "start"));
		columns.add(column(COL_ITEM_TYPE, "th_item_type", "string", "start"));
		columns.add(column(COL_STATUS, "th_status", "string", "start"));
		columns.add(column(COL_DUE, "th_due_date", "date", "start"));
		columns.add(column(COL_SUBMITTED, "th_submitted_date", "date", "start"));
		columns.add(column(COL_AVG_DELAY, "th_avg_delay", "string", "end"));
		return columns;
	}

	private List<SiteStatsTableRow> studentRows(SubmissionSnapshot snapshot) {
		List<CountRow> rows = new ArrayList<CountRow>(snapshot.byUser.values());
		sortCountRows(rows);
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (CountRow row : rows) {
			tableRows.add(countTableRow(COL_USER, displayUser(row.key), row));
		}
		return tableRows;
	}

	private List<SiteStatsTableRow> itemRows(SubmissionSnapshot snapshot) {
		List<CountRow> rows = new ArrayList<CountRow>(snapshot.byItem.values());
		sortCountRows(rows);
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (CountRow row : rows) {
			SiteStatsTableRow tableRow = countTableRow(COL_ITEM, row.label, row);
			tableRow.getCells().put(COL_ITEM_TYPE, cell(itemTypeLabel(row.itemType), row.itemType));
			tableRow.getCells().put(COL_NEVER, numberCell(row.missed));
			tableRows.add(tableRow);
		}
		return tableRows;
	}

	private List<SiteStatsTableRow> atRiskRows(SubmissionSnapshot snapshot) {
		List<CountRow> rows = new ArrayList<CountRow>();
		for (CountRow row : snapshot.byUser.values()) {
			if (row.missed > 0) {
				rows.add(row);
			}
		}
		sortCountRows(rows);
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (CountRow row : rows) {
			tableRows.add(countTableRow(COL_USER, displayUser(row.key), row));
		}
		return tableRows;
	}

	private List<SiteStatsTableRow> detailRows(SubmissionSnapshot snapshot, SubmissionStatus status,
			boolean needsGradingOnly) {
		List<ExpectedSubmission> expected = new ArrayList<ExpectedSubmission>(snapshot.expected);
		Collections.sort(expected, Comparator.comparing(ExpectedSubmission::getUserId, String.CASE_INSENSITIVE_ORDER)
				.thenComparing(ExpectedSubmission::getItemTitle, String.CASE_INSENSITIVE_ORDER));
		List<SiteStatsTableRow> rows = new ArrayList<SiteStatsTableRow>();
		for (ExpectedSubmission row : expected) {
			if (needsGradingOnly && !row.needsGrading) {
				continue;
			}
			if (status != null && row.status != status) {
				continue;
			}
			SiteStatsTableRow tableRow = new SiteStatsTableRow();
			tableRow.getCells().put(COL_USER, cell(displayUser(row.userId), row.userId));
			tableRow.getCells().put(COL_ITEM, cell(row.itemTitle, row.itemTitle));
			tableRow.getCells().put(COL_ITEM_TYPE, cell(itemTypeLabel(row.itemType), row.itemType));
			tableRow.getCells().put(COL_STATUS, cell(statusLabel(row.status), row.status.name()));
			tableRow.getCells().put(COL_DUE, dateCell(row.due));
			tableRow.getCells().put(COL_SUBMITTED, dateCell(row.submittedDate));
			tableRow.getCells().put(COL_AVG_DELAY, cell(row.delayMs > 0 ? metricSupport.msToDelayString(row.delayMs) : "-",
					Long.valueOf(row.delayMs)));
			rows.add(tableRow);
		}
		return rows;
	}

	private SiteStatsTableRow countTableRow(String key, String label, CountRow row) {
		SiteStatsTableRow tableRow = new SiteStatsTableRow();
		tableRow.getCells().put(key, cell(label, label));
		tableRow.getCells().put(COL_ON_TIME, numberCell(row.onTime));
		tableRow.getCells().put(COL_LATE, numberCell(row.late));
		tableRow.getCells().put(COL_MISSED, numberCell(row.missed));
		tableRow.getCells().put(COL_NEEDS_GRADING, numberCell(row.needsGrading));
		tableRow.getCells().put(COL_AT_RISK, atRiskCell(row.missed > 0));
		long delay = median(row.delays);
		tableRow.getCells().put(COL_AVG_DELAY, cell(row.delays.isEmpty() ? "-" : metricSupport.msToDelayString(delay),
				Long.valueOf(delay)));
		return tableRow;
	}

	private SiteStatsTable pagedTable(List<SiteStatsTableColumn> columns, List<SiteStatsTableRow> rows,
			SiteStatsReportRequest request) {
		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(message("overview_title_submissions"));
		table.setColumns(columns);
		table.setPage(request.getPage());
		table.setPageSize(request.getPageSize());
		table.setTotalRows(rows.size());
		table.setRows(page(rows, request));
		return table;
	}

	private SiteStatsChart countChart(List<SiteStatsTableRow> rows, String labelKey, String title) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey(labelKey);
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		if (rows.isEmpty()) {
			return chart;
		}
		SiteStatsChartDataset onTime = dataset("onTime", message("th_on_time"), CHART_COLOR_SUCCESS);
		SiteStatsChartDataset late = dataset("late", message("th_late"), CHART_COLOR_WARNING);
		SiteStatsChartDataset missed = dataset("missed", message("th_missed"), CHART_COLOR_DANGER);
		for (SiteStatsTableRow row : rows) {
			String label = display(row, labelKey);
			onTime.getPoints().add(point(label, number(row, COL_ON_TIME)));
			late.getPoints().add(point(label, number(row, COL_LATE)));
			missed.getPoints().add(point(label, number(row, COL_MISSED)));
		}
		chart.getDatasets().add(onTime);
		chart.getDatasets().add(late);
		chart.getDatasets().add(missed);
		return chart;
	}

	private SiteStatsChart statusPie(SubmissionSnapshot snapshot, String title) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_PIE);
		chart.setXKey(COL_STATUS);
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		if (snapshot.closed() == 0) {
			return chart;
		}
		SiteStatsChartDataset dataset = dataset("status", title);
		dataset.getPoints().add(point(message("overview_status_on_time"), snapshot.onTime, CHART_COLOR_SUCCESS));
		dataset.getPoints().add(point(message("overview_status_late"), snapshot.late, CHART_COLOR_WARNING));
		dataset.getPoints().add(point(message("overview_status_missed"), snapshot.missed, CHART_COLOR_DANGER));
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsChartDataset dataset(String key, String label) {
		return dataset(key, label, null);
	}

	private SiteStatsChartDataset shareDataset(String key, String label, String color, String category, int count) {
		SiteStatsChartDataset dataset = dataset(key, label, color);
		dataset.getPoints().add(point(category, Integer.valueOf(count)));
		return dataset;
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

	private SiteStatsTableCell numberCell(int value) {
		return cell(String.valueOf(value), Integer.valueOf(value));
	}

	private SiteStatsTableCell atRiskCell(boolean atRisk) {
		return cell(message(atRisk ? "overview_status_yes" : "overview_status_no"), Integer.valueOf(atRisk ? 1 : 0));
	}

	private SiteStatsTableCell dateCell(Instant instant) {
		if (instant == null) {
			return cell("-", null);
		}
		Date date = Date.from(instant);
		return cell(metricSupport.formatDate(date), date);
	}

	private List<SiteStatsTableRow> page(List<SiteStatsTableRow> rows, SiteStatsReportRequest request) {
		int from = (request.getPage() - 1) * request.getPageSize();
		if (from >= rows.size()) {
			return Collections.emptyList();
		}
		int to = Math.min(from + request.getPageSize(), rows.size());
		return new ArrayList<SiteStatsTableRow>(rows.subList(from, to));
	}

	private static long median(List<Long> values) {
		if (values == null || values.isEmpty()) {
			return 0L;
		}
		List<Long> sorted = new ArrayList<Long>(values);
		Collections.sort(sorted);
		int mid = sorted.size() / 2;
		if (sorted.size() % 2 == 0) {
			return (sorted.get(mid - 1) + sorted.get(mid)) / 2L;
		}
		return sorted.get(mid);
	}

	private void sortCountRows(List<CountRow> rows) {
		Collections.sort(rows, Comparator.comparing((CountRow row) -> row.label, String.CASE_INSENSITIVE_ORDER));
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

	private String itemTypeLabel(String itemType) {
		return context.toolName(itemType);
	}

	private String statusLabel(SubmissionStatus status) {
		if (status == SubmissionStatus.LATE) {
			return message("overview_status_late");
		}
		if (status == SubmissionStatus.MISSED) {
			return message("overview_status_missed");
		}
		if (status == SubmissionStatus.OPEN) {
			return message("overview_status_open");
		}
		return message("overview_status_on_time");
	}

	private String statusTitleKey(SubmissionStatus status) {
		if (status == SubmissionStatus.LATE) {
			return "overview_title_submissions_late";
		}
		if (status == SubmissionStatus.MISSED) {
			return "overview_title_submissions_missed";
		}
		return "overview_title_submissions_on_time";
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

	private String memberRoleFor(String siteId, String userId) {
		try {
			Site site = context.getSiteService().getSite(siteId);
			Member member = site.getMember(userId);
			if (member != null && member.getRole() != null) {
				return member.getRole().getId();
			}
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
		}
		return null;
	}

	public enum SubmissionStatus {
		ON_TIME, LATE, MISSED, OPEN
	}

	private static class ExpectedSubmission {
		private final String userId;
		private final String itemId;
		private final String itemTitle;
		private final String itemType;
		private final Instant due;
		private final Instant submittedDate;
		private final SubmissionStatus status;
		private final long delayMs;
		private final boolean needsGrading;

		ExpectedSubmission(String userId, String itemId, String itemTitle, String itemType, Instant due,
				Instant submittedDate, SubmissionStatus status, long delayMs, boolean needsGrading) {
			this.userId = userId;
			this.itemId = itemId;
			this.itemTitle = itemTitle;
			this.itemType = itemType;
			this.due = due;
			this.submittedDate = submittedDate;
			this.status = status;
			this.delayMs = delayMs;
			this.needsGrading = needsGrading;
		}

		String getUserId() {
			return userId;
		}

		String getItemTitle() {
			return itemTitle;
		}
	}

	private static class CountRow {
		private final String key;
		private final String label;
		private final String itemType;
		private int onTime;
		private int late;
		private int missed;
		private int needsGrading;
		private final List<Long> delays = new ArrayList<Long>();

		CountRow(String key, String label, String itemType) {
			this.key = key;
			this.label = label;
			this.itemType = itemType;
		}
	}

	private static class SubmissionSnapshot {
		private final List<ExpectedSubmission> expected = new ArrayList<ExpectedSubmission>();
		private final Map<String, CountRow> byUser = new LinkedHashMap<String, CountRow>();
		private final Map<String, CountRow> byItem = new LinkedHashMap<String, CountRow>();
		private final List<Long> delays = new ArrayList<Long>();
		private int onTime;
		private int late;
		private int missed;
		private int needsGrading;
		private int atRiskUsers;

		void add(ExpectedSubmission row) {
			expected.add(row);
		}

		void summarize() {
			Set<String> atRisk = new HashSet<String>();
			for (ExpectedSubmission row : expected) {
				CountRow userRow = byUser.computeIfAbsent(row.userId, key -> new CountRow(key, key, null));
				CountRow itemRow = byItem.computeIfAbsent(row.itemId, key -> new CountRow(key, row.itemTitle, row.itemType));
				if (row.needsGrading) {
					needsGrading++;
					userRow.needsGrading++;
					itemRow.needsGrading++;
				}
				if (row.status == SubmissionStatus.ON_TIME) {
					onTime++;
					userRow.onTime++;
					itemRow.onTime++;
				} else if (row.status == SubmissionStatus.LATE) {
					late++;
					userRow.late++;
					itemRow.late++;
					if (row.delayMs > 0) {
						delays.add(Long.valueOf(row.delayMs));
						userRow.delays.add(Long.valueOf(row.delayMs));
						itemRow.delays.add(Long.valueOf(row.delayMs));
					}
				} else if (row.status == SubmissionStatus.MISSED) {
					missed++;
					userRow.missed++;
					itemRow.missed++;
					atRisk.add(row.userId);
				}
			}
			atRiskUsers = atRisk.size();
		}

		int closed() {
			return onTime + late + missed;
		}

		int submitted() {
			return onTime + late;
		}

		int onTimePercent() {
			return percent(onTime);
		}

		int latePercent() {
			return percent(late);
		}

		int missedPercent() {
			return percent(missed);
		}

		int needsGradingPercent() {
			int total = submitted();
			return total == 0 ? 0 : (int) Math.round(100d * needsGrading / total);
		}

		int atRiskPercent() {
			return byUser.isEmpty() ? 0 : (int) Math.round(100d * atRiskUsers / byUser.size());
		}

		private int percent(int partial) {
			int total = closed();
			return total == 0 ? 0 : (int) Math.round(100d * partial / total);
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
