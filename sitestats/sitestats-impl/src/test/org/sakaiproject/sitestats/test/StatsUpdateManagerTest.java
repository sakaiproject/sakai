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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.impl.CustomEventImpl;
import org.sakaiproject.sitestats.impl.EventStatImpl;
import org.sakaiproject.sitestats.impl.JobRunImpl;
import org.sakaiproject.sitestats.impl.ResourceStatImpl;
import org.sakaiproject.sitestats.impl.SiteActivityImpl;
import org.sakaiproject.sitestats.impl.SitePresenceImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.impl.report.ReportManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeEvent;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
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
public class StatsUpdateManagerTest extends AbstractTransactionalJUnit4SpringContextTests {

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
		FakeSite siteB = spy(FakeSite.class).set(FakeData.SITE_B_ID, Arrays.asList(FakeData.TOOL_CHAT));
		siteB.setUsers(new HashSet<>(Collections.singletonList(FakeData.USER_A_ID)));
		siteB.setMembers(new HashSet<>(Collections.singletonList(FakeData.USER_A_ID)));
		when(siteService.getSite(FakeData.SITE_B_ID)).thenReturn(siteB);
		when(siteService.isUserSite(FakeData.SITE_B_ID)).thenReturn(false);
		when(siteService.isSpecialSite(FakeData.SITE_B_ID)).thenReturn(false);

		// This is needed to make the tests deterministic, otherwise on occasion the collect thread will run
		// and break the tests.
		statsUpdateManager.setCollectThreadEnabled(false);

		((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setShowAnonymousAccessEvents(true);
		((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setEnableSitePresences(true);
		((ReportManagerImpl) ((Advised) reportManager).getTargetSource().getTarget()).setResourceLoader(resourceLoader);
	}

	// Basic tests: not much to test, work is on other methods...
	@SuppressWarnings("unchecked")
	@Test
	public void testCollectEvent() {
		FakeEvent e1 = spy(FakeEvent.class).set(FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, true, 0);
		assertTrue(statsUpdateManager.collectEvent(e1));
		Event e2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, "FakeData.USER_A_ID", "session-id-a");
		assertTrue(statsUpdateManager.collectEvent(e2));
		
		// check results
		List<EventStat> results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, results.size());
		EventStat es1 = results.get(0);
		assertEquals(e1.getContext() ,es1.getSiteId());
		assertEquals(e1.getUserId(), es1.getUserId());
		assertEquals(e1.getEvent(), es1.getEventId());
		assertEquals(1, es1.getCount());
		EventStat es2 = results.get(1);
		assertEquals(e2.getContext(), es2.getSiteId());
		assertEquals(e2.getUserId(), es2.getUserId());
		assertEquals(e2.getEvent(), es2.getEventId());
		assertEquals(1, es2.getCount());
	}		
	
	// Test invalid events
	@SuppressWarnings("unchecked")
	@Test
	public void testInvalidEvents() {
		// #1: send invalid events
		Event e3 = statsUpdateManager.buildEvent(new Date(), "unknown.event", "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e4 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "no_context", null, FakeData.USER_A_ID, "session-id-a");
		Event e5 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, null, null);
		Event e6 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "no_context", null, FakeData.USER_A_ID, "session-id-a");
		Event e7 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "no_context", null, null, null);
		Event e8 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e9 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, null, null);
		Event e10 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/non_existent_site-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event e11 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/user/something", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e12 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/attachment/something", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e13 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/small_ref", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e14 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/private", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e15 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group-user/small_ref", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e16 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		((Observer)statsUpdateManager).update(new Observable(), "this_is_not_an_event");
		assertTrue(statsUpdateManager.collectEvents((List<Event>)null));
		assertTrue(statsUpdateManager.collectEvents(new ArrayList<Event>()));
		assertTrue(statsUpdateManager.collectEvents(new Event[]{}));
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(null, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16)));
		assertTrue(statsUpdateManager.collectEvents(new Event[]{null, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16}));
		assertTrue(statsUpdateManager.collectEvent(null));
		assertTrue(statsUpdateManager.collectEvent(e3));
		assertTrue(statsUpdateManager.collectEvent(e4));
		assertTrue(statsUpdateManager.collectEvent(e5));
		assertTrue(statsUpdateManager.collectEvent(e6));
		assertTrue(statsUpdateManager.collectEvent(e7));
		assertTrue(statsUpdateManager.collectEvent(e8));
		assertTrue(statsUpdateManager.collectEvent(e9));
		assertTrue(statsUpdateManager.collectEvent(e10));
		assertTrue(statsUpdateManager.collectEvent(e11));
		assertTrue(statsUpdateManager.collectEvent(e12));
		assertTrue(statsUpdateManager.collectEvent(e13));
		assertTrue(statsUpdateManager.collectEvent(e14));
		assertTrue(statsUpdateManager.collectEvent(e15));
		assertTrue(statsUpdateManager.collectEvent(e16));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(0, r1.size());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(0, r3.size());
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(0, r4.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSiteVisitsDifferentUsers() {
		// #1 Test: 2 site visit (different users)
		Event eSV1 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_B_ID, "session-id-b");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, r1.size());
		EventStat es1 = r1.get(0);
		EventStat es2 = r1.get(1);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		assertEquals(eSV1.getEvent(), es1.getEventId());
		assertEquals(eSV2.getEvent(), es2.getEventId());
		if (eSV1.getUserId().equals(es1.getUserId())) {
			assertEquals(eSV1.getUserId(), es1.getUserId());
			assertEquals(eSV2.getUserId(), es2.getUserId());
		} else {
			assertEquals(eSV1.getUserId(), es2.getUserId());
			assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		assertEquals(1, es1.getCount());
		assertEquals(1, es2.getCount());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, r2.size());
		SiteVisits sv = r2.get(0);
		assertEquals(FakeData.SITE_A_ID, sv.getSiteId());
		assertEquals(2, sv.getTotalVisits());
		assertEquals(2, sv.getTotalUnique());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(0, r3.size());
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(0, r4.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSiteVisitsSameUsers() {
		// #2 Test: 2 site visit (same users)
		Event eSV1 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #2: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, r1.size());
		EventStat es1 = r1.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eSV1.getEvent(), es1.getEventId());
		assertEquals(eSV1.getUserId(), es1.getUserId());
		assertEquals(2, es1.getCount());
		// #2: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, r2.size());
		SiteVisits sv = r2.get(0);
		assertEquals(FakeData.SITE_A_ID, sv.getSiteId());
		assertEquals(2, sv.getTotalVisits());
		assertEquals(1, sv.getTotalUnique());
		// #2: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(0, r3.size());
		// #2: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(0, r4.size());
	}

	@Test
	public void testSitePresenceSplitUpdates() {
	    // Start and end across collections.
		{
			List<Event> events = new ArrayList<>();
			events.add(statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id"));
			assertTrue(statsUpdateManager.collectEvents(events));
		}
		{
			List<Event> events = new ArrayList<>();
			events.add(statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id"));
			assertTrue(statsUpdateManager.collectEvents(events));
		}

		// Start and end in the same collection.
		{
			List<Event> events = new ArrayList<>();
			events.add(statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id"));
			events.add(statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id"));
			assertTrue(statsUpdateManager.collectEvents(events));
		}
		// Multiple end events in the same collection.
		{
			List<Event> events = new ArrayList<>();
			events.add(statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id"));
			events.add(statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id"));
			events.add(statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id"));
			assertTrue(statsUpdateManager.collectEvents(events));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testActivityEventDifferentUsers() {
		// #1 Test: 2 new chat msg (different users)
		Event eSV1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/" + FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/" + FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, r1.size());
		EventStat es1 = r1.get(0);
		EventStat es2 = r1.get(1);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		assertEquals(eSV1.getEvent(), es1.getEventId());
		assertEquals(eSV2.getEvent(), es2.getEventId());
		assertEquals(1, es1.getCount());
		assertEquals(1, es2.getCount());
		if (eSV1.getUserId().equals(es1.getUserId())) {
			assertEquals(eSV1.getUserId(), es1.getUserId());
			assertEquals(eSV2.getUserId(), es2.getUserId());
		} else {
			assertEquals(eSV1.getUserId(), es2.getUserId());
			assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(1, r3.size());
		SiteActivity sa = r3.get(0);
		assertEquals(FakeData.SITE_A_ID, sa.getSiteId());
		assertEquals(FakeData.EVENT_CHATNEW, sa.getEventId());
		assertEquals(2, sa.getCount());
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(0, r4.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testActivityEventSameUsers() {
		// #2 Test: 2 new chat msg (same users)
		Event eSV1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #2: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, r1.size());
		EventStat es1 = r1.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eSV1.getEvent(), es1.getEventId());
		assertEquals(eSV1.getUserId(), es1.getUserId());
		assertEquals(2, es1.getCount());
		// #2: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #2: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(1, r3.size());
		SiteActivity sa = r3.get(0);
		assertEquals(FakeData.SITE_A_ID, sa.getSiteId());
		assertEquals(FakeData.EVENT_CHATNEW, sa.getEventId());
		assertEquals(2, sa.getCount());
		// #2: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(0, r4.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testResourceActivityTwoSites() {
		// Test:
		//		2 new resource    (site-a, user-a and user-b) 
		//		2 new resource    (site-b, 2x user-a)
		//		1 resource revise (site-b, user-b)
		Event e1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/" + FakeData.SITE_A_ID + "/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/" + FakeData.SITE_A_ID + "/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-b");
		Event e3 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/" + FakeData.SITE_B_ID + "/resource_id", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event e4 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/" + FakeData.SITE_B_ID + "/resource_id", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event e5 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTREV, "/content/group/" + FakeData.SITE_B_ID + "/resource_id", FakeData.SITE_B_ID, FakeData.USER_B_ID, "session-id-b");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(e1, e2, e3, e4, e5)));
		// #1: SST_EVENTS
		List<EventStat> r1 = db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, r1.size());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(1, r3.size());
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(2, r4.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testResourceActivityWithAttachments() {
		// Test: valid resource events
		Event e1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/attachment/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e3 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group-user/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(e1, e2, e3)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, r1.size());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(1, r3.size());
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(3, r4.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSitePresencesDifferentUsers() throws InterruptedException {
		// #1 Test : 2 site visit (different users)
		// BEGIN SITE PRESENCE
		Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final Event eSV1 = statsUpdateManager.buildEvent(Date.from(now), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		final Event eSV2 = statsUpdateManager.buildEvent(Date.from(now), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_B_ID, "session-id-b");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV1, eSV2)));
		// ... check SST_PRESENCES
		List<SitePresence> r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		assertEquals(2, r1.size());
		SitePresence es1 = r1.get(0);
		SitePresence es2 = r1.get(1);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		if (eSV1.getUserId().equals(es1.getUserId())) {
			assertEquals(eSV1.getUserId(), es1.getUserId());
			assertEquals(eSV2.getUserId(), es2.getUserId());
		} else {
			assertEquals(eSV1.getUserId(), es2.getUserId());
			assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		assertNotNull(es1.getLastVisitStartTime());
		assertNotNull(es2.getLastVisitStartTime());
		assertEquals(now.toEpochMilli(), es1.getLastVisitStartTime().getTime());
		assertEquals(now.toEpochMilli(), es2.getLastVisitStartTime().getTime());

		// END SITE PRESENCE
		// give it time before ending presence
		Thread.sleep(1100);
		now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final Event eSV1e = statsUpdateManager.buildEvent(Date.from(now), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		final Event eSV2e = statsUpdateManager.buildEvent(Date.from(now), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_B_ID, "session-id-b");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV1e, eSV2e)));
		// ... check SST_PRESENCES
		r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		assertEquals(2, r1.size());
		es1 = r1.get(0);
		es2 = r1.get(1);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		if (eSV1.getUserId().equals(es1.getUserId())) {
			assertEquals(eSV1.getUserId(), es1.getUserId());
			assertEquals(eSV2.getUserId(), es2.getUserId());
		} else {
			assertEquals(eSV1.getUserId(), es2.getUserId());
			assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		assertNull(es1.getLastVisitStartTime());
		assertNull(es2.getLastVisitStartTime());
		assertTrue(es1.getDuration() >= 1000);
		assertTrue(es2.getDuration() >= 1000);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSitePresencesTwoSitesSameUsers() throws InterruptedException {
		// #2 Test: 2 site visit (same users)
		// BEGIN SITE PRESENCE
		Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final Event eventBegin1 = statsUpdateManager.buildEvent(Date.from(now), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eventBegin1)));
		// give it time before ending presence
		Thread.sleep(1100);
		Instant now2 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		long secondDuration = now2.toEpochMilli() - now.toEpochMilli();
		final Event eventEnd1 = statsUpdateManager.buildEvent(Date.from(now2), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eventEnd1)));
		// ... check SST_PRESENCES
		List<SitePresence> results = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		assertEquals(1, results.size());
		SitePresence es1 = results.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eventBegin1.getUserId(), es1.getUserId());
		assertNull(es1.getLastVisitStartTime());
		long totalDuration = es1.getDuration();
		long firstDuration = totalDuration;
		assertEquals(totalDuration, secondDuration);

		// END SITE PRESENCE
		now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final Event eventBegin2 = statsUpdateManager.buildEvent(Date.from(now), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eventBegin2)));
		// give it time before ending presence
		Thread.sleep(1100);
		now2 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		secondDuration = now2.toEpochMilli() - now.toEpochMilli();
		final Event eventEnd2 = statsUpdateManager.buildEvent(Date.from(now2), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eventEnd2)));
		// ... check SST_PRESENCES
		results = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		assertEquals(1, results.size());
		es1 = results.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eventBegin2.getUserId(), es1.getUserId());
		assertNull(es1.getLastVisitStartTime());
		totalDuration = es1.getDuration();
		assertTrue(totalDuration == firstDuration + secondDuration);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSitePresencesExistingPresence() throws InterruptedException {
		// #3 Test: one pres.end (with one pres.begin already on db)
		// insert related pres.begin directly on db
		Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		long firstDuration = 10000;
		SitePresence sp1 = new SitePresenceImpl();
		sp1.setSiteId(FakeData.SITE_A_ID);
		sp1.setDate(Date.from(now));
		sp1.setUserId(FakeData.USER_A_ID);
		sp1.setDuration(firstDuration);
		sp1.setLastVisitStartTime(Date.from(now));
		db.insertObject(sp1);
		assertTrue(db.getResultsForClass(SitePresenceImpl.class).size() == 1);
		// generate pres.end for processing in SST
		// give it time before ending presence
		Thread.sleep(1100);
		Instant now2 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		long secondDuration = now2.toEpochMilli() - now.toEpochMilli();
		final Event eSV5e = statsUpdateManager.buildEvent(Date.from(now2), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV5e)));
		List<SitePresence> r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		assertEquals(1, r1.size());
		SitePresence es1 = r1.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eSV5e.getUserId(), es1.getUserId());
		assertNull(es1.getLastVisitStartTime());
		long totalDuration = es1.getDuration();
		assertTrue(totalDuration == firstDuration + secondDuration);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSitePresencesExistingPresenceWithNoDuration() throws InterruptedException {
		// #4 Test: one pres.end (with one pres.begin already on db, with duration = 0)
		// insert related pres.begin directly on db
		Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		long firstDuration = 0;
		SitePresence sp1 = new SitePresenceImpl();
		sp1.setSiteId(FakeData.SITE_A_ID);
		sp1.setDate(Date.from(now));
		sp1.setUserId(FakeData.USER_A_ID);
		sp1.setDuration(firstDuration);
		sp1.setLastVisitStartTime(Date.from(now));
		db.insertObject(sp1);
		assertTrue(db.getResultsForClass(SitePresenceImpl.class).size() == 1);
		// generate pres.end for processing in SST
		// give it time before ending presence
		Thread.sleep(1100);
		Instant now2 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		long secondDuration = now2.toEpochMilli() - now.toEpochMilli();
		final Event eSV6e = statsUpdateManager.buildEvent(Date.from(now2), StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(statsUpdateManager.collectEvents(Arrays.asList(eSV6e)));
		List<SitePresence> r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		assertEquals(1, r1.size());
		SitePresence es1 = r1.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eSV6e.getUserId(), es1.getUserId());
		assertNull(es1.getLastVisitStartTime());
		long totalDuration = es1.getDuration();
		assertTrue(totalDuration == firstDuration + secondDuration);
	}
	
	// Test (remaining) CustomEventImpl fields
	public void testCustomEventImpl() {
		CustomEventImpl e1 = new CustomEventImpl(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a", '-');
		assertEquals(false, e1.getModify());
		assertEquals(0, e1.getPriority());
		e1 = new CustomEventImpl(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a", 'm');
		assertEquals(true, e1.getModify());
	}
	
	// Basic configuration test
	@SuppressWarnings("unchecked")
	@Ignore
	@Test
	public void testConfigIsCollectThreadEnabled() throws Exception {
		statsUpdateManager.setCollectThreadUpdateInterval(50);
		statsUpdateManager.setCollectThreadEnabled(true);

		// #1: collect thread enabled/disabled
		assertTrue(statsUpdateManager.isCollectThreadEnabled());
		// turn it off
		statsUpdateManager.setCollectThreadEnabled(false);
		assertFalse(statsUpdateManager.isCollectThreadEnabled());
		// give it time to stop thread
		Thread.sleep(200);
		Event e1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		((StatsUpdateManagerImpl) ((Advised) statsUpdateManager).getTargetSource().getTarget()).update(null, e1);
		List<EventStat> results = db.getResultsForClass(EventStatImpl.class);
		assertEquals(0, results.size());
		// turn it on again
		statsUpdateManager.setCollectThreadEnabled(true);
		assertTrue(statsUpdateManager.isCollectThreadEnabled());
		Thread.sleep(300);
		((StatsUpdateManagerImpl) ((Advised) statsUpdateManager).getTargetSource().getTarget()).update(null, e1);
		while (!statsUpdateManager.isIdle()) {
			// give it time to process event
			Thread.sleep(300);
		}
		results = db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, results.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigCollectThreadUpdateInterval() throws Exception {
		// #2: collect thread update interval
		statsUpdateManager.setCollectThreadUpdateInterval(50);
		assertEquals(50, statsUpdateManager.getCollectThreadUpdateInterval());
		statsUpdateManager.setCollectThreadEnabled(true);

		// make sure it processes
		Event e1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		((StatsUpdateManagerImpl) ((Advised) statsUpdateManager).getTargetSource().getTarget()).update(null, e1);
		Thread.sleep(500);

		List<EventStat> results = db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, results.size());
		
		// make sure it doesn't process it
		statsUpdateManager.setCollectThreadUpdateInterval(500);
		assertEquals(500, statsUpdateManager.getCollectThreadUpdateInterval());
		Event e2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-a");
		((StatsUpdateManagerImpl) ((Advised) statsUpdateManager).getTargetSource().getTarget()).update(null, e2);
		results = db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, results.size());

		statsUpdateManager.setCollectThreadUpdateInterval(50);
		assertEquals(50, statsUpdateManager.getCollectThreadUpdateInterval());
		Thread.sleep(500);
		results = db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, results.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsCollectAdminEvents() {
		// #3: collect admin events
		statsUpdateManager.setCollectAdminEvents(true);
		assertEquals(true, statsUpdateManager.isCollectAdminEvents());
		// make sure it processes admin events
		Event e1 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+PresenceService.PRESENCE_SUFFIX, null, "admin", "session-id-a");
		Event e2 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		statsUpdateManager.collectEvents(Arrays.asList(e1, e2));
		List<SiteVisits> results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		assertEquals(2, results2.get(0).getCount());
		// make sure it doesn't processes admin events
		statsUpdateManager.setCollectAdminEvents(false);
		assertEquals(false, statsUpdateManager.isCollectAdminEvents());
		statsUpdateManager.collectEvents(Arrays.asList(e1, e2));
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		assertEquals(3, results2.get(0).getCount());
		statsUpdateManager.setCollectAdminEvents(true);
		assertEquals(true, statsUpdateManager.isCollectAdminEvents());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsCollectEventsForSiteWithToolOnly() {
		// #4: collect for sites with SiteStats only
		// make sure events get processed for sites with SiteStats only
		statsUpdateManager.setCollectEventsForSiteWithToolOnly(true);
		assertEquals(true, statsUpdateManager.isCollectEventsForSiteWithToolOnly());
		Event e1 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		statsUpdateManager.collectEvent(e1);
		List<SiteVisits> results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		Event e2 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_B_ID + PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		statsUpdateManager.collectEvent(e2);
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsCollectEventsForAnySite() {
		// make sure events get processed for any sites
		statsUpdateManager.setCollectEventsForSiteWithToolOnly(false);
		assertFalse(statsUpdateManager.isCollectEventsForSiteWithToolOnly());
		Event e1 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		statsUpdateManager.collectEvent(e1);
		List<SiteVisits> results = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results.size());
		Event e2 = statsUpdateManager.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_B_ID+PresenceService.PRESENCE_SUFFIX, null, FakeData.USER_A_ID, "session-id-a");
		statsUpdateManager.collectEvent(e2);
		results = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(2, results.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsShowAnonymousAccessEvents() throws Exception {
		// #3: ShowAnonymousAccessEvents
		assertTrue(statsManager.isShowAnonymousAccessEvents());
		// make sure it processes access events from anonymous
		Event e1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, EventTrackingService.UNKNOWN_USER, "session-id-a");
		Event e2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		statsUpdateManager.collectEvents(Arrays.asList(e1, e2));
		List<ResourceStat> results = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(2, results.size());
		// make sure it doesn't processes access events from anonymous
		StatsManagerImpl smi = (StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget();
		smi.setEventContextSupported(true);
		smi.setShowAnonymousAccessEvents(false);
		smi.setEnableSitePresences(true);
		assertTrue(statsManager.isEventContextSupported());
		assertFalse(statsManager.isShowAnonymousAccessEvents());
		assertTrue(statsManager.getEnableSitePresences());

		statsUpdateManager.collectEvents(Arrays.asList(e1, e2));
		results = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(2, results.size());
		// revert setting
		smi.setShowAnonymousAccessEvents(true);
		assertTrue(statsManager.isShowAnonymousAccessEvents());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsEventContextSupported() throws Exception {
		// #3: EventContextSupported
		assertEquals(true, statsManager.isEventContextSupported());

		// make sure it processes both events
		Event e1 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/non_existent_site/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e2 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", null, FakeData.USER_B_ID, "session-id-a");
		statsUpdateManager.collectEvents(Arrays.asList(e1, e2));
		List<EventStat> results1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, results1.size());

		// none of these events will be picked up
		((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setEventContextSupported(false);
		assertEquals(false, statsManager.isEventContextSupported());
		Event e3 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTREAD, "/content/group/non_existent_site/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e4 = statsUpdateManager.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", null, FakeData.USER_B_ID, "session-id-a");
		statsUpdateManager.collectEvents(Arrays.asList(e3, e4));
		List<EventStat> results2 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, results2.size());
	}
	
	// Test JobRun related methods
	@Test
	public void testJobRunMethods() {
		Date now = new Date();
		Date twentyMinBefore = new Date(now.getTime() - 1200*1000);
		Date fifteenMinBefore = new Date(now.getTime() - 900*1000);
		Date tenMinBefore = new Date(now.getTime() - 600*1000);
		Date fiveMinBefore = new Date(now.getTime() - 300*1000);

		// setup JobRun objects
		JobRun jobRun1 = new JobRunImpl();
		jobRun1.setStartEventId(1);
		jobRun1.setEndEventId(10);
		jobRun1.setJobStartDate(fifteenMinBefore);
		jobRun1.setJobEndDate(tenMinBefore);
		jobRun1.setLastEventDate(twentyMinBefore);

		JobRun jobRun2 = new JobRunImpl();
		jobRun2.setStartEventId(11);
		jobRun2.setEndEventId(20);
		jobRun2.setJobStartDate(tenMinBefore);
		jobRun2.setJobEndDate(fiveMinBefore);
		jobRun2.setLastEventDate(fifteenMinBefore);

		// test getEventDateFromLatestJobRun() == null
		try{
			assertNull(statsUpdateManager.getEventDateFromLatestJobRun());
		}catch(Exception e){
			fail("There should be no JobRun in database! [1]");
		}
		
		// test getLatestJobRun() == null
		try{
			assertNull(statsUpdateManager.getLatestJobRun());
		}catch(Exception e){
			fail("There should be no JobRun in database! [2]");
		}
		
		// test save of null JobRun
		assertFalse(statsUpdateManager.saveJobRun(null));
		
		// save objects to db
		assertTrue(statsUpdateManager.saveJobRun(jobRun1));
		assertTrue(statsUpdateManager.saveJobRun(jobRun2));
		
		// test getLatestJobRun()
		JobRun jr = null;
		try{
			jr = statsUpdateManager.getLatestJobRun();
		}catch(Exception e){
			fail("There is no JobRun in database! [1]");
		}
		assertEquals(jobRun2.getId(), jr.getId());
		assertEquals(jobRun2.getStartEventId(), jr.getStartEventId());
		assertEquals(jobRun2.getEndEventId(), jr.getEndEventId());
	}
}
