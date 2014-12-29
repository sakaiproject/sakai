package org.sakaiproject.scheduler.configurable.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.app.scheduler.jobs.AbstractConfigurableJob;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 25, 2010
 * Time: 4:02:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestConfigurableJob extends AbstractConfigurableJob
{
    private static final Log
        LOG = LogFactory.getLog(TestConfigurableJob.class);

    public static final String
        INTEGER_PROPERTY            = "integer.property",
        STRING_PROPERTY             = "string.property",
        BOOLEAN_PROPERTY            = "boolean.property";

    @Override
    public void runJob()
        throws JobExecutionException
    {

        LOG.debug ("running TestConfigurableJob");

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
            LOG.debug ("integer property is null");
        }
        else
        {
            try
            {
                LOG.debug ("integer property is set to integer value: '" + Integer.parseInt(temp) + "'");
            }
            catch (NumberFormatException nfe)
            {
                LOG.error ("integer property is set to a non-integer value: '" + temp + "'");
            }
        }
    }

    private final void readStringProperty()
        throws JobExecutionException
    {
        String temp = getConfiguredProperty(STRING_PROPERTY);

        if (temp == null)
        {
            LOG.debug ("string property is null");
        }
        else if (temp.trim().length() == 0)
        {
            LOG.debug ("string property is empty");
        }
        else
        {
            LOG.debug ("string property is set to: '" + temp + "'");
        }
    }

    private final void readBooleanProperty()
        throws JobExecutionException
    {
        String temp = getConfiguredProperty(BOOLEAN_PROPERTY);

        if (temp == null)
        {
            LOG.debug ("boolean property is null");
        }
        else
        {
            LOG.debug ("boolean property is set to boolean value: '" + Boolean.parseBoolean(temp) + "'");
        }
    }
}
