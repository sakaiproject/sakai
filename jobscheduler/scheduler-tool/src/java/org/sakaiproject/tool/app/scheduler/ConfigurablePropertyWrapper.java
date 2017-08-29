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
