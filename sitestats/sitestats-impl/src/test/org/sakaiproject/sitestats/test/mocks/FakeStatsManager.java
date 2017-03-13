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
package org.sakaiproject.sitestats.test.mocks;

import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.user.api.User;

import java.util.HashMap;
import java.util.Map;

public abstract class FakeStatsManager implements StatsManager {

	public Map<String, SitePresenceTotal> getPresenceTotalsForSite(final String siteId) {

		final Map<String, SitePresenceTotal> totals = new HashMap<String, SitePresenceTotal>();
		return totals;
	}

	public String getUserNameForDisplay(String userId) {
		return userId;
	}
	
	public String getUserNameForDisplay(User user) {
		if(isSortUsersByDisplayName()) {
			return user.getDisplayName();
		}else{
			return user.getSortName();
		}
	}

}
