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
