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
package org.sakaiproject.component.app.scheduler.events;

import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent.TRIGGER_EVENT_TYPE;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple implementation that just stores the events in memory, should never be used in a production service.
 */
public class TriggerEventManagerImpl implements TriggerEventManager
{
    private LinkedList<TriggerEvent>
        events = new LinkedList<TriggerEvent> ();
    
    @Override
    public TriggerEvent createTriggerEvent(TRIGGER_EVENT_TYPE type, JobKey jobKey, TriggerKey triggerKey, Date time, String message) {
        return createTriggerEvent(type, jobKey, triggerKey, time, message, null);
    }

    @Override
    public TriggerEvent createTriggerEvent(TRIGGER_EVENT_TYPE type, JobKey jobKey, TriggerKey triggerKey, Date time, String message, String serverId) {
        TriggerEventImpl event = new TriggerEventImpl();

        event.setEventType(type);
        event.setJobName(jobKey.getName());
        event.setTriggerName(triggerKey.getName());
        event.setTime(time);
        event.setMessage(message);
        event.setServerId(serverId);
        events.add(0, event);

        return event;
    }

    public List<TriggerEvent> getTriggerEvents()
    {
        return Collections.unmodifiableList(events);
    }

    public List<TriggerEvent> getTriggerEvents(Date after, Date before, List<String> jobs, String triggerName,
                                               TriggerEvent.TRIGGER_EVENT_TYPE[] types)
    {
        LinkedList<TriggerEvent>
            results = new LinkedList<TriggerEvent> ();

        for (TriggerEvent event : events)
        {
            if (after != null && event.getTime().compareTo(after) == -1)
                continue;
            if (before != null && event.getTime().compareTo(before) == 1)
                continue;
            if (jobs != null && !jobs.contains(event.getJobName()))
                continue;
            if (triggerName != null && !triggerName.equals(event.getTriggerName()))
                continue;
            if (types != null && types.length > 0)
            {
                boolean matches = false;

                for (TriggerEvent.TRIGGER_EVENT_TYPE type : types)
                {
                    if (type.equals(event.getEventType()))
                    {
                        matches = true;
                        break;
                    }
                }

                if (!matches)
                    continue;
            }

            results.add(event);
        }

        return Collections.unmodifiableList(results);
    }

    public int getTriggerEventsSize()
    {
        return events.size();
    }
    
    public int getTriggerEventsSize (Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types)
    {
    	return getTriggerEvents(after, before, jobs, triggerName, types).size();
    }

    public List<TriggerEvent> getTriggerEvents (int first, int size)
    {
    	return Collections.unmodifiableList(events.subList(first, first+size));
    }
    
    public List<TriggerEvent> getTriggerEvents (Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types, int first, int size)
    {
    	return Collections.unmodifiableList(getTriggerEvents(after, before, jobs, triggerName, types).subList(first, first+size));
    }
    
    public void purgeEvents(Date before)
    {
        int i = 0;

        for (; i < events.size(); i++)
        {
            TriggerEvent event = events.get(i);

            if (before.compareTo(event.getTime()) == -1)
                break;
        }

        int size = events.size();
        int count = size - i;

        for (int x = 0; x < count; x++)
        {
           events.remove(i); 
        }
    }
}
