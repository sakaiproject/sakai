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
package org.sakaiproject.api.app.scheduler;

/**
 * This defines a property which may be configured at the time of Job or Trigger creation. This property is
 * interpreted by the Job itself by retrieving its value from the JobDataMap held within the JobContext sent
 * in to the execute() method.
 *
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jul 27, 2010
 * Time: 10:03:33 AM
 */
public interface ConfigurableJobProperty
{
    /**
     * Returns the resource key for the label that will identify this property in the UI. This resource key must
     * be recognized by the ResourceBundle returned in implementation of ConfigurableJobBeanWrapper.getResourceBundle()
     *
     * @return key to the resource string for the property label
     */
    String getLabelResourceKey();

    /**
     * Returns the resource key for the description of this property. This resource key must be recognized by the
     * ResourceBundle returned in the implementation of ConfigurableJobBeanWrapper.getResourceBundle()
     *
     * @return key for the resource string for the property description
     */
    String getDescriptionResourceKey();

    /**
     * Returns the default value for this property.
     *
     * @return default value or null for no default
     */
    String getDefaultValue();

    /**
     * Indicates whether this property is required for the proper execution of the job.
     *
     * @return if true this property must be configured before a job can execute
     */
    boolean isRequired();
}
