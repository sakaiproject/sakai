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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = {"/hibernate-test.xml"})
@Slf4j
public class EventAggregatorTestPerf extends AbstractJUnit4SpringContextTests {
	private static final int		MAX_USERS				= 250;
	private static final int		MAX_RESOURCES			= 50;
	private static final int		COUNT_USERS_SMALL		= 10;
	private static final int		COUNT_USERS_MEDIUM		= 100;
	private static final int		COUNT_USERS_LARGE		= MAX_USERS;
	private static final int		COUNT_RESOURCES_SMALL	= 5;
	private static final int		COUNT_RESOURCES_MEDIUM	= 10;
	private static final int		COUNT_RESOURCES_LARGE	= MAX_RESOURCES;

	private StatsManager			M_sm;
	private StatsUpdateManager		M_sum;
	private SiteService				M_ss;
	private ContentHostingService	M_chs;
	private ContentTypeImageService	M_ctis;
	private EventTrackingService	M_ets;

	private String					siteId					= FakeData.SITE_A_ID;
	private String					siteRef					= FakeData.SITE_A_REF;
	private List<String>			siteUsers;
	private List<String>			siteResources;

	@Autowired
	public void setStatsManager(StatsManager M_sm) {
		this.M_sm = M_sm;
	}
	@Autowired
	public void setStatsUpdateManager(StatsUpdateManager M_sum) {
		this.M_sum = M_sum;
	}
	@Autowired
	public void setEventTrackingService(EventTrackingService M_ets) {
		this.M_ets = M_ets;
	}

	@Before
	public void onSetUp() throws Exception {
		/** run this before each test starts */
		log.debug("Setting up tests...");

		// Setup site users
		siteUsers = new ArrayList<String>();
		for(int i=0; i<MAX_USERS; i++) {
			siteUsers.add("user-"+i);
		}
		
		// Setup Site Service
		M_ss = createMock(SiteService.class);
		// null site
		expect(M_ss.getSite(null)).andThrow(new IdUnusedException("null")).anyTimes();
		expect(M_ss.getSite("non_existent_site")).andThrow(new IdUnusedException("non_existent_site")).anyTimes();
		// Site A:
		//        - tools {SiteStats, Chat, Resources}
		//        - users {user-0, user-1, ... , user-999}
		Site siteA = Mockito.spy(FakeSite.class).set(siteId,
				Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID)
		);
		Set<String> usersSet = new HashSet<String>(siteUsers);
		((FakeSite)siteA).setUsers(usersSet);
		((FakeSite)siteA).setMembers(usersSet);
		expect(M_ss.getSite(siteId)).andStubReturn(siteA);
		expect(M_ss.isUserSite(siteId)).andStubReturn(false);
		expect(M_ss.isSpecialSite(siteId)).andStubReturn(false);
		Mockito.when(siteA.getCreatedTime()).thenReturn(Mockito.any(Time.class));
		// Site 'non_existent_site' doesn't exist
		expect(M_ss.isUserSite("non_existent_site")).andStubReturn(false);
		expect(M_ss.isSpecialSite("non_existent_site")).andStubReturn(false);
		
		// Setup Content Hosting Service
		M_chs = createMock(ContentHostingService.class);
		M_chs.checkCollection("/group/site-a-id/folder/");
		expectLastCall().anyTimes();
		siteResources = new ArrayList<String>();
		for(int i=0; i<MAX_RESOURCES; i++) {
			String resourceRef = "/content/group/"+ siteId +"/resource-"+ i;
			siteResources.add(resourceRef);
			M_chs.checkResource(resourceRef);
			expectLastCall().anyTimes();
		}
		M_ctis = createMock(ContentTypeImageService.class);
		expect(M_ctis.getContentTypeImage("folder")).andStubReturn("sakai/folder.gif");
		expect(M_ctis.getContentTypeImage("image/png")).andStubReturn("sakai/image.gif");
		
		// Setup I18N Resource Loader
		ResourceLoader msgs = createMock(ResourceLoader.class);
		expect(msgs.getString((String) anyObject())).andStubAnswer(new IAnswer<String>() {
			public String answer() throws Throwable {
				return (String) getCurrentArguments()[0];
			}			
		});
		
		// apply
		replay(M_ss);
		replay(M_chs);
		replay(M_ctis);
		replay(msgs);		
		((StatsManagerImpl)M_sm).setSiteService(M_ss);
		((StatsManagerImpl)M_sm).setContentHostingService(M_chs);
		((StatsManagerImpl)M_sm).setContentTypeImageService(M_ctis);
		((StatsManagerImpl)M_sm).setResourceLoader(msgs);
		((StatsManagerImpl)M_sm).setCountFilesUsingCHS(false);
		((StatsManagerImpl)M_sm).setCountPagesUsingLBS(false);
		((StatsUpdateManagerImpl)M_sum).setSiteService(M_ss);
		((StatsUpdateManagerImpl)M_sum).setStatsManager(M_sm);
	}
	
	// --------------------------------------------------------
	// --- TESTING --------------------------------------------
	// --------------------------------------------------------
	@Test
	public void testSmall() {
		doBasicTest(COUNT_USERS_SMALL, COUNT_RESOURCES_SMALL, 1);
	}
	
	@Test
	public void testMedium() {
		doBasicTest(COUNT_USERS_MEDIUM, COUNT_RESOURCES_MEDIUM, 1);
	}
	
	@Test
	public void testLarge() {
		doBasicTest(COUNT_USERS_LARGE, COUNT_RESOURCES_LARGE, 1);
	}
	
	
	private void doBasicTest(int userCount, int resourceCount, int dayCount) throws IllegalStateException {
		// Check maximum values
		if(userCount > MAX_USERS) {
			throw new IllegalArgumentException("Configured value for user count exceeds MAX_USERS value.");
		}
		if(resourceCount > MAX_RESOURCES) {
			throw new IllegalArgumentException("Configured value for resource count exceeds MAX_RESOURCES value.");
		}
		
		// Do basic test
		Date today = new Date();
		String user0 = "user-0";
		String sitePresenceRef = "/presence/"+siteId+"-presence";
		// 1. Instructor logs in to add files
		M_ets.post(  M_sum.buildEvent(today, StatsManager.SITEVISIT_EVENTID, sitePresenceRef, siteRef, user0, "session-id")  );
		for(int r=0; r<resourceCount; r++) {
			M_ets.post(  M_sum.buildEvent(today, FakeData.EVENT_CONTENTNEW, siteResources.get(r), siteRef, user0, "session-id")  );
		}
		// 2. Students logs in to chat and read files
		for(int u=0; u<userCount; u++) {
			String user = siteUsers.get(u);
			// 2.1. Generate a site visit
			M_ets.post(  M_sum.buildEvent(today, StatsManager.SITEVISIT_EVENTID, sitePresenceRef, siteRef, user, "session-id")  );
			// 2.2 Read [resourceCount] files
			for(int r=0; r<resourceCount; r++) {
				M_ets.post(  M_sum.buildEvent(today, FakeData.EVENT_CONTENTREAD, siteResources.get(r), siteRef, user, "session-id")  );
			}
		}
		
		// Wait until StatsUpdateManager becomes idle...
		while(!M_sum.isIdle()) {
			try{
				Thread.sleep(250);
			}catch(Exception e) {/* ignore */}
		}
		
		// Print result summary
		StringBuilder sb = new StringBuilder("Results for ");
		sb.append(userCount).append(" users, ").append(resourceCount).append(" resources, ").append(dayCount).append(" days: ");
		sb.append(M_sum.getMetricsSummary(true));
		M_sum.resetMetrics();
		log.info(sb.toString());
	}
}
