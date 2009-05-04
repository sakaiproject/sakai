package org.sakaiproject.sitestats.test;


import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.IAnswer;
import org.hibernate.Hibernate;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.impl.report.ReportManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeEventRegistryService;
import org.sakaiproject.sitestats.test.mocks.FakeEventTrackingService;
import org.sakaiproject.sitestats.test.mocks.FakeServerConfigurationService;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;


public class ReportManagerTest extends AbstractAnnotationAwareTransactionalTests { 
	// AbstractAnnotationAwareTransactionalTests / AbstractTransactionalSpringContextTests
	private ReportManager					M_rm;
	private StatsManager					M_sm;
	private StatsUpdateManager				M_sum;
	private DB								db;
	private SiteService						M_ss;
	private ToolManager						M_tm;
	private FakeEventRegistryService		M_ers;
	private FakeServerConfigurationService	M_scs;
	private ContentHostingService			M_chs;
	
	// Spring configuration	
	public void setReportManager(ReportManager M_rm) {
		this.M_rm = M_rm;
	}
	public void setStatsManager(StatsManager M_sm) {
		this.M_sm = M_sm;
	}
	public void setStatsUpdateManager(StatsUpdateManager M_sum) {
		this.M_sum = M_sum;
	}
	public void setServerConfigurationService(FakeServerConfigurationService M_scs) {
		this.M_scs = M_scs;
	}
	public void setToolManager(ToolManager M_tm) {
		this.M_tm = M_tm;
	}
	public void setEventRegistryService(FakeEventRegistryService M_ers) {
		this.M_ers = M_ers;
	}
	public void setDb(DB db) {
		this.db = db;
	}
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "hbm-db.xml", "hibernate-test.xml" };
	}

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
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
		FakeSite userSiteA = new FakeSite("~"+FakeData.USER_A_ID);
		FakeSite userSiteB = new FakeSite("~"+FakeData.USER_B_ID);
		expect(M_ss.getSiteUserId(FakeData.USER_A_ID)).andStubReturn("~"+FakeData.USER_A_ID);
		expect(M_ss.getSiteUserId(FakeData.USER_B_ID)).andStubReturn("~"+FakeData.USER_B_ID);
		expect(M_ss.getSiteUserId("no_user")).andStubReturn(null);
		expect(M_ss.getSite("~"+FakeData.USER_A_ID)).andStubReturn(userSiteA);
		expect(M_ss.getSite("~"+FakeData.USER_B_ID)).andStubReturn(userSiteB);
		
		// Site A has tools {SiteStats, Chat, Resources}, has {user-a,user-b}, created 1 month ago
		Site siteA = new FakeSite(FakeData.SITE_A_ID,
				Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID)
			);
		((FakeSite)siteA).setUsers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID,FakeData.USER_B_ID)));
		((FakeSite)siteA).setMembers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID,FakeData.USER_B_ID)));
		expect(M_ss.getSite(FakeData.SITE_A_ID)).andStubReturn(siteA);
		expect(M_ss.isUserSite(FakeData.SITE_A_ID)).andStubReturn(false);
		expect(M_ss.isSpecialSite(FakeData.SITE_A_ID)).andStubReturn(false);
		//expect(siteA.getCreatedTime()).andStubReturn(timeA).anyTimes();
		expect(siteA.getCreatedTime()).andStubReturn((Time)anyObject());
		
		// Site B has tools {TOOL_CHAT}, has {user-a}, created 2 months ago
		FakeSite siteB = new FakeSite(FakeData.SITE_B_ID, FakeData.TOOL_CHAT);
		((FakeSite)siteB).setUsers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID)));
		((FakeSite)siteB).setMembers(new HashSet<String>(Arrays.asList(FakeData.USER_A_ID)));
		expect(M_ss.getSite(FakeData.SITE_B_ID)).andStubReturn(siteB);
		expect(M_ss.isUserSite(FakeData.SITE_B_ID)).andStubReturn(false);
		expect(M_ss.isSpecialSite(FakeData.SITE_B_ID)).andStubReturn(false);	
		//expect(siteB.getCreatedTime()).andStubReturn(timeB).anyTimes();
		expect(siteB.getCreatedTime()).andStubReturn((Time)anyObject());
		
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
		((ReportManagerImpl)M_rm).setEventRegistryService(M_ers);
		((StatsManagerImpl)M_sm).setSiteService(M_ss);
		//((StatsManagerImpl)M_sm).setContentHostingService(M_chs);
		((StatsManagerImpl)M_sm).setResourceLoader(msgs);
		((ReportManagerImpl)M_rm).setResourceLoader(msgs);
		((StatsUpdateManagerImpl)M_sum).setSiteService(M_ss);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);
	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		db.deleteAll();
	}

	
	// ---- SAMPLE DATA ----
	
	private List<Event> getSampleData() {
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
		assertEquals(1, r.getReportData().size());
		r = M_rm.getReport(rd, false);
		checkCollumns(rd.getReportParams());
		assertEquals(2, r.getReportData().size());
		assertNotNull(M_rm.getReportFormattedParams());
		
		// #2 getReportRowCount(ReportDef reportDef, boolean restrictToToolsInSite)
		assertEquals(1, M_rm.getReportRowCount(rd, true));
		assertEquals(2, M_rm.getReportRowCount(rd, false));
		
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
		assertEquals(1, M_rm.getReportRowCount(rd, true));
		assertEquals(1, M_rm.getReportRowCount(rd, false));
	}
	
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
		assertEquals(1, r.getReportData().size());
		
		// visits
		rp.setWhat(ReportManager.WHAT_VISITS);
		r = M_rm.getReport(rd, true, null, true);
		checkCollumns(rd.getReportParams());
		assertEquals(2, r.getReportData().size());
		
		// visits totals
		rp.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		totalsBy = new ArrayList<String>();
		totalsBy.add(StatsManager.T_SITE);
		rp.setHowTotalsBy(totalsBy);
		rd.setId(1);
		r = M_rm.getReport(rd, true, new PagingPosition(0,5), true);
		assertEquals(1, r.getReportData().size());
		assertEquals(9, ((SiteVisits)(r.getReportData().get(0))).getTotalVisits());
		assertEquals(7, ((SiteVisits)(r.getReportData().get(0))).getTotalUnique());
		
//		// activity totals
//		rp.setWhat(ReportManager.WHAT_ACTIVITY_TOTALS);
//		rp.setWhatEventIds(FakeData.EVENTIDS);
//		rp.setWhen(ReportManager.WHEN_LAST365DAYS);
//		r = M_rm.getReport(rd, false, null, false);
//		System.out.println(r.getReportData());
//		System.out.println("ReportParams: "+ rp);
//		System.out.println("ReportData: "+ r.getReportData());
//		assertEquals(1, r.getReportData().size());
//		assertEquals(1, r.getReportData().get(0).getCount());
		
	}
	
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
			assertNotNull(rep);
			assertEquals(5, rep.getReportData().size());
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
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
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
			rp.setWhatEventIds(M_ers.getEventIds());
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
			assertNotNull(rep);
			assertEquals(6, rep.getReportData().size());
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
			rp.setWhatEventIds(M_ers.getEventIds());
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
			assertNotNull(rep);
			assertEquals(3, rep.getReportData().size());
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
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
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
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
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
			assertNotNull(rep);
			assertEquals(2, rep.getReportData().size());
		}
	}

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
			assertNull(M_rm.getReportDefinition(100));
		}catch(Exception e) {
			assertTrue(true);
		}
		// normal
		assertTrue(M_rm.saveReportDefinition(rd));
		assertNotNull(M_rm.getReportDefinition(1));		
		// hidden
		ReportDef rd2 = new ReportDef();
		rd2.setId(0);
		rd2.setSiteId(siteId);
		rd2.setCreatedBy(FakeData.USER_A_ID);
		rd2.setModifiedBy(FakeData.USER_A_ID);
		rd2.setTitle("Title 2");
		rd2.setHidden(true);
		rd2.setReportParams(new ReportParams());
		assertTrue(M_rm.saveReportDefinition(rd2));
		// pre-defined
		ReportDef rd3 = new ReportDef();
		rd3.setId(0);
		rd3.setSiteId(null);
		rd3.setCreatedBy(FakeData.USER_A_ID);
		rd3.setModifiedBy(FakeData.USER_A_ID);
		rd3.setTitle("Title 3");
		rd3.setHidden(false);
		rd3.setReportParams(new ReportParams());
		assertTrue(M_rm.saveReportDefinition(rd3));
		
		List<ReportDef> list = M_rm.getReportDefinitions(null, true, true); 
		assertNotNull(list);
		assertEquals(1, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, true, true); 
		assertNotNull(list);
		assertEquals(3, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, true, false); 
		assertNotNull(list);
		assertEquals(2, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, false, true); 
		assertNotNull(list);
		assertEquals(2, list.size());
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, false, false); 
		assertNotNull(list);
		assertEquals(1, list.size());
		
		assertTrue(M_rm.removeReportDefinition(rd2));
		list = M_rm.getReportDefinitions(FakeData.SITE_A_ID, true, true); 
		assertNotNull(list);
		assertEquals(2, list.size());
	}
	
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
		assertNotNull(report);
		
		String csv = M_rm.getReportAsCsv(report);
		assertNotNull(csv);
		assertTrue(csv.length() > 0);
		
		// Currently disabled due to classloading trouble
//		byte[] excel = M_rm.getReportAsExcel(report, "sheetname");
//		assertNotNull(excel);
//		assertTrue(excel.length > 0);
//		
//		byte[] pdf = M_rm.getReportAsPDF(report);
//		assertNotNull(pdf);
//		assertTrue(pdf.length > 0);
	}
	
	private void checkCollumns(ReportParams params) {
		List<String> all = Arrays.asList(
				StatsManager.T_SITE, StatsManager.T_USER, StatsManager.T_EVENT, StatsManager.T_TOOL,
				StatsManager.T_RESOURCE, StatsManager.T_RESOURCE_ACTION, StatsManager.T_DATE,
				StatsManager.T_DATEMONTH, StatsManager.T_DATEYEAR, StatsManager.T_LASTDATE, StatsManager.T_TOTAL,
				StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS 
				);
		List<String> totalsBy = params.getHowTotalsBy();
		for(String c : all) {
			boolean containsColumn = M_rm.isReportColumnAvailable(params, c);
			boolean expected = totalsBy == null || totalsBy.contains(c) || c.equals(StatsManager.T_TOTAL);
			//System.out.println("containsColumn("+c+"): "+containsColumn+" expected: "+expected);
			assertEquals(expected, containsColumn);
		}
	}
}
