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


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.impl.CustomEventImpl;
import org.sakaiproject.sitestats.impl.EventStatImpl;
import org.sakaiproject.sitestats.impl.JobRunImpl;
import org.sakaiproject.sitestats.impl.ResourceStatImpl;
import org.sakaiproject.sitestats.impl.SiteActivityImpl;
import org.sakaiproject.sitestats.impl.SitePresenceImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeEvent;
import org.sakaiproject.sitestats.test.mocks.FakeEventRegistryService;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@FixMethodOrder(NAME_ASCENDING)
@ContextConfiguration(locations={
		"/hbm-db.xml",
		"/hibernate-test.xml"})
public class StatsUpdateManagerTest extends AbstractJUnit4SpringContextTests { 

	@Autowired
	private StatsUpdateManager			M_sum;
	private StatsManager				M_sm;
	@Autowired
	private DB							db;
	private SiteService					M_ss;
	@Autowired
	private EventTrackingService		M_ets;
	@Autowired
	private FakeEventRegistryService	M_ers;
	
	@Before
	public void onSetUp() throws Exception {
		db.deleteAll();
		// Site Service
		M_ss = createMock(SiteService.class);		
		// Site A has SiteStats
		Site siteA = new FakeSite(FakeData.SITE_A_ID, StatsManager.SITESTATS_TOOLID);
		expect(M_ss.getSite(FakeData.SITE_A_ID)).andStubReturn(siteA);
		expect(M_ss.isUserSite(FakeData.SITE_A_ID)).andStubReturn(false);
		expect(M_ss.isSpecialSite(FakeData.SITE_A_ID)).andStubReturn(false);		
		// Site B don't have SiteStats
		FakeSite siteB = new FakeSite(FakeData.SITE_B_ID);
		expect(M_ss.getSite(FakeData.SITE_B_ID)).andStubReturn(siteB);
		expect(M_ss.isUserSite(FakeData.SITE_B_ID)).andStubReturn(false);
		expect(M_ss.isSpecialSite(FakeData.SITE_B_ID)).andStubReturn(false);		
		// Site 'non_existent_site' doesn't exist
		expect(M_ss.getSite("non_existent_site")).andThrow(new IdUnusedException("non_existent_site")).anyTimes();
		expect(M_ss.isUserSite("non_existent_site")).andStubReturn(false);
		expect(M_ss.isSpecialSite("non_existent_site")).andStubReturn(false);
		// apply
		replay(M_ss);
		((StatsUpdateManagerImpl)M_sum).setSiteService(M_ss);
		
		// Stats Manager
		M_sm = createMock(StatsManager.class);
		// Default values
		expect(M_sm.isEventContextSupported()).andStubReturn(true);
		expect(M_sm.isShowAnonymousAccessEvents()).andStubReturn(true);
		expect(M_sm.isEnableSitePresences()).andStubReturn(true);	
		// apply
		replay(M_sm);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);
		// Setups fake dependencies.
		M_ers.setStatsManager(M_sm);
		// By default we don't enable the collection thread as it can interfere with tests.
		// If a test explicitly wants to test the collect thread it should re-enable it when it starts.
		M_sum.setCollectThreadEnabled(false);
	}

	// ---- TESTS ----
	
	// Basic tests: not much to test, work is on other methods...
	@SuppressWarnings("unchecked")
	@Test
	public void testCollectEvent() {
		FakeEvent e1 = new FakeEvent(FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, true, 0);
		Assert.assertTrue(M_sum.collectEvent(e1));
		Event e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, "FakeData.USER_A_ID", "session-id-a");
		Assert.assertTrue(M_sum.collectEvent(e2));
		
		// check results
		List<EventStat> results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(2, results.size());
		EventStat es1 = results.get(0);
		Assert.assertEquals(e1.getContext() ,es1.getSiteId());
		Assert.assertEquals(e1.getUserId(), es1.getUserId());
		Assert.assertEquals(e1.getEvent(), es1.getEventId());
		Assert.assertEquals(1, es1.getCount());
		EventStat es2 = results.get(1);
		Assert.assertEquals(e2.getContext(), es2.getSiteId());
		Assert.assertEquals(e2.getUserId(), es2.getUserId());
		Assert.assertEquals(e2.getEvent(), es2.getEventId());
		Assert.assertEquals(1, es2.getCount());
	}		
	
	// Test invalid events
	@SuppressWarnings("unchecked")
	@Test
	public void testInvalidEvents() {
		// #1: send invalid events
		Event e3 = M_sum.buildEvent(new Date(), "unknown.event", "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e4 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "no_context", null, FakeData.USER_A_ID, "session-id-a");
		Event e5 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, null, null);
		Event e6 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "no_context", null, FakeData.USER_A_ID, "session-id-a");
		Event e7 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "no_context", null, null, null);
		Event e8 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e9 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, null, null);
		Event e10 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/non_existent_site-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event e11 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/user/something", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e12 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/attachment/something", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e13 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/small_ref", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e14 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/private", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e15 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group-user/small_ref", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e16 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		((Observer)M_sum).update(new Observable(), "this_is_not_an_event");
		Assert.assertTrue(M_sum.collectEvents((List<Event>)null));
		Assert.assertTrue(M_sum.collectEvents(new ArrayList<Event>()));
		Assert.assertTrue(M_sum.collectEvents(new Event[]{}));
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(null, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16)));
		Assert.assertTrue(M_sum.collectEvents(new Event[]{null, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16}));
		Assert.assertTrue(M_sum.collectEvent(null));
		Assert.assertTrue(M_sum.collectEvent(e3));
		Assert.assertTrue(M_sum.collectEvent(e4));
		Assert.assertTrue(M_sum.collectEvent(e5));
		Assert.assertTrue(M_sum.collectEvent(e6));
		Assert.assertTrue(M_sum.collectEvent(e7));
		Assert.assertTrue(M_sum.collectEvent(e8));
		Assert.assertTrue(M_sum.collectEvent(e9));
		Assert.assertTrue(M_sum.collectEvent(e10));
		Assert.assertTrue(M_sum.collectEvent(e11));
		Assert.assertTrue(M_sum.collectEvent(e12));
		Assert.assertTrue(M_sum.collectEvent(e13));
		Assert.assertTrue(M_sum.collectEvent(e14));
		Assert.assertTrue(M_sum.collectEvent(e15));
		Assert.assertTrue(M_sum.collectEvent(e16));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(0, r1.size());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		Assert.assertEquals(0, r3.size());		
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(0, r4.size());	
	}
	
	// Site visits tests
	@SuppressWarnings("unchecked")
	@Test
	public void testSiteVisits() {
		// #1 Test: 2 site visit (different users)
		Event eSV1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(2, r1.size());
		EventStat es1 = r1.get(0);
		EventStat es2 = r1.get(1);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		Assert.assertEquals(eSV1.getEvent(), es1.getEventId());
		Assert.assertEquals(eSV2.getEvent(), es2.getEventId());
		if(eSV1.getUserId().equals(es1.getUserId())) {
			Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es2.getUserId());
		}else{
			Assert.assertEquals(eSV1.getUserId(), es2.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		Assert.assertEquals(1, es1.getCount());
		Assert.assertEquals(1, es2.getCount());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(1, r2.size());
		SiteVisits sv = r2.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, sv.getSiteId());
		Assert.assertEquals(2, sv.getTotalVisits());
		Assert.assertEquals(2, sv.getTotalUnique());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		Assert.assertEquals(0, r3.size());		
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(0, r4.size());	

		// #2 Test: 2 site visit (same users)
		db.deleteAll();
		eSV1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		eSV2 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #2: SST_EVENTS
		r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(1, r1.size());
		es1 = r1.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(eSV1.getEvent(), es1.getEventId());
		Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
		Assert.assertEquals(2, es1.getCount());
		// #2: SST_SITEVISITS
		r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(1, r2.size());
		sv = r2.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, sv.getSiteId());
		Assert.assertEquals(2, sv.getTotalVisits());
		Assert.assertEquals(1, sv.getTotalUnique());
		// #2: SST_SITEACTIVITY
		r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		Assert.assertEquals(0, r3.size());		
		// #2: SST_RESOURCES
		r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(0, r4.size());	
	}

	@Test
	public void testSitePresenceSplitUpdates() {
	    // Start and end across collections.
		{
			List<Event> events = new ArrayList<>();
			events.add(M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + "-presence", null, FakeData.USER_A_ID, "session-id"));
			Assert.assertTrue(M_sum.collectEvents(events));
		}
		{
			List<Event> events = new ArrayList<>();
			events.add(M_sum.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + "-presence", null, FakeData.USER_A_ID, "session-id"));
			Assert.assertTrue(M_sum.collectEvents(events));
		}

		// Start and end in the same collection.
		{
			List<Event> events = new ArrayList<>();
			events.add(M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + "-presence", null, FakeData.USER_A_ID, "session-id"));
			events.add(M_sum.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + "-presence", null, FakeData.USER_A_ID, "session-id"));
			Assert.assertTrue(M_sum.collectEvents(events));
		}
		// Multiple end events in the same collection.
		{
			List<Event> events = new ArrayList<>();
			events.add(M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/" + FakeData.SITE_A_ID + "-presence", null, FakeData.USER_A_ID, "session-id"));
			events.add(M_sum.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + "-presence", null, FakeData.USER_A_ID, "session-id"));
			events.add(M_sum.buildEvent(new Date(), StatsManager.SITEVISITEND_EVENTID, "/presence/" + FakeData.SITE_A_ID + "-presence", null, FakeData.USER_A_ID, "session-id"));
			Assert.assertTrue(M_sum.collectEvents(events));
		}
	}
	
	// Activity tests
	@SuppressWarnings("unchecked")
	@Test
	public void testActivityEvent() {
		// #1 Test: 2 new chat msg (different users)
		Event eSV1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(2, r1.size());
		EventStat es1 = r1.get(0);
		EventStat es2 = r1.get(1);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		Assert.assertEquals(eSV1.getEvent(), es1.getEventId());
		Assert.assertEquals(eSV2.getEvent(), es2.getEventId());
		Assert.assertEquals(1, es1.getCount());
		Assert.assertEquals(1, es2.getCount());
		if(eSV1.getUserId().equals(es1.getUserId())) {
			Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es2.getUserId());
		}else{
			Assert.assertEquals(eSV1.getUserId(), es2.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		Assert.assertEquals(1, r3.size());	
		SiteActivity sa = r3.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, sa.getSiteId());
		Assert.assertEquals(FakeData.EVENT_CHATNEW, sa.getEventId());
		Assert.assertEquals(2, sa.getCount());
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(0, r4.size());	

		// #2 Test: 2 new chat msg (same users)
		db.deleteAll();
		eSV1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		eSV2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #2: SST_EVENTS
		r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(1, r1.size());
		es1 = r1.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(eSV1.getEvent(), es1.getEventId());
		Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
		Assert.assertEquals(2, es1.getCount());
		// #2: SST_SITEVISITS
		r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(0, r2.size());
		// #2: SST_SITEACTIVITY
		r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		Assert.assertEquals(1, r3.size());	
		sa = r3.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, sa.getSiteId());
		Assert.assertEquals(FakeData.EVENT_CHATNEW, sa.getEventId());
		Assert.assertEquals(2, sa.getCount());
		// #2: SST_RESOURCES
		r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(0, r4.size());	
	}
	
	// Resource tests
	@SuppressWarnings("unchecked")
	@Test
	public void testResourceActivity() {
		// #1 Test: 
		//		2 new resource    (site-a, user-a and user-b) 
		//		2 new resource    (site-b, 2x user-a)
		//		1 resource revise (site-b, user-b)
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-a");
		Event e3 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event e4 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", FakeData.SITE_B_ID, FakeData.USER_A_ID, "session-id-a");
		Event e5 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTREV, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", FakeData.SITE_B_ID, FakeData.USER_B_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(e1, e2, e3, e4, e5)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(4, r1.size());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		Assert.assertEquals(3, r3.size());	
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(4, r4.size());
		
		// #2 Test: valid resource events
		db.deleteAll();
		Event e6 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/attachment/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e7 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e8 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group-user/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(e6, e7, e8)));
		// #1: SST_EVENTS
		r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(1, r1.size());
		// #1: SST_SITEVISITS
		r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		Assert.assertEquals(1, r3.size());	
		// #1: SST_RESOURCES
		r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(3, r4.size());
	}
	
	// Site visits, presence time tests
	@SuppressWarnings("unchecked")
	@Test
	public void testSitePresences() {
		//System.out.println("--- testSitePresences() :: START ---");
		long minPresenceTime = 100;

		// #1 Test : 2 site visit (different users)
		
		// BEGIN SITE PRESENCE
		Date now = new Date();
		Event eSV1 = M_sum.buildEvent(now, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = M_sum.buildEvent(now, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// ... check SST_PRESENCES
		List<SitePresence> r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		Assert.assertEquals(2, r1.size());
		SitePresence es1 = r1.get(0);
		SitePresence es2 = r1.get(1);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		if(eSV1.getUserId().equals(es1.getUserId())) {
			Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es2.getUserId());
		}else{
			Assert.assertEquals(eSV1.getUserId(), es2.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		Assert.assertNotNull(es1.getLastVisitStartTime());
		Assert.assertNotNull(es2.getLastVisitStartTime());
		Assert.assertTrue(es1.getLastVisitStartTime().equals(now) || es1.getLastVisitStartTime().before(now));
		Assert.assertTrue(es2.getLastVisitStartTime().equals(now) || es2.getLastVisitStartTime().before(now));
		
		// END SITE PRESENCE
		try{
			// give it time before ending presence
			Thread.sleep(minPresenceTime);			
		}catch(Exception e) {}
		now = new Date();
		Event eSV1e = M_sum.buildEvent(now, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event eSV2e = M_sum.buildEvent(now, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1e, eSV2e)));
		// ... check SST_PRESENCES
		r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		Assert.assertEquals(2, r1.size());
		es1 = r1.get(0);
		es2 = r1.get(1);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		if(eSV1.getUserId().equals(es1.getUserId())) {
			Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es2.getUserId());
		}else{
			Assert.assertEquals(eSV1.getUserId(), es2.getUserId());
			Assert.assertEquals(eSV2.getUserId(), es1.getUserId());
		}
		Assert.assertNull(es1.getLastVisitStartTime());
		Assert.assertNull(es2.getLastVisitStartTime());
		Assert.assertTrue(es1.getDuration() >= minPresenceTime);
		Assert.assertTrue(es2.getDuration() >= minPresenceTime);

		
		// #2 Test: 2 site visit (same users)
		db.deleteAll();
		//System.out.println("--- testSitePresences() :: START II ---");
		
		// BEGIN SITE PRESENCE
		now = new Date();
		eSV1 = M_sum.buildEvent(now, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1)));
		try{
			// give it time before ending presence
			minPresenceTime = 150;
			Thread.sleep(minPresenceTime);			
		}catch(Exception e) {}
		Date now2 = new Date();
		long secondDuration = now2.getTime() - now.getTime();
		eSV2 = M_sum.buildEvent(now2, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV2)));
		// ... check SST_PRESENCES
		r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		Assert.assertEquals(1, r1.size());
		es1 = r1.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
		Assert.assertNull(es1.getLastVisitStartTime());
		long totalDuration = es1.getDuration();
		long firstDuration = totalDuration;
		Assert.assertTrue(totalDuration == secondDuration);
		
		// END SITE PRESENCE
		now = new Date();
		eSV1e = M_sum.buildEvent(now, StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV1e)));
		try{
			// give it time before ending presence
			minPresenceTime = 250;
			Thread.sleep(minPresenceTime);			
		}catch(Exception e) {}
		now2 = new Date();
		secondDuration = now2.getTime() - now.getTime();
		eSV2e = M_sum.buildEvent(now2, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV2e)));
		// ... check SST_PRESENCES
		r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		Assert.assertEquals(1, r1.size());
		es1 = r1.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
		Assert.assertNull(es1.getLastVisitStartTime());
		totalDuration = es1.getDuration();
		//System.out.println("1. totalDuration: "+totalDuration);
		//System.out.println("1.   firstDuration: "+firstDuration);
		//System.out.println("1.   secondDuration: "+secondDuration);
		Assert.assertTrue(totalDuration == firstDuration + secondDuration);
		
		
		// #3 Test: one pres.end (with one pres.begin already on db)
		db.deleteAll();
		// insert related pres.begin directly on db
		now = new Date();
		Date dbDate = getTruncatedDate(now);
		firstDuration = 10000;
		SitePresence sp1 = new SitePresenceImpl();
		sp1.setSiteId(FakeData.SITE_A_ID);
		sp1.setDate(dbDate);
		sp1.setUserId(FakeData.USER_A_ID);
		sp1.setDuration(firstDuration);
		sp1.setLastVisitStartTime(now);
		db.insertObject(sp1);
		Assert.assertTrue(db.getResultsForClass(SitePresenceImpl.class).size() == 1);
		// generate pres.end for processing in SST
		try{
			// give it time before ending presence
			minPresenceTime = 300;
			Thread.sleep(minPresenceTime);			
		}catch(Exception e) {}
		now2 = new Date();
		secondDuration = now2.getTime() - now.getTime();
		eSV2e = M_sum.buildEvent(now2, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV2e)));
		r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		Assert.assertEquals(1, r1.size());
		es1 = r1.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
		Assert.assertNull(es1.getLastVisitStartTime());
		totalDuration = es1.getDuration();
		//System.out.println("2. totalDuration: "+totalDuration);
		//System.out.println("2.   firstDuration: "+firstDuration);
		//System.out.println("2.   secondDuration: "+secondDuration);
		Assert.assertTrue(totalDuration == firstDuration + secondDuration);
		//System.out.println("--- testSitePresences() :: END ---");
		
		
		// #4 Test: one pres.end (with one pres.begin already on db, with duration = 0)
		db.deleteAll();
		// insert related pres.begin directly on db
		now = new Date();
		firstDuration = 0;
		sp1 = new SitePresenceImpl();
		sp1.setSiteId(FakeData.SITE_A_ID);
		sp1.setDate(dbDate);
		sp1.setUserId(FakeData.USER_A_ID);
		sp1.setDuration(firstDuration);
		sp1.setLastVisitStartTime(now);
		db.insertObject(sp1);
		Assert.assertTrue(db.getResultsForClass(SitePresenceImpl.class).size() == 1);
		// generate pres.end for processing in SST
		try{
			// give it time before ending presence
			minPresenceTime = 300;
			Thread.sleep(minPresenceTime);			
		}catch(Exception e) {}
		now2 = new Date();
		secondDuration = now2.getTime() - now.getTime();
		eSV2e = M_sum.buildEvent(now2, StatsManager.SITEVISITEND_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Assert.assertTrue(M_sum.collectEvents(Arrays.asList(eSV2e)));
		r1 = (List<SitePresence>) db.getResultsForClass(SitePresenceImpl.class);
		Assert.assertEquals(1, r1.size());
		es1 = r1.get(0);
		Assert.assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		Assert.assertEquals(eSV1.getUserId(), es1.getUserId());
		Assert.assertNull(es1.getLastVisitStartTime());
		totalDuration = es1.getDuration();
		//System.out.println("3. totalDuration: "+totalDuration);
		//System.out.println("3.   firstDuration: "+firstDuration);
		//System.out.println("3.   secondDuration: "+secondDuration);
		Assert.assertTrue(totalDuration == firstDuration + secondDuration);
		//System.out.println("--- testSitePresences() :: END ---");
	}
	
	// Test (remaining) CustomEventImpl fields
	public void testCustomEventImpl() {
		CustomEventImpl e1 = new CustomEventImpl(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a", '-');
		Assert.assertEquals(false, e1.getModify());
		Assert.assertEquals(0, e1.getPriority());
		e1 = new CustomEventImpl(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a", 'm');
		Assert.assertEquals(true, e1.getModify());	
	}
	
	// Basic configuration test
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsCollectThreadEnabled() {
		db.deleteAll();
		M_sum.setCollectThreadUpdateInterval(50);
		M_sum.setCollectThreadEnabled(true);

		// #1: collect thread enabled/disabled
		Assert.assertEquals(true, M_sum.isCollectThreadEnabled());
		// turn it off
		M_sum.setCollectThreadEnabled(false);
		Assert.assertEquals(false, M_sum.isCollectThreadEnabled());
		try{
			// give it time to stop thread
			Thread.sleep(200);			
		}catch(Exception e) {}
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		M_ets.post(e1);
		List<EventStat> results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(0, results.size());
		// turn it on again
		M_sum.setCollectThreadEnabled(true);
		Assert.assertEquals(true, M_sum.isCollectThreadEnabled());
		try{
			// give it time to start thread
			Thread.sleep(200);			
		}catch(Exception e) {}
		M_ets.post(e1);
		while(!M_sum.isIdle()) {
			try{
				// give it time to process event
				Thread.sleep(300);			
			}catch(Exception e) {}
		}
		results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(1, results.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigCollectThreadUpdateInterval() {
		db.deleteAll();
		// #2: collect thread update interval
		Assert.assertEquals(50, M_sum.getCollectThreadUpdateInterval());
		M_sum.setCollectThreadEnabled(true);

		// make sure it processes
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		M_ets.post(e1);		
		int tries = 0;
		List<EventStat> results = new ArrayList<EventStat>();
		while(results.size() == 0 && tries++ < 3) {
			results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
			try{
				// give it time to process event
				Thread.sleep(500);			
			}catch(Exception e) {/* ignore */}
		}
		Assert.assertEquals(1, results.size());
		
		// make sure it doesn't process it
		M_sum.setCollectThreadUpdateInterval(500);
		Assert.assertEquals(500, M_sum.getCollectThreadUpdateInterval());
		e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-a");
		M_ets.post(e1);
		results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(1, results.size());
		M_sum.setCollectThreadUpdateInterval(50);
		Assert.assertEquals(50, M_sum.getCollectThreadUpdateInterval());
		tries = 0;
		while(results.size() == 1 && tries++ < 3) {
			results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
			try{
				// give it time to process event before finish
				Thread.sleep(500);			
			}catch(Exception e) {/* ignore */}
		}
		Assert.assertEquals(2, results.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsCollectAdminEvents() {
		db.deleteAll();
		// #3: collect admin events
		Assert.assertEquals(true, M_sum.isCollectAdminEvents());
		// make sure it processes admin events
		Event e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, "admin", "session-id-a");
		Event e2 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		List<SiteVisits> results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(1, results2.size());
		Assert.assertEquals(2, results2.get(0).getCount());
		// make sure it doesn't processes admin events
		M_sum.setCollectAdminEvents(false);
		Assert.assertEquals(false, M_sum.isCollectAdminEvents());
		M_sum.collectEvents(Arrays.asList(e1, e2));
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(1, results2.size());
		Assert.assertEquals(3, results2.get(0).getCount());
		M_sum.setCollectAdminEvents(true);
		Assert.assertEquals(true, M_sum.isCollectAdminEvents());	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsCollectEventsForSiteWithToolOnly() {
		db.deleteAll();
		// #4: collect for sites with SiteStats only
		Assert.assertEquals(false, M_sum.isCollectEventsForSiteWithToolOnly());
		// make sure events get processed for sites with SiteStats only
		M_sum.setCollectEventsForSiteWithToolOnly(true);
		Assert.assertEquals(true, M_sum.isCollectEventsForSiteWithToolOnly());
		Event e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		List<SiteVisits> results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(1, results2.size());
		e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_B_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(1, results2.size());
		// make sure events get processed for any sites
		db.deleteAll();
		M_sum.setCollectEventsForSiteWithToolOnly(false);
		Assert.assertEquals(false, M_sum.isCollectEventsForSiteWithToolOnly());
		e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(1, results2.size());
		e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_B_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		Assert.assertEquals(2, results2.size());	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsShowAnonymousAccessEvents() {
		db.deleteAll();
		// #3: ShowAnonymousAccessEvents
		Assert.assertEquals(true, M_sm.isShowAnonymousAccessEvents());
		// make sure it processes access events from anonymous
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, "?", "session-id-a");
		Event e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		List<ResourceStat> results = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(2, results.size());
		// make sure it doesn't processes access events from anonymous
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(true).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(false).anyTimes();
		expect(M_sm.isEnableSitePresences()).andReturn(true).anyTimes();
		replay(M_sm);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);		
		Assert.assertEquals(false, M_sm.isShowAnonymousAccessEvents());
		M_sum.collectEvents(Arrays.asList(e1, e2));
		results = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		Assert.assertEquals(2, results.size());
		// revert setting
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(true).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(true).anyTimes();
		replay(M_sm);
		Assert.assertEquals(true, M_sm.isShowAnonymousAccessEvents());	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigIsEventContextSupported() {
		db.deleteAll();
		// #3: EventContextSupported
		Assert.assertEquals(true, M_sm.isEventContextSupported());
		// make sure it processes both events
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/non_existent_site/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", null, FakeData.USER_B_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		List<EventStat> results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(2, results.size());
		// make sure it processes one of the events
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(false).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(true).anyTimes();
		expect(M_sm.isEnableSitePresences()).andReturn(true).anyTimes();
		replay(M_sm);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);		
		Assert.assertEquals(false, M_sm.isEventContextSupported());
		e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/non_existent_site/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", null, FakeData.USER_B_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		Assert.assertEquals(3, results.size());
		// revert setting
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(true).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(true).anyTimes();
		replay(M_sm);
		Assert.assertEquals(true, M_sm.isEventContextSupported());	
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
			Assert.assertNull(M_sum.getEventDateFromLatestJobRun());
		}catch(Exception e){
			Assert.fail("There should be no JobRun in database! [1]");
		}
		
		// test getLatestJobRun() == null
		try{
			Assert.assertNull(M_sum.getLatestJobRun());
		}catch(Exception e){
			Assert.fail("There should be no JobRun in database! [2]");
		}
		
		// test save of null JobRun
		Assert.assertFalse(M_sum.saveJobRun(null));
		
		// save objects to db
		Assert.assertTrue(M_sum.saveJobRun(jobRun1));
		Assert.assertTrue(M_sum.saveJobRun(jobRun2));
		
		// test getLatestJobRun()
		JobRun jr = null;
		try{
			jr = M_sum.getLatestJobRun();
		}catch(Exception e){
			Assert.fail("There is no JobRun in database! [1]");
		}
		Assert.assertEquals(jobRun2.getStartEventId(), jr.getStartEventId());
		Assert.assertEquals(jobRun2.getEndEventId(), jr.getEndEventId());
		Assert.assertEquals(jobRun2.getJobStartDate(), jr.getJobStartDate());
		Assert.assertEquals(jobRun2.getJobEndDate(), jr.getJobEndDate());
		Assert.assertEquals(jobRun2.getLastEventDate(), jr.getLastEventDate());

		// test getEventDateFromLatestJobRun()
		try{
			Assert.assertEquals(jobRun2.getLastEventDate(), M_sum.getEventDateFromLatestJobRun());
		}catch(Exception e){
			Assert.fail("There is no JobRun in database! [2]");
		}
	}
	
	private Date getTruncatedDate(Date date) {
		if(date == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}

}
