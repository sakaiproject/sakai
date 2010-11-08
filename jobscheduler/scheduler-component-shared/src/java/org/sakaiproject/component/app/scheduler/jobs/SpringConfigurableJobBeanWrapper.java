package org.sakaiproject.component.app.scheduler.jobs;

import org.sakaiproject.api.app.scheduler.ConfigurableJobBeanWrapper;
import org.sakaiproject.api.app.scheduler.ConfigurableJobProperty;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;
import org.sakaiproject.api.app.scheduler.DefaultJobPropertyValidator;

import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jul 29, 2010
 * Time: 2:22:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpringConfigurableJobBeanWrapper
    extends SpringJobBeanWrapper
    implements ConfigurableJobBeanWrapper
{
    private Set<ConfigurableJobProperty>
        jobProperties;
    //private ResourceBundle
    //    rb;
    private String
        resourceBundleBase;
    private ConfigurableJobPropertyValidator
        validator = DEFAULT_VALIDATOR;
    private static final DefaultJobPropertyValidator
        DEFAULT_VALIDATOR = new DefaultJobPropertyValidator();

    public void setResourceBundleBase (String base)
    {
        resourceBundleBase = base;
    }

    public String getResourceBundleBase()
    {
        return resourceBundleBase;
    }
/*
    public ResourceBundle getResourceBundle()
    {
        if (rb == null)
        {
            if (resourceBundleBase == null)
                return null;
            rb = ResourceBundle.getBundle(resourceBundleBase);
        }
        return rb;
    }

    public String getResourceString (String key)
    {
        return getResourceBundle().getString(key);
    }
*/    
    public void setConfigurableJobProperties (Set<ConfigurableJobProperty> properties)
    {
        jobProperties = properties;
    }

    public Set<ConfigurableJobProperty> getConfigurableJobProperties()
    {
        return jobProperties;
    }

    public void setConfigurableJobPropertyValidator(ConfigurableJobPropertyValidator validator)
    {
        this.validator = validator;
    }
    public ConfigurableJobPropertyValidator getConfigurableJobPropertyValidator()
    {
        return validator;
    }
}
