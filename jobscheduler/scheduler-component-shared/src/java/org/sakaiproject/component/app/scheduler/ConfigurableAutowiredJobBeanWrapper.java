/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
