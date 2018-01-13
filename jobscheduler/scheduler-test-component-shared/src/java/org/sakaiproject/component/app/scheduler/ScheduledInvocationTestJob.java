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
package org.sakaiproject.component.app.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.time.api.TimeService;

@Slf4j
public class ScheduledInvocationTestJob implements Job {

	/** Dependency: ScheduledInvocationManager */
	protected ScheduledInvocationManager m_sim = null;
	
	public void setSim(ScheduledInvocationManager service)
	{
		m_sim = service;
	}
	

	/** Dependency: TimeService */
	protected TimeService m_timeService = null;
	
	public void setTimeService(TimeService service)
	{
		m_timeService = service;
	}
	
	
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		log.info("Creating a delayed invocation");
		String uuid = m_sim.createDelayedInvocation(Instant.now(), "scheduledInvocationTestCommand", "Hello World!");
	
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
		    log.warn("Got interrupted: "+ e.getMessage());
		}
		
		log.info("SimTester: Deleting invocation ["+uuid+"]");
		m_sim.deleteDelayedInvocation(uuid);
		
		log.info("SimTester: Creating another delayed invocation");
		m_sim.createDelayedInvocation(Instant.now(), "scheduledInvocationTestCommand", "Hello World!");

		m_sim.createDelayedInvocation(Instant.now().plus(1, ChronoUnit.MINUTES), "scheduledInvocationTestCommand", "Delayed");
	
	}

}
