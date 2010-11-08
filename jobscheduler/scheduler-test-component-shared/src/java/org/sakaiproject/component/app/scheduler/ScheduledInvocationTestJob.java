package org.sakaiproject.component.app.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.time.api.TimeService;

public class ScheduledInvocationTestJob implements Job {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationTestJob.class);

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

		LOG.info("SimTester: Creating a delayed invocation");
		String uuid = m_sim.createDelayedInvocation(m_timeService.newTime(), "scheduledInvocationTestCommand", "Hello World!");
	
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOG.info("SimTester: Deleting invocation ["+uuid+"]");
		m_sim.deleteDelayedInvocation(uuid);
		
		LOG.info("SimTester: Creating another delayed invocation");
		m_sim.createDelayedInvocation(m_timeService.newTime(), "scheduledInvocationTestCommand", "Hello World!");
	

	}

	

}
