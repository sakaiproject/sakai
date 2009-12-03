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
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test;


import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.sakaiproject.sitestats.api.report.ReportManager;
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
	private final static boolean			enableLargeMembershipTest = false;
	
	private StatsManager					M_sm;
	private StatsUpdateManager				M_sum;
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

		if(enableLargeMembershipTest) {
			// Site C has tools {SiteStats, Chat}, has 2002 users (user-1..user-2002), created 1 month ago
			Site siteC = new FakeSite(FakeData.SITE_C_ID,
					Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID)
				);
			List<String> siteCUsersList = new ArrayList<String>();
			for(int i=0; i<FakeData.SITE_C_USER_COUNT; i++) {
				siteCUsersList.add(FakeData.USER_ID_PREFIX + (i+1));
			}
			Set<String> siteCUsersSet = new HashSet<String>(siteCUsersList);
			((FakeSite)siteC).setUsers(siteCUsersSet);
			((FakeSite)siteC).setMembers(siteCUsersSet);
			expect(M_ss.getSite(FakeData.SITE_C_ID)).andStubReturn(siteC);
			expect(M_ss.isUserSite(FakeData.SITE_C_ID)).andStubReturn(false);
			expect(M_ss.isSpecialSite(FakeData.SITE_C_ID)).andStubReturn(false);
			expect(siteC.getCreatedTime()).andStubReturn((Time)anyObject());
		}
		
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
		((StatsManagerImpl)M_sm).setCountFilesUsingCHS(false);
		((StatsUpdateManagerImpl)M_sum).setSiteService(M_ss);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);
	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		db.deleteAll();
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
		// isEnableResourceStats
		((StatsManagerImpl)M_sm).setEnableResourceStats(null);
		((StatsManagerImpl)M_sm).setDefaultPropertiesIfNotSet();
		assertEquals(true, M_sm.isEnableResourceStats());
		((StatsManagerImpl)M_sm).setEnableResourceStats(false);
		assertEquals(false, M_sm.isEnableResourceStats());
		((StatsManagerImpl)M_sm).setEnableResourceStats(true);
		assertEquals(true, M_sm.isEnableResourceStats());
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
		
		
		// #5 getTotalResources()
		M_sum.collectEvents(Arrays.asList(
				/* Files */
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id3", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				/* Folders */
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/folder_id1/", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/folder_id2/", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/folder_id1/", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b")
		));
		int totalFiles = M_sm.getTotalResources(FakeData.SITE_A_ID, true);
		int totalFilesAndFolders = M_sm.getTotalResources(FakeData.SITE_A_ID, false);
		assertEquals(1, totalFiles);
		assertEquals(2, totalFilesAndFolders);
		
	}
	
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
		Event aAToday = M_sum.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBToday = M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
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
		
		// visits
		// even numbered users has visits
		for(int i=0; i<FakeData.SITE_C_USER_COUNT; i+=2) {
			String userId = FakeData.USER_ID_PREFIX + (i+1);
			samples.add(
					M_sum.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_C_ID+"-presence", null, userId, "session-id-a")
					);
		}
		
		// activity
		// odd numbered users has chat.new activity event
		for(int i=1; i<FakeData.SITE_C_USER_COUNT; i+=2) {
			String userId = FakeData.USER_ID_PREFIX + (i+1);
			samples.add(
					M_sum.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_C_ID, FakeData.SITE_C_ID, userId, "session-id-a")
					);
		}
		
		// resources
		// all users has content.new activity event
		for(int i=0; i<FakeData.SITE_C_USER_COUNT; i++) {
			String userId = FakeData.USER_ID_PREFIX + (i+1);
			samples.add(
					M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_C_ID+"/resource_id", FakeData.SITE_C_ID, userId, "session-id-b")
					);
		}

		return samples;
	}
	
	public void testSummaryMethods() {
		M_sum.collectEvents(getSampleData());
		
		// #1 getSummaryVisitsTotals
		SummaryVisitsTotals svt = M_sm.getSummaryVisitsTotals(FakeData.SITE_A_ID);
		assertEquals(2, svt.getTotalUsers());
		assertEquals(9, svt.getTotalVisits());
		assertEquals(2, svt.getTotalUniqueVisits());
		assertEquals((100 * 2) / (double) 2, svt.getPercentageOfUsersThatVisitedSite());
		assertEquals(1.3, svt.getLast7DaysVisitsAverage());
		assertEquals(0.3, svt.getLast30DaysVisitsAverage());
		assertEquals(0.0, svt.getLast365DaysVisitsAverage());
		
		// #2 getSummaryActivityTotals
		SummaryActivityTotals sat = M_sm.getSummaryActivityTotals(FakeData.SITE_A_ID);
		assertEquals(8, sat.getTotalActivity());
		assertEquals(1.1, sat.getLast7DaysActivityAverage());
		assertEquals(0.3, sat.getLast30DaysActivityAverage());
		assertEquals(0.0, sat.getLast365DaysActivityAverage());
		
		// #3 getSummaryVisitsChartData: week
		SummaryVisitsChartData svcd = M_sm.getSummaryVisitsChartData(FakeData.SITE_A_ID, StatsManager.VIEW_WEEK);
		assertEquals(5, svcd.getSiteVisits().size());
		assertNotNull(svcd.getFirstDay());
		long[] wv = svcd.getVisits();
		long[] wuv = svcd.getUniqueVisits();
		assertEquals(1, wv[0]); // 6
		assertEquals(1, wuv[0]);
		assertEquals(0, wv[1]); // 5
		assertEquals(0, wuv[1]);
		assertEquals(2, wv[2]); // 4
		assertTrue(wuv[2] <= 2);
		assertEquals(0, wv[3]); // 3
		assertEquals(0, wuv[3]);
		assertEquals(2, wv[4]); // 2
		assertEquals(1, wuv[4]);
		assertEquals(1, wv[5]); // 1
		assertEquals(1, wuv[5]);
		assertEquals(3, wv[6]); // 0
		assertEquals(2, wuv[6]);
		
		// #4 getSummaryVisitsChartData: month
		svcd = M_sm.getSummaryVisitsChartData(FakeData.SITE_A_ID, StatsManager.VIEW_MONTH);
		assertEquals(5, svcd.getSiteVisits().size());
		assertNotNull(svcd.getFirstDay());
		wv = svcd.getVisits();
		wuv = svcd.getUniqueVisits();
		for(int i=0; i<wv.length ; i++) {
			if(i==23) {
				assertEquals(1, wv[i]);
				assertEquals(1, wuv[i]);				
			}else if(i==25) {
				assertEquals(2, wv[i]);
				assertEquals(2, wuv[i]);				
			}else if(i==27) {
				assertEquals(2, wv[i]);
				assertEquals(1, wuv[i]);				
			}else if(i==28) {
				assertEquals(1, wv[i]);
				assertEquals(1, wuv[i]);				
			}else if(i==29) {
				assertEquals(3, wv[i]);
				assertEquals(2, wuv[i]);				
			}else{
				assertEquals(0, wv[i]);
				assertEquals(0, wuv[i]);
			}
		}
		
		// #5 getSummaryVisitsChartData: year
		svcd = M_sm.getSummaryVisitsChartData(FakeData.SITE_A_ID, StatsManager.VIEW_YEAR);
		assertTrue(svcd.getSiteVisits().size() >= 1);
		assertNotNull(svcd.getFirstDay());
		wv = svcd.getVisits();
		wuv = svcd.getUniqueVisits();
//		for(int i=0; i<wv.length ; i++) {
//			if(i==11) {
//				assertEquals(9, wv[i]);
//				assertEquals(2, wuv[i]);				
//			}else{
//				assertEquals(0, wv[i]);
//				assertEquals(0, wuv[i]);
//			}
//		}
		
		// #6 getSummaryActivityChartData: week, BAR
		SummaryActivityChartData sacd = M_sm.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_WEEK, StatsManager.CHARTTYPE_BAR);
		assertEquals(0, sacd.getActivityByToolTotal());
		assertNotNull(sacd.getFirstDay());
		long[] a = sacd.getActivity();
		assertEquals(1, a[0]); // 6
		assertEquals(0, a[1]); // 5
		assertEquals(2, a[2]); // 4
		assertEquals(0, a[3]); // 3
		assertEquals(2, a[4]); // 2
		assertEquals(1, a[5]); // 1
		assertEquals(2, a[6]); // 0
		
		// #7 getSummaryActivityChartData: month, BAR
		sacd = M_sm.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_MONTH, StatsManager.CHARTTYPE_BAR);
		assertEquals(0, sacd.getActivityByToolTotal());
		assertNotNull(sacd.getFirstDay());
		a = sacd.getActivity();
		for(int i=0; i<a.length ; i++) {
			if(i==23) {
				assertEquals(1, a[i]);				
			}else if(i==25) {
				assertEquals(2, a[i]);				
			}else if(i==27) {
				assertEquals(2, a[i]);				
			}else if(i==28) {
				assertEquals(1, a[i]);				
			}else if(i==29) {
				assertEquals(2, a[i]);				
			}else{
				assertEquals(0, a[i]);
			}
		}
		
		// #8 getSummaryActivityChartData: year, BAR
		sacd = M_sm.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_YEAR, StatsManager.CHARTTYPE_BAR);
		assertEquals(0, sacd.getActivityByToolTotal());
		assertNotNull(sacd.getFirstDay());
		a = sacd.getActivity();
//		for(int i=0; i<a.length ; i++) {
//			if(i==11) {
//				assertEquals(8, a[i]);
//			}else{
//				assertEquals(0, a[i]);
//			}
//		}
		
		// #9 getSummaryActivityChartData: week, TOOL
		sacd = M_sm.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_WEEK, StatsManager.CHARTTYPE_PIE);
		List<SiteActivityByTool> sabt = sacd.getActivityByTool();
		assertNotNull(sabt);
		assertEquals(2, sabt.size());
		for(SiteActivityByTool s : sabt) {
			assertEquals(FakeData.SITE_A_ID, s.getSiteId());
			ToolInfo ti = s.getTool();
			if(FakeData.TOOL_CHAT.equals(ti.getToolId())) {
				assertEquals(6, s.getCount());
			}else if(StatsManager.RESOURCES_TOOLID.equals(ti.getToolId())) {
				assertEquals(2, s.getCount());
			}
		}
		
		// #10 getSummaryActivityChartData: month, TOOL
		sacd = M_sm.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_MONTH, StatsManager.CHARTTYPE_PIE);
		sabt = sacd.getActivityByTool();
		assertNotNull(sabt);
		assertEquals(2, sabt.size());
		for(SiteActivityByTool s : sabt) {
			assertEquals(FakeData.SITE_A_ID, s.getSiteId());
			ToolInfo ti = s.getTool();
			if(FakeData.TOOL_CHAT.equals(ti.getToolId())) {
				assertEquals(6, s.getCount());
			}else if(StatsManager.RESOURCES_TOOLID.equals(ti.getToolId())) {
				assertEquals(2, s.getCount());
			}
		}
		
		// #11 getSummaryActivityChartData: year, TOOL
		sacd = M_sm.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_YEAR, StatsManager.CHARTTYPE_PIE);
		sabt = sacd.getActivityByTool();
		assertNotNull(sabt);
		assertEquals(2, sabt.size());
		for(SiteActivityByTool s : sabt) {
			assertEquals(FakeData.SITE_A_ID, s.getSiteId());
			ToolInfo ti = s.getTool();
			if(FakeData.TOOL_CHAT.equals(ti.getToolId())) {
				assertEquals(6, s.getCount());
			}else if(StatsManager.RESOURCES_TOOLID.equals(ti.getToolId())) {
				assertEquals(2, s.getCount());
			}
		}
		
	}
	
	public void testEventStats() {
		M_sum.collectEvents(getSampleData());
		Date now = new Date();
		Date today = new Date(now.getTime() + 24*60*60*1000);
		Date threeDaysBefore = new Date(now.getTime() - 3*24*60*60*1000);
		
		// #1 getEventStats()
		List<Stat> stats = M_sm.getEventStats(FakeData.SITE_A_ID, null);
		assertEquals(14, stats.size());
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW));
		assertEquals(5, stats.size());
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW, FakeData.EVENT_CONTENTNEW));
		assertEquals(6, stats.size());
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, new ArrayList<String>());
		assertEquals(0, stats.size());
		
		// #2
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(14, stats.size());
		int statsCount = M_sm.getEventStatsRowCount(null, null, 
				null, null, null, false, null);
		assertEquals(14, statsCount);
		
		
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				threeDaysBefore, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(8, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null, 
				threeDaysBefore, null, null, false, null);
		assertEquals(8, statsCount);
		
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW, FakeData.EVENT_CONTENTNEW), 
				null, today, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW, FakeData.EVENT_CONTENTNEW),
				null, today, null, false, null);
		assertEquals(6, statsCount);
		
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null);
		assertEquals(6, statsCount);
		
		// test inverse selection
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW), 
				null, null, null, true, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(FakeData.USER_A_ID, stats.get(0).getUserId());
		
		// test paging
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, new PagingPosition(0, 5), 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = M_sm.getEventStatsRowCount(null, null,
				null, null, null, false, null);
		assertEquals(14, statsCount);
		
		// test max results
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, new PagingPosition(0, 5), 
				null, null, false, 3);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		
		// test columns with sorting
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_EVENT, StatsManager.T_USER), StatsManager.T_USER, true, 0);
		assertNotNull(stats);
		assertEquals(FakeData.USER_A_ID, stats.get(0).getUserId());
		for(Stat s : stats) {
			EventStat es = (EventStat) s;
			assertNotNull(es.getEventId());
			assertNotNull(es.getUserId());
			assertNotNull(es.getCount());
			assertNull(es.getSiteId());
			assertNull(es.getDate());
		}
		assertEquals(6, stats.size());
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_EVENT, StatsManager.T_USER), StatsManager.T_USER, false, 0);
		assertNotNull(stats);
		assertEquals(FakeData.USER_B_ID, stats.get(0).getUserId());
		assertEquals(6, stats.size());
		
		// anonymous
		Event eAnon = M_sum.buildEvent(today, FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		M_sum.collectEvent(eAnon);
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(15, stats.size());
		boolean foundAnonymousUser = false;
		for(Stat s : stats) {
			if("-".equals(s.getUserId())) {
				foundAnonymousUser = true;
			}
		}
		assertTrue(foundAnonymousUser);
		statsCount = M_sm.getEventStatsRowCount(null, null,
				null, null, null, false, null);
		assertEquals(15, statsCount);
		stats = M_sm.getEventStats(null, null, 
				null, null, new ArrayList<String>(), false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(0, stats.size());
		statsCount = M_sm.getEventStatsRowCount(null, null,
				null, null, new ArrayList<String>(), false, null);
		assertEquals(0, statsCount);
		
		// group by: defaults
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(15, stats.size());		
		statsCount = M_sm.getEventStatsRowCount(null, null,
				null, null, null, false, null);
		assertEquals(15, statsCount);
		// group by: site
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_EVENT), null, false, 0);
		assertNotNull(stats);
		assertEquals(5, stats.size());
		statsCount = M_sm.getEventStatsRowCount(null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_EVENT));
		assertEquals(5, statsCount);
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_TOOL), null, false, 0);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		statsCount = M_sm.getEventStatsRowCount(null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_TOOL));
		assertEquals(5, statsCount);
		stats = M_sm.getEventStats(null, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getEventStatsRowCount(null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE));
		assertEquals(1, statsCount);
		// group by: user
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_USER), null, false, 0);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_USER));
		assertEquals(3, statsCount);
		// group by: tool
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW), 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_TOOL), null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, Arrays.asList(StatsManager.T_TOOL));
		assertEquals(3, statsCount);
		// group by: event
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_TOOL, StatsManager.T_EVENT), null, false, 0);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, Arrays.asList(StatsManager.T_TOOL, StatsManager.T_EVENT));
		assertEquals(3, statsCount);
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_EVENT), null, false, 0);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, Arrays.asList(StatsManager.T_EVENT));
		assertEquals(3, statsCount);
		// group by: date
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATE));
		assertEquals(6, statsCount);
		// group by: datemonth
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEMONTH), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATEMONTH));
		assertTrue(statsCount <= 2);
		// group by: dateyear
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEYEAR), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATEYEAR));
		assertTrue(statsCount <= 2);
		// group by: lastdate
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_LASTDATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_LASTDATE));
		assertEquals(1, statsCount);
		// group by: visits
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_VISITS), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(18, ((SiteVisits)stats.get(0)).getTotalVisits());
		assertEquals(2, ((SiteVisits)stats.get(0)).getTotalUnique());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_VISITS));
		assertEquals(1, statsCount);
		// group by: uniquevisits
		stats = M_sm.getEventStats(FakeData.SITE_A_ID, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_UNIQUEVISITS), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(18, ((SiteVisits)stats.get(0)).getTotalVisits());
		assertEquals(2, ((SiteVisits)stats.get(0)).getTotalUnique());
		statsCount = M_sm.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_UNIQUEVISITS));
		assertEquals(1, statsCount);
		
		//System.out.println("Stats: "+stats);
		//System.out.println("Size: "+stats.size());
	}
	
	public void testResourceStats() {
		M_sum.collectEvents(getSampleData());
		Date now = new Date();
		Date today = new Date(now.getTime() + 24*60*60*1000);
		Date sixDaysBefore = new Date(today.getTime() - 6*24*60*60*1000);
		
		// #1
		List<Stat> stats = M_sm.getResourceStats(FakeData.SITE_A_ID);
		assertEquals(2, stats.size());
		
		// #2
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());
		int statsCount = M_sm.getResourceStatsRowCount(null, null, null,
				null, null, null, false, null);
		assertEquals(2, statsCount);
		
		
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, null, null,
				sixDaysBefore, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				sixDaysBefore, null, null, false, null);
		assertEquals(1, statsCount);
			
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_REVS, null,
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_REVS, null,
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null);
		assertEquals(1, statsCount);
		
		// test inverse selection
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_NEW ,null, 
				null, null, null, true, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(FakeData.USER_A_ID, stats.get(0).getUserId());
		
		// test paging
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, new PagingPosition(0, 0), 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(null, null, null,
				null, null, null, false, null);
		assertEquals(2, statsCount);
		
		// test max results
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, new PagingPosition(0, 0), 
				null, null, false, 1);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		
		// test columns with sorting
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_RESOURCE_ACTION), StatsManager.T_RESOURCE_ACTION, true, 0);
		assertNotNull(stats);
		assertEquals(ReportManager.WHAT_RESOURCES_ACTION_NEW, ((ResourceStat)stats.get(0)).getResourceAction());
		for(Stat s : stats) {
			ResourceStat es = (ResourceStat) s;
			assertNotNull(es.getResourceAction());
			assertNotNull(es.getCount());
			assertNull(es.getSiteId());
			assertNull(es.getDate());
			assertNull(es.getUserId());
			assertNull(es.getResourceRef());
		}
		assertEquals(2, stats.size());
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_RESOURCE_ACTION), StatsManager.T_RESOURCE_ACTION, false, 0);
		assertNotNull(stats);
		assertEquals(ReportManager.WHAT_RESOURCES_ACTION_REVS, ((ResourceStat)stats.get(0)).getResourceAction());
		assertEquals(2, stats.size());
		
		// group by: defaults
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());		
		statsCount = M_sm.getResourceStatsRowCount(null, null, null,
				null, null, null, false, null);
		assertEquals(2, statsCount);
		// group by: site
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_USER), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(null, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_USER));
		assertEquals(1, statsCount);
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_RESOURCE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(null, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_RESOURCE));
		assertEquals(1, statsCount);
		stats = M_sm.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(null, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE));
		assertEquals(1, statsCount);
		// group by: user
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_USER), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_USER));
		assertEquals(1, statsCount);
		// group by: resource
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, null, Arrays.asList("/content/group/"+FakeData.SITE_A_ID+"/resource_id"), 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_RESOURCE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, null, Arrays.asList("/content/group/"+FakeData.SITE_A_ID+"/resource_id"),
				null, null, null, false, Arrays.asList(StatsManager.T_RESOURCE));
		assertEquals(1, statsCount);
		// group by: resource action
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_NEW, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_RESOURCE_ACTION), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_NEW, null,
				null, null, null, false, Arrays.asList(StatsManager.T_RESOURCE_ACTION));
		assertEquals(1, statsCount);
		// group by: date
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATE));
		assertEquals(2, statsCount);
		// group by: datemonth
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEMONTH), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATEMONTH));
		assertTrue(statsCount <= 2);
		// group by: dateyear
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, null, null, 
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEYEAR), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null, 
				null, null, null, false, Arrays.asList(StatsManager.T_DATEYEAR));
		assertTrue(statsCount <= 2);
		// group by: lastdate
		stats = M_sm.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_LASTDATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = M_sm.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_LASTDATE));
		assertEquals(1, statsCount);
	}
	
	public void testLargeMembershipSite() {
		// For development only: this tests take too long!
		if(enableLargeMembershipTest) {
			// sample data
			M_sum.collectEvents(getSampleData2());
			
			// all users list
			List<String> allUsers = new ArrayList<String>();
			for(int i=0; i<FakeData.SITE_C_USER_COUNT; i++) {
				allUsers.add(FakeData.USER_ID_PREFIX + (i+1));
			}
			
			// test visits
			{
				List<Stat> stats = M_sm.getEventStats(null, Arrays.asList(StatsManager.SITEVISIT_EVENTID), 
						null, null, allUsers, false, null, 
						null, null, false, 0);
				assertNotNull(stats);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, stats.size());
				int statsCount = M_sm.getEventStatsRowCount(null, Arrays.asList(StatsManager.SITEVISIT_EVENTID), 
						null, null, allUsers, false, null);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, statsCount);
			}
			
			// test activity
			{
				List<Stat> stats = M_sm.getEventStats(null, Arrays.asList(FakeData.EVENT_CHATNEW), 
						null, null, allUsers, false, null, 
						null, null, false, 0);
				assertNotNull(stats);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, stats.size());
				int statsCount = M_sm.getEventStatsRowCount(null, Arrays.asList(FakeData.EVENT_CHATNEW), 
						null, null, allUsers, false, null);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, statsCount);
			}
			
			// test resources
			{
				List<Stat> stats = M_sm.getResourceStats(null, null, null, 
						null, null, allUsers, false, null, 
						null, null, false, 0);
				assertNotNull(stats);
				assertEquals(FakeData.SITE_C_USER_COUNT, stats.size());
				int statsCount = M_sm.getResourceStatsRowCount(null, null, null, 
						null, null, allUsers, false, null);
				assertEquals(FakeData.SITE_C_USER_COUNT, statsCount);
			}
		}
	}
}
