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
package org.sakaiproject.sitestats.api;

import java.util.Collection;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * Seeds SiteStats aggregate tables and sample session User-Agents for the sitestats tool demo.
 * Invoked from {@code SeedSitesAndUsersJob} when {@code sitestats.sakai.demo=true}.
 */
public interface SiteStatsDemoSeedService {

	String DEMO_SITE_PROPERTY = "sitestats.demo.site";

	/**
	 * Seed SST_* events for the given course sites and insert sample SAKAI_SESSION rows with User-Agents.
	 * Requires {@code sitestats.sakai.demo=true} and sites marked with {@link #DEMO_SITE_PROPERTY}.
	 * Site and server aggregates have separate transactional completion records; session UAs are keyed by id prefix.
	 *
	 * @param sites course sites to seed (typically those just created by SeedSites)
	 * @param students student users available for visit/activity attribution
	 * @param instructors instructor users (used for some activity events)
	 */
	void seed(Collection<Site> sites, Collection<User> students, Collection<User> instructors);
}
