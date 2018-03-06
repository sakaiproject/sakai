/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;

public class SiteNeighbourhoodServiceImplTest extends TestCase {
	
	@Override
		protected void setUp() throws Exception {
			super.setUp();
		}

	public void testGetAllSites() {
		// Mock the session as admin
		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn("admin");

		// Create a new SiteNeighbourhoodServiceImpl
		SiteNeighbourhoodServiceImpl siteNeighbourhoodService = new SiteNeighbourhoodServiceImpl();
		siteNeighbourhoodService.setServerConfigurationService(mock(ServerConfigurationService.class));
		
		// Mock a site service
		SiteService siteService = mock(SiteService.class);
		siteNeighbourhoodService.setSiteService(siteService);
		
		// Make a whole lot of sites and an ordered list
		List<Site> siteList = new ArrayList<Site>(3000);
		List<String> orderList = new ArrayList<String>(3000);
		ResourceProperties rp = mock(ResourceProperties.class);
		for (int i = 0; i < 3000; i++) {
			Site site = mock(Site.class);
			when(site.getProperties()).thenReturn(rp);
			when(site.getId()).thenReturn("ID:" + i);
			orderList.add("ID:" + i);
			siteList.add(site);
		}
		// Change the order to make it more interesting...
		Collections.reverse(orderList);
		
		when(siteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null,
				null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null)).thenReturn(siteList);
		
		// Make sure the prefs return the order list
		PreferencesService preferenceService = mock(PreferencesService.class);
		Preferences preferences = mock(Preferences.class);
		ResourceProperties siteNavProps = mock(ResourceProperties.class);
		when(siteNavProps.getPropertyList("order")).thenReturn(orderList );
		when(preferences.getProperties(PreferencesService.SITENAV_PREFS_KEY)).thenReturn(siteNavProps);
		when(preferenceService.getPreferences("admin")).thenReturn(preferences);
		siteNeighbourhoodService.setPreferencesService(preferenceService);
		HttpServletRequest req = mock(HttpServletRequest.class);
		
		// Call the method
		List<Site> allSites = siteNeighbourhoodService.getAllSites(req, session, false);
		// Check the result
		int index = 0;
		for (Site site : allSites) {
			assertEquals(orderList.get(index++), site.getId());
		}

	}

}
