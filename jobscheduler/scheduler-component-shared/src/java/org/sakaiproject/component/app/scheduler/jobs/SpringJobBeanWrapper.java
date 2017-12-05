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

import lombok.extern.slf4j.Slf4j;

import org.quartz.StatefulJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * This class is final to ensure that it will be constructable
 * by the scheduler infrastructure
 * @see SpringStatefulJobBeanWrapper
 */
@Slf4j
public class SpringJobBeanWrapper implements JobBeanWrapper, Job {

   private String beanId;
   private String jobName;
   private SchedulerManager schedulerManager;

   public SpringJobBeanWrapper() {
   }

   public Class<? extends Job> getJobClass() {
      return this.getClass();
   }

   public String getBeanId() {
      return beanId;
   }

   public void setBeanId(String beanId) {
      this.beanId = beanId;
   }

   public String getJobType() {
      return jobName;
   }

   public void setJobName(String jobName) {
      this.jobName = jobName;
   }

   public void init() {
      getSchedulerManager().registerBeanJob(getJobType(), this);
   }

   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
      String beanId = jobExecutionContext.getJobDetail().getJobDataMap().getString(SPRING_BEAN_NAME);
      Job job = (Job) ComponentManager.get(beanId);
      if (job instanceof StatefulJob) {
         log.warn("Non-stateful wrapper used with stateful job: "+ beanId+
         " You probably wanted to use SpringStatefulJobBeanWrapper for this job.");
      }
      job.execute(jobExecutionContext);
   }

   public SchedulerManager getSchedulerManager() {
      return schedulerManager;
   }

   public void setSchedulerManager(SchedulerManager schedulerManager) {
      this.schedulerManager = schedulerManager;
   }
}
