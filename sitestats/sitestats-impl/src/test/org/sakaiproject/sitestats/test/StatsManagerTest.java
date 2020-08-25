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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.memory.api.MemoryService;
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
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.sakaiproject.tool.api.Placement;
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
public class StatsManagerTest extends AbstractTransactionalJUnit4SpringContextTests {

	private final static boolean enableLargeMembershipTest = false;

	@Resource(name = "org.sakaiproject.sitestats.test.DB")
	private DB db;
	@Resource(name = "org.sakaiproject.memory.api.MemoryService")
	private MemoryService memoryService;
	@Resource(name = "org.sakaiproject.util.ResourceLoader.sitestats")
	private ResourceLoader resourceLoader;
	@Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;
	@Resource(name = "org.sakaiproject.site.api.SiteService")
	private SiteService siteService;
	@Resource(name = "org.sakaiproject.sitestats.api.StatsManager")
	private StatsManager statsManager;
	@Resource(name = "org.sakaiproject.sitestats.api.StatsUpdateManager")
	private StatsUpdateManager statsUpdateManager;
	@Resource(name = "org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;

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
		FakeSite siteB = spy(FakeSite.class).set(FakeData.SITE_B_ID, FakeData.TOOL_CHAT);
		siteB.setUsers(new HashSet<>(Collections.singletonList(FakeData.USER_A_ID)));
		siteB.setMembers(new HashSet<>(Collections.singletonList(FakeData.USER_A_ID)));
		when(siteService.getSite(FakeData.SITE_B_ID)).thenReturn(siteB);
		when(siteService.isUserSite(FakeData.SITE_B_ID)).thenReturn(false);
		when(siteService.isSpecialSite(FakeData.SITE_B_ID)).thenReturn(false);

		if(enableLargeMembershipTest) {
			// Site C has tools {SiteStats, Chat}, has 2002 users (user-1..user-2002), created 1 month ago
			FakeSite siteC = spy(FakeSite.class).set(FakeData.SITE_C_ID, Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID));
			List<String> siteCUsersList = new ArrayList<>();
			for(int i=0; i<FakeData.SITE_C_USER_COUNT; i++) {
				siteCUsersList.add(FakeData.USER_ID_PREFIX + (i+1));
			}
			Set<String> siteCUsersSet = new HashSet<String>(siteCUsersList);
			siteC.setUsers(siteCUsersSet);
			siteC.setMembers(siteCUsersSet);
			when(siteService.getSite(FakeData.SITE_C_ID)).thenReturn(siteC);
			when(siteService.isUserSite(FakeData.SITE_C_ID)).thenReturn(false);
			when(siteService.isSpecialSite(FakeData.SITE_C_ID)).thenReturn(false);
			when(siteC.getCreatedDate()).thenReturn(new Date());
		}
		
		// This is needed to make the tests deterministic, otherwise on occasion the collect thread will run
		// and break the tests.
		statsUpdateManager.setCollectThreadEnabled(false);

		((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setResourceLoader(resourceLoader);
	}

	// ---- TESTS ----
	@Test
	public void testEnableVisibleSiteVisits() throws Exception {
		when(serverConfigurationService.getBoolean(eq("display.users.present"), anyBoolean())).thenReturn(true);
		when(serverConfigurationService.getBoolean(eq("presence.events.log"), anyBoolean())).thenReturn(true);
		StatsManagerImpl smi = (StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget();
		smi.setEnableSiteVisits(null);
		smi.setVisitsInfoAvailable(null);
		smi.setEnableSitePresences(null);
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.getEnableSiteVisits());
		assertEquals(true, statsManager.getVisitsInfoAvailable());
		assertEquals(false, statsManager.getEnableSitePresences()); // off, by default

		when(serverConfigurationService.getBoolean(eq("display.users.present"), anyBoolean())).thenReturn(false);
		when(serverConfigurationService.getBoolean(eq("presence.events.log"), anyBoolean())).thenReturn(true);
		smi.setEnableSiteVisits(null);
		smi.setVisitsInfoAvailable(null);
		smi.setEnableSitePresences(null);
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.getEnableSiteVisits());
		assertEquals(true, statsManager.getVisitsInfoAvailable());
		assertEquals(false, statsManager.getEnableSitePresences()); // off, by default

		when(serverConfigurationService.getBoolean(eq("display.users.present"), anyBoolean())).thenReturn(true);
		when(serverConfigurationService.getBoolean(eq("presence.events.log"), anyBoolean())).thenReturn(false);
		smi.setEnableSiteVisits(null);
		smi.setVisitsInfoAvailable(null);
		smi.setEnableSitePresences(null);
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.getEnableSiteVisits());
		assertEquals(true, statsManager.getVisitsInfoAvailable());
		assertEquals(false, statsManager.getEnableSitePresences()); // off, by default

		when(serverConfigurationService.getBoolean(eq("display.users.present"), anyBoolean())).thenReturn(false);
		when(serverConfigurationService.getBoolean(eq("presence.events.log"), anyBoolean())).thenReturn(false);
		smi.setEnableSiteVisits(null);
		smi.setVisitsInfoAvailable(null);
		smi.setEnableSitePresences(null);
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(false, statsManager.getEnableSiteVisits());
		assertEquals(false, statsManager.getVisitsInfoAvailable());
		assertEquals(false, statsManager.getEnableSitePresences()); // off, by default

		when(serverConfigurationService.getBoolean(eq("display.users.present"), anyBoolean())).thenReturn(true);
		when(serverConfigurationService.getBoolean(eq("presence.events.log"), anyBoolean())).thenReturn(true);
		smi.setEnableSiteVisits(null);
		smi.setVisitsInfoAvailable(null);
		smi.setEnableSitePresences(null);
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.getEnableSiteVisits());
		assertEquals(true, statsManager.getVisitsInfoAvailable());
		assertEquals(false, statsManager.getEnableSitePresences()); // off, by default
		
		// revert
		when(serverConfigurationService.getBoolean(eq("display.users.present"), anyBoolean())).thenReturn(false);
		when(serverConfigurationService.getBoolean(eq("presence.events.log"), anyBoolean())).thenReturn(true);
		smi.setEnableSiteVisits(null);
		smi.setVisitsInfoAvailable(null);
		smi.setEnableSitePresences(null);
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.getEnableSiteVisits());
		assertEquals(true, statsManager.getVisitsInfoAvailable());
		assertEquals(false, statsManager.getEnableSitePresences()); // off, by default
	}
	
	@Test
	public void testOtherConfig() throws Exception {
		// isEnableSiteActivity
		StatsManagerImpl smi = (StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget();
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.isEnableSiteActivity());
		smi.setEnableSiteActivity(false);
		assertEquals(false, statsManager.isEnableSiteActivity());
		smi.setEnableSiteActivity(true);
		assertEquals(true, statsManager.isEnableSiteActivity());
		// isEnableResourceStats
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.isEnableResourceStats());
		smi.setEnableResourceStats(false);
		assertEquals(false, statsManager.isEnableResourceStats());
		smi.setEnableResourceStats(true);
		assertEquals(true, statsManager.isEnableResourceStats());
		// isEnableLessonsStats
		smi.checkAndSetDefaultPropertiesIfNotSet();
		assertEquals(true, statsManager.isEnableLessonsStats());
		smi.setEnableLessonsStats(false);
		assertEquals(false, statsManager.isEnableLessonsStats());
		smi.setEnableLessonsStats(true);
		assertEquals(true, statsManager.isEnableLessonsStats());
		// isServerWideStatsEnabled
		smi.setServerWideStatsEnabled(false);
		assertEquals(false, statsManager.isServerWideStatsEnabled());
		smi.setServerWideStatsEnabled(true);
		assertEquals(true, statsManager.isServerWideStatsEnabled());
		// ChartBackgroundColor
		smi.setChartBackgroundColor("#000");
		assertEquals("#000", statsManager.getChartBackgroundColor());
		smi.setChartBackgroundColor("#fff");
		assertEquals("#fff", statsManager.getChartBackgroundColor());
		// isChartIn3D
		smi.setChartIn3D(false);
		assertEquals(false, statsManager.isChartIn3D());
		smi.setChartIn3D(true);
		assertEquals(true, statsManager.isChartIn3D());
		// ChartTransparency
		smi.setChartTransparency(0.5f);
		assertEquals(0.5f, statsManager.getChartTransparency(), 1e-8);
		smi.setChartTransparency(1.0f);
		assertEquals(1.0f, statsManager.getChartTransparency(), 1e-8);
		// isItemLabelsVisible
		smi.setItemLabelsVisible(false);
		assertEquals(false, statsManager.isItemLabelsVisible());
		smi.setItemLabelsVisible(true);
		assertEquals(true, statsManager.isItemLabelsVisible());
		// isShowAnonymousAccessEvents
		smi.setShowAnonymousAccessEvents(false);
		assertEquals(false, statsManager.isShowAnonymousAccessEvents());
		smi.setShowAnonymousAccessEvents(true);
		assertEquals(true, statsManager.isShowAnonymousAccessEvents());
		// isLastJobRunDateVisible
		smi.setLastJobRunDateVisible(false);
		assertEquals(false, statsManager.isLastJobRunDateVisible());
		smi.setLastJobRunDateVisible(true);
		assertEquals(true, statsManager.isLastJobRunDateVisible());
		// getEnableSitePresences
		smi.setEnableSitePresences(false);
		assertEquals(false, statsManager.getEnableSitePresences());
		smi.setEnableSitePresences(true);
		assertEquals(true, statsManager.getEnableSitePresences());
	}
	
	@Test
	public void testPreferences() {
		try {
			statsManager.getPreferences(null, true);
			fail("Expected an IllegalArgumentException");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}
		try {
			statsManager.getPreferences(null, false);
			fail("Expected an IllegalArgumentException");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}
		try {
			statsManager.setPreferences(null, null);
			fail("Expected an IllegalArgumentException");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}
		try {
			statsManager.setPreferences(null, new PrefsData());
			fail("Expected an IllegalArgumentException");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}
		try {
			statsManager.setPreferences(FakeData.SITE_A_ID, null);
			fail("Expected an IllegalArgumentException");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}

		// get nonexistent preferences (will return default)
		assertNotNull(statsManager.getPreferences(FakeData.SITE_A_ID, false));
		assertNotNull(statsManager.getPreferences(FakeData.SITE_A_ID, true));
		
		// #1 add/get preferences
		PrefsData p1 = new PrefsData();
		p1.setChartIn3D(true);
		p1.setChartTransparency(0.5f);
		p1.setItemLabelsVisible(true);
		p1.setListToolEventsOnlyAvailableInSite(true);
		p1.setUseAllTools(true);
		assertTrue(statsManager.setPreferences(FakeData.SITE_A_ID, p1));
		PrefsData p1L = statsManager.getPreferences(FakeData.SITE_A_ID, false);
		assertNotNull(p1L);
		assertEquals(p1.isChartIn3D(), p1L.isChartIn3D());
		assertEquals(p1.getChartTransparency(), p1L.getChartTransparency(), 1e-8);
		assertEquals(p1.isItemLabelsVisible(), p1L.isItemLabelsVisible());
		assertEquals(p1.isListToolEventsOnlyAvailableInSite(), p1L.isListToolEventsOnlyAvailableInSite());
		assertEquals(p1.isUseAllTools(), p1L.isUseAllTools());

		// #2 add/get preferences
		PrefsData p2 = new PrefsData();
		p2.setChartIn3D(false);
		p2.setChartTransparency(0.8f);
		p2.setItemLabelsVisible(false);
		p2.setListToolEventsOnlyAvailableInSite(false);
		p2.setToolEventsDef(FakeData.EVENT_REGISTRY);
		p2.setUseAllTools(false);
		assertTrue(statsManager.setPreferences(FakeData.SITE_A_ID, p2));
		PrefsData p2L = statsManager.getPreferences(FakeData.SITE_A_ID, false);
		assertNotNull(p2L);
		assertEquals(p2.isChartIn3D(), p2L.isChartIn3D());
		assertEquals(p2.getChartTransparency(), p2L.getChartTransparency(), 1e-8);
		assertEquals(p2.isItemLabelsVisible(), p2L.isItemLabelsVisible());
		assertEquals(p2.isListToolEventsOnlyAvailableInSite(), p2L.isListToolEventsOnlyAvailableInSite());
		assertEquals(p2.isUseAllTools(), p2L.isUseAllTools());

		// #3 get default/edit/set/get preferences
		PrefsData p3 = statsManager.getPreferences(FakeData.SITE_B_ID, false);
		List<ToolInfo> tiList = p3.getToolEventsDef();
		for(ToolInfo ti : tiList) {
			if(StatsManagerImpl.RESOURCES_TOOLID.equals(ti.getToolId())) {
				ti.setSelected(false);
			}
		}
		p3.setToolEventsDef(tiList);
		p3.setListToolEventsOnlyAvailableInSite(true);
		assertTrue(statsManager.setPreferences(FakeData.SITE_B_ID, p3));
		PrefsData p3L = statsManager.getPreferences(FakeData.SITE_B_ID, false);
		assertNotNull(p3L);
		assertEquals(p3.isChartIn3D(), p3L.isChartIn3D());
		assertEquals(p3.getChartTransparency(), p3L.getChartTransparency(), 1e-8);
		assertEquals(p3.isItemLabelsVisible(), p3L.isItemLabelsVisible());
		assertEquals(p3.isListToolEventsOnlyAvailableInSite(), p3L.isListToolEventsOnlyAvailableInSite());
		assertEquals(p3.isUseAllTools(), p3L.isUseAllTools());
		assertEquals(FakeData.EVENT_REGISTRY_CHAT, p3L.getToolEventsDef());
	}

	@Test
	public void testSiteUsers() {
		// invalid
		Placement placement = mock(Placement.class);
		when(placement.getContext()).thenReturn(null);
		when(toolManager.getCurrentPlacement()).thenReturn(placement);
		assertNull(statsManager.getSiteUsers(null));
		assertNull(statsManager.getSiteUsers("non_existent_site"));
		
		// valid
		Set<String> siteAUsers =  statsManager.getSiteUsers(FakeData.SITE_A_ID);
		Set<String> expectedSiteAUsers = new HashSet<>(Arrays.asList(FakeData.USER_A_ID, FakeData.USER_B_ID));
		assertEquals(expectedSiteAUsers, siteAUsers);
		
		Set<String> siteBUsers =  statsManager.getSiteUsers(FakeData.SITE_B_ID);
		Set<String> expectedSiteBUsers = new HashSet<>(Arrays.asList(FakeData.USER_A_ID));
		assertEquals(expectedSiteBUsers, siteBUsers);
	}

	@Test
	public void testUsersWithVisits() {
		// invalid
		boolean thrown;
		try{
			thrown = false;
			statsManager.getUsersWithVisits(null);
		}catch(IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
		
		// valid
		Event e1 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		statsUpdateManager.collectEvent(e1);
		Set<String> expectedSiteA = new HashSet<>();
		expectedSiteA.add(FakeData.USER_A_ID);
		Set<String> expectedSiteB = new HashSet<>();
		assertEquals(expectedSiteA, statsManager.getUsersWithVisits(FakeData.SITE_A_ID));
		assertEquals(expectedSiteB, statsManager.getUsersWithVisits(FakeData.SITE_B_ID));
	}
	
	@Test
	public void testResourceInfo() {
		// #1 getResourceName()
		// (null)
		assertNull(statsManager.getResourceName(null, false));
		
		// (no location prefix)
		assertEquals(
				FakeData.RES_MYWORKSPACE_A+"-name", 
				statsManager.getResourceName(FakeData.RES_MYWORKSPACE_A, false));
		
		// My Workspace
		assertEquals(
				"[~"+FakeData.USER_A_ID+"] "+FakeData.RES_MYWORKSPACE_A+"-name", 
				statsManager.getResourceName(FakeData.RES_MYWORKSPACE_A));
		assertEquals(
				"[~"+FakeData.USER_B_ID+"] "+FakeData.RES_MYWORKSPACE_B_F+"-name", 
				statsManager.getResourceName(FakeData.RES_MYWORKSPACE_B_F));
		assertEquals(
				"[My Workspace] "+FakeData.RES_MYWORKSPACE_NO_F+"-name", 
				statsManager.getResourceName(FakeData.RES_MYWORKSPACE_NO_F));
		
		// Attachments
		assertEquals(
				"[Attachments] "+FakeData.RES_ATTACH_SITE+"-name", 
				statsManager.getResourceName(FakeData.RES_ATTACH_SITE));
		assertEquals(
				"[Attachments: Discussion] "+FakeData.RES_ATTACH+"-name", 
				statsManager.getResourceName(FakeData.RES_ATTACH));
		
		// Dropbox
		Tool dropboxTool = mock(Tool.class);
		when(dropboxTool.getTitle()).thenReturn("DropBox");
		when(toolManager.getTool(StatsManager.DROPBOX_TOOLID)).thenReturn(dropboxTool);
		assertEquals(
				FakeData.RES_DROPBOX_SITE_A+"-name", 
				statsManager.getResourceName(FakeData.RES_DROPBOX_SITE_A));
		assertEquals(
				"[DropBox] "+FakeData.RES_DROPBOX_SITE_A_USER_A+"-name", 
				statsManager.getResourceName(FakeData.RES_DROPBOX_SITE_A_USER_A));
		assertEquals(
				"[DropBox: "+FakeData.RES_DROPBOX_SITE_A_USER_A+"-name] "+FakeData.RES_DROPBOX_SITE_A_USER_A_FILE+"-name", 
				statsManager.getResourceName(FakeData.RES_DROPBOX_SITE_A_USER_A_FILE));
		
		// Resources
		assertEquals(
				FakeData.RES_ROOT_SITE_A+"-name", 
				statsManager.getResourceName(FakeData.RES_ROOT_SITE_A));
		assertEquals(
				FakeData.RES_FILE_SITE_A+"-name", 
				statsManager.getResourceName(FakeData.RES_FILE_SITE_A));
		assertEquals(
				FakeData.RES_FOLDER_SITE_A+"-name", 
				statsManager.getResourceName(FakeData.RES_FOLDER_SITE_A));
		
		
		// #2 getResourceImageLibraryRelativePath()
		assertEquals("image/sakai/folder.gif", statsManager.getResourceImageLibraryRelativePath(FakeData.RES_FOLDER_SITE_A));
		assertEquals("image/sakai/image.gif", statsManager.getResourceImageLibraryRelativePath(FakeData.RES_DROPBOX_SITE_A_USER_A_FILE));
		assertEquals("image/sakai/generic.gif", statsManager.getResourceImageLibraryRelativePath(null));
		
		
		// #3 getResourceImage()
		assertEquals("http://localhost:8080/library/image/sakai/folder.gif", statsManager.getResourceImage(FakeData.RES_FOLDER_SITE_A));
		
		
		// #4 getResourceURL()
		assertEquals(
				"http://localhost:8080"+FakeData.RES_FOLDER_SITE_A, 
				statsManager.getResourceURL(FakeData.RES_FOLDER_SITE_A));
		assertEquals(
				"http://localhost:8080"+FakeData.RES_DROPBOX_SITE_A_USER_A_FILE, 
				statsManager.getResourceURL(FakeData.RES_DROPBOX_SITE_A_USER_A_FILE));
		assertNull(statsManager.getResourceURL(null));
		
		
		// #5 getTotalResources()
		try {
			((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setCountFilesUsingCHS(false);
		} catch (Exception e) {
			fail("Set count files using CHS to false, " + e.getMessage());
		}
		statsUpdateManager.collectEvents(Arrays.asList(
				/* Files */
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id3", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id1", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id2", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				/* Folders */
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/folder_id1/", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/folder_id2/", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b"),
				statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/folder_id1/", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b")
		));
		int totalFiles = statsManager.getTotalResources(FakeData.SITE_A_ID, true);
		int totalFilesAndFolders = statsManager.getTotalResources(FakeData.SITE_A_ID, false);
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
		Event aAToday = statsUpdateManager.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event aBToday = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
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
		
		// visits
		// even numbered users has visits
		for(int i=0; i<FakeData.SITE_C_USER_COUNT; i+=2) {
			String userId = FakeData.USER_ID_PREFIX + (i+1);
			samples.add(
					statsUpdateManager.buildEvent(today, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_C_ID+"-presence", null, userId, "session-id-a")
					);
		}
		
		// activity
		// odd numbered users has chat.new activity event
		for(int i=1; i<FakeData.SITE_C_USER_COUNT; i+=2) {
			String userId = FakeData.USER_ID_PREFIX + (i+1);
			samples.add(
					statsUpdateManager.buildEvent(today, FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_C_ID, FakeData.SITE_C_ID, userId, "session-id-a")
					);
		}
		
		// resources
		// all users has content.new activity event
		for(int i=0; i<FakeData.SITE_C_USER_COUNT; i++) {
			String userId = FakeData.USER_ID_PREFIX + (i+1);
			samples.add(
					statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_C_ID+"/resource_id", FakeData.SITE_C_ID, userId, "session-id-b")
					);
		}

		return samples;
	}
	
	@Test
	public void testSummaryMethods() {
		statsUpdateManager.collectEvents(getSampleData());
		
		// #1 getSummaryVisitsTotals
		SummaryVisitsTotals svt = statsManager.getSummaryVisitsTotals(FakeData.SITE_A_ID);
		assertEquals(2, svt.getTotalUsers());
		assertEquals(9, svt.getTotalVisits());
		assertEquals(2, svt.getTotalUniqueVisits());
		assertEquals((100 * 2) / (double) 2, svt.getPercentageOfUsersThatVisitedSite(), 1e-8);
		assertEquals(1.3, svt.getLast7DaysVisitsAverage(), 1e-8);
		assertEquals(0.3, svt.getLast30DaysVisitsAverage(), 1e-8);
		assertEquals(0.0, svt.getLast365DaysVisitsAverage(), 1e-8);
		
		// #2 getSummaryActivityTotals
		SummaryActivityTotals sat = statsManager.getSummaryActivityTotals(FakeData.SITE_A_ID);
		assertEquals(8, sat.getTotalActivity());
		assertEquals(1.1, sat.getLast7DaysActivityAverage(), 1e-8);
		assertEquals(0.3, sat.getLast30DaysActivityAverage(), 1e-8);
		assertEquals(0.0, sat.getLast365DaysActivityAverage(), 1e-8);
		
		// #3 getSummaryVisitsChartData: week
		SummaryVisitsChartData svcd = statsManager.getSummaryVisitsChartData(FakeData.SITE_A_ID, StatsManager.VIEW_WEEK);
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
		svcd = statsManager.getSummaryVisitsChartData(FakeData.SITE_A_ID, StatsManager.VIEW_MONTH);
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
		svcd = statsManager.getSummaryVisitsChartData(FakeData.SITE_A_ID, StatsManager.VIEW_YEAR);
		assertTrue(svcd.getSiteVisits().size() >= 1);
		assertNotNull(svcd.getFirstDay());
		wv = svcd.getVisits();
		wuv = svcd.getUniqueVisits();
//		for(int i=0; i<wv.length ; i++) {
//			if(i==11) {
//				Assert.assertEquals(9, wv[i]);
//				Assert.assertEquals(2, wuv[i]);				
//			}else{
//				Assert.assertEquals(0, wv[i]);
//				Assert.assertEquals(0, wuv[i]);
//			}
//		}
		
		// #6 getSummaryActivityChartData: week, BAR
		SummaryActivityChartData sacd = statsManager.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_WEEK, StatsManager.CHARTTYPE_BAR);
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
		sacd = statsManager.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_MONTH, StatsManager.CHARTTYPE_BAR);
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
		sacd = statsManager.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_YEAR, StatsManager.CHARTTYPE_BAR);
		assertEquals(0, sacd.getActivityByToolTotal());
		assertNotNull(sacd.getFirstDay());
		a = sacd.getActivity();
//		for(int i=0; i<a.length ; i++) {
//			if(i==11) {
//				Assert.assertEquals(8, a[i]);
//			}else{
//				Assert.assertEquals(0, a[i]);
//			}
//		}
		
		// #9 getSummaryActivityChartData: week, TOOL
		sacd = statsManager.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_WEEK, StatsManager.CHARTTYPE_PIE);
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
		sacd = statsManager.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_MONTH, StatsManager.CHARTTYPE_PIE);
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
		sacd = statsManager.getSummaryActivityChartData(FakeData.SITE_A_ID, StatsManager.VIEW_YEAR, StatsManager.CHARTTYPE_PIE);
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
	
	@Test
	@Ignore		// TODO JUNIT test is not working on hsqldb need to look into
	public void testEventStats() {
		statsUpdateManager.collectEvents(getSampleData());
		Date now = new Date();
		Date today = new Date(now.getTime() + 24*60*60*1000);
		Date threeDaysBefore = new Date(now.getTime() - 3*24*60*60*1000);
		
		// #1 getEventStats()
		List<Stat> stats = statsManager.getEventStats(FakeData.SITE_A_ID, null);
		assertEquals(14, stats.size());
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW));
		assertEquals(5, stats.size());
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW, FakeData.EVENT_CONTENTNEW));
		assertEquals(6, stats.size());
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, new ArrayList<String>());
		assertEquals(0, stats.size());
		
		// #2
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(14, stats.size());
		int statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, null, false, null);
		assertEquals(14, statsCount);
		
		
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				threeDaysBefore, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(8, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				threeDaysBefore, null, null, false, null);
		assertEquals(8, statsCount);
		
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW, FakeData.EVENT_CONTENTNEW),
				null, today, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CHATNEW, FakeData.EVENT_CONTENTNEW),
				null, today, null, false, null);
		assertEquals(6, statsCount);
		
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null);
		assertEquals(6, statsCount);
		
		// test inverse selection
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW),
				null, null, null, true, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(FakeData.USER_A_ID, stats.get(0).getUserId());
		
		// test paging
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, new PagingPosition(0, 5), 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, null, false, null);
		assertEquals(14, statsCount);
		
		// test max results
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, new PagingPosition(0, 5), 
				null, null, false, 3);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		
		// test columns with sorting
		stats = statsManager.getEventStats(null, null,
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
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_EVENT, StatsManager.T_USER), StatsManager.T_USER, false, 0);
		assertNotNull(stats);
		assertEquals(FakeData.USER_B_ID, stats.get(0).getUserId());
		assertEquals(6, stats.size());
		
		// anonymous
		Event eAnon = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTDEL, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		statsUpdateManager.collectEvent(eAnon);
		stats = statsManager.getEventStats(null, null,
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
		statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, null, false, null);
		assertEquals(15, statsCount);
		stats = statsManager.getEventStats(null, null,
				null, null, new ArrayList<String>(), false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(0, stats.size());
		statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, new ArrayList<String>(), false, null);
		assertEquals(0, statsCount);
		
		// group by: defaults
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(15, stats.size());
		statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, null, false, null);
		assertEquals(15, statsCount);
		// group by: site
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_EVENT), null, false, 0);
		assertNotNull(stats);
		assertEquals(5, stats.size());
		statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_EVENT));
		assertEquals(5, statsCount);
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_TOOL), null, false, 0);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_TOOL));
		assertEquals(5, statsCount); // is not 3 because count has not (manually) aggregated by tool
		stats = statsManager.getEventStats(null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getEventStatsRowCount(null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE));
		assertEquals(1, statsCount);
		// group by: user
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_USER), null, false, 0);
		assertNotNull(stats);
		// updated to 2, since there were only 2 users in 'site-a-id' at the time with matching events
		assertEquals(2, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_USER));
		// updated to 2, since there were only 2 users in 'site-a-id' at the time with matching events
		assertEquals(2, statsCount);
		// group by: tool
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_TOOL), null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, Arrays.asList(StatsManager.T_TOOL));
		assertEquals(3, statsCount);  // is not 2 because count has not (manually) aggregated by tool
		// group by: tool and user (previously, getting wrong results due to STAT-221)
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_USER, StatsManager.T_TOOL), null, false, 0);
		assertNotNull(stats);
		assertEquals(4, stats.size()); // before STAT-221 was 2 (wrong)
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, Arrays.asList(StatsManager.T_USER, StatsManager.T_TOOL));
		assertEquals(4, statsCount);
		// group by: event
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_TOOL, StatsManager.T_EVENT), null, false, 0);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, Arrays.asList(StatsManager.T_TOOL, StatsManager.T_EVENT));
		assertEquals(3, statsCount);
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_EVENT), null, false, 0);
		assertNotNull(stats);
		assertEquals(3, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, Arrays.asList(FakeData.EVENT_CONTENTNEW, FakeData.EVENT_CONTENTDEL, FakeData.EVENT_CHATNEW),
				null, null, null, false, Arrays.asList(StatsManager.T_EVENT));
		assertEquals(3, statsCount);
		// group by: date
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(6, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATE));
		assertEquals(6, statsCount);
		// group by: datemonth
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEMONTH), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATEMONTH));
		assertTrue(statsCount <= 2);
		// group by: dateyear
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEYEAR), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATEYEAR));
		assertTrue(statsCount <= 2);
		// group by: lastdate
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_LASTDATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_LASTDATE));
		assertEquals(1, statsCount);
		// group by: visits
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_VISITS), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(18, ((SiteVisits)stats.get(0)).getTotalVisits());
		assertEquals(2, ((SiteVisits)stats.get(0)).getTotalUnique());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_VISITS));
		assertEquals(1, statsCount);
		// group by: uniquevisits
		stats = statsManager.getEventStats(FakeData.SITE_A_ID, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_UNIQUEVISITS), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(18, ((SiteVisits)stats.get(0)).getTotalVisits());
		assertEquals(2, ((SiteVisits)stats.get(0)).getTotalUnique());
		statsCount = statsManager.getEventStatsRowCount(FakeData.SITE_A_ID, null,
				null, null, null, false, Arrays.asList(StatsManager.T_UNIQUEVISITS));
		assertEquals(1, statsCount);
		
		//log.debug("Stats: "+stats);
		//log.debug("Size: "+stats.size());
	}
	
	@Test
	public void testResourceStats() {
		statsUpdateManager.collectEvents(getSampleData());
		Date now = new Date();
		Date today = new Date(now.getTime() + 24*60*60*1000);
		Date sixDaysBefore = new Date(today.getTime() - 6*24*60*60*1000);
		
		// #1
		List<Stat> stats = statsManager.getResourceStats(FakeData.SITE_A_ID);
		assertEquals(2, stats.size());
		
		// #2
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());
		int statsCount = statsManager.getResourceStatsRowCount(null, null, null,
				null, null, null, false, null);
		assertEquals(2, statsCount);
		
		
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, null, null,
				sixDaysBefore, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				sixDaysBefore, null, null, false, null);
		assertEquals(1, statsCount);
			
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_REVS, null,
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_REVS, null,
				null, null, Arrays.asList(FakeData.USER_B_ID), false, null);
		assertEquals(1, statsCount);
		
		// test inverse selection
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_NEW ,null,
				null, null, null, true, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		assertEquals(FakeData.USER_A_ID, stats.get(0).getUserId());
		
		// test paging
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, new PagingPosition(1, 1),
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(null, null, null,
				null, null, null, false, null);
		assertEquals(2, statsCount);
		
		// test max results
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, new PagingPosition(1, 1),
				null, null, false, 1);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		
		// test columns with sorting
		stats = statsManager.getResourceStats(null, null, null,
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
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_RESOURCE_ACTION), StatsManager.T_RESOURCE_ACTION, false, 0);
		assertNotNull(stats);
		assertEquals(ReportManager.WHAT_RESOURCES_ACTION_REVS, ((ResourceStat)stats.get(0)).getResourceAction());
		assertEquals(2, stats.size());
		
		// group by: defaults
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, null, 
				null, null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(null, null, null,
				null, null, null, false, null);
		assertEquals(2, statsCount);
		// group by: site
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_USER), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(null, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_USER));
		assertEquals(1, statsCount);
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE, StatsManager.T_RESOURCE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(null, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE, StatsManager.T_RESOURCE));
		assertEquals(1, statsCount);
		stats = statsManager.getResourceStats(null, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_SITE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(null, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_SITE));
		assertEquals(1, statsCount);
		// group by: user
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_USER), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_USER));
		assertEquals(1, statsCount);
		// group by: resource
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, null, Arrays.asList("/content/group/"+FakeData.SITE_A_ID+"/resource_id"),
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_RESOURCE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, null, Arrays.asList("/content/group/"+FakeData.SITE_A_ID+"/resource_id"),
				null, null, null, false, Arrays.asList(StatsManager.T_RESOURCE));
		assertEquals(1, statsCount);
		// group by: resource action
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_NEW, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_RESOURCE_ACTION), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, ReportManager.WHAT_RESOURCES_ACTION_NEW, null,
				null, null, null, false, Arrays.asList(StatsManager.T_RESOURCE_ACTION));
		assertEquals(1, statsCount);
		// group by: date
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(2, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATE));
		assertEquals(2, statsCount);
		// group by: datemonth
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEMONTH), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATEMONTH));
		assertTrue(statsCount <= 2);
		// group by: dateyear
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_DATEYEAR), null, false, 0);
		assertNotNull(stats);
		assertTrue(stats.size() <= 2);
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_DATEYEAR));
		assertTrue(statsCount <= 2);
		// group by: lastdate
		stats = statsManager.getResourceStats(FakeData.SITE_A_ID, null, null,
				null, null, null, false, null, 
				Arrays.asList(StatsManager.T_LASTDATE), null, false, 0);
		assertNotNull(stats);
		assertEquals(1, stats.size());
		statsCount = statsManager.getResourceStatsRowCount(FakeData.SITE_A_ID, null, null,
				null, null, null, false, Arrays.asList(StatsManager.T_LASTDATE));
		assertEquals(1, statsCount);
	}
	
	@Test
	public void testLargeMembershipSite() {
		// For development only: this tests take too long!
		if(enableLargeMembershipTest) {
			// sample data
			statsUpdateManager.collectEvents(getSampleData2());
			
			// all users list
			List<String> allUsers = new ArrayList<String>();
			for(int i=0; i<FakeData.SITE_C_USER_COUNT; i++) {
				allUsers.add(FakeData.USER_ID_PREFIX + (i+1));
			}
			
			// test visits
			{
				List<Stat> stats = statsManager.getEventStats(null, Arrays.asList(StatsManager.SITEVISIT_EVENTID),
						null, null, allUsers, false, null, 
						null, null, false, 0);
				assertNotNull(stats);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, stats.size());
				int statsCount = statsManager.getEventStatsRowCount(null, Arrays.asList(StatsManager.SITEVISIT_EVENTID),
						null, null, allUsers, false, null);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, statsCount);
			}
			
			// test activity
			{
				List<Stat> stats = statsManager.getEventStats(null, Arrays.asList(FakeData.EVENT_CHATNEW),
						null, null, allUsers, false, null, 
						null, null, false, 0);
				assertNotNull(stats);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, stats.size());
				int statsCount = statsManager.getEventStatsRowCount(null, Arrays.asList(FakeData.EVENT_CHATNEW),
						null, null, allUsers, false, null);
				assertEquals(FakeData.SITE_C_USER_COUNT / 2, statsCount);
			}
			
			// test resources
			{
				List<Stat> stats = statsManager.getResourceStats(null, null, null,
						null, null, allUsers, false, null, 
						null, null, false, 0);
				assertNotNull(stats);
				assertEquals(FakeData.SITE_C_USER_COUNT, stats.size());
				int statsCount = statsManager.getResourceStatsRowCount(null, null, null,
						null, null, allUsers, false, null);
				assertEquals(FakeData.SITE_C_USER_COUNT, statsCount);
			}
		}
	}
}
