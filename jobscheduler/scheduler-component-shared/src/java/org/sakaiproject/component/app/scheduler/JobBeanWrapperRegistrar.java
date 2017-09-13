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
package org.sakaiproject.component.app.scheduler;

import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;

import java.util.List;

/**
 * Just registers JobBeanWrappers with the Schedule Manager, this is needed because the autowired
 * jobs don't have to register themselves.
 */
public class JobBeanWrapperRegistrar {

    private SchedulerManager schedulerManager;
    private List<JobBeanWrapper> jobBeans;

    public void setSchedulerManager(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    public void setJobBeans(List<JobBeanWrapper> jobBeans) {
        this.jobBeans = jobBeans;
    }

    public void init() {
        jobBeans.stream().forEach(wrapper -> schedulerManager.registerBeanJob(wrapper.getJobType(), wrapper));
    }
}
