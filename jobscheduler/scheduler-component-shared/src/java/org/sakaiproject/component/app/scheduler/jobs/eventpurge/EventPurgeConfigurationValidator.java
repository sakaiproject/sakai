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
