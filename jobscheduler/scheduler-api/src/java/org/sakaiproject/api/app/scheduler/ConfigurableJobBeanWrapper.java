/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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
package org.sakaiproject.api.app.scheduler;

import java.util.Set;

/**
 * This interface extends the JobWrapper interface such that some jobs may provide definitions of properties which
 * can be configured for the Job, or its Triggers.
 *
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jul 26, 2010
 * Time: 3:41:58 PM
 */
public interface ConfigurableJobBeanWrapper
    extends JobBeanWrapper
{
    /**
     *  Since the job will be defined in a component outside of the job scheduler, the job scheduler has no knowledge
     *  of the keys nor the resource files to use when rendering these properties in the UI. Thus, the job should
     *  be configured in a wrapper which supplies a ResourceBundle to the job scheduler. That ResourceBundle will
     *  supply the strings to use in the UI.
     */
    //public ResourceBundle getResourceBundle();

    //public String getResourceString(String key);

    public String getResourceBundleBase();
    
    /**
     *  Returns the definitions of the properties which should be presented to the user for configuring the job
     *  that this object wraps.
     */
    public Set<ConfigurableJobProperty> getConfigurableJobProperties();

    public ConfigurableJobPropertyValidator getConfigurableJobPropertyValidator();
}
