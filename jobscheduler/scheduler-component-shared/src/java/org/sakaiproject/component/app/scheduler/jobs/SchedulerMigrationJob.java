package org.sakaiproject.component.app.scheduler.jobs;

import org.quartz.*;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.component.app.scheduler.DelayedInvocationDAO;
import org.sakaiproject.scheduler.events.hibernate.DelayedInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * This attempts to migrate all the scheduled invocations that used a quartz job that ran every 5 minutes to
 * a use quartz directly to run the jobs.
 */
public class SchedulerMigrationJob implements Job {

    public static final String SCHEDULED_JOB_NAME = "org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner";
    public static final String SCHEDULED_JOB_GROUP = "org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl";

    private Logger log = LoggerFactory.getLogger(SchedulerMigrationJob.class);

    @Inject
    private Scheduler scheduler;

    @Inject
    private DelayedInvocationDAO invocationDAO;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // First need to stop the existing quartz job.
        try {
            if(scheduler.deleteJob(new JobKey(SCHEDULED_JOB_NAME, SCHEDULED_JOB_GROUP))) {
                log.info("Removed old scheduler job.");
            } else {
                // This isn't a failure as we might have run the job before.
                log.info("Didn't remove old scheduler job.");
            }
        } catch (SchedulerException e) {
            throw new JobExecutionException("Failed to remove the existing job, stopping.", e);
        }
        List<DelayedInvocation> all = invocationDAO.all();
        for (DelayedInvocation invocation: all) {
            
        }

        // Then write out new quartz jobs and triggers.

    }
}
