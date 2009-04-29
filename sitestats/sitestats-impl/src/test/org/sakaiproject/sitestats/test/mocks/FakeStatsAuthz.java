package org.sakaiproject.sitestats.test.mocks;

import org.sakaiproject.sitestats.api.StatsAuthz;

public class FakeStatsAuthz implements StatsAuthz {

	public boolean isSiteStatsAdminPage() {
		return true;
	}

	public boolean isSiteStatsPage() {
		return true;
	}

	public boolean isUserAbleToViewSiteStats(String siteId) {
		return true;
	}

	public boolean isUserAbleToViewSiteStatsAdmin(String siteId) {
		return true;
	}

}
