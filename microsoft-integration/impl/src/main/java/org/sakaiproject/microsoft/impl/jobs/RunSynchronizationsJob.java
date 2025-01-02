/**
 * Copyright (c) 2024 The Apereo Foundation
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

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftLoggingService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.data.MicrosoftLogInvokers;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.model.MicrosoftLog;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.RollbackException;

@Slf4j
public class RunSynchronizationsJob implements Job {

	@Setter
	private SecurityService securityService;

	@Setter
	private SessionManager sessionManager;
	
	@Setter
	private MicrosoftSynchronizationService microsoftSynchronizationService;

	@Setter
	private MicrosoftLoggingService microsoftLoggingService;
	
	@Setter
	MicrosoftConfigurationService microsoftConfigurationService;

	private final int MAX_RETRIES = 3;

	public void init() {
		log.info("Initializing Run Synchronizations Job");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("RunSynchronizationsJob started.");
		Session session = sessionManager.getCurrentSession();
		ZonedDateTime startTime = ZonedDateTime.now();

		try {
			session.setUserEid("admin");
			session.setUserId("admin");
			session.setAttribute("origin", MicrosoftLogInvokers.JOB.getCode());
			SakaiSiteFilter siteFilter = microsoftConfigurationService.getJobSiteFilter();
			
			List<SiteSynchronization> list = microsoftSynchronizationService.getAllSiteSynchronizations(true);
			for(SiteSynchronization ss : list) {
				int retryCount = 0;
				while (retryCount < MAX_RETRIES) {
					try {
						if(retryCount > 0) {
							log.debug("Retrying Site Synchronization for siteId={}, teamId={} for the {} time", ss.getSiteId(), ss.getTeamId(), retryCount);
						}

						if(ss.getSite() == null || !siteFilter.match(ss.getSite())) {
							log.debug("Site with id={} skipped due to filter restrinctions", ss.getSiteId());
							break;
						}

						microsoftSynchronizationService.runSiteSynchronization(ss);

						if (ss.getGroupSynchronizationsList().stream().anyMatch(group -> group.getStatus().equals(SynchronizationStatus.OK)) && ss.getStatus().equals(SynchronizationStatus.ERROR)) {
							ss.setStatus(SynchronizationStatus.PARTIAL_OK);
							microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
						}

						break;
					} catch (MicrosoftGenericException e) {
						log.debug("MicrosoftGenericException running Site Synchronization for siteId={}, teamId={}", ss.getSiteId(), ss.getTeamId());
						retryCount++;
					} catch (RollbackException e) {
						log.debug("RollbackException running Site Synchronization for siteId={}, teamId={}", ss.getSiteId(), ss.getTeamId());
						retryCount++;
					} catch (ConcurrentModificationException e) {
						log.debug("ConcurrentModificationException running Site Synchronization for siteId={}, teamId={}", ss.getSiteId(), ss.getTeamId());
						retryCount++;
					} catch (Exception e) {
						log.error("Exception while running RunSynchronizationsJob", e);
						retryCount++;
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception while running RunSynchronizationsJob", e);
		}
		finally {
			session.clear();
		}

		List<MicrosoftLog> logs = microsoftLoggingService.findFromZonedDateTime(startTime);
		//if log is not from job invoker and is an error, remove it
		logs = logs.stream().filter(l -> Arrays.stream(MicrosoftLog.MICROSOFT_ERRORS).noneMatch(e -> e.equals(l.getEvent())) && l.getContext().containsKey("origin") && l.getContext().get("origin").equals(MicrosoftLogInvokers.JOB.getCode())).collect(Collectors.toList());
		Set<String> sites = logs.stream().filter(l -> l.getEvent().equals(MicrosoftLog.EVENT_ADD_MEMBER) || l.getEvent().equals(MicrosoftLog.EVENT_ADD_OWNER) || l.getEvent().equals(MicrosoftLog.EVENT_USER_ADDED_TO_CHANNEL)).collect(Collectors.toList()).stream().map(l -> l.getContext().get("siteId")).collect(Collectors.toSet());
		Map<String, String> data = new HashMap<>();
		data.put("log_amount", String.valueOf(logs.size()));
		data.put("start_time", startTime.toLocalDateTime().toString());
		data.put("sites_modified_amount", String.valueOf(sites.size()));
		data.put("sites_modified", String.join(",", sites));
		data.putAll(logs.stream().collect(Collectors.groupingBy(MicrosoftLog::getEvent, Collectors.counting())).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))));

		microsoftLoggingService.saveLog(MicrosoftLog.builder().event(MicrosoftLog.EVENT_JOB_RESULT).context(data).build());
		
		log.info("RunSynchronizationsJob completed.");
	}
}
