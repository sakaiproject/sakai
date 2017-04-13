/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.api.ServerConfigurationService;

import java.util.List;

/**
 * This class runs some quartz jobs automatically at startup.
 */
public class AutoRun {

    private static final Log log = LogFactory.getLog(AutoRun.class);

    private SchedulerManager schedulerManager;
    private ServerConfigurationService serverConfigurationService;
    private List<JobBeanWrapper> startup;
    private String config;

    public void setSchedulerManager(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setStartup(List<JobBeanWrapper> startup) {
        this.startup = startup;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public void init() {
        if (config == null || serverConfigurationService.getBoolean(config, false)) {
            log.info("AutoRun running");
            Scheduler scheduler = schedulerManager.getScheduler();

            for (JobBeanWrapper job : startup) {
                try {
                    JobDataMap jobData = new JobDataMap();
                    jobData.put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
                    jobData.put(JobBeanWrapper.JOB_TYPE, job.getJobType());

                    JobDetail jobDetail = JobBuilder.newJob(job.getJobClass())
                            .withIdentity(job.getJobType(), null)
                            .setJobData(jobData)
                            .build();

                    // Non durable job that will get removed
                    scheduler.addJob(jobDetail, true, true);
                    scheduler.triggerJob(jobDetail.getKey());
                    log.info("Triggered job: " + job.getJobType());
                } catch (SchedulerException se) {
                    log.warn("Failed to run job: " + startup, se);
                }

            }
        }
    }

}
