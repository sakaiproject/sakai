package org.sakaiproject.sitestats.test.perf.mock;

import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.sitestats.api.StatsManager;

import java.util.HashMap;
import java.util.Map;

public abstract class MockStatsManager implements StatsManager {

	@Override
	public Map<String, SitePresenceTotal> getPresenceTotalsForSite(final String siteId) {

		final Map<String, SitePresenceTotal> totals = new HashMap<String, SitePresenceTotal>();
		return totals;
	}

}
