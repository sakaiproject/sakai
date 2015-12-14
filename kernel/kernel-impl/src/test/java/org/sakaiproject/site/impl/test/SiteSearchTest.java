package org.sakaiproject.site.impl.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Just a simple search to test the SQL is correct.
 * Doesn't actually verify the results are correct.
 * @author buckett
 *
 */
public class SiteSearchTest extends SakaiKernelTestBase {
	private static Log log = LogFactory.getLog(SiteSearchTest.class);
	
	@BeforeClass
	public static void beforeClass() {
		try {
			oneTimeSetup("sitesearch");
		} catch (Exception e) {
			log.warn(e);
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
		String type = UUID.randomUUID().toString();
		Site site = siteService.addSite(UUID.randomUUID().toString(), type);
		site.setJoinable(true);
		site.setJoinerRole("access");
		site.setPublished(true);
		site.setTitle("Site");
		site.getPropertiesEdit().addProperty("key", "value");
		siteService.save(site);

		Map stringMap = Collections.singletonMap("key", "value");

		// Need to switch user so we're not a member of the site.
		session.setUserEid("");
		session.setUserId("");
		// First test search for any.
		List<Site> sites;
		sites = siteService.getSites(SelectionType.ANY, type, null, stringMap, SiteService.SortType.TITLE_ASC, null);
		Assert.assertEquals(1, sites.size());
		// Then test that it's joinable
		sites = siteService.getSites(SelectionType.JOINABLE, type, null, stringMap, SiteService.SortType.TITLE_ASC, null);
		Assert.assertEquals(1, sites.size());

	}
}