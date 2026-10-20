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
package org.sakaiproject.sitestats.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.SiteStatsDemoSeedService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Optional Job Scheduler entry to re-run the sitestats tool demo seed without AutoRun.
 */
@Slf4j
public class SiteStatsDemoSeedJob implements Job {

	@Setter private SiteStatsDemoSeedService siteStatsDemoSeedService;
	@Setter private SiteService siteService;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private SessionManager sessionManager;
	@Setter private SecurityService securityService;
	@Setter private ServerConfigurationService serverConfigurationService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (!serverConfigurationService.getBoolean("sitestats.sakai.demo", false)) {
			log.info("sitestats.sakai.demo is false; skipping SiteStatsDemoSeedJob");
			return;
		}

		log.info("SiteStatsDemoSeedJob started");

		Session session = sessionManager.getCurrentSession();
		String originalUserId = session.getUserId();
		String originalUserEid = session.getUserEid();
		session.setUserId("admin");
		session.setUserEid("admin");

		SecurityAdvisor advisor = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
		securityService.pushAdvisor(advisor);
		try {
			List<Site> sites = new ArrayList<>();
			for (String siteId : siteService.getSiteIds(SiteService.SelectionType.NON_USER, "course",
					null, null, SiteService.SortType.CREATED_ON_DESC, null)) {
				try {
					Site site = siteService.getSite(siteId);
					// A SiteStats tool alone does not identify a demo course.
					if (site.getToolForCommonId(StatsManager.SITESTATS_TOOLID) != null
							&& site.getProperties() != null
							&& "true".equalsIgnoreCase(site.getProperties().getProperty(SiteStatsDemoSeedService.DEMO_SITE_PROPERTY))) {
						sites.add(site);
					}
				} catch (IdUnusedException e) {
					log.debug("Skipping missing site {}", siteId);
				}
			}

			List<User> students = new ArrayList<>();
			List<User> instructors = new ArrayList<>();
			Set<String> studentIds = new HashSet<>();
			Set<String> instructorIds = new HashSet<>();
			for (Site site : sites) {
				for (String userId : site.getUsers()) {
					try {
						User user = userDirectoryService.getUser(userId);
						if ("maintain".equals(user.getType())) {
							if (instructorIds.add(user.getId())) {
								instructors.add(user);
							}
						} else if (studentIds.add(user.getId())) {
							students.add(user);
						}
					} catch (Exception e) {
						log.debug("Skipping user {}: {}", userId, e.toString());
					}
				}
			}

			siteStatsDemoSeedService.seed(sites, students, instructors);
		} finally {
			securityService.popAdvisor(advisor);
			session.setUserId(originalUserId);
			session.setUserEid(originalUserEid);
		}

		log.info("SiteStatsDemoSeedJob completed");
	}
}
