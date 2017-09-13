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
