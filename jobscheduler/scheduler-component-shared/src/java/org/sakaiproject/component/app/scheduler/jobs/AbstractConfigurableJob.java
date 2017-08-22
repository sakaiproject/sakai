/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 25, 2010
 * Time: 3:56:08 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractConfigurableJob implements Job
{

    private JobExecutionContext
        executionContext = null;

    public void setJobExecutionContext(JobExecutionContext jec)
    {
        executionContext = jec;
    }
    
    public JobExecutionContext getJobExecutionContext()
    {
        return executionContext;
    }

    public String getConfiguredProperty (String key)
    {
        return getJobExecutionContext().getMergedJobDataMap().get(key).toString();
    }

    public final void execute(JobExecutionContext jobExecutionContext)
        throws JobExecutionException
    {
        setJobExecutionContext(jobExecutionContext);

        runJob();
    }

    public abstract void runJob()
        throws JobExecutionException;
}
