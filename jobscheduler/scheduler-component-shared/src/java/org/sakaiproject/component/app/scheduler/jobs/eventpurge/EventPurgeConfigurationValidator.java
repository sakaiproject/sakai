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
package org.sakaiproject.component.app.scheduler.jobs.eventpurge;

import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 27, 2010
 * Time: 2:58:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventPurgeConfigurationValidator implements ConfigurableJobPropertyValidator
{
    public void assertValid(String propertyLabel, String value)
        throws ConfigurableJobPropertyValidationException
    {
        if (EventLogPurgeJob.NUMBER_DAYS.equals(propertyLabel))
        {
            if (value == null || value.trim().length() < 1)
            {
                throw new ConfigurableJobPropertyValidationException ("days.empty");
            }

            int num = 0;

            try
            {
                num = Integer.parseInt(value);
            }
            catch (NumberFormatException nfe)
            {
                throw new ConfigurableJobPropertyValidationException ("days.numberformat");
            }

            if (num < 1)
                throw new ConfigurableJobPropertyValidationException ("days.lessthanone");

        }
    }
}
