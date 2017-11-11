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
package org.sakaiproject.api.app.scheduler;

import org.quartz.Job;

/**
 * This is used to represent a job that can be run through the Sakai Scheduler tool.
 */
public interface JobBeanWrapper {

   String SPRING_BEAN_NAME = "org.sakaiproject.api.app.scheduler.JobBeanWrapper.bean";
   String JOB_TYPE = "org.sakaiproject.api.app.scheduler.JobBeanWrapper.jobType";

   /**
    * @return The Spring Bean ID to retrieve from the application context.
     */
   String getBeanId();

   /**
    * This is the class that will get registered with Quartz to be run.
    * @return A Class that implements the Job interface.
     */
   Class<? extends Job> getJobClass();

   /**
    * This is the name that is displayed in the interface for the job.
    * @return A summary of the job.
     */
   String getJobType();

}
