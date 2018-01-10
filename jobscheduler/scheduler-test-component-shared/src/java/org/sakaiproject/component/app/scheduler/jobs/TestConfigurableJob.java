/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 25, 2010
 * Time: 4:02:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class TestConfigurableJob extends AbstractConfigurableJob
{
    public static final String
        INTEGER_PROPERTY            = "integer.property",
        STRING_PROPERTY             = "string.property",
        BOOLEAN_PROPERTY            = "boolean.property";

    @Override
    public void runJob()
        throws JobExecutionException
    {

        log.debug ("running TestConfigurableJob");

        readIntegerProperty();
        readStringProperty();
        readBooleanProperty();

    }

    private final void readIntegerProperty()
        throws JobExecutionException
    {
        String temp = getConfiguredProperty(INTEGER_PROPERTY);

        if (temp == null)
        {
            log.debug ("integer property is null");
        }
        else
        {
            try
            {
                log.debug ("integer property is set to integer value: '" + Integer.parseInt(temp) + "'");
            }
            catch (NumberFormatException nfe)
            {
                log.error ("integer property is set to a non-integer value: '" + temp + "'");
            }
        }
    }

    private final void readStringProperty()
        throws JobExecutionException
    {
        String temp = getConfiguredProperty(STRING_PROPERTY);

        if (temp == null)
        {
            log.debug ("string property is null");
        }
        else if (temp.trim().length() == 0)
        {
            log.debug ("string property is empty");
        }
        else
        {
            log.debug ("string property is set to: '" + temp + "'");
        }
    }

    private final void readBooleanProperty()
        throws JobExecutionException
    {
        String temp = getConfiguredProperty(BOOLEAN_PROPERTY);

        if (temp == null)
        {
            log.debug ("boolean property is null");
        }
        else
        {
            log.debug ("boolean property is set to boolean value: '" + Boolean.parseBoolean(temp) + "'");
        }
    }
}
