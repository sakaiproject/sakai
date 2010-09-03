package org.sakaiproject.scheduler.events.hibernate;

import org.sakaiproject.api.app.scheduler.events.TriggerEvent;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 5:04:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class TriggerEventHibernateImpl
   implements TriggerEvent
{
    private TRIGGER_EVENT_TYPE
        type;
    private String
        id,
        jobName,
        triggerName;
    private Date
        time;
    private String
        message;

    public TriggerEventHibernateImpl()
    {}

    public void setId(String i)
    {
        id = i;
    }

    public String getId()
    {
        return id;
    }
    
    public void setEventType (TRIGGER_EVENT_TYPE t)
    {
        type = t;
    }

    public TRIGGER_EVENT_TYPE getEventType()
    {
        return type;
    }

    public void setJobName(String name)
    {
        jobName = name;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setTriggerName(String name)
    {
        triggerName = name;
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    public void setTime(Date t)
    {
        time = t;
    }

    public Date getTime()
    {
        return time;
    }

    public void setMessage(String m)
    {
        message = m;
    }

    public String getMessage()
    {
        return message;
    }

    public int hashCode ()
    {
        return id.hashCode();
    }

    public boolean equals (Object o)
    {
        if (!TriggerEventHibernateImpl.class.isAssignableFrom (o.getClass()))
            return false;

        final TriggerEventHibernateImpl
            that = (TriggerEventHibernateImpl)o;

        if (this == that)
            return true;

        if (that == null)
            return false;

        return (id.equals(that.id));
    }
}
