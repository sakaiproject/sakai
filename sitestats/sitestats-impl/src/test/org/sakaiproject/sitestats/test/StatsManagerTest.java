package org.sakaiproject.sitestats.test;


import static org.easymock.classextension.EasyMock.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeServerConfigurationService;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;


public class StatsManagerTest extends AbstractAnnotationAwareTransactionalTests { 
	// AbstractAnnotationAwareTransactionalTests / AbstractTransactionalSpringContextTests
	private StatsManager					M_sm;
	private StatsUpdateManager		M_sum;
	private DB								db;
	private SiteService						M_ss;
	private FakeServerConfigurationService	M_scs;
	private ContentHostingService			M_chs;
	private ContentTypeImageService			M_ctis;
	
	// Spring configuration	
	public void setStatsManager(StatsManager M_sm) {
		this.M_sm = M_sm;
	}
	public void setStatsUpdateManager(StatsUpdateManager M_sum) {
		this.M_sum = M_sum;
	}
	public void setServerConfigurationService(FakeServerConfigurationService M_scs) {
		this.M_scs = M_scs;
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
		
		// Site A has tools {SiteStats, Chat}, has {user-a,user-b}, created 1 month ago
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
		M_chs = createMock(ContentHostingService.class);
		M_chs.checkCollection("/group/site-a-id/folder/");
		expectLastCall().anyTimes();
		M_chs.checkResource("/group-user/site-a-id/user-a/resource1");
		expectLastCall().anyTimes();
		M_ctis = createMock(ContentTypeImageService.class);
		expect(M_ctis.getContentTypeImage("folder")).andStubReturn("sakai/folder.gif");
		expect(M_ctis.getContentTypeImage("image/png")).andStubReturn("sakai/image.gif");
		
		ResourceLoader msgs = createMock(ResourceLoader.class);
		expect(msgs.getString("report_content_attachments")).andStubReturn("Attachments");
		
		// apply
		replay(M_ss);
		replay(M_chs);
		replay(msgs);
		replay(M_ctis);
		((StatsManagerImpl)M_sm).setSiteService(M_ss);
		((StatsManagerImpl)M_sm).setContentHostingService(M_chs);
		((StatsManagerImpl)M_sm).setContentTypeImageService(M_ctis);
		((StatsManagerImpl)M_sm).setResourceLoader(msgs);
		((StatsUpdateManagerImpl)M_sum).setSiteService(M_ss);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);
	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		//db.deleteAll();
	}

	
	// ---- TESTS ----
	
	public void testEnableVisibleSiteVisits() {
		M_scs.setProperty("display.users.present", "true");
		M_scs.setProperty("presence.events.log", "true");
		((StatsManagerImpl)M_sm).setEnableSiteVisits(null);
		((StatsManagerImpl)M_sm).setVisitsInfoAvailable(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(true, M_sm.isEnableSiteVisits());
		assertEquals(true, M_sm.isVisitsInfoAvailable());
		
		M_scs.setProperty("display.users.present", "false");
		M_scs.setProperty("presence.events.log", "true");
		((StatsManagerImpl)M_sm).setEnableSiteVisits(null);
		((StatsManagerImpl)M_sm).setVisitsInfoAvailable(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(true, M_sm.isEnableSiteVisits());
		assertEquals(true, M_sm.isVisitsInfoAvailable());
		
		M_scs.setProperty("display.users.present", "true");
		M_scs.setProperty("presence.events.log", "false");
		((StatsManagerImpl)M_sm).setEnableSiteVisits(null);
		((StatsManagerImpl)M_sm).setVisitsInfoAvailable(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(true, M_sm.isEnableSiteVisits());
		assertEquals(true, M_sm.isVisitsInfoAvailable());
		
		M_scs.setProperty("display.users.present", "false");
		M_scs.setProperty("presence.events.log", "false");
		((StatsManagerImpl)M_sm).setEnableSiteVisits(null);
		((StatsManagerImpl)M_sm).setVisitsInfoAvailable(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(false, M_sm.isEnableSiteVisits());
		assertEquals(false, M_sm.isVisitsInfoAvailable());
		
		M_scs.removeProperty("display.users.present");
		M_scs.removeProperty("presence.events.log");
		((StatsManagerImpl)M_sm).setEnableSiteVisits(null);
		((StatsManagerImpl)M_sm).setVisitsInfoAvailable(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(false, M_sm.isEnableSiteVisits());
		assertEquals(false, M_sm.isVisitsInfoAvailable());
		
		// revert
		M_scs.setProperty("display.users.present", "false");
		M_scs.setProperty("presence.events.log", "true");
		((StatsManagerImpl)M_sm).setEnableSiteVisits(null);
		((StatsManagerImpl)M_sm).setVisitsInfoAvailable(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(true, M_sm.isEnableSiteVisits());
		assertEquals(true, M_sm.isVisitsInfoAvailable());
	}
	
	public void testOtherConfig() {
		// isEnableSiteActivity
		((StatsManagerImpl)M_sm).setEnableSiteActivity(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(true, M_sm.isEnableSiteActivity());
		((StatsManagerImpl)M_sm).setEnableSiteActivity(false);
		assertEquals(false, M_sm.isEnableSiteActivity());
		((StatsManagerImpl)M_sm).setEnableSiteActivity(true);
		assertEquals(true, M_sm.isEnableSiteActivity());
		// isServerWideStatsEnabled
		((StatsManagerImpl)M_sm).setServerWideStatsEnabled(false);
		assertEquals(false, M_sm.isServerWideStatsEnabled());
		((StatsManagerImpl)M_sm).setServerWideStatsEnabled(true);
		assertEquals(true, M_sm.isServerWideStatsEnabled());
		// ChartBackgroundColor
		((StatsManagerImpl)M_sm).setChartBackgroundColor("#000");
		assertEquals("#000", M_sm.getChartBackgroundColor());
		((StatsManagerImpl)M_sm).setChartBackgroundColor("#fff");
		assertEquals("#fff", M_sm.getChartBackgroundColor());
		// isChartIn3D
		((StatsManagerImpl)M_sm).setChartIn3D(false);
		assertEquals(false, M_sm.isChartIn3D());
		((StatsManagerImpl)M_sm).setChartIn3D(true);
		assertEquals(true, M_sm.isChartIn3D());
		// ChartTransparency
		((StatsManagerImpl)M_sm).setChartTransparency(0.5f);
		assertEquals(0.5f, M_sm.getChartTransparency());
		((StatsManagerImpl)M_sm).setChartTransparency(1.0f);
		assertEquals(1.0f, M_sm.getChartTransparency());
		// isItemLabelsVisible
		((StatsManagerImpl)M_sm).setItemLabelsVisible(false);
		assertEquals(false, M_sm.isItemLabelsVisible());
		((StatsManagerImpl)M_sm).setItemLabelsVisible(true);
		assertEquals(true, M_sm.isItemLabelsVisible());
		// isShowAnonymousAccessEvents
		((StatsManagerImpl)M_sm).setShowAnonymousAccessEvents(false);
		assertEquals(false, M_sm.isShowAnonymousAccessEvents());
		((StatsManagerImpl)M_sm).setShowAnonymousAccessEvents(true);
		assertEquals(true, M_sm.isShowAnonymousAccessEvents());
		// isLastJobRunDateVisible
		((StatsManagerImpl)M_sm).setLastJobRunDateVisible(false);
		assertEquals(false, M_sm.isLastJobRunDateVisible());
		((StatsManagerImpl)M_sm).setLastJobRunDateVisible(true);
		assertEquals(true, M_sm.isLastJobRunDateVisible());
	}
	
	public void testPreferences() {
		// invalid
		boolean thrown;
		try{
			thrown = false;
			M_sm.getPreferences(null, true);
		}catch(IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
		try{
			thrown = false;
			M_sm.getPreferences(null, false);
		}catch(IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
		try{
			thrown = false;
			M_sm.setPreferences(null, null);
		}catch(IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
		try{
			thrown = false;
			M_sm.setPreferences(null, new PrefsData());
		}catch(IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
		try{
			thrown = false;
			M_sm.setPreferences(FakeData.SITE_A_ID, null);
		}catch(IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
		
		// get inexistent preferences (will return default)
		assertNotNull(M_sm.getPreferences(FakeData.SITE_A_ID, false));
		assertNotNull(M_sm.getPreferences(FakeData.SITE_A_ID, true));
		
		// #1 add/get preferences
		PrefsData p1 = new PrefsData();
		p1.setChartIn3D(true);
		p1.setChartTransparency(0.5f);
		p1.setItemLabelsVisible(true);
		p1.setListToolEventsOnlyAvailableInSite(true);
		p1.setUseAllTools(true);
		assertTrue(M_sm.setPreferences(FakeData.SITE_A_ID, p1));
		PrefsData p1L = M_sm.getPreferences(FakeData.SITE_A_ID, false);
		assertNotNull(p1L);
		assertEquals(p1.isChartIn3D(), p1L.isChartIn3D());
		assertEquals(p1.getChartTransparency(), p1L.getChartTransparency());
		assertEquals(p1.isItemLabelsVisible(), p1L.isItemLabelsVisible());
		assertEquals(p1.isListToolEventsOnlyAvailableInSite(), p1L.isListToolEventsOnlyAvailableInSite());
		assertEquals(p1.isUseAllTools(), p1L.isUseAllTools());
		assertEquals(FakeData.EVENT_REGISTRY, p1L.getToolEventsDef());
		
		// #2 add/get preferences
		PrefsData p2 = new PrefsData();
		p2.setChartIn3D(false);
		p2.setChartTransparency(0.8f);
		p2.setItemLabelsVisible(false);
		p2.setListToolEventsOnlyAvailableInSite(false);
		p2.setToolEventsDef(FakeData.EVENT_REGISTRY);
		p2.setUseAllTools(false);
		assertTrue(M_sm.setPreferences(FakeData.SITE_A_ID, p2));
		PrefsData p2L = M_sm.getPreferences(FakeData.SITE_A_ID, false);
		assertNotNull(p2L);
		assertEquals(p2.isChartIn3D(), p2L.isChartIn3D());
		assertEquals(p2.getChartTransparency(), p2L.getChartTransparency());
		assertEquals(p2.isItemLabelsVisible(), p2L.isItemLabelsVisible());
		assertEquals(p2.isListToolEventsOnlyAvailableInSite(), p2L.isListToolEventsOnlyAvailableInSite());
		assertEquals(p2.isUseAllTools(), p2L.isUseAllTools());
		assertEquals(FakeData.EVENT_REGISTRY, p2L.getToolEventsDef());
		
		// #3 get default/edit/set/get preferences
		PrefsData p3 = M_sm.getPreferences(FakeData.SITE_B_ID, false);
		List<ToolInfo> tiList = p3.getToolEventsDef();
		for(ToolInfo ti : tiList) {
			if(StatsManagerImpl.RESOURCES_TOOLID.equals(ti.getToolId())) {
				ti.setSelected(false);
			}
		}
		p3.setToolEventsDef(tiList);
		p3.setListToolEventsOnlyAvailableInSite(true);
		assertTrue(M_sm.setPreferences(FakeData.SITE_B_ID, p3));
		PrefsData p3L = M_sm.getPreferences(FakeData.SITE_B_ID, false);
		assertNotNull(p3L);
		assertEquals(p3.isChartIn3D(), p3L.isChartIn3D());
		assertEquals(p3.getChartTransparency(), p3L.getChartTransparency());
		assertEquals(p3.isItemLabelsVisible(), p3L.isItemLabelsVisible());
		assertEquals(p3.isListToolEventsOnlyAvailableInSite(), p3L.isListToolEventsOnlyAvailableInSite());
		assertEquals(p3.isUseAllTools(), p3L.isUseAllTools());
		assertEquals(FakeData.EVENT_REGISTRY_CHAT, p3L.getToolEventsDef());
	}

	public void testSiteUsers() {
		// invalid
		assertNull(M_sm.getSiteUsers(null));
		assertNull(M_sm.getSiteUsers("non_existent_site"));
		
		// valid
		Set<String> siteAUsers =  M_sm.getSiteUsers(FakeData.SITE_A_ID);
		Set<String> expectedSiteAUsers = new HashSet<String>(Arrays.asList(FakeData.USER_A_ID,FakeData.USER_B_ID));
		assertEquals(expectedSiteAUsers, siteAUsers);
		
		Set<String> siteBUsers =  M_sm.getSiteUsers(FakeData.SITE_B_ID);
		Set<String> expectedSiteBUsers = new HashSet<String>(Arrays.asList(FakeData.USER_A_ID));
		assertEquals(expectedSiteBUsers, siteBUsers);
	}

	public void testUsersWithVisits() {
		// invalid
		boolean thrown;
		try{
			thrown = false;
			M_sm.getUsersWithVisits(null);
		}catch(IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
		
		// valid
		Event e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		Set<String> expectedSiteA = new HashSet<String>();
		expectedSiteA.add(FakeData.USER_A_ID);
		Set<String> expectedSiteB = new HashSet<String>();
		assertEquals(expectedSiteA, M_sm.getUsersWithVisits(FakeData.SITE_A_ID));
		assertEquals(expectedSiteB, M_sm.getUsersWithVisits(FakeData.SITE_B_ID));
	}
	
	public void testResourceInfo() {
		// #1 getResourceName()
		// (null)
		assertNull(M_sm.getResourceName(null, false));
		
		// (no location prefix)
		assertEquals(
				FakeData.RES_MYWORKSPACE_A+"-name", 
				M_sm.getResourceName(FakeData.RES_MYWORKSPACE_A, false));
		
		// My Workspace
		assertEquals(
				"[~"+FakeData.USER_A_ID+"] "+FakeData.RES_MYWORKSPACE_A+"-name", 
				M_sm.getResourceName(FakeData.RES_MYWORKSPACE_A));
		assertEquals(
				"[~"+FakeData.USER_B_ID+"] "+FakeData.RES_MYWORKSPACE_B_F+"-name", 
				M_sm.getResourceName(FakeData.RES_MYWORKSPACE_B_F));
		assertEquals(
				"[My Workspace] "+FakeData.RES_MYWORKSPACE_NO_F+"-name", 
				M_sm.getResourceName(FakeData.RES_MYWORKSPACE_NO_F));	
		
		// Attachments
		assertEquals(
				"[Attachments] "+FakeData.RES_ATTACH_SITE+"-name", 
				M_sm.getResourceName(FakeData.RES_ATTACH_SITE));
		assertEquals(
				"[Attachments: Discussion] "+FakeData.RES_ATTACH+"-name", 
				M_sm.getResourceName(FakeData.RES_ATTACH));
		
		// Dropbox
		assertEquals(
				FakeData.RES_DROPBOX_SITE_A+"-name", 
				M_sm.getResourceName(FakeData.RES_DROPBOX_SITE_A));
		assertEquals(
				"[DropBox] "+FakeData.RES_DROPBOX_SITE_A_USER_A+"-name", 
				M_sm.getResourceName(FakeData.RES_DROPBOX_SITE_A_USER_A));
		assertEquals(
				"[DropBox: "+FakeData.RES_DROPBOX_SITE_A_USER_A+"-name] "+FakeData.RES_DROPBOX_SITE_A_USER_A_FILE+"-name", 
				M_sm.getResourceName(FakeData.RES_DROPBOX_SITE_A_USER_A_FILE));
		
		// Resources
		assertEquals(
				FakeData.RES_ROOT_SITE_A+"-name", 
				M_sm.getResourceName(FakeData.RES_ROOT_SITE_A));
		assertEquals(
				FakeData.RES_FILE_SITE_A+"-name", 
				M_sm.getResourceName(FakeData.RES_FILE_SITE_A));
		assertEquals(
				FakeData.RES_FOLDER_SITE_A+"-name", 
				M_sm.getResourceName(FakeData.RES_FOLDER_SITE_A));
		
		
		// #2 getResourceImageLibraryRelativePath()
		assertEquals("image/sakai/folder.gif", M_sm.getResourceImageLibraryRelativePath(FakeData.RES_FOLDER_SITE_A));
		assertEquals("image/sakai/image.gif", M_sm.getResourceImageLibraryRelativePath(FakeData.RES_DROPBOX_SITE_A_USER_A_FILE));
		assertEquals("image/sakai/generic.gif", M_sm.getResourceImageLibraryRelativePath(null));
		
		
		// #3 getResourceImage()
		assertEquals("http://localhost:8080/library/image/sakai/folder.gif", M_sm.getResourceImage(FakeData.RES_FOLDER_SITE_A));
		
		
		// #4 getResourceURL()
		assertEquals(
				"http://localhost:8080"+FakeData.RES_FOLDER_SITE_A, 
				M_sm.getResourceURL(FakeData.RES_FOLDER_SITE_A));
		assertEquals(
				"http://localhost:8080"+FakeData.RES_DROPBOX_SITE_A_USER_A_FILE, 
				M_sm.getResourceURL(FakeData.RES_DROPBOX_SITE_A_USER_A_FILE));
		assertNull(M_sm.getResourceURL(null));
	}
	
}
