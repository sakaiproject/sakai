package org.sakaiproject.site.impl.test;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;

/**
 * Just a simple search to test the SQL is correct.
 * Doesn't actually verify the results are correct.
 * @author buckett
 *
 */
public class SiteSearchTest extends SakaiKernelTestBase {

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(SiteSearchTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup("sitesearch");
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

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
}