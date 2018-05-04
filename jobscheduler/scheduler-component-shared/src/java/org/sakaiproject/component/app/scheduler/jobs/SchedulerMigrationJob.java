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
package org.sakaiproject.component.app.scheduler.jobs;

import java.time.Instant;
import java.util.List;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.quartz.*;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;

import org.sakaiproject.component.app.scheduler.DelayedInvocationDAO;
import org.sakaiproject.scheduler.events.hibernate.DelayedInvocation;

/**
 * This attempts to migrate all the scheduled invocations that used a quartz job that ran every 5 minutes to
 * a use quartz directly to run the jobs.
 */
@Slf4j
public class SchedulerMigrationJob implements Job {

    public static final String SCHEDULED_JOB_NAME = "org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner";
    public static final String SCHEDULED_JOB_GROUP = "org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl";

    @Inject
    private DelayedInvocationDAO invocationDAO;

    @Inject
    private ScheduledInvocationManager manager;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // First need to stop the existing quartz job.
        Scheduler scheduler = context.getScheduler();

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
            Instant instant = Instant.ofEpochMilli(invocation.getTime().getTime());
            // This will create the job as well.
            manager.createDelayedInvocation(instant, invocation.getComponent(), invocation.getContext());
            invocationDAO.remove(invocation);
            log.info("Migrated "+ invocation.getId()+ " of "+ invocation.getComponent() +" at "+ instant);
        }
    }
}
