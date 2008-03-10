package org.sakaiproject.component.app.scheduler.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.cover.ComponentManager;

public class SpringStatefulJobBeanWrapper extends SpringJobBeanWrapper implements StatefulJob {

	private String beanId;
	private String jobName;
	private SchedulerManager schedulerManager;

	public SpringStatefulJobBeanWrapper() {
		super();
	}

	public void execute(JobExecutionContext jobExecutionContext) throws 
	JobExecutionException {
		String beanId = 
			jobExecutionContext.getJobDetail().getJobDataMap().getString(SPRING_BEAN_NAME);
		Job job = (Job) ComponentManager.get(beanId);
		job.execute(jobExecutionContext);
	}
}