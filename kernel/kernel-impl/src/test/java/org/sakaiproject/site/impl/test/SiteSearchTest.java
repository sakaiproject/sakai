/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.site.impl.test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.test.annotation.DirtiesContext;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Just a simple search to test the SQL is correct.
 * Doesn't actually verify the results are correct.
 * @author buckett
 *
 */
@DirtiesContext
@Slf4j
public class SiteSearchTest extends SakaiKernelTestBase {
	@BeforeClass
	public static void beforeClass() {
		try {
			oneTimeSetup("sitesearch");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
				
	@Test
	public void testSearch() throws Exception {
		SiteService siteService = getService(SiteService.class);
		siteService.countSites(SelectionType.ACCESS, null, null, null);
		siteService.countSites(SelectionType.ANY, null, null, null);
		siteService.countSites(SelectionType.JOINABLE, null, null, null);
		siteService.countSites(SelectionType.NON_USER, null, null, null);
		siteService.countSites(SelectionType.PUBVIEW, null, null, null);
		siteService.countSites(SelectionType.UPDATE, null, null, null);

		siteService.countSites(SelectionType.DELETED, null, null, null);
		siteService.countSites(SelectionType.ANY_DELETED, null, null, null);
	}

	@Test
	public void testJoinableSiteSQL() throws Exception {
		// This test came about through KNL-1294 and was written to test that joinable sites search worked
		// when also supplying a map of properties to search for.
		SessionManager sessionManager = getService(SessionManager.class);
		SiteService siteService = getService(SiteService.class);

		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");

		// Use a random UUID for type so database state can't break the test.
		String siteId = UUID.randomUUID().toString();
		String type = "random-site-type-" + UUID.randomUUID().toString().substring(0, 8);

		Site site = siteService.addSite(siteId, type);
		site.setJoinable(true);
		site.setJoinerRole("access");
		site.setPublished(true);
		site.setTitle("Site");
		site.getPropertiesEdit().addProperty("key", "value");
		siteService.save(site);

		Map<String, String> criteriaMap = Collections.singletonMap("key", "value");
		Map<String, String> restrictionMap = Collections.singletonMap("xxxxx", "yyyyy");

		// Need to switch user so we're not a member of the site.
		session.setUserEid("someuser");
		session.setUserId("someuser");
		List<Site> sites;
		// First test search for any with properties.
		sites = siteService.getSites(SelectionType.ANY, type, null, criteriaMap, SiteService.SortType.TITLE_ASC, null);
		Assert.assertEquals(1, sites.size());
		// Then test that it's joinable with properties
		sites = siteService.getSites(SelectionType.JOINABLE, type, null, criteriaMap, SiteService.SortType.TITLE_ASC, null);
		Assert.assertEquals(1, sites.size());
		// Then test that it's joinable and with criteria
		sites = siteService.getSites(SelectionType.JOINABLE, type, "Site", null, SiteService.SortType.TITLE_ASC, null);
		Assert.assertEquals(1, sites.size());
		// Then test that it's joinable and with criteria and properties
		sites = siteService.getSites(SelectionType.JOINABLE, type, "Site", criteriaMap, SiteService.SortType.TITLE_ASC, null);
		Assert.assertEquals(1, sites.size());

		// Then test propertyCriteria and null propertyRestrictions
		List<String> siteIds = siteService.getSiteIds(SelectionType.JOINABLE, type, "Site", criteriaMap, null, SiteService.SortType.TITLE_ASC, null, "someuser");
		Assert.assertEquals(1, siteIds.size());
		// Then test propertyCriteria and identical propertyRestrictions (expect them to cancel each other out)
		siteIds = siteService.getSiteIds(SelectionType.JOINABLE, type, "Site", criteriaMap, criteriaMap, SiteService.SortType.TITLE_ASC, null, "someuser");
		Assert.assertEquals(0, siteIds.size());
		// Then test propertyCriteria and propertyRestrictions 
		siteIds = siteService.getSiteIds(SelectionType.JOINABLE, type, "Site", criteriaMap, restrictionMap, SiteService.SortType.TITLE_ASC, null, "someuser");
		Assert.assertEquals(1, siteIds.size());

	}
}
