package org.sakaiproject.component.app.scheduler.events.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.scheduler.events.hibernate.TriggerEventHibernateImpl;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 5:24:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TriggerEventManagerHibernateImpl
    extends HibernateDaoSupport
    implements TriggerEventManager
{
    public TriggerEvent createTriggerEvent(TriggerEvent.TRIGGER_EVENT_TYPE type, String jobName, String triggerName,
                                           Date time, String message)
    {
        TriggerEventHibernateImpl
            event = new TriggerEventHibernateImpl();

        event.setEventType(type);
        event.setJobName(jobName);
        event.setTriggerName(triggerName);
        event.setTime(time);
        event.setMessage(message);

        getHibernateTemplate().save(event);

        return event;
    }

    public List<TriggerEvent> getTriggerEvents()
    {
        return getHibernateTemplate().loadAll(TriggerEventHibernateImpl.class);
    }

    public List<TriggerEvent> getTriggerEvents(Date after, Date before, String jobName, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types)
    {
        final Session
            session = this.getSession();
        final Criteria
            criteria = session.createCriteria(TriggerEventHibernateImpl.class);

        if (after != null)
        {
            criteria.add(Restrictions.gt("time", after));
        }
        if (before != null)
        {
            criteria.add(Restrictions.lt("time", before));
        }
        if (jobName != null)
        {
            criteria.add(Restrictions.eq("job", jobName));
        }
        if (triggerName != null)
        {
            criteria.add(Restrictions.eq("trigger", triggerName));
        }
        if (types != null)
        {
            criteria.add(Restrictions.in("type", types));
        }

        return criteria.list();
    }

    public void purgeEvents(Date before)
    {
    }
}
