package org.sakaiproject.sitestats.test;


import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.impl.CustomEventImpl;
import org.sakaiproject.sitestats.impl.EventStatImpl;
import org.sakaiproject.sitestats.impl.JobRunImpl;
import org.sakaiproject.sitestats.impl.ResourceStatImpl;
import org.sakaiproject.sitestats.impl.SiteActivityImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeEvent;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;


public class StatsUpdateManagerTest extends AbstractAnnotationAwareTransactionalTests { 
	// AbstractAnnotationAwareTransactionalTests / AbstractTransactionalSpringContextTests
	private StatsUpdateManager		M_sum;
	private StatsManager			M_sm;
	private DB						db;
	private SiteService				M_ss;
	private EventTrackingService	M_ets;
	
	// Spring configuration	
	public void setStatsUpdateManager(StatsUpdateManager M_sum) {
		this.M_sum = M_sum;
	}
	public void setEventTrackingService(EventTrackingService M_ets) {
		this.M_ets = M_ets;
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
		// apply
		replay(M_sm);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);
	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		db.deleteAll();
	}

	
	// ---- TESTS ----
	
	
	// Basic tests: not much to test, work is on other methods...
	@SuppressWarnings("unchecked")
	public void testCollectEvent() {
		FakeEvent e1 = new FakeEvent(FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, true, 0);
		assertTrue(M_sum.collectEvent(e1));
		Event e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, "FakeData.USER_A_ID", "session-id-a");
		assertTrue(M_sum.collectEvent(e2));
		
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
		assertTrue(M_sum.collectEvents((List<Event>)null));
		assertTrue(M_sum.collectEvents(new ArrayList<Event>()));
		assertTrue(M_sum.collectEvents(new Event[]{}));
		assertTrue(M_sum.collectEvents(Arrays.asList(null, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16)));
		assertTrue(M_sum.collectEvents(new Event[]{null, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16}));
		assertTrue(M_sum.collectEvent(null));
		assertTrue(M_sum.collectEvent(e3));
		assertTrue(M_sum.collectEvent(e4));
		assertTrue(M_sum.collectEvent(e5));
		assertTrue(M_sum.collectEvent(e6));
		assertTrue(M_sum.collectEvent(e7));
		assertTrue(M_sum.collectEvent(e8));
		assertTrue(M_sum.collectEvent(e9));
		assertTrue(M_sum.collectEvent(e10));
		assertTrue(M_sum.collectEvent(e11));
		assertTrue(M_sum.collectEvent(e12));
		assertTrue(M_sum.collectEvent(e13));
		assertTrue(M_sum.collectEvent(e14));
		assertTrue(M_sum.collectEvent(e15));
		assertTrue(M_sum.collectEvent(e16));
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
	
	// Site visits tests
	@SuppressWarnings("unchecked")
	public void testSiteVisits() {
		// #1 Test: 2 site visit (different users)
		Event eSV1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_B_ID, "session-id-b");
		assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, r1.size());
		EventStat es1 = r1.get(0);
		EventStat es2 = r1.get(1);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(FakeData.SITE_A_ID, es2.getSiteId());
		assertEquals(eSV1.getEvent(), es1.getEventId());
		assertEquals(eSV2.getEvent(), es2.getEventId());
		if(eSV1.getUserId().equals(es1.getUserId())) {
			assertEquals(eSV1.getUserId(), es1.getUserId());
			assertEquals(eSV2.getUserId(), es2.getUserId());
		}else{
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

		// #2 Test: 2 site visit (same users)
		db.deleteAll();
		eSV1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		eSV2 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #2: SST_EVENTS
		r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, r1.size());
		es1 = r1.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eSV1.getEvent(), es1.getEventId());
		assertEquals(eSV1.getUserId(), es1.getUserId());
		assertEquals(2, es1.getCount());
		// #2: SST_SITEVISITS
		r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, r2.size());
		sv = r2.get(0);
		assertEquals(FakeData.SITE_A_ID, sv.getSiteId());
		assertEquals(2, sv.getTotalVisits());
		assertEquals(1, sv.getTotalUnique());
		// #2: SST_SITEACTIVITY
		r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(0, r3.size());		
		// #2: SST_RESOURCES
		r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(0, r4.size());	
	}
	
	// Activity tests
	@SuppressWarnings("unchecked")
	public void testActivityEvent() {
		// #1 Test: 2 new chat msg (different users)
		Event eSV1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event eSV2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-a");
		assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
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
		if(eSV1.getUserId().equals(es1.getUserId())) {
			assertEquals(eSV1.getUserId(), es1.getUserId());
			assertEquals(eSV2.getUserId(), es2.getUserId());
		}else{
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

		// #2 Test: 2 new chat msg (same users)
		db.deleteAll();
		eSV1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		eSV2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		assertTrue(M_sum.collectEvents(Arrays.asList(eSV1, eSV2)));
		// #2: SST_EVENTS
		r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, r1.size());
		es1 = r1.get(0);
		assertEquals(FakeData.SITE_A_ID, es1.getSiteId());
		assertEquals(eSV1.getEvent(), es1.getEventId());
		assertEquals(eSV1.getUserId(), es1.getUserId());
		assertEquals(2, es1.getCount());
		// #2: SST_SITEVISITS
		r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #2: SST_SITEACTIVITY
		r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(1, r3.size());	
		sa = r3.get(0);
		assertEquals(FakeData.SITE_A_ID, sa.getSiteId());
		assertEquals(FakeData.EVENT_CHATNEW, sa.getEventId());
		assertEquals(2, sa.getCount());
		// #2: SST_RESOURCES
		r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(0, r4.size());	
	}
	
	// Resource tests
	@SuppressWarnings("unchecked")
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
		assertTrue(M_sum.collectEvents(Arrays.asList(e1, e2, e3, e4, e5)));
		// #1: SST_EVENTS
		List<EventStat> r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(4, r1.size());
		// #1: SST_SITEVISITS
		List<SiteVisits> r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		List<SiteActivity> r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(3, r3.size());	
		// #1: SST_RESOURCES
		List<ResourceStat> r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(4, r4.size());
		
		// #2 Test: valid resource events
		db.deleteAll();
		Event e6 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/attachment/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e7 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e8 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group-user/something/more/and/more", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		assertTrue(M_sum.collectEvents(Arrays.asList(e6, e7, e8)));
		// #1: SST_EVENTS
		r1 = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, r1.size());
		// #1: SST_SITEVISITS
		r2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(0, r2.size());
		// #1: SST_SITEACTIVITY
		r3 = (List<SiteActivity>) db.getResultsForClass(SiteActivityImpl.class);
		assertEquals(1, r3.size());	
		// #1: SST_RESOURCES
		r4 = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(3, r4.size());
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
	public void testConfigIsCollectThreadEnabled() {
		db.deleteAll();
		M_sum.setCollectThreadUpdateInterval(50);
		// #1: collect thread enabled/disabled
		assertEquals(true, M_sum.isCollectThreadEnabled());
		// turn it off
		M_sum.setCollectThreadEnabled(false);
		assertEquals(false, M_sum.isCollectThreadEnabled());
		try{
			// give it time to stop thread
			Thread.sleep(200);			
		}catch(Exception e) {}
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		M_ets.post(e1);
		List<EventStat> results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(0, results.size());
		// turn it on again
		M_sum.setCollectThreadEnabled(true);
		assertEquals(true, M_sum.isCollectThreadEnabled());
		try{
			// give it time to start thread
			Thread.sleep(200);			
		}catch(Exception e) {}
		M_ets.post(e1);
		try{
			// give it time to process event
			Thread.sleep(500);			
		}catch(Exception e) {}
		results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, results.size());
	}
	
	@SuppressWarnings("unchecked")
	public void testConfigCollectThreadUpdateInterval() {
		db.deleteAll();
		// #2: collect thread update interval
		assertEquals(50, M_sum.getCollectThreadUpdateInterval());
		
		// make sure it processes
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		M_ets.post(e1);		
		int tries = 0;
		List<EventStat> results = new ArrayList<EventStat>();
		while(results.size() == 0 && tries++ < 3) {
			results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
			try{
				// give it time to process event
				Thread.sleep(250);			
			}catch(Exception e) {/* ignore */}
		}
		assertEquals(1, results.size());
		
		// make sure it doesn't process it
		M_sum.setCollectThreadUpdateInterval(500);
		assertEquals(500, M_sum.getCollectThreadUpdateInterval());
		e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CHATNEW, "/chat/msg/"+FakeData.SITE_A_ID, FakeData.USER_B_ID, "session-id-a");
		M_ets.post(e1);
		results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(1, results.size());
		M_sum.setCollectThreadUpdateInterval(50);
		assertEquals(50, M_sum.getCollectThreadUpdateInterval());
		tries = 0;
		while(results.size() == 1 && tries++ < 3) {
			results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
			try{
				// give it time to process event before finish
				Thread.sleep(250);			
			}catch(Exception e) {/* ignore */}
		}
		assertEquals(2, results.size());
	}
	
	@SuppressWarnings("unchecked")
	public void testConfigIsCollectAdminEvents() {
		db.deleteAll();
		// #3: collect admin events
		assertEquals(true, M_sum.isCollectAdminEvents());
		// make sure it processes admin events
		Event e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, "admin", "session-id-a");
		Event e2 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		List<SiteVisits> results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		assertEquals(2, results2.get(0).getCount());
		// make sure it doesn't processes admin events
		M_sum.setCollectAdminEvents(false);
		assertEquals(false, M_sum.isCollectAdminEvents());
		M_sum.collectEvents(Arrays.asList(e1, e2));
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		assertEquals(3, results2.get(0).getCount());
		M_sum.setCollectAdminEvents(true);
		assertEquals(true, M_sum.isCollectAdminEvents());	
	}
	
	@SuppressWarnings("unchecked")
	public void testConfigIsCollectEventsForSiteWithToolOnly() {
		db.deleteAll();
		// #4: collect for sites with SiteStats only
		assertEquals(false, M_sum.isCollectEventsForSiteWithToolOnly());
		// make sure events get processed for sites with SiteStats only
		M_sum.setCollectEventsForSiteWithToolOnly(true);
		assertEquals(true, M_sum.isCollectEventsForSiteWithToolOnly());
		Event e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		List<SiteVisits> results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_B_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		// make sure events get processed for any sites
		db.deleteAll();
		M_sum.setCollectEventsForSiteWithToolOnly(false);
		assertEquals(false, M_sum.isCollectEventsForSiteWithToolOnly());
		e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_A_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(1, results2.size());
		e1 = M_sum.buildEvent(new Date(), StatsManager.SITEVISIT_EVENTID, "/presence/"+FakeData.SITE_B_ID+"-presence", null, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvent(e1);
		results2 = (List<SiteVisits>) db.getResultsForClass(SiteVisitsImpl.class);
		assertEquals(2, results2.size());	
	}
	
	@SuppressWarnings("unchecked")
	public void testConfigIsShowAnonymousAccessEvents() {
		db.deleteAll();
		// #3: ShowAnonymousAccessEvents
		assertEquals(true, M_sm.isShowAnonymousAccessEvents());
		// make sure it processes access events from anonymous
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, "?", "session-id-a");
		Event e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		List<ResourceStat> results = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(2, results.size());
		// make sure it doesn't processes access events from anonymous
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(true).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(false).anyTimes();
		replay(M_sm);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);		
		assertEquals(false, M_sm.isShowAnonymousAccessEvents());
		M_sum.collectEvents(Arrays.asList(e1, e2));
		results = (List<ResourceStat>) db.getResultsForClass(ResourceStatImpl.class);
		assertEquals(2, results.size());
		// revert setting
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(true).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(true).anyTimes();
		replay(M_sm);
		assertEquals(true, M_sm.isShowAnonymousAccessEvents());	
	}
	
	@SuppressWarnings("unchecked")
	public void testConfigIsEventContextSupported() {
		db.deleteAll();
		// #3: EventContextSupported
		assertEquals(true, M_sm.isEventContextSupported());
		// make sure it processes both events
		Event e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/non_existent_site/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		Event e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_A_ID+"/resource_id", null, FakeData.USER_B_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		List<EventStat> results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(2, results.size());
		// make sure it processes one of the events
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(false).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(true).anyTimes();
		replay(M_sm);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);		
		assertEquals(false, M_sm.isEventContextSupported());
		e1 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/non_existent_site/resource_id", FakeData.SITE_A_ID, FakeData.USER_A_ID, "session-id-a");
		e2 = M_sum.buildEvent(new Date(), FakeData.EVENT_CONTENTNEW, "/content/group/"+FakeData.SITE_B_ID+"/resource_id", null, FakeData.USER_B_ID, "session-id-a");
		M_sum.collectEvents(Arrays.asList(e1, e2));
		results = (List<EventStat>) db.getResultsForClass(EventStatImpl.class);
		assertEquals(3, results.size());
		// revert setting
		reset(M_sm);
		expect(M_sm.isEventContextSupported()).andReturn(true).anyTimes();
		expect(M_sm.isShowAnonymousAccessEvents()).andReturn(true).anyTimes();
		replay(M_sm);
		assertEquals(true, M_sm.isEventContextSupported());	
	}
	
	// Test JobRun related methods
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
			assertNull(M_sum.getEventDateFromLatestJobRun());
		}catch(Exception e){
			fail("There should be no JobRun in database! [1]");
		}
		
		// test getLatestJobRun() == null
		try{
			assertNull(M_sum.getLatestJobRun());
		}catch(Exception e){
			fail("There should be no JobRun in database! [2]");
		}
		
		// test save of null JobRun
		assertFalse(M_sum.saveJobRun(null));
		
		// save objects to db
		assertTrue(M_sum.saveJobRun(jobRun1));
		assertTrue(M_sum.saveJobRun(jobRun2));
		
		// test getLatestJobRun()
		JobRun jr = null;
		try{
			jr = M_sum.getLatestJobRun();
		}catch(Exception e){
			fail("There is no JobRun in database! [1]");
		}
		assertEquals(jobRun2.getStartEventId(), jr.getStartEventId());
		assertEquals(jobRun2.getEndEventId(), jr.getEndEventId());
		assertEquals(jobRun2.getJobStartDate(), jr.getJobStartDate());
		assertEquals(jobRun2.getJobEndDate(), jr.getJobEndDate());
		assertEquals(jobRun2.getLastEventDate(), jr.getLastEventDate());

		// test getEventDateFromLatestJobRun()
		try{
			assertEquals(jobRun2.getLastEventDate(), M_sum.getEventDateFromLatestJobRun());
		}catch(Exception e){
			fail("There is no JobRun in database! [2]");
		}
	}

}
