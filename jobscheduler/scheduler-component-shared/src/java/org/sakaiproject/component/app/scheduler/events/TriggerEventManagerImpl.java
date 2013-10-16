package org.sakaiproject.component.app.scheduler.events;

import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 4:04:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class TriggerEventManagerImpl implements TriggerEventManager
{
    private LinkedList<TriggerEvent>
        events = new LinkedList<TriggerEvent> ();

    /* (non-Javadoc)
     * @see org.sakaiproject.api.app.scheduler.events.TriggerEventManager#createTriggerEvent(org.sakaiproject.api.app.scheduler.events.TriggerEvent.TRIGGER_EVENT_TYPE, java.lang.String, java.lang.String, java.util.Date, java.lang.String)
     */
    public TriggerEvent createTriggerEvent(TriggerEvent.TRIGGER_EVENT_TYPE type, String jobName, String triggerName, Date time, String message) {
        return createTriggerEvent(type, jobName, triggerName, time, message, null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.api.app.scheduler.events.TriggerEventManager#createTriggerEvent(org.sakaiproject.api.app.scheduler.events.TriggerEvent.TRIGGER_EVENT_TYPE, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.lang.String)
     */
    public TriggerEvent createTriggerEvent(TriggerEvent.TRIGGER_EVENT_TYPE type, String jobName, String triggerName, Date time, String message, String serverId)
    {
        TriggerEventImpl event = new TriggerEventImpl();

        event.setEventType(type);
        event.setJobName(jobName);
        event.setTriggerName(triggerName);
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
