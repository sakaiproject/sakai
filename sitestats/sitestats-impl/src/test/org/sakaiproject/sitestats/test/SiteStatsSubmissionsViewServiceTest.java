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

package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_GROUP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM_TYPE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_SUBMISSIONS_ON_TIME;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_AT_RISK;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_AVG_DELAY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_LATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_MISSED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_NEEDS_GRADING;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_ON_TIME;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_SUBMISSIONS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_SUBMISSIONS;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.site;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.tool;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetricSnapshot;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.impl.view.SiteStatsSamigoAttempt;
import org.sakaiproject.sitestats.impl.view.SiteStatsSamigoDateOverride;
import org.sakaiproject.sitestats.impl.view.SiteStatsSamigoLookup;
import org.sakaiproject.sitestats.impl.view.SiteStatsSamigoQuiz;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
public class SiteStatsSubmissionsViewServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String SITE_ID = FakeData.SITE_A_ID;
	private static final String SITE_REF = FakeData.SITE_A_REF;
	private static final String USER_A_ID = FakeData.USER_A_ID;
	private static final String USER_B_ID = FakeData.USER_B_ID;

	@Autowired private DB db;
	@Autowired private SiteStatsViewService service;
	@Autowired private AssignmentService assignmentService;
	@Autowired private SiteStatsSamigoLookup samigoLookup;
	@Autowired private SecurityService securityService;
	@Autowired private SessionManager sessionManager;
	@Autowired private UserDirectoryService userDirectoryService;
	@Autowired private SiteService siteService;
	@Autowired private StatsManager statsManager;

	private final Map<String, Set<AssignmentSubmission>> submissionsByAssignmentId = new HashMap<String, Set<AssignmentSubmission>>();

	@Before
	public void setUp() throws Exception {
		db.deleteAll();
		reset(securityService, siteService, sessionManager, userDirectoryService, assignmentService, samigoLookup);
		submissionsByAssignmentId.clear();
		when(assignmentService.getSubmissions(anyCollection())).thenAnswer(invocation -> {
			Collection<String> ids = invocation.getArgument(0);
			Map<String, Set<AssignmentSubmission>> result = new HashMap<String, Set<AssignmentSubmission>>();
			if (ids == null) {
				return result;
			}
			for (String id : ids) {
				if (id == null || id.isEmpty()) {
					continue;
				}
				Set<AssignmentSubmission> submissions = submissionsByAssignmentId.get(id);
				result.put(id, submissions == null ? Collections.emptySet() : submissions);
			}
			return result;
		});

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_A_ID);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		User userA = mock(User.class);
		when(userA.getDisplayId()).thenReturn(USER_A_ID);
		when(userA.getDisplayName()).thenReturn("User A");
		when(userA.getSortName()).thenReturn("User A");
		when(userDirectoryService.getUser(USER_A_ID)).thenReturn(userA);

		User userB = mock(User.class);
		when(userB.getDisplayId()).thenReturn(USER_B_ID);
		when(userB.getDisplayName()).thenReturn("User B");
		when(userB.getSortName()).thenReturn("User B");
		when(userDirectoryService.getUser(USER_B_ID)).thenReturn(userB);

		Site site = site(SITE_ID, "Site A", "Instructor");
		when(site.getUsers()).thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID)));
		when(site.getUsersIsAllowed(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION))
				.thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID)));
		when(site.getUsersIsAllowed(SamigoConstants.AUTHZ_TAKE_ASSESSMENT))
				.thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID)));
		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(siteService.isUserSite(SITE_ID)).thenReturn(false);
		when(siteService.isSpecialSite(SITE_ID)).thenReturn(false);

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_OWN, SITE_REF)).thenReturn(true);

		when(assignmentService.getSubmittableAssignmentsForContext(anyString())).thenReturn(Collections.emptyMap());
		when(samigoLookup.publishedQuizzes(anyString())).thenReturn(Collections.emptyList());
		when(samigoLookup.submittedAttempts(anyString())).thenReturn(Collections.emptyList());

		PrefsData prefsData = new PrefsData();
		prefsData.setShowOwnStatisticsToStudents(true);
		prefsData.setListToolEventsOnlyAvailableInSite(false);
		prefsData.setToolEventsDef(Arrays.asList(tool(FakeData.TOOL_CHAT, FakeData.EVENT_CHATNEW)));
		assertTrue(statsManager.setPreferences(SITE_ID, prefsData));
	}

	@Test
	public void instructorOverviewIncludesSubmissionsWidgetNotStudentSubmissions() {
		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertTrue(hasWidget(overview, WIDGET_SUBMISSIONS));
		assertFalse(hasWidget(overview, WIDGET_STUDENT_SUBMISSIONS));
		assertTrue(widget(overview, WIDGET_SUBMISSIONS).getHighlights().isEmpty());
	}

	@Test
	public void instructorOverviewIncludesCompactStackedStatusShareWhenSubmissionsExist() {
		stubOnTimeAndMissedAssignment();

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsChart chart = widget(overview, WIDGET_SUBMISSIONS).getHighlights().get(0);

		assertEquals(1, widget(overview, WIDGET_SUBMISSIONS).getHighlights().size());
		assertTrue(chart.isCompact());
		assertTrue(chart.isStacked());
		assertTrue(chart.isHorizontal());
		assertEquals(3, chart.getDatasets().size());
		assertEquals("success", chart.getDatasets().get(0).getColor());
		assertEquals("warning", chart.getDatasets().get(1).getColor());
		assertEquals("danger", chart.getDatasets().get(2).getColor());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(0), chart.getDatasets().get(1).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(2).getPoints().get(0).getY());
	}

	@Test
	public void studentOverviewStatusShareCountsOnlyTheCurrentUser() {
		stubOnTimeAndMissedAssignment();
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsChart chart = widget(overview, WIDGET_STUDENT_SUBMISSIONS).getHighlights().get(0);

		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(0), chart.getDatasets().get(1).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(0), chart.getDatasets().get(2).getPoints().get(0).getY());
	}

	@Test
	public void studentOverviewIncludesStudentSubmissionsWhenAllPermissionIsNotGranted() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertTrue(hasWidget(overview, WIDGET_STUDENT_SUBMISSIONS));
		assertFalse(hasWidget(overview, WIDGET_SUBMISSIONS));
	}

	@Test
	public void assignmentOnTimeAndMissedAreCountedForInstructorByUserReport() {
		stubOnTimeAndMissedAssignment();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertNotNull(view.getTable());
		assertEquals(2, view.getTable().getTotalRows());
		assertEquals(2, view.getTable().getRows().size());
		SiteStatsTableRow userA = rowByUser(view.getTable(), "User A");
		SiteStatsTableRow userB = rowByUser(view.getTable(), "User B");
		assertNotNull(userA);
		assertNotNull(userB);
		assertEquals(Integer.valueOf(0), cellRaw(userA, "atRisk"));
		assertEquals("1", cellDisplay(userA, "needsGrading"));
		assertEquals(Integer.valueOf(1), cellRaw(userB, "atRisk"));
		assertEquals("0", cellDisplay(userB, "needsGrading"));

		SiteStatsReportView items = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_ITEM, request);
		assertEquals(1, items.getTable().getTotalRows());
		assertEquals("Homework 1", cellDisplay(items.getTable().getRows().get(0), "item"));
		assertEquals("1", cellDisplay(items.getTable().getRows().get(0), "needsGrading"));
		assertEquals("success", items.getChart().getDatasets().get(0).getColor());
		assertEquals("warning", items.getChart().getDatasets().get(1).getColor());
		assertEquals("danger", items.getChart().getDatasets().get(2).getColor());

		SiteStatsWidgetMetricSnapshot onTime = snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_ON_TIME);
		assertEquals("1", onTime.getPrimary());
		assertEquals(Integer.valueOf(50), onTime.getPercentage());
		assertEquals("0", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_LATE).getPrimary());
		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED).getPrimary());
		assertEquals("1 / 2", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_AT_RISK).getPrimary());
		assertEquals("1 / 1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_NEEDS_GRADING).getPrimary());
		assertEquals(Integer.valueOf(100), snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_NEEDS_GRADING).getPercentage());
		assertEquals(StatsManager.CHARTTYPE_BAR, view.getChart().getType());
		assertEquals(3, view.getChart().getDatasets().size());
		assertEquals("success", view.getChart().getDatasets().get(0).getColor());
		assertEquals("warning", view.getChart().getDatasets().get(1).getColor());
		assertEquals("danger", view.getChart().getDatasets().get(2).getColor());
	}

	@Test
	public void byUserChartUsesStatusPieWhenASingleItemIsSelected() {
		stubOnTimeAndMissedAssignment();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID);
		request.setItem(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID + ":asn-homework-1");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEquals(2, view.getTable().getTotalRows());
		assertEquals(StatsManager.CHARTTYPE_PIE, view.getChart().getType());
		assertEquals(1, view.getChart().getDatasets().size());
		assertEquals("success", view.getChart().getDatasets().get(0).getPoints().get(0).getColor());
		assertEquals("warning", view.getChart().getDatasets().get(0).getPoints().get(1).getColor());
		assertEquals("danger", view.getChart().getDatasets().get(0).getPoints().get(2).getColor());
		assertEquals(3, view.getChart().getDatasets().get(0).getPoints().size());
		assertEquals(Integer.valueOf(1), view.getChart().getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(0), view.getChart().getDatasets().get(0).getPoints().get(1).getY());
		assertEquals(Integer.valueOf(1), view.getChart().getDatasets().get(0).getPoints().get(2).getY());
	}

	@Test
	public void assignmentOpenUntilCloseDateIsNotMissed() {
		Assignment assignment = publishedAssignment("asn-homework-1", "Homework 1", "2026-06-15T23:59:59Z");
		assignment.setCloseDate(Instant.parse("2027-12-31T23:59:59Z"));

		Map<Assignment, List<String>> assignments = new LinkedHashMap<Assignment, List<String>>();
		assignments.put(assignment, Arrays.asList(USER_A_ID, USER_B_ID));
		when(assignmentService.getSubmittableAssignmentsForContext(SITE_ID)).thenReturn(assignments);

		AssignmentSubmission submission = new AssignmentSubmission();
		submission.setId("sub-user-a");
		submission.setSubmitted(Boolean.TRUE);
		submission.setUserSubmission(Boolean.TRUE);
		submission.setDateSubmitted(Instant.parse("2026-06-14T12:00:00Z"));
		AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
		submitter.setSubmitter(USER_A_ID);
		submitter.setSubmission(submission);
		submission.getSubmitters().add(submitter);
		stubSubmissions(assignment, Collections.singleton(submission));

		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_ON_TIME).getPrimary());
		assertEquals("0", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED).getPrimary());
	}

	@Test
	public void quizItemTypeExcludesAssignmentsFromByItemReport() {
		stubOnTimeAndMissedAssignment();
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-midterm", "Midterm", Instant.parse("2026-06-20T23:59:59Z"))));
		when(samigoLookup.submittedAttempts("quiz-midterm")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_A_ID, Instant.parse("2026-06-21T12:00:00Z"), true, true)));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(SamigoConstants.TOOL_ID);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_ITEM, request);

		assertNotNull(view.getTable());
		assertEquals(1, view.getTable().getTotalRows());
		assertEquals(1, view.getTable().getRows().size());
		assertEquals("Midterm", cellDisplay(view.getTable().getRows().get(0), "item"));
	}

	@Test
	public void quizReleasedToGroupExcludesStudentsWithoutThatQuiz() {
		stubGroup("group-1", "Lab 1", USER_A_ID);
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-midterm", "Midterm", Instant.parse("2026-06-20T23:59:59Z"),
						Collections.singletonList("group-1"))));
		when(samigoLookup.submittedAttempts("quiz-midterm")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_A_ID, Instant.parse("2026-06-19T12:00:00Z"), true, false)));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(SamigoConstants.TOOL_ID);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("1", cellDisplay(view.getTable().getRows().get(0), "onTime"));
		assertEquals("0", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED).getPrimary());
	}

	@Test
	public void needsGradingCountsUngradedEssayQuizzesAndIgnoresAutoScoredQuizzes() {
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Arrays.asList(
				new SiteStatsSamigoQuiz("quiz-essay", "Essay exam", Instant.parse("2026-06-20T23:59:59Z"),
						Collections.emptyList(), true),
				new SiteStatsSamigoQuiz("quiz-mc", "Multiple choice", Instant.parse("2026-06-20T23:59:59Z"),
						Collections.emptyList(), false)));
		when(samigoLookup.submittedAttempts("quiz-essay")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_A_ID, Instant.parse("2026-06-19T12:00:00Z"), true, false, false)));
		when(samigoLookup.submittedAttempts("quiz-mc")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_B_ID, Instant.parse("2026-06-19T12:00:00Z"), true, false, false)));

		assertEquals("1 / 2", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_NEEDS_GRADING).getPrimary());
		assertEquals(Integer.valueOf(50), snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_NEEDS_GRADING).getPercentage());

		when(samigoLookup.submittedAttempts("quiz-essay")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_A_ID, Instant.parse("2026-06-19T12:00:00Z"), true, false, true)));

		assertEquals("0 / 2", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_NEEDS_GRADING).getPrimary());
		assertEquals(Integer.valueOf(0), snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_NEEDS_GRADING).getPercentage());
	}

	@Test
	public void closedQuizWithoutDueDateCountsUnsubmittedStudentsAsMissed() {
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-practice", "Practice", null, Instant.parse("2026-06-20T23:59:59Z"),
						Collections.emptyList(), false)));
		when(samigoLookup.submittedAttempts("quiz-practice")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_A_ID, Instant.parse("2026-06-19T12:00:00Z"), true, false)));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(SamigoConstants.TOOL_ID);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, request).getPrimary());
		assertEquals("1", cellDisplay(rowByUser(view.getTable(), "User A"), "onTime"));
		assertEquals("0", cellDisplay(rowByUser(view.getTable(), "User A"), "missed"));
		assertEquals("0", cellDisplay(rowByUser(view.getTable(), "User B"), "onTime"));
		assertEquals("1", cellDisplay(rowByUser(view.getTable(), "User B"), "missed"));
	}

	@Test
	public void quizTitlesDecodeHtmlEntities() {
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-practice",
						"Examen post-pr&aacute;ctica - sesi&oacute;n 1 - 2 - 3",
						Instant.parse("2026-06-20T23:59:59Z"))));
		when(samigoLookup.submittedAttempts("quiz-practice")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_A_ID, Instant.parse("2026-06-19T12:00:00Z"), true, false)));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(SamigoConstants.TOOL_ID);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_ITEM, request);

		assertEquals("Examen post-práctica - sesión 1 - 2 - 3",
				cellDisplay(view.getTable().getRows().get(0), "item"));
	}

	@Test
	public void quizUserDateExceptionKeepsThatStudentOpenAfterDefaultClose() {
		Instant closed = Instant.parse("2026-06-20T23:59:59Z");
		Instant later = Instant.parse("2027-12-31T23:59:59Z");
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-practice", "Practice", closed, closed, Collections.emptyList(), false,
						true, Collections.singletonList(
								new SiteStatsSamigoDateOverride(USER_B_ID, null, later, later)))));
		when(samigoLookup.submittedAttempts("quiz-practice")).thenReturn(Collections.emptyList());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(SamigoConstants.TOOL_ID);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, request).getPrimary());
		assertEquals("1", cellDisplay(rowByUser(view.getTable(), "User A"), "missed"));
		assertEquals("0", cellDisplay(rowByUser(view.getTable(), "User B"), "missed"));
	}

	@Test
	public void quizGroupDateExceptionKeepsGroupMembersOpenAfterDefaultClose() {
		stubGroup("group-1", "Lab 1", USER_B_ID);
		Instant closed = Instant.parse("2026-06-20T23:59:59Z");
		Instant later = Instant.parse("2027-12-31T23:59:59Z");
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-practice", "Practice", closed, closed, Collections.emptyList(), false,
						true, Collections.singletonList(
								new SiteStatsSamigoDateOverride(null, "group-1", later, later)))));
		when(samigoLookup.submittedAttempts("quiz-practice")).thenReturn(Collections.emptyList());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(SamigoConstants.TOOL_ID);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, request).getPrimary());
		assertEquals("1", cellDisplay(rowByUser(view.getTable(), "User A"), "missed"));
		assertEquals("0", cellDisplay(rowByUser(view.getTable(), "User B"), "missed"));
	}

	@Test
	public void quizUserDateExceptionTakesPriorityOverGroupException() {
		stubGroup("group-1", "Lab 1", USER_B_ID);
		Instant closed = Instant.parse("2026-06-20T23:59:59Z");
		Instant later = Instant.parse("2027-12-31T23:59:59Z");
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-practice", "Practice", closed, closed, Collections.emptyList(), false,
						true, Arrays.asList(
								new SiteStatsSamigoDateOverride(null, "group-1", later, later),
								new SiteStatsSamigoDateOverride(USER_B_ID, null, closed, closed)))));
		when(samigoLookup.submittedAttempts("quiz-practice")).thenReturn(Collections.emptyList());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(SamigoConstants.TOOL_ID);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEquals("2", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, request).getPrimary());
		assertEquals("1", cellDisplay(rowByUser(view.getTable(), "User A"), "missed"));
		assertEquals("1", cellDisplay(rowByUser(view.getTable(), "User B"), "missed"));
	}

	@Test
	public void usersWithoutSubmitPermissionAreExcludedEvenIfListedAsSubmitters() throws Exception {
		assertInstructorIsExcludedFromByUserReport(false);
	}

	@Test
	public void usersWhoCanCreateAssignmentsAreExcludedEvenIfTheyCanSubmit() throws Exception {
		assertInstructorIsExcludedFromByUserReport(true);
	}

	private void assertInstructorIsExcludedFromByUserReport(boolean instructorCanSubmit) throws Exception {
		String instructorId = "instructor-user";
		User instructor = mock(User.class);
		when(instructor.getDisplayId()).thenReturn(instructorId);
		when(instructor.getDisplayName()).thenReturn("Instructor");
		when(instructor.getSortName()).thenReturn("Instructor");
		when(userDirectoryService.getUser(instructorId)).thenReturn(instructor);

		Site site = siteService.getSite(SITE_ID);
		when(site.getUsers()).thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID, instructorId)));
		Set<String> assignmentSubmitters = instructorCanSubmit
				? new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID, instructorId))
				: new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID));
		when(site.getUsersIsAllowed(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION))
				.thenReturn(assignmentSubmitters);
		when(site.getUsersIsAllowed(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT))
				.thenReturn(new HashSet<String>(Collections.singletonList(instructorId)));
		when(site.getUsersIsAllowed(SamigoConstants.AUTHZ_TAKE_ASSESSMENT))
				.thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID)));

		Assignment assignment = publishedAssignment("asn-homework-1", "Homework 1", "2026-06-15T23:59:59Z");
		Map<Assignment, List<String>> assignments = new LinkedHashMap<Assignment, List<String>>();
		assignments.put(assignment, Arrays.asList(USER_A_ID, USER_B_ID, instructorId));
		when(assignmentService.getSubmittableAssignmentsForContext(SITE_ID)).thenReturn(assignments);
		stubSubmissions(assignment, Collections.emptySet());

		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-midterm", "Midterm", Instant.parse("2026-06-20T23:59:59Z"))));
		when(samigoLookup.submittedAttempts("quiz-midterm")).thenReturn(Collections.emptyList());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEquals(2, view.getTable().getTotalRows());
		for (SiteStatsTableRow row : view.getTable().getRows()) {
			assertFalse("Instructor".equals(cellDisplay(row, "user")));
			assertFalse(instructorId.equals(cellDisplay(row, "user")));
		}
	}

	@Test
	public void studentWidgetOnlyCountsCurrentUser() {
		stubOnTimeAndMissedAssignment();
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		assertEquals("1", snapshot(WIDGET_STUDENT_SUBMISSIONS, METRIC_STUDENT_SUBMISSIONS_ON_TIME).getPrimary());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_STUDENT_SUBMISSIONS, TAB_BY_ITEM, request);

		assertNotNull(view.getTable());
		assertEquals(1, view.getTable().getTotalRows());
		assertEquals(1, view.getTable().getRows().size());
	}

	@Test
	public void last7DaysExcludesJuneDuesButKeepsColumns() {
		stubOnTimeAndMissedAssignment();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_LAST7DAYS);
		request.setItemType(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEmptyTableWithColumns(view.getTable());
	}

	@Test
	public void customRangeWithoutDatesReturnsEmptyRowsWithColumns() {
		stubOnTimeAndMissedAssignment();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertEmptyTableWithColumns(view.getTable());
	}

	@Test
	public void instructorTabsExposeGroupAndItemFilters() {
		stubOnTimeAndMissedAssignment();
		stubGroup("group-1", "Lab 1", USER_A_ID);

		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER);

		SiteStatsFilter group = filterById(tab, FILTER_GROUP);
		SiteStatsFilter item = filterById(tab, FILTER_ITEM);
		assertNotNull(group);
		assertNotNull(item);
		assertEquals("all", group.getOptions().get(0).getValue());
		assertEquals("group-1", group.getOptions().get(1).getValue());
		assertEquals("all", item.getOptions().get(0).getValue());
		assertEquals(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID + ":asn-homework-1", item.getOptions().get(1).getValue());
		assertNotNull(filterById(tab, FILTER_ROLE));
		assertNull(filterById(tab, FILTER_ITEM_TYPE));
	}

	@Test
	public void instructorSubmissionsWidgetExposesToolFiltersInsteadOfTabItemType() {
		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsWidget widget = widget(overview, WIDGET_SUBMISSIONS);

		assertEquals(2, widget.getToolFilters().size());
		Set<String> toolIds = new HashSet<String>();
		for (int i = 0; i < widget.getToolFilters().size(); i++) {
			toolIds.add(widget.getToolFilters().get(i).getValue());
			assertEquals("si-" + widget.getToolFilters().get(i).getValue().replace('.', '-'),
					widget.getToolFilters().get(i).getIcon());
		}
		assertEquals(new HashSet<String>(Arrays.asList(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, SamigoConstants.TOOL_ID)), toolIds);
		assertTrue(widget.isHighlightsConfigured());
	}

	@Test
	public void studentTabExposesItemFilterWithoutGroupOrRole() {
		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_STUDENT_SUBMISSIONS, TAB_BY_ITEM);

		assertNotNull(filterById(tab, FILTER_ITEM));
		assertNull(filterById(tab, FILTER_ITEM_TYPE));
		assertNotNull(filterById(tab, "date"));
		assertNull(filterById(tab, FILTER_GROUP));
		assertNull(filterById(tab, FILTER_ROLE));
	}

	@Test
	public void submissionMetricsAndHighlightsHonorItemTypeFilter() {
		stubOnTimeAndMissedAssignment();
		when(samigoLookup.publishedQuizzes(SITE_ID)).thenReturn(Collections.singletonList(
				new SiteStatsSamigoQuiz("quiz-midterm", "Midterm", Instant.parse("2026-06-20T23:59:59Z"))));
		when(samigoLookup.submittedAttempts("quiz-midterm")).thenReturn(Collections.singletonList(
				new SiteStatsSamigoAttempt(USER_A_ID, Instant.parse("2026-06-21T12:00:00Z"), true, true)));

		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_ON_TIME).getPrimary());
		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_LATE).getPrimary());
		assertEquals("2", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED).getPrimary());

		SiteStatsReportRequest assignmentOnly = new SiteStatsReportRequest();
		assignmentOnly.setItemType(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID);
		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_ON_TIME, assignmentOnly).getPrimary());
		assertEquals("0", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_LATE, assignmentOnly).getPrimary());
		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, assignmentOnly).getPrimary());

		SiteStatsReportRequest quizOnly = new SiteStatsReportRequest();
		quizOnly.setItemType(SamigoConstants.TOOL_ID);
		assertEquals("0", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_ON_TIME, quizOnly).getPrimary());
		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_LATE, quizOnly).getPrimary());
		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, quizOnly).getPrimary());

		SiteStatsReportRequest both = new SiteStatsReportRequest();
		both.setItemType(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID + "," + SamigoConstants.TOOL_ID);
		assertEquals("1", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_LATE, both).getPrimary());
		assertEquals("2", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, both).getPrimary());

		List<SiteStatsChart> assignmentHighlights = service.getWidgetHighlights(SITE_ID, WIDGET_SUBMISSIONS, assignmentOnly);
		assertEquals(1, assignmentHighlights.size());
		assertEquals(Integer.valueOf(0), assignmentHighlights.get(0).getDatasets().get(1).getPoints().get(0).getY());

		List<SiteStatsChart> quizHighlights = service.getWidgetHighlights(SITE_ID, WIDGET_SUBMISSIONS, quizOnly);
		assertEquals(1, quizHighlights.size());
		assertEquals(Integer.valueOf(1), quizHighlights.get(0).getDatasets().get(1).getPoints().get(0).getY());
	}

	@Test
	public void groupFilterRestrictsByUserReportToGroupMembers() {
		stubOnTimeAndMissedAssignment();
		stubGroup("group-1", "Lab 1", USER_A_ID);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID);
		request.setGroup("group-1");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_USER, request);

		assertNotNull(view.getTable());
		assertEquals(1, view.getTable().getTotalRows());
		assertEquals(1, view.getTable().getRows().size());
		assertEquals("1", cellDisplay(view.getTable().getRows().get(0), "onTime"));
		assertEquals("0", cellDisplay(view.getTable().getRows().get(0), "missed"));
	}

	@Test
	public void itemFilterRestrictsByItemReportToSelectedAssignment() {
		Assignment homework = publishedAssignment("asn-homework-1", "Homework 1", "2026-06-15T23:59:59Z");
		Assignment essay = publishedAssignment("asn-essay-2", "Essay 2", "2026-06-16T23:59:59Z");
		Map<Assignment, List<String>> assignments = new LinkedHashMap<Assignment, List<String>>();
		assignments.put(homework, Arrays.asList(USER_A_ID, USER_B_ID));
		assignments.put(essay, Arrays.asList(USER_A_ID, USER_B_ID));
		when(assignmentService.getSubmittableAssignmentsForContext(SITE_ID)).thenReturn(assignments);
		stubSubmissions(homework, Collections.emptySet());
		stubSubmissions(essay, Collections.emptySet());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID);
		request.setItem(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID + ":asn-homework-1");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_SUBMISSIONS, TAB_BY_ITEM, request);

		assertNotNull(view.getTable());
		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("Homework 1", cellDisplay(view.getTable().getRows().get(0), "item"));
	}

	@Test
	public void medianDelayIgnoresOutliersAndFormatsLongDelaysInDays() {
		Assignment homework = publishedAssignment("asn-homework-1", "Homework 1", "2026-06-15T12:00:00Z");
		Assignment essay = publishedAssignment("asn-essay-2", "Essay 2", "2026-01-01T00:00:00Z");
		Map<Assignment, List<String>> assignments = new LinkedHashMap<Assignment, List<String>>();
		assignments.put(homework, Arrays.asList(USER_A_ID, USER_B_ID));
		assignments.put(essay, Arrays.asList(USER_A_ID, USER_B_ID));
		when(assignmentService.getSubmittableAssignmentsForContext(SITE_ID)).thenReturn(assignments);

		Set<AssignmentSubmission> homeworkSubs = new HashSet<AssignmentSubmission>();
		homeworkSubs.add(lateSubmission("sub-hw-a", USER_A_ID, "2026-06-15T14:00:00Z"));
		homeworkSubs.add(lateSubmission("sub-hw-b", USER_B_ID, "2026-06-15T15:00:00Z"));
		stubSubmissions(homework, homeworkSubs);
		stubSubmissions(essay, Collections.singleton(
				lateSubmission("sub-essay-a", USER_A_ID, "2026-05-12T00:00:00Z")));

		assertEquals("3 hours_abbr", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_AVG_DELAY).getPrimary());
	}

	@Test
	public void longSingleDelayIsFormattedInDays() {
		Assignment essay = publishedAssignment("asn-essay-2", "Essay 2", "2026-01-01T00:00:00Z");
		Map<Assignment, List<String>> assignments = new LinkedHashMap<Assignment, List<String>>();
		assignments.put(essay, Arrays.asList(USER_A_ID, USER_B_ID));
		when(assignmentService.getSubmittableAssignmentsForContext(SITE_ID)).thenReturn(assignments);
		stubSubmissions(essay, Collections.singleton(
				lateSubmission("sub-essay-a", USER_A_ID, "2026-05-12T23:08:00Z")));

		assertEquals("131 days_abbr 23 hours_abbr", snapshot(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_AVG_DELAY).getPrimary());
	}

	private void stubOnTimeAndMissedAssignment() {
		Assignment assignment = publishedAssignment("asn-homework-1", "Homework 1", "2026-06-15T23:59:59Z");

		Map<Assignment, List<String>> assignments = new LinkedHashMap<Assignment, List<String>>();
		assignments.put(assignment, Arrays.asList(USER_A_ID, USER_B_ID));
		when(assignmentService.getSubmittableAssignmentsForContext(SITE_ID)).thenReturn(assignments);

		AssignmentSubmission submission = new AssignmentSubmission();
		submission.setId("sub-user-a");
		submission.setSubmitted(Boolean.TRUE);
		submission.setUserSubmission(Boolean.TRUE);
		submission.setDateSubmitted(Instant.parse("2026-06-14T12:00:00Z"));

		AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
		submitter.setSubmitter(USER_A_ID);
		submitter.setSubmission(submission);
		submission.getSubmitters().add(submitter);

		stubSubmissions(assignment, Collections.singleton(submission));
	}

	private void stubSubmissions(Assignment assignment, Set<AssignmentSubmission> submissions) {
		submissionsByAssignmentId.put(assignment.getId(), submissions);
	}

	private AssignmentSubmission lateSubmission(String id, String userId, String submittedDate) {
		AssignmentSubmission submission = new AssignmentSubmission();
		submission.setId(id);
		submission.setSubmitted(Boolean.TRUE);
		submission.setUserSubmission(Boolean.TRUE);
		submission.setDateSubmitted(Instant.parse(submittedDate));
		AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
		submitter.setSubmitter(userId);
		submitter.setSubmission(submission);
		submission.getSubmitters().add(submitter);
		return submission;
	}

	private Assignment publishedAssignment(String id, String title, String dueDate) {
		Assignment assignment = new Assignment();
		assignment.setId(id);
		assignment.setTitle(title);
		assignment.setDueDate(Instant.parse(dueDate));
		assignment.setDraft(Boolean.FALSE);
		assignment.setDeleted(Boolean.FALSE);
		return assignment;
	}

	private void stubGroup(String groupId, String title, String... userIds) {
		try {
			Site site = siteService.getSite(SITE_ID);
			Group group = mock(Group.class);
			when(group.getId()).thenReturn(groupId);
			when(group.getTitle()).thenReturn(title);
			when(group.getUsers()).thenReturn(new HashSet<String>(Arrays.asList(userIds)));
			when(site.getGroup(groupId)).thenReturn(group);
			when(site.getGroups()).thenReturn(Collections.singletonList(group));
		} catch (Exception e) {
			throw new AssertionError("Unable to stub site group", e);
		}
	}

	private SiteStatsFilter filterById(SiteStatsWidgetTab tab, String id) {
		for (SiteStatsFilter filter : tab.getFilters()) {
			if (id.equals(filter.getId())) {
				return filter;
			}
		}
		return null;
	}

	private void assertEmptyTableWithColumns(SiteStatsTable table) {
		assertNotNull(table);
		assertEquals(0, table.getTotalRows());
		assertTrue(table.getRows().isEmpty());
		assertFalse(table.getColumns().isEmpty());
	}

	private SiteStatsWidgetMetricSnapshot snapshot(String widgetId, String metricId) {
		return snapshot(widgetId, metricId, null);
	}

	private SiteStatsWidgetMetricSnapshot snapshot(String widgetId, String metricId, SiteStatsReportRequest request) {
		for (SiteStatsWidgetMetric metric : service.getWidgetMetrics(SITE_ID, widgetId, request)) {
			if (metricId.equals(metric.getId())) {
				return metric.getSnapshot();
			}
		}
		throw new AssertionError("Missing metric value " + widgetId + "/" + metricId);
	}

	private boolean hasWidget(SiteStatsOverview overview, String widgetId) {
		return widget(overview, widgetId) != null;
	}

	private SiteStatsWidget widget(SiteStatsOverview overview, String widgetId) {
		for (SiteStatsWidget widget : overview.getWidgets()) {
			if (widgetId.equals(widget.getId())) {
				return widget;
			}
		}
		return null;
	}

	private String cellDisplay(SiteStatsTableRow row, String key) {
		SiteStatsTableCell cell = row.getCells().get(key);
		return cell == null ? null : cell.getDisplay();
	}

	private Integer cellRaw(SiteStatsTableRow row, String key) {
		SiteStatsTableCell cell = row.getCells().get(key);
		return cell == null || !(cell.getRaw() instanceof Number) ? null : Integer.valueOf(((Number) cell.getRaw()).intValue());
	}

	private SiteStatsTableRow rowByUser(SiteStatsTable table, String display) {
		for (SiteStatsTableRow row : table.getRows()) {
			if (display.equals(cellDisplay(row, "user"))) {
				return row;
			}
		}
		return null;
	}
}
