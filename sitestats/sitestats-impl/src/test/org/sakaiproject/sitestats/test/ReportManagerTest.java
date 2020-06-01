/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.impl.report.ReportManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.aop.framework.Advised;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@Transactional(transactionManager = "org.sakaiproject.sitestats.SiteStatsTransactionManager")
public class ReportManagerTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Resource(name = "org.sakaiproject.sitestats.test.DB")
	private DB db;
	@Resource(name = "org.sakaiproject.memory.api.MemoryService")
	private MemoryService memoryService;
	@Resource(name = "org.sakaiproject.sitestats.api.report.ReportManager")
	private ReportManager reportManager;
	@Resource(name = "org.sakaiproject.util.ResourceLoader.sitestats")
	private ResourceLoader resourceLoader;
	@Resource(name = "org.sakaiproject.site.api.SiteService")
	private SiteService siteService;
	@Resource(name = "org.sakaiproject.sitestats.api.StatsManager")
	private StatsManager statsManager;
	@Resource(name = "org.sakaiproject.sitestats.api.StatsUpdateManager")
	private StatsUpdateManager statsUpdateManager;
	@Resource(name = "org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;
	@Resource(name = "org.sakaiproject.sitestats.api.event.EventRegistryService")
	private EventRegistryService eventRegistryService;

	@Before
	public void onSetUp() throws Exception {
		db.deleteAll();
		memoryService.resetCachers();

		FakeSite userSiteA = spy(FakeSite.class).set("~"+FakeData.USER_A_ID);
		FakeSite userSiteB = spy(FakeSite.class).set("~"+FakeData.USER_B_ID);
		when(siteService.getSiteUserId(FakeData.USER_A_ID)).thenReturn("~"+FakeData.USER_A_ID);
		when(siteService.getSiteUserId(FakeData.USER_B_ID)).thenReturn("~"+FakeData.USER_B_ID);
		when(siteService.getSiteUserId("no_user")).thenReturn(null);
		when(siteService.getSite("~"+FakeData.USER_A_ID)).thenReturn(userSiteA);
		when(siteService.getSite("~"+FakeData.USER_B_ID)).thenReturn(userSiteB);

		// Site A has tools {SiteStats, Chat}, has {user-a,user-b}
		FakeSite siteA = spy(FakeSite.class).set(FakeData.SITE_A_ID, Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID));
		siteA.setUsers(new HashSet<>(Arrays.asList(FakeData.USER_A_ID, FakeData.USER_B_ID)));
		siteA.setMembers(new HashSet<>(Arrays.asList(FakeData.USER_A_ID, FakeData.USER_B_ID)));
		when(siteService.getSite(FakeData.SITE_A_ID)).thenReturn(siteA);
		when(siteService.isUserSite(FakeData.SITE_A_ID)).thenReturn(false);
		when(siteService.isSpecialSite(FakeData.SITE_A_ID)).thenReturn(false);

		// Site B has tools {TOOL_CHAT}, has {user-a}, notice this site doesn't have the site stats tool
		FakeSite siteB = spy(FakeSite.class).set(FakeData.SITE_B_ID, Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT));
		siteB.setUsers(new HashSet<>(Collections.singletonList(FakeData.USER_A_ID)));
		siteB.setMembers(new HashSet<>(Collections.singletonList(FakeData.USER_A_ID)));
		when(siteService.getSite(FakeData.SITE_B_ID)).thenReturn(siteB);
		when(siteService.isUserSite(FakeData.SITE_B_ID)).thenReturn(false);
		when(siteService.isSpecialSite(FakeData.SITE_B_ID)).thenReturn(false);

		// This is needed to make the tests deterministic, otherwise on occasion the collect thread will run
		// and break the tests.

		statsUpdateManager.setCollectThreadEnabled(false);
		((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setEnableSitePresences(true);
		((ReportManagerImpl) ((Advised) reportManager).getTargetSource().getTarget()).setResourceLoader(resourceLoader);
	}

	// ---- SAMPLE DATA ----
	
	private List<Event> getSampleData() {
		List<Event> samples = new ArrayList<Event>();
		Date today = new Date();
		Date today_2 = new Date(today.getTime() + 60*1000);
		Date today_3 = new Date(today.getTime() + 2*60*1000);
		Date today_4 = new Date(today.getTime() + 3*60*1000);
		Date oneDayBefore = new Date(today.getTime() - 24*60*60*1000);
		Date oneDayBefore_2 = new Date(today.getTime() - 24*60*60*1000 + 60*1000);
		Date oneDayBefore_3 = new Date(today.getTime() - 24*60*60*1000 + 2*60*1000);
		Date oneDayBefore_4 = new Date(today.getTime() - 24*60*60*1000 + 3*60*1000);
		Date twoDaysBefore = new Date(today.getTime() - 2*24*60*60*1000);
		Date twoDaysBefore_2 = new Date(today.getTime() - 2*24*60*60*1000 + 60*1000);
		Date fourDaysBefore = new Date(today.getTime() - 4*24*60*60*1000);
		Date fourDaysBefore_2 = new Date(today.getTime() - 4*24*60*60*1000 + 60*1000);
		Date sixDaysBefore = new Date(today.getTime() - 6*24*60*60*1000);
		Date sixDaysBefore_2 = new Date(today.getTime() - 6*24*60*60*1000 + 60*1000);
		// visits
		Event vATodayS1 = statsUpdateManager.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATodayE1 = statsUpdateManager.buildEvent(today_2, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATodayS2 = statsUpdateManager.buildEvent(today_3, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATodayE2 = statsUpdateManager.buildEvent(today_4, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBTodayS1 = statsUpdateManager.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vBTodayE1 = statsUpdateManager.buildEvent(today_3, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vAOneDayBefore = statsUpdateManager.buildEvent(oneDayBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATowDaysBefore = statsUpdateManager.buildEvent(twoDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vAFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vBSixDaysBefore = statsUpdateManager.buildEvent(sixDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		samples.addAll(Arrays.asList(
				vATodayS1, vATodayE1, 
				vATodayS2, vATodayE2, 
				vBTodayS1, vBTodayE1, 				// today:			3 visits, 2 unique
				vAOneDayBefore, 					// 1 day before:	1 visits, 1 unique
				vATowDaysBefore, vATowDaysBefore, 	// 2 day before:	2 visits, 1 unique
				vAFourDaysBefore, vBFourDaysBefore, // 4 day before:	2 visits, 2 unique
				vBSixDaysBefore						// 6 day before:	1 visits, 1 unique
				));
		// activity
		Event aAToday = statsUpdateManager.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_B_ID, FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBToday = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event aAOneDayBefore = statsUpdateManager.buildEvent(oneDayBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aATowDaysBefore = statsUpdateManager.buildEvent(twoDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aAFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event aBSixDaysBefore = statsUpdateManager.buildEvent(sixDaysBefore, FakeData.EVENT_CONTENTREV, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		samples.addAll(Arrays.asList(
				aAToday, aBToday,  					// today:			2 (1 chat + 1 content)
				aAOneDayBefore, 					// 1 day before:	1 (1 chat)
				aATowDaysBefore, aATowDaysBefore, 	// 2 day before:	2 (2 chat)
				aAFourDaysBefore, aBFourDaysBefore, // 4 day before:	2 (2 chat)
				aBSixDaysBefore						// 6 day before:	1 (1 chat)
				));
		return samples;
	}
	
	private List<Event> getSampleData2() {
		List<Event> samples = new ArrayList<Event>();
		Date today = new Date();
		Date oneDayBefore = new Date(today.getTime() - 24*60*60*1000);
		Date twoDaysBefore = new Date(today.getTime() - 2*24*60*60*1000);
		Date fourDaysBefore = new Date(today.getTime() - 4*24*60*60*1000);
		Date sixDaysBefore = new Date(today.getTime() - 6*24*60*60*1000);
		// visits
		Event vAToday = statsUpdateManager.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBToday = statsUpdateManager.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vAOneDayBefore = statsUpdateManager.buildEvent(oneDayBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATowDaysBefore = statsUpdateManager.buildEvent(twoDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vAFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vBSixDaysBefore = statsUpdateManager.buildEvent(sixDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		samples.addAll(Arrays.asList(
				vAToday, vAToday, vBToday, 			// today:			3 visits, 2 unique
				vAOneDayBefore, 					// 1 day before:	1 visits, 1 unique
				vATowDaysBefore, vATowDaysBefore, 	// 2 day before:	2 visits, 1 unique
				vAFourDaysBefore, vBFourDaysBefore, // 4 day before:	2 visits, 2 unique
				vBSixDaysBefore						// 6 day before:	1 visits, 1 unique
				));
		// activity
		Event aAToday = statsUpdateManager.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_B_ID, FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBToday = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event aAOneDayBefore = statsUpdateManager.buildEvent(oneDayBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aATowDaysBefore = statsUpdateManager.buildEvent(twoDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aAFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBFourDaysBefore = statsUpdateManager.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event aBSixDaysBefore = statsUpdateManager.buildEvent(sixDaysBefore, FakeData.EVENT_CONTENTREV, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		samples.addAll(Arrays.asList(
				aAToday, aBToday,  					// today:			2 (1 chat + 1 content)
				aAOneDayBefore, 					// 1 day before:	1 (1 chat)
				aATowDaysBefore, aATowDaysBefore, 	// 2 day before:	2 (2 chat)
				aAFourDaysBefore, aBFourDaysBefore, // 4 day before:	2 (2 chat)
				aBSixDaysBefore						// 6 day before:	1 (1 chat)
				));
		// resources
		Event r1 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r1a = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r1b = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r1c = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r2 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r2a = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r2b = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r3 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r4 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id2", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		// resources: attachment
		Event r5 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, FakeData.RES_ATTACH, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		// resources: dropbox
		Event r6 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, FakeData.RES_DROPBOX_SITE_A_USER_A_FILE, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		
		samples.addAll(Arrays.asList( r1, r1a, r1b, r1c, r2, r2a, r2b, r3, r4, r5, r6 ));
		return samples;
	}

	
	// ---- TESTS ----
	@Test
	public void testGetReport() {
		statsUpdateManager.collectEvents(getSampleData());
		String siteId = null;
		Report r = null;
		ReportDef rd = null;
		ReportParams rp = null;
		List<String> totalsBy = null;
		List<String> listUserA = new ArrayList<>();
		List<String> listUserB = new ArrayList<>();
		List<String> listNoUsers = new ArrayList<>();
		listUserA.add(FakeData.USER_A_ID);
		listUserB.add(FakeData.USER_B_ID);
		
		// #1 getReport(ReportDef reportDef, boolean restrictToToolsInSite)
		siteId = FakeData.SITE_B_ID;
		rd = new ReportDef();
		rd.setId(0);
		rd.setSiteId(siteId);
		rp = new ReportParams(siteId);
		rp.setWhat(ReportManager.WHAT_EVENTS);
		rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYTOOL);
		rp.setWhatToolIds(Arrays.asList(ReportManager.WHAT_EVENTS_ALLTOOLS));
		rp.setWhen(ReportManager.WHEN_ALL);
		rp.setWho(ReportManager.WHO_ALL);
		// grouping
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_TOOL);
		totalsBy.add(StatsManager.T_DATE);
		rp.setHowTotalsBy(totalsBy);
		// sorting
		rp.setHowSort(true);
		rp.setHowSortBy(StatsManager.T_TOOL);
		rp.setHowSortAscending(false);
		// chart
		rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		rp.setHowChartSource(StatsManager.T_DATE);
		rp.setHowChartSeriesSource(StatsManager.T_NONE);
		rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
		rd.setReportParams(rp);
		r = reportManager.getReport(rd, true);
		checkCollumns(rd.getReportParams());
		assertEquals(1, r.getReportData().size());
		r = reportManager.getReport(rd, false);
		checkCollumns(rd.getReportParams());
		assertEquals(2, r.getReportData().size());
		assertNotNull(reportManager.getReportFormattedParams());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserA);
		r = reportManager.getReport(rd, true);
		checkCollumns(rd.getReportParams());
		assertEquals(1, r.getReportData().size());
		rp.setWhoUserIds(listNoUsers);
		r = reportManager.getReport(rd, true);
		checkCollumns(rd.getReportParams());
		assertEquals(0, r.getReportData().size());

		// #2 getReportRowCount(ReportDef reportDef, boolean restrictToToolsInSite, List<String> userIds)
		rp.setWho(ReportManager.WHO_ALL);
		assertEquals(1, reportManager.getReportRowCount(rd, true));
		assertEquals(2, reportManager.getReportRowCount(rd, false));
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserA);
		assertEquals(1, reportManager.getReportRowCount(rd, true));
		rp.setWhoUserIds(listNoUsers);
		assertEquals(0, reportManager.getReportRowCount(rd, true));
		
		siteId = FakeData.SITE_B_ID;
		rd = new ReportDef();
		rd.setId(0);
		rd.setSiteId(siteId);
		rp = new ReportParams(siteId);
		rp.setWhat(ReportManager.WHAT_RESOURCES);
		rp.setWhen(ReportManager.WHEN_ALL);
		rp.setWho(ReportManager.WHO_ALL);
		// grouping
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_RESOURCE);
		totalsBy.add(StatsManager.T_DATE);
		rp.setHowTotalsBy(totalsBy);
		// sorting
		rp.setHowSort(true);
		rp.setHowSortBy(StatsManager.T_TOOL);
		rp.setHowSortAscending(false);
		// chart
		rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		rd.setReportParams(rp);
		assertEquals(1, reportManager.getReportRowCount(rd, true));
		assertEquals(1, reportManager.getReportRowCount(rd, false));
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserA);
		assertEquals(1, reportManager.getReportRowCount(rd, false));
		rp.setWhoUserIds(listNoUsers);
		assertEquals(0, reportManager.getReportRowCount(rd, true));
	}

	@Test
	@Ignore		// TODO JUNIT test is not working on hsqldb need to look into
	public void testGetMoreReports() {
		statsUpdateManager.collectEvents(getSampleData());
		String siteId = null;
		Report r = null;
		ReportDef rd = null;
		ReportParams rp = null;
		List<String> totalsBy = null;
		List<String> listUserA = new ArrayList<>();
		List<String> listUserB = new ArrayList<>();
		List<String> listNoUsers = new ArrayList<>();
		listUserA.add(FakeData.USER_A_ID);
		listUserB.add(FakeData.USER_B_ID);
		
		// resources
		siteId = FakeData.SITE_A_ID;
		rd = new ReportDef();
		rd.setId(0);
		rd.setSiteId(siteId);
		rp = new ReportParams(siteId);
		rp.setWhat(ReportManager.WHAT_RESOURCES);
		rp.setWhen(ReportManager.WHEN_ALL);
		rp.setWho(ReportManager.WHO_ALL);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		totalsBy.add(StatsManager.T_USER);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		rd.setReportParams(rp);
		r = reportManager.getReport(rd, true, null, false);
		checkCollumns(rd.getReportParams());
		assertEquals(1, r.getReportData().size());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserB);
		r = reportManager.getReport(rd, true);
		r = reportManager.getReport(rd, true, null, false);
		checkCollumns(rd.getReportParams());
		assertEquals(1, r.getReportData().size());
		
		// visits
		rp.setWhat(ReportManager.WHAT_VISITS);
		rp.setWho(ReportManager.WHO_ALL);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(2, r.getReportData().size());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listNoUsers);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(0, r.getReportData().size());
		
		// visits totals
		rp.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		rp.setHowTotalsBy(totalsBy);
		rd.setId(1);
		rp.setWho(ReportManager.WHO_ALL);
		r = reportManager.getReport(rd, true, new PagingPosition(0,5), true);
		assertEquals(1, r.getReportData().size());
		assertEquals(9, ((SiteVisits)(r.getReportData().get(0))).getTotalVisits());
		assertEquals(7, ((SiteVisits)(r.getReportData().get(0))).getTotalUnique());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserB);
		r = reportManager.getReport(rd, true, new PagingPosition(0,5), true);
		assertEquals(1, r.getReportData().size());
		assertEquals(9, ((SiteVisits)(r.getReportData().get(0))).getTotalVisits());
		assertEquals(7, ((SiteVisits)(r.getReportData().get(0))).getTotalUnique());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listNoUsers);
		r = reportManager.getReport(rd, true, new PagingPosition(0,5), true);
		assertEquals(0, r.getReportData().size());
		assertEquals(0, ((SiteVisits)(r.getReportData().get(0))).getTotalVisits());
		assertEquals(0, ((SiteVisits)(r.getReportData().get(0))).getTotalUnique());
		
//		// activity totals
//		rp.setWhat(ReportManager.WHAT_ACTIVITY_TOTALS);
//		rp.setWhatEventIds(FakeData.EVENTIDS);
//		rp.setWhen(ReportManager.WHEN_LAST365DAYS);
//		r = M_rm.getReport(rd, false, null, false);
//		log.info(r.getReportData());
//		log.info("ReportParams: "+ rp);
//		log.info("ReportData: "+ r.getReportData());
//		Assert.assertEquals(1, r.getReportData().size());
//		Assert.assertEquals(1, r.getReportData().get(0).getCount());
		
		// presences I
		rp.setWhat(ReportManager.WHAT_PRESENCES);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		totalsBy.add(StatsManager.T_USER);
		totalsBy.add(StatsManager.T_DATE);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		rp.setWho(ReportManager.WHO_ALL);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(7, r.getReportData().size());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserA);
		r = reportManager.getReport(rd, true, null, true);
		assertEquals(1, r.getReportData().size());
		rp.setWhoUserIds(listNoUsers);
		r = reportManager.getReport(rd, true, null, true);
		assertEquals(0, r.getReportData().size());
		
		// presences II
		rp.setWhat(ReportManager.WHAT_PRESENCES);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		totalsBy.add(StatsManager.T_DATE);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		rp.setWho(ReportManager.WHO_ALL);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(5, r.getReportData().size());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserA);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(1, r.getReportData().size());
		rp.setWhoUserIds(listNoUsers);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(0, r.getReportData().size());
		
		// presences III
		rp.setWhat(ReportManager.WHAT_PRESENCES);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		rp.setWho(ReportManager.WHO_ALL);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(1, r.getReportData().size());
		rp.setWho(ReportManager.WHO_CUSTOM);
		rp.setWhoUserIds(listUserA);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(1, r.getReportData().size());
		rp.setWhoUserIds(listNoUsers);
		r = reportManager.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(0, r.getReportData().size());
		
	}
	
	@Test
	@Ignore        // TODO JUNIT test is not working on hsqldb need to look into
	public void testReportsFromOverviewPage() {
		List<String> listUserA = new ArrayList<>();
		List<String> listNoUsers = new ArrayList<>();
		listUserA.add(FakeData.USER_A_ID);

		statsUpdateManager.collectEvents(getSampleData2());
		
		// MiniStatsVisits & MiniStatUniqueVisits
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			rp.setWhat(ReportManager.WHAT_VISITS_TOTALS);
			rp.setWhen(ReportManager.WHEN_ALL);
			rp.setWho(ReportManager.WHO_ALL);
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_DATE);
			totalsBy.add(StatsManager.T_VISITS);
			totalsBy.add(StatsManager.T_UNIQUEVISITS);
			rp.setHowTotalsBy(totalsBy);
			rp.setHowSort(true);
			rp.setHowSortBy(StatsManager.T_DATE);
			rp.setHowSortAscending(false);
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
			rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
			rp.setHowChartSource(StatsManager.T_DATE);
			rp.setHowChartSeriesSource(StatsManager.T_NONE);
			rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(5, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(1, rep.getReportData().size());
			rp.setWhoUserIds(listNoUsers);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
		
		// MiniStatEnrolledUsersWithVisits
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			rp.setWhat(ReportManager.WHAT_VISITS);
			rp.setWhen(ReportManager.WHEN_ALL);
			rp.setWho(ReportManager.WHO_ALL);
			// grouping
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_USER);
			rp.setHowTotalsBy(totalsBy);
			// sorting
			rp.setHowSort(false);
			// chart
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(1, rep.getReportData().size());
			rp.setWhoUserIds(listNoUsers);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
		
		// MiniStatEnrolledUsersWithoutVisits
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			rp.setWhat(ReportManager.WHAT_VISITS);
			rp.setWhen(ReportManager.WHEN_ALL);
			rp.setWho(ReportManager.WHO_NONE);
			// grouping
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_USER);
			rp.setHowTotalsBy(totalsBy);
			// sorting
			rp.setHowSort(false);
			// chart
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
			rp.setWhoUserIds(listNoUsers);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
		
		// MiniStatActivityEvents
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			// what
			rp.setWhat(ReportManager.WHAT_EVENTS);
			rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
			rp.setWhatEventIds(new ArrayList<String>(eventRegistryService.getEventIds()));
			// when
			rp.setWhen(ReportManager.WHEN_ALL);
			// who
			rp.setWho(ReportManager.WHO_ALL);
			// grouping
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_EVENT);
			rp.setHowTotalsBy(totalsBy);
			// sorting
			rp.setHowSort(true);
			rp.setHowSortBy(StatsManager.T_EVENT);
			rp.setHowSortAscending(true);
			// chart
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(6, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(1, rep.getReportData().size());
			rp.setWhoUserIds(listNoUsers);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
		
		// MiniStatMostActiveUser
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			// what
			rp.setWhat(ReportManager.WHAT_EVENTS);
			rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
			rp.setWhatEventIds(new ArrayList<String>(eventRegistryService.getEventIds()));
			// when
			rp.setWhen(ReportManager.WHEN_ALL);
			// who
			rp.setWho(ReportManager.WHO_ALL);
			// grouping
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_USER);
			rp.setHowTotalsBy(totalsBy);
			// sorting
			rp.setHowSort(true);
			rp.setHowSortBy(StatsManager.T_TOTAL);
			rp.setHowSortAscending(false);
			// chart
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			// updated to 2, since there were only 2 users in 'site-a-id' at the time with matching events
			assertEquals(2, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(1, rep.getReportData().size());
			rp.setWhoUserIds(listNoUsers);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
		
		// MiniStatFiles (files with new event)
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			// what
			rp.setWhat(ReportManager.WHAT_RESOURCES);
			rp.setWhatLimitedAction(true);
			rp.setWhatResourceAction(ReportManager.WHAT_RESOURCES_ACTION_NEW);
			rp.setWhatLimitedResourceIds(true);
			rp.setWhatResourceIds(Arrays.asList(StatsManager.RESOURCES_DIR + FakeData.SITE_A_ID + "/"));
			// when
			rp.setWhen(ReportManager.WHEN_ALL);
			// who
			rp.setWho(ReportManager.WHO_ALL);
			// grouping
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_RESOURCE);
			rp.setHowTotalsBy(totalsBy);
			// sorting
			rp.setHowSort(true);
			rp.setHowSortBy(StatsManager.T_RESOURCE);
			rp.setHowSortAscending(true);
			// chart
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(1, rep.getReportData().size());
			rp.setWhoUserIds(listNoUsers);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
		
		// MiniStatOpenedFiles (files with read event)
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			// what
			rp.setWhat(ReportManager.WHAT_RESOURCES);
			rp.setWhatLimitedAction(true);
			rp.setWhatResourceAction(ReportManager.WHAT_RESOURCES_ACTION_READ);
			rp.setWhatLimitedResourceIds(true);
			rp.setWhatResourceIds(Arrays.asList(StatsManager.RESOURCES_DIR + FakeData.SITE_A_ID + "/"));
			// when
			rp.setWhen(ReportManager.WHEN_ALL);
			// who
			rp.setWho(ReportManager.WHO_ALL);
			// grouping
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_RESOURCE);
			rp.setHowTotalsBy(totalsBy);
			// sorting
			rp.setHowSort(true);
			rp.setHowSortBy(StatsManager.T_TOTAL);
			rp.setHowSortAscending(false);
			// chart
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(1, rep.getReportData().size());
			rp.setWhoUserIds(listNoUsers);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
		
		// MiniStatUserThatOpenedMoreFiles
		{
			ReportDef r = new ReportDef();
			r.setId(0);
			r.setSiteId(FakeData.SITE_A_ID);
			ReportParams rp = new ReportParams(FakeData.SITE_A_ID);
			// what
			rp.setWhat(ReportManager.WHAT_RESOURCES);
			rp.setWhatLimitedAction(true);
			rp.setWhatResourceAction(ReportManager.WHAT_RESOURCES_ACTION_READ);
			rp.setWhatLimitedResourceIds(true);
			rp.setWhatResourceIds(Arrays.asList(StatsManager.RESOURCES_DIR + FakeData.SITE_A_ID + "/"));
			// when
			rp.setWhen(ReportManager.WHEN_ALL);
			// who
			rp.setWho(ReportManager.WHO_ALL);
			// grouping
			List<String> totalsBy = new ArrayList<String>();
			totalsBy.add(StatsManager.T_USER);
			rp.setHowTotalsBy(totalsBy);
			// sorting
			rp.setHowSort(true);
			rp.setHowSortBy(StatsManager.T_TOTAL);
			rp.setHowSortAscending(false);
			// chart
			rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
			r.setReportParams(rp);
			Report rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
			rp.setWho(ReportManager.WHO_CUSTOM);
			rp.setWhoUserIds(listUserA);
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			rp.setWhoUserIds(listNoUsers);
			assertEquals(1, rep.getReportData().size());
			rep = reportManager.getReport(r, false);
			assertNotNull(rep);
			assertEquals(0, rep.getReportData().size());
		}
	}

	@Test
	public void testLoadSaveReports() {
		String siteId = null;
		Report r = null;
		ReportParams rp = null;
		List<String> totalsBy = null;
		
		siteId = FakeData.SITE_A_ID;
		ReportDef rd = new ReportDef();
		rd.setId(0);
		rd.setSiteId(siteId);
		rd.setCreatedBy(FakeData.USER_A_ID);
		rd.setModifiedBy(FakeData.USER_A_ID);
		rd.setTitle("Title 1");
		rp = new ReportParams(siteId);
		rp.setWhat(ReportManager.WHAT_RESOURCES);
		rp.setWhen(ReportManager.WHEN_ALL);
		rp.setWho(ReportManager.WHO_ALL);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		totalsBy.add(StatsManager.T_USER);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		rd.setReportParams(rp);

		assertNull(reportManager.getReportDefinition(100));
		// normal
		assertTrue(reportManager.saveReportDefinition(rd));
		assertNotNull(reportManager.getReportDefinition(1));
		// hidden
		ReportDef rd2 = new ReportDef();
		rd2.setId(0);
		rd2.setSiteId(siteId);
		rd2.setCreatedBy(FakeData.USER_A_ID);
		rd2.setModifiedBy(FakeData.USER_A_ID);
		rd2.setTitle("Title 2");
		rd2.setHidden(true);
		rd2.setReportParams(new ReportParams());
		assertTrue(reportManager.saveReportDefinition(rd2));
		// pre-defined
		ReportDef rd3 = new ReportDef();
		rd3.setId(0);
		rd3.setSiteId(null);
		rd3.setCreatedBy(FakeData.USER_A_ID);
		rd3.setModifiedBy(FakeData.USER_A_ID);
		rd3.setTitle("Title 3");
		rd3.setHidden(false);
		rd3.setReportParams(new ReportParams());

		Tool tool = mock(Tool.class);
		when(tool.getId()).thenReturn(StatsManager.SITESTATS_ADMIN_TOOLID);
		when(toolManager.getCurrentTool()).thenReturn(tool);

		assertTrue(reportManager.saveReportDefinition(rd3));
		
		List<ReportDef> list = reportManager.getReportDefinitions(null, true, true);
		assertNotNull(list);
		assertEquals(1, list.size());
		list = reportManager.getReportDefinitions(FakeData.SITE_A_ID, true, true);
		assertNotNull(list);
		assertEquals(3, list.size());
		list = reportManager.getReportDefinitions(FakeData.SITE_A_ID, true, false);
		assertNotNull(list);
		assertEquals(2, list.size());
		list = reportManager.getReportDefinitions(FakeData.SITE_A_ID, false, true);
		assertNotNull(list);
		assertEquals(2, list.size());
		list = reportManager.getReportDefinitions(FakeData.SITE_A_ID, false, false);
		assertNotNull(list);
		assertEquals(1, list.size());
		
		assertTrue(reportManager.removeReportDefinition(rd2));
		list = reportManager.getReportDefinitions(FakeData.SITE_A_ID, true, true);
		assertNotNull(list);
		assertEquals(2, list.size());
	}
	
	@Test
	public void testReportExporting() {
		String siteId = null;
		Report r = null;
		ReportParams rp = null;
		List<String> totalsBy = null;
		List<String> listOneUser = new ArrayList<>();
		List<String> listNoUsers = new ArrayList<>();
		listOneUser.add(FakeData.USER_A_ID);
		
		siteId = FakeData.SITE_A_ID;
		ReportDef rd = new ReportDef();
		rd.setId(0);
		rd.setSiteId(siteId);
		rd.setCreatedBy(FakeData.USER_A_ID);
		rd.setModifiedBy(FakeData.USER_A_ID);
		rd.setTitle("Title 1");
		rp = new ReportParams(siteId);
		rp.setWhat(ReportManager.WHAT_RESOURCES);
		rp.setWhen(ReportManager.WHEN_ALL);
		rp.setWho(ReportManager.WHO_CUSTOM);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		totalsBy.add(StatsManager.T_USER);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		rp.setWhoUserIds(listOneUser);
		rd.setReportParams(rp);
		Report report = reportManager.getReport(rd, false);
		assertNotNull(report);
		rp.setWhoUserIds(listNoUsers);
		report = reportManager.getReport(rd, false);
		assertNotNull(report);
		rp.setWho(ReportManager.WHO_ALL);
		report = reportManager.getReport(rd, false);
		assertNotNull(report);
		
		// CSV
		String csv = reportManager.getReportAsCsv(report);
		assertNotNull(csv);
		assertTrue(csv.length() > 0);
		
		// EXCEL
		byte[] excel = reportManager.getReportAsExcel(report, "sheetname");
		assertNotNull(excel);
		assertTrue(excel.length > 0);
		// To verify locally...
//		File file = new File("d:/sitestats-test.xls");
//		if(file.exists()) {file.delete();}
//		FileOutputStream out = null;
//		try{
//			out = new FileOutputStream(file);
//			out.write(excel);
//			out.flush();
//		}catch(FileNotFoundException e){
//			log.error(e.getMessage(), e);
//		}catch(IOException e){
//			log.error(e.getMessage(), e);
//		}finally{
//			if(out != null) {
//				try{ out.close(); }catch(IOException e){ /* IGNORE */}
//			}
//		}
		
		// PDF: currently disabled due to classloading trouble
//		byte[] pdf = M_rm.getReportAsPDF(report);
//		Assert.assertNotNull(pdf);
//		Assert.assertTrue(pdf.length > 0);
	}
	
	private void checkCollumns(ReportParams params) {
		List<String> all = Arrays.asList(
				StatsManager.T_SITE, StatsManager.T_USER, StatsManager.T_EVENT, StatsManager.T_TOOL,
				StatsManager.T_RESOURCE, StatsManager.T_RESOURCE_ACTION, StatsManager.T_DATE,
				StatsManager.T_DATEMONTH, StatsManager.T_DATEYEAR, StatsManager.T_LASTDATE, StatsManager.T_TOTAL,
				StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS, StatsManager.T_DURATION  
				);
		List<String> totalsBy = params.getHowTotalsBy();
		for(String c : all) {
			boolean containsColumn = reportManager.isReportColumnAvailable(params, c);
			boolean expected = totalsBy == null || totalsBy.contains(c) 
				|| (c.equals(StatsManager.T_TOTAL) && !ReportManager.WHAT_PRESENCES.equals(params.getWhat()))
				|| (c.equals(StatsManager.T_DURATION) && ReportManager.WHAT_PRESENCES.equals(params.getWhat()));
			assertEquals(expected, containsColumn);
		}
	}
}
