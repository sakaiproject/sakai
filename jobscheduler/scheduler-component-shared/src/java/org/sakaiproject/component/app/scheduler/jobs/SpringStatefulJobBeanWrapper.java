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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * This is the wrapper that tells quartz that is should make the jobs stateful.
 * @see SpringJobBeanWrapper
 */
public class SpringStatefulJobBeanWrapper extends SpringJobBeanWrapper implements StatefulJob {

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