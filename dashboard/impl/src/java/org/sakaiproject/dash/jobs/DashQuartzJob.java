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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.scheduler.SchedulerManager;

import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.SakaiProxy;
//TODO: Find all statsUpdateManager and replace with this dashboard job

public class DashQuartzJob implements Job {
	private Logger	logger = LoggerFactory.getLogger(DashQuartzJob.class);

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


