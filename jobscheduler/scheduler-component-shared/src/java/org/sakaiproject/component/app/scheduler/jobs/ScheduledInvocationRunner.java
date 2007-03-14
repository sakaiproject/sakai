package org.sakaiproject.component.app.scheduler.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public class ScheduledInvocationRunner implements Job {

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		// DbService.dbRead("SELECT JOB_ID FROM JOBS WHERE DATE < NOW()");
		
		//ScheduledInvocationCommand command = ComponentManager.get(JOB_ID);
		// command.execute();
		
		// DbService.dbWrite("DELETE ROM JOBS WHERE JOBS_ID = jobid");
		

	}

}
