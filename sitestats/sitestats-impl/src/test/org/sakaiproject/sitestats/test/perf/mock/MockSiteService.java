package org.sakaiproject.sitestats.test.perf.mock;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public abstract class MockSiteService implements SiteService {

	public Site getSite(String id) throws IdUnusedException {
		return StubUtils.stubClass(MockSite.class);
	}
}
