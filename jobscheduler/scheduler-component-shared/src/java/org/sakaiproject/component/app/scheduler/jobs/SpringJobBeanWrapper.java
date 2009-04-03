/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.component.app.scheduler.jobs;

import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This class is final to ensure that it will be constructable
 * by the scheduler infrastructure
 */
public class SpringJobBeanWrapper implements JobBeanWrapper, Job {

   private String beanId;
   private String jobName;
   private SchedulerManager schedulerManager;

   public SpringJobBeanWrapper() {
   }

   public Class getJobClass() {
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
      job.execute(jobExecutionContext);
   }

   public SchedulerManager getSchedulerManager() {
      return schedulerManager;
   }

   public void setSchedulerManager(SchedulerManager schedulerManager) {
      this.schedulerManager = schedulerManager;
   }
}
