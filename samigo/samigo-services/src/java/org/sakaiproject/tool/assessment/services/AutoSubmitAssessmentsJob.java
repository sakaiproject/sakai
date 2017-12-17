/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.services;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.samigo.api.SamigoETSProvider;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

@Slf4j
public class AutoSubmitAssessmentsJob implements StatefulJob {
	
	protected String serverName = "unknown";

	private AuthzGroupService authzGroupService;
	private SamigoETSProvider etsProvider;

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
	
	public void setSamigoETSProvider(SamigoETSProvider value)
	{
		etsProvider = value;
	}

	public void init() {
		log.debug("AutoSubmitAssessmentsJob init()  ");
	}

	public void destroy() {
		log.debug("AutoSubmitAssessmentsJob destroy()");
	}

	
	public AutoSubmitAssessmentsJob() {
		super();
	}
 
	/*
	 * Quartz job to check for assessment attempts that should be autosubmitted
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jobInfo) throws JobExecutionException {
		loginToSakai("admin");

		String jobName = jobInfo.getJobDetail().getKey().getName(); 
		String triggerName = jobInfo.getTrigger().getKey().getName();
 		Date requestedFire = jobInfo.getScheduledFireTime();
		Date actualfire = jobInfo.getFireTime();

		StringBuffer whoAmI = new StringBuffer("AutoSubmitAssessmentsJob $");
		whoAmI.append(" Job: ");
		whoAmI.append(jobName);
		whoAmI.append(" Trigger: ");
		whoAmI.append(triggerName);
		
		if (requestedFire != null) {
			whoAmI.append(" Fire scheduled: ");
			whoAmI.append(requestedFire.toString());
		}
		
		if (actualfire != null) {
			whoAmI.append(" Fire actual: ");
			whoAmI.append(actualfire.toString());
		}
		
		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_AUTO_SUBMIT_JOB,
				safeEventLength(whoAmI.toString()), true));			

		log.info("Start Job: " + whoAmI.toString());
		
		GradingService gradingService = new GradingService();
		int failures = gradingService.autoSubmitAssessments();
		
		if (failures > 0)
		{
			etsProvider.notifyAutoSubmitFailures(failures);
		}
		
		log.info("End Job: " + whoAmI.toString() + " (" + failures + " failures)");
		
		logoutFromSakai();
	}
	
	/**
	 * <p>Login to sakai and start a user session. This users is intended
	 * to be one of the 'hard wired' users; admin, postmaster, or synchrobot.</p>
	 * <p>( this list of users can be extended; add the user via UI, update
	 * the sakai_users table so their EID matches ID, add them to the
	 * admin realm, restart )</p>
	 * @param whoAs - who to log in as
	 */
	protected void loginToSakai(String whoAs) {
		
		serverName = ServerConfigurationService.getServerName();
		log.debug(" AutoSubmitAssessmentsJob Logging into Sakai on " + serverName + " as " + whoAs);

		UsageSession session = UsageSessionService.startSession(whoAs, serverName, "AutoSubmitAssessmentsJob");
        if (session == null)
        {
    		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_AUTO_SUBMIT_JOB_ERROR, whoAs + " unable to log into " + serverName, true));
    		return;
        }
		
		Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.setUserId(whoAs);
		sakaiSession.setUserEid(whoAs);

		// update the user's externally provided realm definitions
		authzGroupService.refreshUser(whoAs);

		// post the login events
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, whoAs + " running " + serverName, true));

	}


	protected void logoutFromSakai() {
		String serverName = ServerConfigurationService.getServerName();
		log.debug(" AutoSubmitAssessmentsJob Logging out of Sakai on " + serverName);
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
		UsageSessionService.logout(); // safe to logout? what if other jobs are running?
	}
	
	/**
	 * Sometimes when logging to the sakai_events table it's possible to be logging
	 * with a string you don't know the size of. (An exception message is a good
	 * example)
	 * 
	 * This method is supplied to keep the lengh of logged messages w/in the limits
	 * of the sakai_event.ref column size.
	 * 
	 * The sakai_event.ref column size is currently 256
	 * 
	 * @param target
	 * @return
	 */
	static final public String safeEventLength(final String target) 
	{
		return (target.length() > 255 ? target.substring(0, 255) : target);
	}	
}
