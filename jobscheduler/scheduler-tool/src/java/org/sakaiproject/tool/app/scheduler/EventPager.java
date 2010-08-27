package org.sakaiproject.tool.app.scheduler;

import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;

import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 27, 2010
 * Time: 12:07:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventPager
{
    private TriggerEventManager
        evtManager = null;
    private Date
        after = null,
        before = null;
    private String
        jobName = null,
        triggerName = null;
    private TriggerEvent.TRIGGER_EVENT_TYPE
        types[] = null;
    private int
        first = 0,
        numRows = 10;

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    public void setTriggerName(String triggerName)
    {
        this.triggerName = triggerName;
    }

    public TriggerEvent.TRIGGER_EVENT_TYPE[] getTypes()
    {
        return types;
    }

    public void setTypes(TriggerEvent.TRIGGER_EVENT_TYPE[] types)
    {
        this.types = types;
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
        return getEvents().size();
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
        return getTriggerEventManager().getTriggerEvents(after, before, jobName, triggerName, types);
    }

}
