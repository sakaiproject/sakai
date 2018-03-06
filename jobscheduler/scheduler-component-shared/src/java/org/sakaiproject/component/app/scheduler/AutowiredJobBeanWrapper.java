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

import org.quartz.Job;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;

/**
 * This is a JobBeanWrapper that will just be registered with the Job Scheduler.
 * This class isn't actually used when running the job as quartz will directly create an instance of the class.
 */
public class AutowiredJobBeanWrapper  implements JobBeanWrapper {

    private String jobType;
    private Class<? extends Job> aClass;

    public AutowiredJobBeanWrapper(Class<? extends Job> aClass, String jobType) {
        this.aClass = aClass;
        this.jobType = jobType;
    }

    @Override
    public String getBeanId() {
        // We don't need a bean ID as a new instance will be created and will get autowired.
        return null;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return aClass;
    }

    @Override
    public String getJobType() {
        return jobType;
    }
}
