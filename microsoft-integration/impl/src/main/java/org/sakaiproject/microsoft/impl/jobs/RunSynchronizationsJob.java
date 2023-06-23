/**
* Copyright (c) 2023 Apereo Foundation
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
package org.sakaiproject.microsoft.impl.jobs;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunSynchronizationsJob implements Job {

	@Setter
	private SecurityService securityService;

	@Setter
	private SessionManager sessionManager;
	
	@Setter
	private MicrosoftSynchronizationService microsoftSynchronizationService;
	
	@Setter
	MicrosoftConfigurationService microsoftConfigurationService;
	
	public void init() {
		log.info("Initializing Run Synchronizations Job");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("RunSynchronizationsJob started.");
		
		Session session = sessionManager.getCurrentSession();
		try {
			session.setUserEid("admin");
			session.setUserId("admin");
			
			SakaiSiteFilter siteFilter = microsoftConfigurationService.getJobSiteFilter();
			
			List<SiteSynchronization> list = microsoftSynchronizationService.getAllSiteSynchronizations(true);
			for(SiteSynchronization ss : list) {
				try {
					//check filters
					if(ss.getSite() == null || !siteFilter.match(ss.getSite())) {
						log.debug("Site with id={} skipped due to filter restrinctions", ss.getSiteId());
						continue;
					}
					
					microsoftSynchronizationService.runSiteSynchronization(ss);
				} catch (MicrosoftGenericException e) {
					log.debug("Exception running Site Synchronization for siteId={}, teamId={}", ss.getSiteId(), ss.getTeamId());
				}
			}
		} catch (Exception e) {
			log.error("Exception while running RunSynchronizationsJob", e);
		}
		finally {
			session.clear();
		}
		
		log.info("RunSynchronizationsJob completed.");
	}
}
