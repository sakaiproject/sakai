/**
 * $URL:  $
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
package org.sakaiproject.dash.jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.sakaiproject.api.app.scheduler.SchedulerManager;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.app.scheduler.jobs.SpringJobBeanWrapper;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.model.JobRun;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.dash.dao.JobRunImpl;
import org.sakaiproject.dash.dao.DashHibernateDao;
import org.sakaiproject.dash.logic.EventCopy;
import org.sakaiproject.dash.logic.DashboardCommonLogicImpl.DashboardLogicSecurityAdvisor;

//TODO: Find all statsUpdateManager and replace with this dashboard job

public class DashCheckAdminChangesJob extends DashQuartzJob {
	private Log	logger = LogFactory.getLog(DashCheckAdminChangesJob.class);
	
	//Matches the bean id
	final static String beanId = "dashCheckAdminChangesJob";
	 
	//Matches the jobName
	final static String jobName = "Dashboard Check for Admin Configuration Changes Job";
	 
	public void init() {
		super.init();
	    logger.info(this + " init()");
	}

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	    String quartzServer = sakaiProxy.getConfigParam("dashboard_quartzServer", null);
	    String serverName = sakaiProxy.getServerId();
    	if (quartzServer != null && serverName != null && quartzServer.equals(serverName))
    	{
    		// the current server is the server to execute dashboard quartz jobs
    		logger.info(this + " execute: " + getConfigMessage());
    		try {
				dashboardCommonLogic.checkForAdminChanges();
			} catch (Exception e) {
				logger.warn("Error executing dashboard quartz job for checking admin changes " , e);
			}	
    	}
    }
}


