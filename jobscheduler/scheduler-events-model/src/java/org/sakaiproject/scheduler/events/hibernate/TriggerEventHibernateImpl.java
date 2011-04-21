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

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TriggerEventHibernateImpl))
			return false;
		TriggerEventHibernateImpl other = (TriggerEventHibernateImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
