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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.component.cover.ComponentManager;

@PersistJobDataAfterExecution
public class ScheduledInvocationJob implements Job {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationJob.class);


	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {

		String contextId = jobContext.getMergedJobDataMap().getString("contextId");
		String componentId = jobContext.getJobDetail().getKey().getName();

		try {
			ScheduledInvocationCommand command = (ScheduledInvocationCommand) ComponentManager.get(componentId);
			command.execute(contextId);
		} catch (Exception e) {
			LOG.error("Failed to execute component: [" + componentId + "]: ", e);
		}
	}

}
