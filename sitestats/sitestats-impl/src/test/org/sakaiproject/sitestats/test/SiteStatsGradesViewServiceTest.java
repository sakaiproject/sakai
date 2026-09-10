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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_GROUP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM_TYPE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_THRESHOLD;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_AVERAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_BELOW_THRESHOLD;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_COMPLETE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_GRADED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_GRADES_BELOW_THRESHOLD;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_GRADES_COMPLETE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_GRADES_GRADED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_GRADES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_GRADES;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.site;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.tool;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingSecurityException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsFilterOption;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetricSnapshot;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
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
public class SiteStatsGradesViewServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String SITE_ID = FakeData.SITE_A_ID;
	private static final String SITE_REF = FakeData.SITE_A_REF;
	private static final String USER_A_ID = FakeData.USER_A_ID;
	private static final String USER_B_ID = FakeData.USER_B_ID;

	@Autowired private DB db;
	@Autowired private SiteStatsViewService service;
	@Autowired private GradingService gradingService;
	@Autowired private SecurityService securityService;
	@Autowired private SessionManager sessionManager;
	@Autowired private UserDirectoryService userDirectoryService;
	@Autowired private SiteService siteService;
	@Autowired private StatsManager statsManager;

	@Before
	public void setUp() throws Exception {
		db.deleteAll();
		reset(securityService, siteService, sessionManager, userDirectoryService, gradingService);

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
		when(site.getUsersIsAllowed("section.role.student"))
				.thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID)));
		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(siteService.isUserSite(SITE_ID)).thenReturn(false);
		when(siteService.isSpecialSite(SITE_ID)).thenReturn(false);

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_OWN, SITE_REF)).thenReturn(true);

		when(gradingService.getAssignments(anyString(), anyString(), any(SortType.class))).thenReturn(Collections.emptyList());
		when(gradingService.getViewableAssignmentsForCurrentUser(anyString(), anyString(), any(SortType.class)))
				.thenReturn(Collections.emptyList());
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(anyString(), anyString(), anyList(), anyList()))
				.thenReturn(Collections.emptyMap());

		PrefsData prefsData = new PrefsData();
		prefsData.setShowOwnStatisticsToStudents(true);
		prefsData.setListToolEventsOnlyAvailableInSite(false);
		prefsData.setToolEventsDef(Arrays.asList(tool(FakeData.TOOL_CHAT, FakeData.EVENT_CHATNEW)));
		assertTrue(statsManager.setPreferences(SITE_ID, prefsData));
	}

	@Test
	public void instructorOverviewIncludesGradesWidgetNotStudentGrades() {
		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertTrue(hasWidget(overview, WIDGET_GRADES));
		assertFalse(hasWidget(overview, WIDGET_STUDENT_GRADES));
		assertTrue(widget(overview, WIDGET_GRADES).getHighlights().isEmpty());
	}

	@Test
	public void instructorOverviewIncludesCompactGradingFunnelWhenGradesExist() {
		stubEssayWithOneGrade();

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsChart chart = widget(overview, WIDGET_GRADES).getHighlights().get(0);

		assertEquals(1, widget(overview, WIDGET_GRADES).getHighlights().size());
		assertTrue(chart.isCompact());
		assertTrue(chart.isHorizontal());
		assertTrue(chart.isStacked());
		assertEquals(2, chart.getDatasets().size());
		assertEquals("complete", chart.getDatasets().get(0).getKey());
		assertEquals("partial", chart.getDatasets().get(1).getKey());
		assertEquals("warning", chart.getDatasets().get(1).getColor());
		assertEquals(3, chart.getDatasets().get(0).getPoints().size());
		assertEquals(Integer.valueOf(2), chart.getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(1).getY());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(2).getY());
		assertEquals("success", chart.getDatasets().get(0).getPoints().get(2).getColor());
		assertEquals(Integer.valueOf(0), chart.getDatasets().get(1).getPoints().get(1).getY());
	}

	@Test
	public void studentOverviewFunnelCountsTheCurrentUsersItems() {
		stubEssayWithOneGrade();
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsChart chart = widget(overview, WIDGET_STUDENT_GRADES).getHighlights().get(0);

		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(1).getY());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(2).getY());
		assertFalse(chart.isStacked());
		assertEquals(1, chart.getDatasets().size());
	}

	@Test
	public void instructorOverviewSplitsWithGradesBetweenCompleteAndPartial() {
		stubTwoItemsWithOnePartialGrade();

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsChart chart = widget(overview, WIDGET_GRADES).getHighlights().get(0);

		assertEquals(Integer.valueOf(2), chart.getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(0), chart.getDatasets().get(0).getPoints().get(1).getY());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(0).getPoints().get(2).getY());
		assertEquals(Integer.valueOf(0), chart.getDatasets().get(1).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(1), chart.getDatasets().get(1).getPoints().get(1).getY());
		assertEquals(Integer.valueOf(0), chart.getDatasets().get(1).getPoints().get(2).getY());
	}

	@Test
	public void studentOverviewIncludesStudentGradesWhenAllPermissionIsNotGranted() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertTrue(hasWidget(overview, WIDGET_STUDENT_GRADES));
		assertFalse(hasWidget(overview, WIDGET_GRADES));
		assertTrue(widget(overview, WIDGET_STUDENT_GRADES).getToolFilters().isEmpty());
	}

	@Test
	public void instructorTabsExposeGroupAndItemFiltersWithoutThreshold() {
		stubEssayWithOneGrade();

		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_GRADES, TAB_BY_USER);

		assertNull(filterById(tab, FILTER_THRESHOLD));
		assertNotNull(filterById(tab, FILTER_GROUP));
		assertNotNull(filterById(tab, FILTER_ITEM));
		assertEquals("gradebook:11", filterById(tab, FILTER_ITEM).getOptions().get(1).getValue());
		assertNotNull(filterById(tab, FILTER_ROLE));
		assertNull(filterById(tab, FILTER_ITEM_TYPE));
	}

	@Test
	public void instructorGradesWidgetHidesToolFiltersUntilMultipleGradebookSourcesExist() {
		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsWidget widget = widget(overview, WIDGET_GRADES);

		assertTrue(widget.getToolFilters().isEmpty());
		assertTrue(widget.isHighlightsConfigured());

		stubEssayWithOneGrade();
		overview = service.getOverview(SITE_ID);
		assertTrue(widget(overview, WIDGET_GRADES).getToolFilters().isEmpty());
	}

	@Test
	public void gradesIncludeExternallyMaintainedAssignmentAndQuizItems() {
		stubMixedGradebookSources();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM, request);

		assertEquals(3, view.getTable().getTotalRows());
		assertEquals(3, itemNames(view).size());
		assertTrue(itemNames(view).contains("Essay"));
		assertTrue(itemNames(view).contains("Homework"));
		assertTrue(itemNames(view).contains("Quiz"));

		request.setItemType("sakai.assignment.grades");
		view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM, request);
		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("Homework", cellDisplay(view.getTable().getRows().get(0), "item"));

		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM);
		assertEquals(4, filterById(tab, FILTER_ITEM).getOptions().size());
	}

	@Test
	public void gradesToolFiltersOnlyListToolsPresentInGradebook() {
		stubMixedGradebookSources();

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsWidget widget = widget(overview, WIDGET_GRADES);

		assertEquals(new HashSet<String>(Arrays.asList("sakai.assignment.grades", "sakai.samigo", "sakai.gradebookng")),
				toolFilterValues(widget));
		assertToolFilterIcons(widget);
	}

	@Test
	public void gradesToolFiltersOmitMissingGradebookSources() {
		Assignment homework = countedItem(12L, "Homework", 10d, "2026-06-15T23:59:59Z");
		homework.setExternallyMaintained(Boolean.TRUE);
		homework.setExternalAppName("sakai.assignment.grades");
		Assignment quiz = countedItem(13L, "Quiz", 10d, "2026-06-15T23:59:59Z");
		quiz.setExternallyMaintained(Boolean.TRUE);
		quiz.setExternalAppName("sakai.samigo");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Arrays.asList(homework, quiz));

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsWidget widget = widget(overview, WIDGET_GRADES);

		assertEquals(new HashSet<String>(Arrays.asList("sakai.assignment.grades", "sakai.samigo")),
				toolFilterValues(widget));
		assertToolFilterIcons(widget);
	}

	@Test
	public void gradesIncludeOtherToolsLinkedToGradebook() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		Assignment topic = countedItem(14L, "Discussion", 10d, "2026-06-15T23:59:59Z");
		topic.setExternallyMaintained(Boolean.TRUE);
		topic.setExternalAppName("sakai.conversations");
		Assignment lesson = countedItem(15L, "Lesson page", 10d, "2026-06-15T23:59:59Z");
		lesson.setExternallyMaintained(Boolean.TRUE);
		lesson.setExternalAppName("sakai.lessonbuildertool");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Arrays.asList(essay, topic, lesson));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("8");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
		grade.setGradeReleased(true);
		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		grades.put(Long.valueOf(14), Collections.singletonList(grade));
		grades.put(Long.valueOf(15), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM, request);
		assertEquals(3, view.getTable().getTotalRows());
		assertTrue(itemNames(view).contains("Discussion"));
		assertTrue(itemNames(view).contains("Lesson page"));

		request.setItemType("sakai.conversations");
		view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM, request);
		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("Discussion", cellDisplay(view.getTable().getRows().get(0), "item"));

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		SiteStatsWidget widget = widget(overview, WIDGET_GRADES);
		assertEquals(new HashSet<String>(Arrays.asList("sakai.conversations", "sakai.lessonbuildertool",
				"sakai.gradebookng")), toolFilterValues(widget));
		assertToolFilterIcons(widget);
	}

	@Test
	public void studentTabExposesItemWithoutGroupRoleOrThreshold() {
		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_STUDENT_GRADES, TAB_BY_ITEM);

		assertNotNull(filterById(tab, FILTER_ITEM));
		assertNull(filterById(tab, FILTER_ITEM_TYPE));
		assertNull(filterById(tab, FILTER_THRESHOLD));
		assertNull(filterById(tab, FILTER_GROUP));
		assertNull(filterById(tab, FILTER_ROLE));
	}

	@Test
	public void widgetThresholdUsesSitePropertyWhenPreferencesAreEmpty() throws Exception {
		stubEssayWithOneGrade();
		org.sakaiproject.entity.api.ResourceProperties properties = mock(org.sakaiproject.entity.api.ResourceProperties.class);
		when(properties.getProperty(StatsManager.GRADES_THRESHOLD_PROPERTY)).thenReturn("90");
		when(siteService.getSite(SITE_ID).getProperties()).thenReturn(properties);

		assertEquals(1, belowCount());
	}

	@Test
	public void widgetThresholdPrefersSavedPreferencesOverSiteProperty() throws Exception {
		stubEssayWithOneGrade();
		org.sakaiproject.entity.api.ResourceProperties properties = mock(org.sakaiproject.entity.api.ResourceProperties.class);
		when(properties.getProperty(StatsManager.GRADES_THRESHOLD_PROPERTY)).thenReturn("50");
		when(siteService.getSite(SITE_ID).getProperties()).thenReturn(properties);

		PrefsData prefsData = statsManager.getPreferences(SITE_ID, false);
		prefsData.setGradesThreshold(Double.valueOf(90));
		assertTrue(statsManager.setPreferences(SITE_ID, prefsData));

		assertEquals(1, belowCount());
	}

	@Test
	public void belowThresholdAndCompletionUsePointsFromGradebook() {
		stubEssayWithOneGrade();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_USER, request);

		assertEquals(2, view.getTable().getTotalRows());
		assertEquals("overview_status_meeting_threshold", cellDisplay(view.getTable().getRows().get(0), "belowThreshold"));
		assertEquals("-", cellDisplay(view.getTable().getRows().get(1), "belowThreshold"));
		assertEquals("0 / 1", snapshot(WIDGET_GRADES, METRIC_GRADES_BELOW_THRESHOLD).getPrimary());
		assertEquals(Integer.valueOf(0), snapshot(WIDGET_GRADES, METRIC_GRADES_BELOW_THRESHOLD).getPercentage());
		assertEquals("1 / 2", snapshot(WIDGET_GRADES, METRIC_GRADES_COMPLETE).getPrimary());
		assertEquals(Integer.valueOf(50), snapshot(WIDGET_GRADES, METRIC_GRADES_COMPLETE).getPercentage());
		assertEquals("80%", snapshot(WIDGET_GRADES, METRIC_GRADES_AVERAGE).getPrimary());
		assertNull(snapshot(WIDGET_GRADES, METRIC_GRADES_AVERAGE).getPercentage());
		assertEquals("1 / 2", snapshot(WIDGET_GRADES, METRIC_GRADES_GRADED).getPrimary());
		assertEquals(Integer.valueOf(50), snapshot(WIDGET_GRADES, METRIC_GRADES_GRADED).getPercentage());
	}

	@Test
	public void higherThresholdCountsMoreStudentsBelow() {
		stubEssayWithOneGrade();

		PrefsData prefsData = statsManager.getPreferences(SITE_ID, false);
		prefsData.setGradesThreshold(Double.valueOf(90));
		assertTrue(statsManager.setPreferences(SITE_ID, prefsData));

		assertEquals(1, belowCount());
	}

	@Test
	public void itemFilterUsesThresholdPieOnByUserChart() {
		stubEssayWithOneGrade();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItem("gradebook:11");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_USER, request);

		assertEquals(2, view.getTable().getTotalRows());
		assertEquals(StatsManager.CHARTTYPE_PIE, view.getChart().getType());
		assertEquals(Integer.valueOf(0), view.getChart().getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(Integer.valueOf(1), view.getChart().getDatasets().get(0).getPoints().get(1).getY());
	}

	@Test
	public void itemFilterRestrictsByItemReport() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		Assignment quiz = countedItem(22L, "Quiz", 10d, "2026-06-16T23:59:59Z");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Arrays.asList(essay, quiz));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(Collections.emptyMap());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItem("gradebook:11");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM, request);

		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("Essay", cellDisplay(view.getTable().getRows().get(0), "item"));
	}

	@Test
	public void studentWidgetOnlyCountsCurrentUser() {
		stubEssayWithOneGrade();
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		assertEquals("8 / 10", snapshot(WIDGET_STUDENT_GRADES, METRIC_STUDENT_GRADES_COMPLETE).getPrimary());
		assertEquals(Integer.valueOf(80), snapshot(WIDGET_STUDENT_GRADES, METRIC_STUDENT_GRADES_COMPLETE).getPercentage());
		assertEquals("overview_status_meeting_threshold",
				snapshot(WIDGET_STUDENT_GRADES, METRIC_STUDENT_GRADES_BELOW_THRESHOLD).getPrimary());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_STUDENT_GRADES, TAB_BY_ITEM, request);

		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("Essay", cellDisplay(view.getTable().getRows().get(0), "item"));
	}

	@Test
	public void studentWithoutGradesIsNotMeetingThreshold() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));
		when(gradingService.getViewableAssignmentsForCurrentUser(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		assertEquals("-", snapshot(WIDGET_STUDENT_GRADES, METRIC_STUDENT_GRADES_BELOW_THRESHOLD).getPrimary());
	}

	@Test
	public void studentWidgetLoadsReleasedGradesWhenGetAssignmentsIsForbidden() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		when(gradingService.getAssignments(anyString(), anyString(), any(SortType.class)))
				.thenThrow(GradingSecurityException.class);
		when(gradingService.getViewableAssignmentsForCurrentUser(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("8");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
		grade.setGradeReleased(true);
		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		assertEquals("1 / 1", snapshot(WIDGET_STUDENT_GRADES, METRIC_STUDENT_GRADES_GRADED).getPrimary());
		assertEquals("8 / 10", snapshot(WIDGET_STUDENT_GRADES, METRIC_STUDENT_GRADES_COMPLETE).getPrimary());

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		assertEquals(1, widget(overview, WIDGET_STUDENT_GRADES).getHighlights().size());
		assertEquals(Integer.valueOf(1), widget(overview, WIDGET_STUDENT_GRADES).getHighlights().get(0)
				.getDatasets().get(0).getPoints().get(1).getY());
	}

	@Test
	public void letterGradesUseGradebookScale() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("B");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_LETTER);
		grade.setGradeReleased(true);
		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);

		GradebookInformation information = new GradebookInformation();
		Map<String, Double> percents = new HashMap<String, Double>();
		percents.put("A", Double.valueOf(90));
		percents.put("B", Double.valueOf(80));
		information.setSelectedGradingScaleBottomPercents(percents);
		when(gradingService.getGradebookInformation(SITE_ID, SITE_ID)).thenReturn(information);

		assertEquals("0 / 1", snapshot(WIDGET_GRADES, METRIC_GRADES_BELOW_THRESHOLD).getPrimary());
		assertEquals("80%", snapshot(WIDGET_GRADES, METRIC_GRADES_AVERAGE).getPrimary());
		assertNull(snapshot(WIDGET_GRADES, METRIC_GRADES_AVERAGE).getPercentage());
	}

	@Test
	public void usersWithoutStudentRoleAreExcludedFromByUserReport() throws Exception {
		stubEssayWithOneGrade();
		Site site = siteService.getSite(SITE_ID);
		when(site.getUsers()).thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID, "instructor-user")));
		when(site.getUsersIsAllowed("section.role.student"))
				.thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID)));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_USER, request);

		assertEquals(2, view.getTable().getTotalRows());
	}

	@Test
	public void multiGradebookOnlyCountsMembersOfTheItemGroup() throws Exception {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		essay.setContext("group-gb");
		when(gradingService.isGradebookGroupEnabled(SITE_ID)).thenReturn(true);
		when(gradingService.getGradebookGroupInstancesIds(SITE_ID)).thenReturn(Collections.singletonList("group-gb"));
		when(gradingService.getAssignments(eq("group-gb"), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("8");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
		grade.setGradeReleased(true);
		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq("group-gb"), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);

		stubGroup("group-gb", "Lab 1", USER_A_ID);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_USER, request);

		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("User A", cellDisplay(view.getTable().getRows().get(0), "user"));

		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM);
		assertTrue(filterById(tab, FILTER_ITEM).getOptions().get(1).getLabel().contains("Lab 1"));
	}

	@Test
	public void last7DaysExcludesJuneDuesButKeepsColumns() {
		stubEssayWithOneGrade();

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_LAST7DAYS);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_USER, request);

		assertNotNull(view.getTable());
		assertEquals(0, view.getTable().getTotalRows());
		assertTrue(view.getTable().getRows().isEmpty());
		assertFalse(view.getTable().getColumns().isEmpty());
	}

	@Test
	public void last7DaysIncludesGradebookItemsWithoutDueDate() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		essay.setDueDate(null);
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));
		when(gradingService.getViewableAssignmentsForCurrentUser(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("8");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
		grade.setGradeReleased(true);
		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_LAST7DAYS);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_GRADES, TAB_BY_ITEM, request);

		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("Essay", cellDisplay(view.getTable().getRows().get(0), "item"));
	}

	private void stubEssayWithOneGrade() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));
		when(gradingService.getViewableAssignmentsForCurrentUser(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Collections.singletonList(essay));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("8");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
		grade.setGradeReleased(true);

		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);
	}

	private void stubTwoItemsWithOnePartialGrade() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		Assignment homework = countedItem(12L, "Homework", 10d, "2026-06-16T23:59:59Z");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Arrays.asList(essay, homework));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("8");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
		grade.setGradeReleased(true);
		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);
	}

	private void stubMixedGradebookSources() {
		Assignment essay = countedItem(11L, "Essay", 10d, "2026-06-15T23:59:59Z");
		Assignment homework = countedItem(12L, "Homework", 10d, "2026-06-15T23:59:59Z");
		homework.setExternallyMaintained(Boolean.TRUE);
		homework.setExternalAppName("sakai.assignment.grades");
		Assignment quiz = countedItem(13L, "Quiz", 10d, "2026-06-15T23:59:59Z");
		quiz.setExternallyMaintained(Boolean.TRUE);
		quiz.setExternalAppName("sakai.samigo");
		when(gradingService.getAssignments(eq(SITE_ID), eq(SITE_ID), any(SortType.class)))
				.thenReturn(Arrays.asList(essay, homework, quiz));

		GradeDefinition grade = new GradeDefinition();
		grade.setStudentUid(USER_A_ID);
		grade.setGrade("8");
		grade.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
		grade.setGradeReleased(true);
		Map<Long, List<GradeDefinition>> grades = new HashMap<Long, List<GradeDefinition>>();
		grades.put(Long.valueOf(11), Collections.singletonList(grade));
		grades.put(Long.valueOf(12), Collections.singletonList(grade));
		grades.put(Long.valueOf(13), Collections.singletonList(grade));
		when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(SITE_ID), eq(SITE_ID), anyList(), anyList()))
				.thenReturn(grades);
	}

	private Assignment countedItem(Long id, String name, double points, String dueDate) {
		Assignment assignment = new Assignment();
		assignment.setId(id);
		assignment.setName(name);
		assignment.setPoints(Double.valueOf(points));
		assignment.setDueDate(Date.from(Instant.parse(dueDate)));
		assignment.setReleased(Boolean.TRUE);
		assignment.setCounted(Boolean.TRUE);
		assignment.setUngraded(Boolean.FALSE);
		assignment.setExtraCredit(Boolean.FALSE);
		assignment.setExternallyMaintained(Boolean.FALSE);
		return assignment;
	}

	private void stubGroup(String groupId, String title, String... userIds) throws Exception {
		Site site = siteService.getSite(SITE_ID);
		Group group = mock(Group.class);
		when(group.getId()).thenReturn(groupId);
		when(group.getTitle()).thenReturn(title);
		when(group.getUsers()).thenReturn(new HashSet<String>(Arrays.asList(userIds)));
		when(site.getGroup(groupId)).thenReturn(group);
		when(site.getGroups()).thenReturn(Collections.singletonList(group));
	}

	private SiteStatsFilter filterById(SiteStatsWidgetTab tab, String id) {
		for (SiteStatsFilter filter : tab.getFilters()) {
			if (id.equals(filter.getId())) {
				return filter;
			}
		}
		return null;
	}

	private int belowCount() {
		String primary = snapshot(WIDGET_GRADES, METRIC_GRADES_BELOW_THRESHOLD).getPrimary();
		int separator = primary.indexOf(" / ");
		return Integer.parseInt(separator < 0 ? primary : primary.substring(0, separator));
	}

	private SiteStatsWidgetMetricSnapshot snapshot(String widgetId, String metricId) {
		for (SiteStatsWidgetMetric metric : service.getWidgetMetrics(SITE_ID, widgetId)) {
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

	private HashSet<String> itemNames(SiteStatsReportView view) {
		HashSet<String> names = new HashSet<String>();
		for (SiteStatsTableRow row : view.getTable().getRows()) {
			names.add(cellDisplay(row, "item"));
		}
		return names;
	}

	private HashSet<String> toolFilterValues(SiteStatsWidget widget) {
		HashSet<String> values = new HashSet<String>();
		for (SiteStatsFilterOption option : widget.getToolFilters()) {
			values.add(option.getValue());
		}
		return values;
	}

	private void assertToolFilterIcons(SiteStatsWidget widget) {
		for (SiteStatsFilterOption option : widget.getToolFilters()) {
			assertEquals("si-" + option.getValue().replace('.', '-'), option.getIcon());
		}
	}
}
