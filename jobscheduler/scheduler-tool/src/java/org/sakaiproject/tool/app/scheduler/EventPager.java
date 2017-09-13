/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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

import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;

import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 27, 2010
 * Time: 12:07:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventPager
{
    private static String[]
        EVENT_TYPE_STRINGS = {"FIRED", "COMPLETE", "INFO", "DEBUG", "ERROR"};
    private TriggerEventManager
        evtManager = null;
    private Date
        after = null,
        before = null;
    private List<String>
        jobs = new LinkedList<String>();
    private String
        triggerName = null;
    private int
        first = 0,
        numRows = 100;
    private boolean
        filterEnabled = false;
    private HashMap<String, Boolean>
        selectedEventTypes = new HashMap<String, Boolean>();

    public EventPager()
    {
        setAllEventTypes(true);
    }

    public Map<String, Boolean> getSelectedEventTypes()
    {
        return selectedEventTypes;
    }

    public List<String> getJobs()
    {
        return jobs;
    }

    public void setJobs(List<String> jobs)
    {
        this.jobs.clear();
        this.jobs.addAll(jobs);
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    public void setTriggerName(String triggerName)
    {
        this.triggerName = triggerName;
    }

    public void setFilterEnabled (boolean filter)
    {
        filterEnabled = filter;

        if (!filterEnabled)
        {
            after = null;
            before = null;
            jobs.clear();
            triggerName = null;
            setAllEventTypes(true);
        }

        first = 0;
    }

    public boolean isFilterEnabled()
    {
        return filterEnabled;
    }

    public String[] getEventTypes()
    {
        return EVENT_TYPE_STRINGS;
    }

    public TriggerEvent.TRIGGER_EVENT_TYPE[] getTypes()
    {
        LinkedList<TriggerEvent.TRIGGER_EVENT_TYPE>
            evtList = new LinkedList<TriggerEvent.TRIGGER_EVENT_TYPE>();

        for (String type : selectedEventTypes.keySet())
        {
            Boolean
                selected = selectedEventTypes.get(type);

            if (selected != null && selected.booleanValue())
            {
                if ("FIRED".equals(type))
                    evtList.add(TriggerEvent.TRIGGER_EVENT_TYPE.FIRED);
                else if ("COMPLETE".equals(type))
                    evtList.add(TriggerEvent.TRIGGER_EVENT_TYPE.COMPLETE);
                else if ("INFO".equals(type))
                    evtList.add(TriggerEvent.TRIGGER_EVENT_TYPE.INFO);
                else if ("ERROR".equals(type))
                    evtList.add(TriggerEvent.TRIGGER_EVENT_TYPE.ERROR);
                else if ("DEBUG".equals(type))
                    evtList.add(TriggerEvent.TRIGGER_EVENT_TYPE.DEBUG);
            }
        }

        TriggerEvent.TRIGGER_EVENT_TYPE[]
            typeArr = new TriggerEvent.TRIGGER_EVENT_TYPE[evtList.size()];

        evtList.toArray(typeArr);

        return typeArr;
    }

    private void setAllEventTypes(boolean b)
    {
        selectedEventTypes.put("FIRED", new Boolean(b));
        selectedEventTypes.put("COMPLETE", new Boolean(b));
        selectedEventTypes.put("INFO", new Boolean(b));
        selectedEventTypes.put("DEBUG", new Boolean(b));
        selectedEventTypes.put("ERROR", new Boolean(b));        
    }

    public void setTypes(TriggerEvent.TRIGGER_EVENT_TYPE[] types)
    {
        setAllEventTypes(false);

        for (TriggerEvent.TRIGGER_EVENT_TYPE type : types)
        {
            switch (type)
            {
                case FIRED:
                    selectedEventTypes.put("FIRED", Boolean.TRUE);
                    break;
                case COMPLETE:
                    selectedEventTypes.put("COMPLETE", Boolean.TRUE);
                    break;
                case INFO:
                    selectedEventTypes.put("INFO", Boolean.TRUE);
                    break;
                case ERROR:
                    selectedEventTypes.put("ERROR", Boolean.TRUE);
                    break;
                case DEBUG:
                    selectedEventTypes.put("DEBUG", Boolean.TRUE);
                    break;
            }
        }
    }

    public Date getAfter()
    {
        return after;
    }

    public void setAfter(Date after)
    {
        this.after = after;
    }

    public Date getBefore()
    {
        return before;
    }

    public void setBefore(Date before)
    {
        this.before = before;
    }

    public void setTriggerEventManager(TriggerEventManager mgr)
    {
        evtManager = mgr;
    }

    public TriggerEventManager getTriggerEventManager()
    {
        return evtManager;
    }

    public int getTotalItems()
    {
        if (isFilterEnabled())
        {
            return getTriggerEventManager().getTriggerEventsSize(after, before, jobs, triggerName, getTypes());
        }
        else
        {
            return getTriggerEventManager().getTriggerEventsSize();
        }
    }

    public int getFirstItem()
    {
        return first;
    }

    public void setFirstItem(int f)
    {
        first = f;
    }

    public int getPageSize()
    {
        return numRows;
    }

    public void setPageSize(int p)
    {
        numRows = p;
    }

    public void handleValueChange (ValueChangeEvent event)
    {
        PhaseId
            phaseId = event.getPhaseId();

        String
            oldValue = (String) event.getOldValue(),
            newValue = (String) event.getNewValue();

        if (phaseId.equals(PhaseId.ANY_PHASE))
        {
            event.setPhaseId(PhaseId.UPDATE_MODEL_VALUES);
            event.queue();
        }
        else if (phaseId.equals(PhaseId.UPDATE_MODEL_VALUES))
        {
        // do you method here
        }
    }

    public List<TriggerEvent> getEvents()
    {
        if (isFilterEnabled())
        {
            return getTriggerEventManager().getTriggerEvents(after, before, jobs, triggerName, getTypes(), first, numRows);
        }
        else
        {
            return getTriggerEventManager().getTriggerEvents(first, numRows);
        }
    }

}
