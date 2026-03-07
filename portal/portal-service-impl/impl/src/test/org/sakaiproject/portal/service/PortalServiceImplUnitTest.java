/*
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
package org.sakaiproject.portal.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.api.model.PinnedSite;
import org.sakaiproject.portal.api.model.RecentSite;
import org.sakaiproject.portal.api.repository.PinnedSiteRepository;
import org.sakaiproject.portal.api.repository.RecentSiteRepository;
import org.sakaiproject.site.api.SiteService;

@RunWith(MockitoJUnitRunner.class)
public class PortalServiceImplUnitTest {

	private static final String USER_ID = "user-1";

	private PortalServiceImpl portalService;
	private PinnedSiteRepository pinnedSiteRepository;
	private RecentSiteRepository recentSiteRepository;
	private SiteService siteService;
	private ServerConfigurationService serverConfigurationService;

	@Before
	public void setUp() {
		portalService = new PortalServiceImpl();

		pinnedSiteRepository = mock(PinnedSiteRepository.class);
		recentSiteRepository = mock(RecentSiteRepository.class);
		siteService = mock(SiteService.class);
		serverConfigurationService = mock(ServerConfigurationService.class);

		portalService.setPinnedSiteRepository(pinnedSiteRepository);
		portalService.setRecentSiteRepository(recentSiteRepository);
		portalService.setSiteService(siteService);
		portalService.setServerConfigurationService(serverConfigurationService);

		when(siteService.isSpecialSite(anyString())).thenReturn(false);
		when(siteService.isUserSite(anyString())).thenReturn(false);
		when(serverConfigurationService.getInt("portal.max.recent.sites", PortalServiceImpl.DEFAULT_MAX_RECENT_SITES))
				.thenReturn(PortalServiceImpl.DEFAULT_MAX_RECENT_SITES);
		when(serverConfigurationService.getInt("portal.max.pinned.sites", PortalServiceImpl.DEFAULT_MAX_PINNED_SITES))
				.thenReturn(PortalServiceImpl.DEFAULT_MAX_PINNED_SITES);
		when(pinnedSiteRepository.findByUserIdAndHasBeenUnpinnedOrderByPosition(anyString(), anyBoolean()))
				.thenReturn(Collections.<PinnedSite>emptyList());
		when(recentSiteRepository.findByUserId(USER_ID)).thenReturn(Collections.<RecentSite>emptyList());
	}

	@Test
	public void addPinnedSiteSkipsBlankUserIdBeforeLoadPortalNavState() {
		portalService.addPinnedSite(null, "site-1", true);

		verifyNoInteractions(pinnedSiteRepository, recentSiteRepository);
	}

	@Test
	public void addPinnedSiteSkipsUserSitesBeforeLoadPortalNavState() {
		String userSiteId = "~" + USER_ID;
		when(siteService.isUserSite(userSiteId)).thenReturn(true);

		portalService.addPinnedSite(USER_ID, userSiteId, true);

		verifyNoInteractions(pinnedSiteRepository, recentSiteRepository);
	}

	@Test
	public void savePinnedSitesSkipsUnchangedPinnedRows() {

		PinnedSite firstPinnedSite = new PinnedSite(USER_ID, "site-1");
		firstPinnedSite.setId(1L);
		firstPinnedSite.setPosition(0);
		firstPinnedSite.setHasBeenUnpinned(false);

		PinnedSite secondPinnedSite = new PinnedSite(USER_ID, "site-2");
		secondPinnedSite.setId(2L);
		secondPinnedSite.setPosition(1);
		secondPinnedSite.setHasBeenUnpinned(false);

		when(pinnedSiteRepository.findByUserIdAndHasBeenUnpinnedOrderByPosition(USER_ID, false)).thenReturn(List.of(firstPinnedSite, secondPinnedSite));
		when(pinnedSiteRepository.findByUserIdAndHasBeenUnpinnedOrderByPosition(USER_ID, true)).thenReturn(Collections.<PinnedSite>emptyList());

		portalService.savePinnedSites(USER_ID, List.of("site-1", "site-2"));

		verify(pinnedSiteRepository, never()).save(any(PinnedSite.class));
		verify(recentSiteRepository, never()).save(any(RecentSite.class));
	}

	@Test
	public void savePinnedSitesFiltersUserSitesFromBulkPath() {
		String userSiteId = "~" + USER_ID;
		when(siteService.isUserSite(userSiteId)).thenReturn(true);

		portalService.savePinnedSites(USER_ID, List.of("site-1", userSiteId));

		verify(pinnedSiteRepository).save(argThat(pinnedSite -> "site-1".equals(pinnedSite.getSiteId())));
		verify(pinnedSiteRepository, never()).save(argThat(pinnedSite -> userSiteId.equals(pinnedSite.getSiteId())));
	}

	@Test
	public void addRecentSiteSkipsInsertWhenMaxRecentSitesIsZero() {
		when(serverConfigurationService.getInt("portal.max.recent.sites", PortalServiceImpl.DEFAULT_MAX_RECENT_SITES)).thenReturn(0);

		portalService.addRecentSite(USER_ID, "site-1");

		verify(recentSiteRepository, never()).save(any(RecentSite.class));
	}

	@Test
	public void savePinnedSitesRespectsMaxPinnedSitesLimit() {
		when(serverConfigurationService.getInt("portal.max.pinned.sites", PortalServiceImpl.DEFAULT_MAX_PINNED_SITES)).thenReturn(1);

		portalService.savePinnedSites(USER_ID, List.of("site-1", "site-2"));

		verify(pinnedSiteRepository).save(argThat(pinnedSite -> "site-2".equals(pinnedSite.getSiteId())));
		verify(pinnedSiteRepository, never()).save(argThat(pinnedSite -> "site-1".equals(pinnedSite.getSiteId())));
	}
}
