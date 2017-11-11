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
package org.sakaiproject.component.app.scheduler.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * This job finds all sites that have been softly deleted and checks their
 * deletion time. If it is older than site.soft.deletion.gracetime, they are
 * then really deleted. The value is in days and defaults to 30 if not set.
 * </p>
 * 
 * <p>
 * This does not take into account whether or not site.soft.deletion is enabled
 * since there may be sites which have been softly deleted but then the param is
 * disabled, leaving them in limbo.
 * </p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
@Slf4j
public class SoftSiteDeletionJob implements Job {

	private final int GRACETIME_DEFAULT = 30;

	private SecurityAdvisor securityAdvisor;
	
	public void init() {
		// Create our security advisor.
		securityAdvisor = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};

	}

	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		log.info("SoftSiteDeletionJob started.");

		Date graceDate = getGraceDate();

		// get sites
		List<Site> sites = siteService.getSites(SelectionType.ANY_DELETED, null, null, null, null, null);
		log.info(sites.size() + " softly deleted site(s) will be processed");

		// foreach site, check soft deletion time
		// Note: we could do this in the SQL so we only get a list of sites that
		// all need to be deleted.
		// but this would no doubt be db specific so would add extra complexity
		// to the SQL layer
		// for now, just do it in code. There won't be many sites to process at
		// once.
		for (Site s : sites) {
			log.debug("Looking at : " + s.getTitle() + " (" + s.getId() + ")");

			if (!s.isSoftlyDeleted()) {
				log.warn("Site was in returned list but isn't deleted: " + s.getId());
				continue;
			}

			// get calendar for the softly deleted date
			Date deletedDate = s.getSoftlyDeletedDate();
			if (deletedDate == null) {
				log.warn("Site doesn't have a deleted date: " + s.getId());
				continue;
			}

			// if this deleted date is before the gracetime, delete the site.
			if (deletedDate.before(graceDate)) {
				log.info("Site: " + s.getId() + " is due for deletion");

				try {
					enableSecurityAdvisor();

					siteService.removeSite(s);
					log.info("Removed site: " + s.getId());

				} catch (PermissionException e) {
					log.error("Error removing site: " + s.getId() + ", " + e.getMessage());
				} catch (IdUnusedException e) {
					log.error("Error removing site: " + s.getId() + ", " + e.getMessage());
				} finally {
					disableSecurityAdvisor();
				}
			} else {
				log.info("Site: " + s.getId() + " has not passed the gracetime yet and will be skipped.");
			}
		}
		
		log.info("SoftSiteDeletionJob completed.");
	}

	/**
	 * Time in the past which sites deleted before can be deleted.
	 * 
	 * @return
	 */
	private Date getGraceDate() {
		// get the gracetime config param in days.
		int gracetime = serverConfigurationService.getInt(
				"site.soft.deletion.gracetime", GRACETIME_DEFAULT);

		// get calendar for gracetime
		Calendar grace = Calendar.getInstance();
		grace.add(Calendar.DATE, -gracetime);
		Date graceDate = grace.getTime();
		log.debug("Grace set to: " + graceDate);
		return graceDate;
	}

	/**
	 * Setup a security advisor for this transaction
	 */
	private void enableSecurityAdvisor() {
		securityService.pushAdvisor(securityAdvisor);
	}

	/**
	 * Remove security advisor
	 */
	private void disableSecurityAdvisor() {
		securityService.popAdvisor();
	}

	@Setter
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private SiteService siteService;

	@Setter
	private SecurityService securityService;
}
