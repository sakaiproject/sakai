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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.impl.report.ReportManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeEventRegistryService;
import org.sakaiproject.sitestats.test.mocks.FakeServerConfigurationService;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

import javax.annotation.Resource;

@ContextConfiguration(locations = {"/hibernate-test.xml"})
@Slf4j
public class ReportManagerTest extends AbstractJUnit4SpringContextTests {

	@Resource(name = "org.sakaiproject.sitestats.test.ReportManager")
	private ReportManager					M_rm;
	@Resource(name = "org.sakaiproject.sitestats.test.StatsManager")
	private StatsManager					M_sm;
	@Resource(name = "org.sakaiproject.sitestats.test.StatsUpdateManager")
	private StatsUpdateManager				M_sum;
	@Autowired
	private StatsAuthz						M_sa;
	@Resource(name = "org.sakaiproject.sitestats.test.DB")
	private DB								db;
	private SiteService						M_ss;
	@Autowired
	private ToolManager						M_tm;
	@Autowired
	private FakeEventRegistryService		M_ers;
	@Autowired
	private FakeServerConfigurationService	M_scs;
	private ContentHostingService			M_chs;

	@Before
	public void onSetUp() throws Exception {
		db.deleteAll();
		// Time
		/*long oneMonthAgoMs = new Date().getTime() - (30 * 24 * 60 * 60 * 1000);
		long twoMonthAgoMs = new Date().getTime() - (2 * 30 * 24 * 60 * 60 * 1000);
		Time timeA = createMock(Time.class);
		expect(timeA.getTime()).andReturn(oneMonthAgoMs).anyTimes();
		replay(timeA);
		Time timeB = createMock(Time.class);
		expect(timeB.getTime()).andReturn(twoMonthAgoMs).anyTimes();
		replay(timeB);*/
		
		// Site Service
		M_ss = createMock(SiteService.class);
		
		// null site
		expect(M_ss.getSite(null)).andThrow(new IdUnusedException("null")).anyTimes();
		expect(M_ss.getSite("non_existent_site")).andThrow(new IdUnusedException("non_existent_site")).anyTimes();
		
		// My Workspace - user sites
		FakeSite userSiteA = Mockito.spy(FakeSite.class).set("~"+FakeData.USER_A_ID);
		FakeSite userSiteB = Mockito.spy(FakeSite.class).set("~"+FakeData.USER_B_ID);
		expect(M_ss.getSiteUserId(FakeData.USER_A_ID)).andStubReturn("~"+FakeData.USER_A_ID);
		expect(M_ss.getSiteUserId(FakeData.USER_B_ID)).andStubReturn("~"+FakeData.USER_B_ID);
		expect(M_ss.getSiteUserId("no_user")).andStubReturn(null);
		expect(M_ss.getSite("~"+FakeData.USER_A_ID)).andStubReturn(userSiteA);
		expect(M_ss.getSite("~"+FakeData.USER_B_ID)).andStubReturn(userSiteB);
		
		// Site A has tools {SiteStats, Chat, Resources}, has {user-a,user-b}, created 1 month ago
		Site siteA = Mockito.spy(FakeSite.class).set(FakeData.SITE_A_ID,
				Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID)
			);
		((FakeSite)siteA).setUsers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID,FakeData.USER_B_ID)));
		((FakeSite)siteA).setMembers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID,FakeData.USER_B_ID)));
		expect(M_ss.getSite(FakeData.SITE_A_ID)).andStubReturn(siteA);
		expect(M_ss.isUserSite(FakeData.SITE_A_ID)).andStubReturn(false);
		expect(M_ss.isSpecialSite(FakeData.SITE_A_ID)).andStubReturn(false);
		
		// Site B has tools {TOOL_CHAT}, has {user-a}, created 2 months ago
		FakeSite siteB = Mockito.spy(FakeSite.class).set(FakeData.SITE_B_ID, FakeData.TOOL_CHAT);
		((FakeSite)siteB).setUsers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID)));
		((FakeSite)siteB).setMembers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID)));
		expect(M_ss.getSite(FakeData.SITE_B_ID)).andStubReturn(siteB);
		expect(M_ss.isUserSite(FakeData.SITE_B_ID)).andStubReturn(false);
		expect(M_ss.isSpecialSite(FakeData.SITE_B_ID)).andStubReturn(false);
		
		// Site 'non_existent_site' doesn't exist
		expect(M_ss.isUserSite("non_existent_site")).andStubReturn(false);
		expect(M_ss.isSpecialSite("non_existent_site")).andStubReturn(false);
		
		// Content Hosting Service
//		M_chs = createMock(ContentHostingService.class);
//		M_chs.checkCollection("/group/site-a-id/folder/");
//		expectLastCall().anyTimes();
//		M_chs.checkResource("/group-user/site-a-id/user-a/resource1");
//		expectLastCall().anyTimes();
		
		ResourceLoader msgs = createMock(ResourceLoader.class);
		expect(msgs.getString((String) anyObject())).andStubAnswer(new IAnswer<String>() {
			public String answer() throws Throwable {
				return (String) getCurrentArguments()[0];
			}			
		});
		
		// apply
		replay(M_ss);
		//replay(M_chs);
		replay(msgs);
		((FakeEventRegistryService)M_ers).setSiteService(M_ss);
		((FakeEventRegistryService)M_ers).setToolManager(M_tm);
		((FakeEventRegistryService)M_ers).setStatsManager(M_sm);
		ReportManagerImpl rmi = (ReportManagerImpl) ((Advised) M_rm).getTargetSource().getTarget();
		StatsManagerImpl smi = (StatsManagerImpl) ((Advised) M_sm).getTargetSource().getTarget();
		StatsUpdateManagerImpl sumi = (StatsUpdateManagerImpl) ((Advised) M_sum).getTargetSource().getTarget();
		rmi.setEventRegistryService(M_ers);
		smi.setSiteService(M_ss);
		//((StatsManagerImpl)M_sm).setContentHostingService(M_chs);
		smi.setResourceLoader(msgs);
		smi.setEnableSitePresences(true);
		rmi.setResourceLoader(msgs);
		sumi.setSiteService(M_ss);
		sumi.setStatsManager(M_sm);
		// This is needed to make the tests deterministic, otherwise on occasion the collect thread will run
		// and break the tests.
		M_sum.setCollectThreadEnabled(false);
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
		Event vATodayS1 = M_sum.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATodayE1 = M_sum.buildEvent(today_2, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATodayS2 = M_sum.buildEvent(today_3, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATodayE2 = M_sum.buildEvent(today_4, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBTodayS1 = M_sum.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vBTodayE1 = M_sum.buildEvent(today_3, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vAOneDayBefore = M_sum.buildEvent(oneDayBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATowDaysBefore = M_sum.buildEvent(twoDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vAFourDaysBefore = M_sum.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBFourDaysBefore = M_sum.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vBSixDaysBefore = M_sum.buildEvent(sixDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
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
		Event aAToday = M_sum.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_B_ID, FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBToday = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", FakeData.SITE_B_ID, FakeData.USER_B_ID, "session-id-b");
		Event aAOneDayBefore = M_sum.buildEvent(oneDayBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aATowDaysBefore = M_sum.buildEvent(twoDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aAFourDaysBefore = M_sum.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBFourDaysBefore = M_sum.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event aBSixDaysBefore = M_sum.buildEvent(sixDaysBefore, FakeData.EVENT_CONTENTREV, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
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
		Event vAToday = M_sum.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBToday = M_sum.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vAOneDayBefore = M_sum.buildEvent(oneDayBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vATowDaysBefore = M_sum.buildEvent(twoDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vAFourDaysBefore = M_sum.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event vBFourDaysBefore = M_sum.buildEvent(fourDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Event vBSixDaysBefore = M_sum.buildEvent(sixDaysBefore, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		samples.addAll(Arrays.asList(
				vAToday, vAToday, vBToday, 			// today:			3 visits, 2 unique
				vAOneDayBefore, 					// 1 day before:	1 visits, 1 unique
				vATowDaysBefore, vATowDaysBefore, 	// 2 day before:	2 visits, 1 unique
				vAFourDaysBefore, vBFourDaysBefore, // 4 day before:	2 visits, 2 unique
				vBSixDaysBefore						// 6 day before:	1 visits, 1 unique
				));
		// activity
		Event aAToday = M_sum.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_B_ID, FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBToday = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", FakeData.SITE_B_ID, FakeData.USER_B_ID, "session-id-b");
		Event aAOneDayBefore = M_sum.buildEvent(oneDayBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aATowDaysBefore = M_sum.buildEvent(twoDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aAFourDaysBefore = M_sum.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBFourDaysBefore = M_sum.buildEvent(fourDaysBefore, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event aBSixDaysBefore = M_sum.buildEvent(sixDaysBefore, FakeData.EVENT_CONTENTREV, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		samples.addAll(Arrays.asList(
				aAToday, aBToday,  					// today:			2 (1 chat + 1 content)
				aAOneDayBefore, 					// 1 day before:	1 (1 chat)
				aATowDaysBefore, aATowDaysBefore, 	// 2 day before:	2 (2 chat)
				aAFourDaysBefore, aBFourDaysBefore, // 4 day before:	2 (2 chat)
				aBSixDaysBefore						// 6 day before:	1 (1 chat)
				));
		// resources
		Event r1 = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r1a = M_sum.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r1b = M_sum.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r1c = M_sum.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r2 = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r2a = M_sum.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r2b = M_sum.buildEvent(today, FakeData.EVENT_CONTENTREAD, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event r3 = M_sum.buildEvent(today, FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event r4 = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id2", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		// resources: attachment
		Event r5 = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, FakeData.RES_ATTACH, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		// resources: dropbox
		Event r6 = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, FakeData.RES_DROPBOX_SITE_A_USER_A_FILE, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		
		samples.addAll(Arrays.asList( r1, r1a, r1b, r1c, r2, r2a, r2b, r3, r4, r5, r6 ));
		return samples;
	}

	
	// ---- TESTS ----
	@Test
	public void testGetReport() {
		M_sum.collectEvents(getSampleData());
		String siteId = null;
		Report r = null;
		ReportDef rd = null;
		ReportParams rp = null;
		List<String> totalsBy = null;
		
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
		r = M_rm.getReport(rd, true);
		checkCollumns(rd.getReportParams());
		Assert.assertEquals(1, r.getReportData().size());
		r = M_rm.getReport(rd, false);
		checkCollumns(rd.getReportParams());
		Assert.assertEquals(2, r.getReportData().size());
		Assert.assertNotNull(M_rm.getReportFormattedParams());
		
		// #2 getReportRowCount(ReportDef reportDef, boolean restrictToToolsInSite)
		Assert.assertEquals(1, M_rm.getReportRowCount(rd, true));
		Assert.assertEquals(2, M_rm.getReportRowCount(rd, false));
		
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
		Assert.assertEquals(1, M_rm.getReportRowCount(rd, true));
		Assert.assertEquals(1, M_rm.getReportRowCount(rd, false));
	}
	
	@Test
	@Ignore		// TODO JUNIT test is not working on hsqldb need to look into
	public void testGetMoreReports() {
		M_sum.collectEvents(getSampleData());
		String siteId = null;
		Report r = null;
		ReportDef rd = null;
		ReportParams rp = null;
		List<String> totalsBy = null;
		
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
		r = M_rm.getReport(rd, true, null, false);
		checkCollumns(rd.getReportParams());
		Assert.assertEquals(1, r.getReportData().size());
		
		// visits
		rp.setWhat(ReportManager.WHAT_VISITS);
		r = M_rm.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		Assert.assertEquals(2, r.getReportData().size());
		
		// visits totals
		rp.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		rp.setHowTotalsBy(totalsBy);
		rd.setId(1);
		r = M_rm.getReport(rd, true, new PagingPosition(0,5), true);
		Assert.assertEquals(1, r.getReportData().size());
		Assert.assertEquals(9, ((SiteVisits)(r.getReportData().get(0))).getTotalVisits());
		Assert.assertEquals(7, ((SiteVisits)(r.getReportData().get(0))).getTotalUnique());
		
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
		r = M_rm.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		Assert.assertEquals(7, r.getReportData().size());
		
		// presences II
		rp.setWhat(ReportManager.WHAT_PRESENCES);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		totalsBy.add(StatsManager.T_DATE);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		r = M_rm.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		Assert.assertEquals(5, r.getReportData().size());
		
		// presences III
		rp.setWhat(ReportManager.WHAT_PRESENCES);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		rp.setHowTotalsBy(totalsBy);
		rp.setHowSort(false);
		r = M_rm.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		Assert.assertEquals(1, r.getReportData().size());
		
	}
	
	@Test
	@Ignore        // TODO JUNIT test is not working on hsqldb need to look into
	public void testReportsFromOverviewPage() {
		M_sum.collectEvents(getSampleData2());
		
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			Assert.assertEquals(5, rep.getReportData().size());
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			Assert.assertEquals(2, rep.getReportData().size());
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			Assert.assertEquals(0, rep.getReportData().size());
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
			rp.setWhatEventIds(new ArrayList<String>(M_ers.getEventIds()));
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			Assert.assertEquals(6, rep.getReportData().size());
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
			rp.setWhatEventIds(new ArrayList<String>(M_ers.getEventIds()));
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			// updated to 2, since there were only 2 users in 'site-a-id' at the time with matching events
			Assert.assertEquals(2, rep.getReportData().size());
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			Assert.assertEquals(2, rep.getReportData().size());
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			Assert.assertEquals(2, rep.getReportData().size());
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
			Report rep = M_rm.getReport(r, false);
			Assert.assertNotNull(rep);
			Assert.assertEquals(2, rep.getReportData().size());
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

		// non-existent
		try{
			Assert.assertNull(M_rm.getReportDefinition(100));
		}catch(Exception e) {
			Assert.assertTrue(true);
		}
		// normal
		Assert.assertTrue(M_rm.saveReportDefinition(rd));
		Assert.assertNotNull(M_rm.getReportDefinition(1));		
		// hidden
		ReportDef rd2 = new ReportDef();
		rd2.setId(0);
		rd2.setSiteId(siteId);
		rd2.setCreatedBy(FakeData.USER_A_ID);
		rd2.setModifiedBy(FakeData.USER_A_ID);
		rd2.setTitle("Title 2");
		rd2.setHidden(true);
		rd2.setReportParams(new ReportParams());
		Assert.assertTrue(M_rm.saveReportDefinition(rd2));
		// pre-defined
		ReportDef rd3 = new ReportDef();
		rd3.setId(0);
		rd3.setSiteId(null);
		rd3.setCreatedBy(FakeData.USER_A_ID);
		rd3.setModifiedBy(FakeData.USER_A_ID);
		rd3.setTitle("Title 3");
		rd3.setHidden(false);
		rd3.setReportParams(new ReportParams());
		Mockito.when(M_sa.isSiteStatsAdminPage()).thenReturn(true);
		Assert.assertTrue(M_rm.saveReportDefinition(rd3));
		
		List<ReportDef> list = M_rm.getReportDefinitions(null, true, true); 
		Assert.assertNotNull(list);
		Assert.assertEquals(1, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, true, true); 
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, true, false); 
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, false, true); 
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, false, false); 
		Assert.assertNotNull(list);
		Assert.assertEquals(1, list.size());
		
		Assert.assertTrue(M_rm.removeReportDefinition(rd2));
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, true, true); 
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
	}
	
	@Test
	public void testReportExporting() {
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
		Report report = M_rm.getReport(rd, false);
		Assert.assertNotNull(report);
		
		// CSV
		String csv = M_rm.getReportAsCsv(report);
		Assert.assertNotNull(csv);
		Assert.assertTrue(csv.length() > 0);
		
		// EXCEL
		byte[] excel = M_rm.getReportAsExcel(report, "sheetname");
		Assert.assertNotNull(excel);
		Assert.assertTrue(excel.length > 0);
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
			boolean containsColumn = M_rm.isReportColumnAvailable(params, c);
			boolean expected = totalsBy == null || totalsBy.contains(c) 
				|| (c.equals(StatsManager.T_TOTAL) && !ReportManager.WHAT_PRESENCES.equals(params.getWhat()))
				|| (c.equals(StatsManager.T_DURATION) && ReportManager.WHAT_PRESENCES.equals(params.getWhat()));
			Assert.assertEquals(expected, containsColumn);
		}
	}
}
