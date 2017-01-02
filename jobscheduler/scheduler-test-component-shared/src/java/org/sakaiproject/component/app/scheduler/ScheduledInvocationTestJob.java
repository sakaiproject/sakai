package org.sakaiproject.component.app.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.time.api.TimeService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ScheduledInvocationTestJob implements Job {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledInvocationTestJob.class);

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

		LOG.info("Creating a delayed invocation");
		String uuid = m_sim.createDelayedInvocation(Instant.now(), "scheduledInvocationTestCommand", "Hello World!");
	
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
		    LOG.warn("Got interrupted: "+ e.getMessage());
		}
		
		LOG.info("SimTester: Deleting invocation ["+uuid+"]");
		m_sim.deleteDelayedInvocation(uuid);
		
		LOG.info("SimTester: Creating another delayed invocation");
		m_sim.createDelayedInvocation(Instant.now(), "scheduledInvocationTestCommand", "Hello World!");

		m_sim.createDelayedInvocation(Instant.now().plus(1, ChronoUnit.MINUTES), "scheduledInvocationTestCommand", "Delayed");
	
	}

}
