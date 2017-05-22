package org.sakaiproject.component.app.scheduler;

import org.quartz.Job;
import org.sakaiproject.api.app.scheduler.ConfigurableJobBeanWrapper;
import org.sakaiproject.api.app.scheduler.ConfigurableJobProperty;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;
import org.sakaiproject.api.app.scheduler.DefaultJobPropertyValidator;

import java.util.Set;

/**
 * Allow configurable autowired jobs.
 */
public class ConfigurableAutowiredJobBeanWrapper extends AutowiredJobBeanWrapper implements ConfigurableJobBeanWrapper {


    private Set<ConfigurableJobProperty>
            jobProperties;
    private String
            resourceBundleBase;
    private ConfigurableJobPropertyValidator
            validator = DEFAULT_VALIDATOR;
    private static final DefaultJobPropertyValidator
            DEFAULT_VALIDATOR = new DefaultJobPropertyValidator();

    public ConfigurableAutowiredJobBeanWrapper(Class<? extends Job> aClass, String jobType) {
        super(aClass, jobType);
    }

    public void setResourceBundleBase(String base) {
        resourceBundleBase = base;
    }

    public String getResourceBundleBase() {
        return resourceBundleBase;
    }

    public void setConfigurableJobProperties(Set<ConfigurableJobProperty> properties) {
        jobProperties = properties;
    }

    public Set<ConfigurableJobProperty> getConfigurableJobProperties() {
        return jobProperties;
    }

    public void setConfigurableJobPropertyValidator(ConfigurableJobPropertyValidator validator) {
        this.validator = validator;
    }

    public ConfigurableJobPropertyValidator getConfigurableJobPropertyValidator() {
        return validator;
    }
}
