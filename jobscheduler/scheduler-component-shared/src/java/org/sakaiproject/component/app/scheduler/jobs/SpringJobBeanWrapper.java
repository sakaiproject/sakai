/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
public final class SpringJobBeanWrapper implements JobBeanWrapper, Job {

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
