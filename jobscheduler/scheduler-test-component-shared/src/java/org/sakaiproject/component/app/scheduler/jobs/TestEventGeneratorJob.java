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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Nov 2, 2010
 * Time: 9:25:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestEventGeneratorJob extends AbstractConfigurableJob
{
    private static final Logger
        LOG = LoggerFactory.getLogger(TestEventGeneratorJob.class);

    public static final String
        STARTDATE_PROPERTY          = "startDate.property",
        NUMEVENTS_PROPERTY          = "numEvents.property",
        MININTERVAL_PROPERTY        = "minInterval.property";

    private TriggerEventManager
        manager = null;

    private int
        numEvents = 10,
        minuteInterval = 60;

    private Date
        startDate = null;

    public TestEventGeneratorJob()
    {
        Calendar
            cal = Calendar.getInstance();

        //initialize the startDate to a week ago by default
        cal.add(Calendar.DAY_OF_YEAR, -7);
        startDate = new Date(cal.getTimeInMillis());
    }

    public final void setTriggerEventManager (TriggerEventManager tem)
    {
        manager = tem;
    }

    public final TriggerEventManager getTriggerEventManager()
    {
        return manager;
    }

    private int getNumEvents()
    {
        return numEvents;
    }

    private int getMinuteInterval()
    {
        return minuteInterval;
    }

    private Date getStartDate()
    {
        return startDate;
    }

    private final void processConfiguration()
        throws Exception
    {
        final String
            startStr = getConfiguredProperty(STARTDATE_PROPERTY),
            numEventsStr = getConfiguredProperty(NUMEVENTS_PROPERTY),
            minIntervalStr = getConfiguredProperty(MININTERVAL_PROPERTY);

        try
        {
            numEvents = Integer.parseInt(numEventsStr);
            minuteInterval = Integer.parseInt(minIntervalStr);
        }
        catch (NumberFormatException nfe)
        {
            LOG.error("configuration parameter is not an integer", nfe);
            throw nfe;
        }

        if (numEvents < 1)
        {
            LOG.error ("number of events cannot be less than 1");
            throw new Exception ("number of events cannot be less than 1");
        }
        if (minuteInterval < 1)
        {
            LOG.error ("minute interval cannot be less than 1");
            throw new Exception ("minute interval cannot be less than 1");
        }

        final SimpleDateFormat
            df = new SimpleDateFormat ("yyyy-MM-dd");

        try
        {
            startDate = df.parse(startStr);
        }
        catch (ParseException e)
        {
            LOG.error ("error parsing start date", e);
            throw e;
        }
    }

    @Override
    public void runJob()
        throws JobExecutionException
    {

        try
        {
            processConfiguration();
        }
        catch (Exception e)
        {
            LOG.error ("configuration error - aborting job", e);
            throw new JobExecutionException (e);
        }

        final TriggerEventManager
            tem = getTriggerEventManager();

        if (tem == null)
        {
            LOG.error ("configuration error - no TriggerEventManager provided");
            throw new JobExecutionException ("configuration error - no TriggerEventManager provided");
        }

        final int
            num = getNumEvents(),
            interval = getMinuteInterval();

        final Calendar
            cal = Calendar.getInstance();

        final TriggerEvent.TRIGGER_EVENT_TYPE
            evtTypes[] = TriggerEvent.TRIGGER_EVENT_TYPE.values();

        JobKey jobKey = getJobExecutionContext().getJobDetail().getKey();
        TriggerKey triggerKey = getJobExecutionContext().getTrigger().getKey();

        cal.setTime(getStartDate());
        cal.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < num; i++)
        {
            Date
                evtTime = new Date (cal.getTimeInMillis());

            cal.add(Calendar.MINUTE, interval);

            tem.createTriggerEvent(evtTypes[i % evtTypes.length], jobKey, triggerKey, evtTime, "Bogus data created by " + this.getClass().getName(), "bogusServer");
        }
    }
}
