/**********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.syllabus;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;


public class FixPublicSyllabusAttachmentsJob implements Job {

	private static final Log LOG = LogFactory.getLog(FixPublicSyllabusAttachmentsJob.class);
	private SyllabusManager syllabusManager;
	private String userId = "admin";

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		loginToSakai();

		Set<SyllabusData> syllabi = syllabusManager.findPublicSyllabusData();
		if (LOG.isInfoEnabled()) {
			LOG.info("Executing FixPublicSyllabusAttachmentsJob now");
			LOG.info("Number of public syllabus found: "+syllabi.size());
		}
		for (SyllabusData syllabus: syllabi) {
			syllabusManager.updateSyllabusAttachmentsViewState(syllabus);
		}

		logoutFromSakai();
	}

	protected void loginToSakai() {
	    Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.setUserId(userId);
		sakaiSession.setUserEid(userId);

		// establish the user's session
		UsageSessionService.startSession(userId, "127.0.0.1", FixPublicSyllabusAttachmentsJob.class.getName());
		
		// update the user's externally provided realm definitions
		AuthzGroupService.refreshUser(userId);

		// post the login event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, null, true));
	}

	protected void logoutFromSakai() {
		// post the logout event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
	}

	public SyllabusManager getSyllabusManager() {
		return syllabusManager;
	}

	public void setSyllabusManager(SyllabusManager syllabusManager) {
		this.syllabusManager = syllabusManager;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}

