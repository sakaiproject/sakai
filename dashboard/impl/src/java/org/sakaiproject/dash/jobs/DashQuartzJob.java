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

public class DashQuartzJob implements Job {
	private Log	logger = LogFactory.getLog(DashQuartzJob.class);

	private String configMessage;
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	protected DashboardCommonLogic dashboardCommonLogic;
	public void setDashboardCommonLogic(DashboardCommonLogic dashboardCommonLogic) {
		this.dashboardCommonLogic = dashboardCommonLogic;
	}

	protected SchedulerManager schedulerManager;
	public void setSchedulerManager(SchedulerManager schedulerManager) {
		this.schedulerManager = schedulerManager;
	}
	
	//Matches the bean id
	final static String beanId = "";
	 
	//Matches the jobName
	final static String jobName = "";
	

	public void init() {
		
	}

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    	// to-do
    }

    public String getConfigMessage() {
            return configMessage;
    }

    public void setConfigMessage(String configMessage) {
            this.configMessage = configMessage;
    }

}


