package org.sakaiproject.component.app.scheduler.jobs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Sep 20, 2010
 * Time: 3:36:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpringInitialJobSchedule
{
    private SpringJobBeanWrapper jobWrapper;
    private String name;
    private String triggerName;
    private String cronExpression;
    private Map <String, String> configuration = new HashMap<String, String>();

    public void setJobBeanWrapper (SpringJobBeanWrapper jobWrapper)
    {
        this.jobWrapper = jobWrapper;
    }

    public SpringJobBeanWrapper getJobBeanWrapper()
    {
        return jobWrapper;
    }

    public void setJobName (String name)
    {
        this.name = name;
    }

    public String getJobName()
    {
        return name;
    }

    public void setTriggerName (String tName)
    {
        this.triggerName = tName;
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    public void setCronExpression (String exp)
    {
        this.cronExpression = exp;
    }

    public String getCronExpression ()
    {
        return cronExpression;
    }

    public void setConfiguration (Map<String, String> conf)
    {
        configuration.clear();

        if (conf != null)
            configuration.putAll(conf);
    }

    public Map<String, String> getConfiguration()
    {
        return configuration;
    }
}
