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

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringJobBeanWrapper implements ApplicationContextAware, Job, JobBeanWrapper {

   @Inject @Setter protected ApplicationContext applicationContext;
   @Inject @Setter protected SchedulerManager schedulerManager;

   @Getter @Setter private String jobName;
   @Getter @Setter private String beanId;

   public void init() {
      schedulerManager.registerBeanJob(this.getJobName(), this);
   }

   public Class<? extends Job> getJobClass() {
      return this.getClass();
   }

   @Override
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
      String beanId = jobExecutionContext.getJobDetail().getJobDataMap().getString(SPRING_BEAN_NAME);
      Job job = (Job) applicationContext.getBean(beanId);
      if (job instanceof StatefulJob) {
         log.warn("Non-stateful wrapper used with stateful job: {}, use SpringStatefulJobBeanWrapper", beanId);
      }
      job.execute(jobExecutionContext);
   }
}
