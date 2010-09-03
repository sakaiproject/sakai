package org.sakaiproject.component.app.scheduler.jobs;

import org.sakaiproject.api.app.scheduler.ConfigurableJobProperty;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jul 29, 2010
 * Time: 2:29:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpringConfigurableJobProperty
    implements ConfigurableJobProperty
{
    private String
        labelKey,
        descriptionKey,
        defaultValue;
    private boolean
        required = false;

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public void setDescriptionResourceKey(String descriptionKey)
    {
        this.descriptionKey = descriptionKey;
    }

    public void setLabelResourceKey(String labelKey)
    {
        this.labelKey = labelKey;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public String getLabelResourceKey()
    {
        return labelKey;
    }

    public String getDescriptionResourceKey()
    {
        return descriptionKey;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public boolean isRequired()
    {
        return required;
    }
}
