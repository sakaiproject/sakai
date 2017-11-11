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

import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;
import org.sakaiproject.component.app.scheduler.jobs.AbstractConfigurableJob;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 27, 2010
 * Time: 2:30:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventLogPurgeJob extends AbstractConfigurableJob
{
    public static String
        NUMBER_DAYS     = "number.days";

    private TriggerEventManager
        manager = null;

    @Override
    public void runJob() throws JobExecutionException
    {
        final String
            noDays = getConfiguredProperty (NUMBER_DAYS);

        if (noDays == null || noDays.trim().length() == 0)
        {
            throw new JobExecutionException("job improperly configured - number of days not set for purge cutoff");
        }

        final TriggerEventManager
            eMgr = getTriggerEventManager();

        if (eMgr == null)
            throw new JobExecutionException ("job is not configured with a TriggerEventManager, aborting");

        final Calendar
            cal = Calendar.getInstance();

        int
            numDays = 0;

        try
        {
            numDays = Integer.parseInt(noDays);
        }
        catch (NumberFormatException nfe)
        {
            throw new JobExecutionException ("job improperly configured - number of days for cutoff must be an integer greater than 1");
        }

        if (numDays < 1)
        {
            throw new JobExecutionException ("job improperly configured - number of days must be 1 or more");
        }

        cal.add(Calendar.DAY_OF_YEAR, -numDays);

        Date
            cutoffDate = new Date(cal.getTimeInMillis());

        eMgr.purgeEvents(cutoffDate);
    }

    public void setTriggerEventManager (TriggerEventManager tMgr)
    {
        manager = tMgr;
    }

    public TriggerEventManager getTriggerEventManager()
    {
        return manager;
    }
}
