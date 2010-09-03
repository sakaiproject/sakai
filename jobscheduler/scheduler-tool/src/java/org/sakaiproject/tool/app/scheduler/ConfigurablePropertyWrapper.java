package org.sakaiproject.tool.app.scheduler;

import org.sakaiproject.api.app.scheduler.ConfigurableJobProperty;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 3, 2010
 * Time: 1:38:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurablePropertyWrapper
{
    private ConfigurableJobProperty
        property;
    private String
        value;

    public void setJobProperty (ConfigurableJobProperty prop)
    {
        property = prop;
    }

    public ConfigurableJobProperty getJobProperty()
    {
        return property;
    }

    public void setValue (String v)
    {
        value = v;
    }

    public String getValue()
    {
        return value;
    }
}
