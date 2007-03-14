package org.sakaiproject.component.app.scheduler.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.db.api.SqlService;

public class ScheduledInvocationRunner implements Job {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationRunner.class);

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;
	
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}
	
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		String sql = "SELECT INVOCATION_ID, INVOCATION_TIME, COMPONENT, CONTEXT FROM SCHEDULER_DELAYED_INVOCATION WHERE TIME < NOW()";
		
		m_sqlService.dbRead(sql);
		
		//foreach row:
		
		//String uuid;
		//String component;
		
		// DbService.dbWrite("DELETE FROM SCHEDULER_DELAYED_INVOCATION WHERE UUID = uuid");
		
		//ScheduledInvocationCommand command = ComponentManager.get(JOB_ID);
		// command.execute(CONTEXT);
		
		//done!
		

	}

	

}
