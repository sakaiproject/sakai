/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.SiteStatsDemoSeedService;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.sakaiproject.user.api.User;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
public class SiteStatsDemoSeedServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired private DB db;
	@Autowired private SiteService siteService;
	@Autowired private StatsManager statsManager;
	@Autowired private StatsUpdateManager statsUpdateManager;
	@Autowired private ServerConfigurationService serverConfigurationService;
	@Autowired private SimplePageToolDao simplePageToolDao;

	@Autowired private SiteStatsDemoSeedService seeder;

	@Before
	public void setUp() throws Exception {
		db.deleteAll();

		FakeSite siteA = spy(FakeSite.class).set(FakeData.SITE_A_ID,
				Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID));
		siteA.setUsers(new HashSet<>(Arrays.asList(FakeData.USER_A_ID, FakeData.USER_B_ID)));
		siteA.setMembers(new HashSet<>(Arrays.asList(FakeData.USER_A_ID, FakeData.USER_B_ID)));
		when(siteService.getSite(FakeData.SITE_A_ID)).thenReturn(siteA);
		when(siteService.isUserSite(FakeData.SITE_A_ID)).thenReturn(false);
		when(siteService.isSpecialSite(FakeData.SITE_A_ID)).thenReturn(false);
		doNothing().when(siteService).save(any(Site.class));

		ResourcePropertiesEdit props = mock(ResourcePropertiesEdit.class);
		when(props.getProperty(anyString())).thenReturn(null);
		when(props.getProperty(SiteStatsDemoSeedService.DEMO_SITE_PROPERTY)).thenReturn("true");
		when(siteA.getProperties()).thenReturn(props);
		when(siteA.getPropertiesEdit()).thenReturn(props);

		when(simplePageToolDao.getSitePages(anyString())).thenReturn(Collections.emptyList());

		when(serverConfigurationService.getInt("sitestats.demo.seed.days", 30)).thenReturn(7);
		when(serverConfigurationService.getInt("sitestats.demo.seed.visits.per.user", 8)).thenReturn(3);
		when(serverConfigurationService.getInt("sitestats.demo.seed.sessions", 24)).thenReturn(0);

		statsUpdateManager.setCollectThreadEnabled(false);
		statsUpdateManager.setCollectEventsForSiteWithToolOnly(false);
		((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setShowAnonymousAccessEvents(true);
		((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setEnableSitePresences(true);

	}

	@Test
	public void seedProducesNonEmptyVisitTotals() throws Exception {
		User studentA = mockUser(FakeData.USER_A_ID);
		User studentB = mockUser(FakeData.USER_B_ID);
		Site site = siteService.getSite(FakeData.SITE_A_ID);

		seeder.seed(List.of(site), List.of(studentA, studentB), List.of());

		SummaryVisitsTotals totals = statsManager.getSummaryVisitsTotals(FakeData.SITE_A_ID);
		assertNotNull(totals);
		assertTrue("Expected demo seed to create visits", totals.getTotalVisits() > 0);

		List<SiteVisits> visits = statsManager.getSiteVisits(FakeData.SITE_A_ID);
		assertNotNull(visits);
		assertFalse("Expected site visit rows after demo seed", visits.isEmpty());
	}

	@Test
	public void seedIsIdempotentWhenSiteAlreadySeeded() throws Exception {
		Site site = siteService.getSite(FakeData.SITE_A_ID);

		User studentA = mockUser(FakeData.USER_A_ID);
		seeder.seed(List.of(site), List.of(studentA), List.of());
		long visits = statsManager.getSummaryVisitsTotals(FakeData.SITE_A_ID).getTotalVisits();
		assertTrue("Expected the first seed to create visits", visits > 0);
		seeder.seed(List.of(site), List.of(studentA), List.of());

		SummaryVisitsTotals totals = statsManager.getSummaryVisitsTotals(FakeData.SITE_A_ID);
		assertNotNull(totals);
		assertEquals("Repeating a completed seed must not add visits", visits, totals.getTotalVisits());
	}

	private static User mockUser(String id) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(id);
		when(user.getEid()).thenReturn(id);
		return user;
	}
}
