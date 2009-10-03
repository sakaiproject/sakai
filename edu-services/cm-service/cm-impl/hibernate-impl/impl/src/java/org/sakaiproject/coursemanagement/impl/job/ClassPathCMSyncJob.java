/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.impl.job;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * A sample quartz job to synchronize the CM data in Sakai's hibernate impl with an
 * xml file available in the classpath.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class ClassPathCMSyncJob extends CmSynchronizer implements Job {
	private static final Log log = LogFactory.getLog(ClassPathCMSyncJob.class);

	protected String classPathToXml;

	public void init() {
		if(log.isInfoEnabled()) log.info("init()");
	}
	
	public void destroy() {
		if(log.isInfoEnabled()) log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream getXmlInputStream() {
		return getClass().getClassLoader().getResourceAsStream(classPathToXml);
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		loginToSakai();
		syncAllCmObjects();
		logoutFromSakai();
	}
		
	protected void loginToSakai() {
	    Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.setUserId("admin");
		sakaiSession.setUserEid("admin");

		// establish the user's session
		UsageSessionService.startSession("admin", "127.0.0.1", "CMSync");
		
		// update the user's externally provided realm definitions
		AuthzGroupService.refreshUser("admin");

		// post the login event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, null, true));
	}

	protected void logoutFromSakai() {
		// post the logout event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
	}

	// Dependency Injection
	public void setClassPathToXml(String classPathToXml) {
		this.classPathToXml = classPathToXml;
	}
}
