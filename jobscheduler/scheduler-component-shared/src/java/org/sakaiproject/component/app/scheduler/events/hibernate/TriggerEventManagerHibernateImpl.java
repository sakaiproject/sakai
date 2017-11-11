/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.events.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent.TRIGGER_EVENT_TYPE;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;
import org.sakaiproject.scheduler.events.hibernate.TriggerEventHibernateImpl;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 5:24:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TriggerEventManagerHibernateImpl extends HibernateDaoSupport implements TriggerEventManager
{
    @Override
    @Transactional
    public TriggerEvent createTriggerEvent(TRIGGER_EVENT_TYPE type, JobKey jobKey, TriggerKey triggerKey, Date time, String message) {
        return createTriggerEvent(type, jobKey, triggerKey, time, message, null);
    }

    @Override
    @Transactional
    public TriggerEvent createTriggerEvent(TRIGGER_EVENT_TYPE type, JobKey jobKey, TriggerKey triggerKey, Date time, String message, String serverId) {
        TriggerEventHibernateImpl event = new TriggerEventHibernateImpl();

        event.setEventType(type);
        event.setJobName(jobKey.getName());
        event.setTriggerName(triggerKey.getName());
        event.setTime(time);
        event.setMessage(message);
        event.setServerId(serverId);

        try {
            getSessionFactory().getCurrentSession().save(event);
        } catch (HibernateException he) {
            getSessionFactory().getCurrentSession().evict(event);
            throw he;
        }

        return event;
    }

    @Transactional(readOnly = true)
    public List<TriggerEvent> getTriggerEvents() {
        return getTriggerEvents(null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<TriggerEvent> getTriggerEvents(int first, int size) {
        return getTriggerEvents(null, null, null, null, null, first, size);
    }

    @Transactional(readOnly = true)
    public int getTriggerEventsSize() {
        return getTriggerEventsSize(null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public int getTriggerEventsSize(Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types) {
        final Criteria criteria = buildCriteria(after, before, jobs, triggerName, types);
        criteria.setProjection(Projections.rowCount());
        return ((Long) criteria.list().get(0)).intValue();
    }

    @Transactional(readOnly = true)
    public List<TriggerEvent> getTriggerEvents(Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types) {
        return getTriggerEvents(after, before, jobs, triggerName, types, null, null);

    }

    @Transactional(readOnly = true)
    public List<TriggerEvent> getTriggerEvents(Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types, int first, int size) {
        return getTriggerEvents(after, before, jobs, triggerName, types, Integer.valueOf(first),  Integer.valueOf(size));
    }

    /**
     * Internal search for events. Applies the sort and optionally the limit/offset.
     */
    protected List<TriggerEvent> getTriggerEvents(Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types, Integer first, Integer size) {
        final Criteria criteria = buildCriteria(after, before, jobs, triggerName, types);
        // We put the newest items first as generally that's what people are most interested in.
        criteria.addOrder(Order.desc("time"));
        // Sort by event type so that if the time of 2 events is the same the fired event happens before
        // the completed event.
        criteria.addOrder(Order.asc("eventType"));
        if (first != null && size != null)
        {
            criteria.setFirstResult(first).setMaxResults(size);
        }
        return criteria.list();
    }

    /**
     * Creates a criteria with all restrictions applied.
     */
    protected Criteria buildCriteria(Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types) {
        final Session session = getSessionFactory().getCurrentSession();
        final Criteria criteria = session.createCriteria(TriggerEventHibernateImpl.class);
        if (after != null)
        {
            criteria.add(Restrictions.ge("time", after));
        }
        if (before != null)
        {
            criteria.add(Restrictions.le("time", before));
        }
        if (jobs != null && !jobs.isEmpty())
        {
            criteria.add(Restrictions.in("jobName", jobs));
        }
        if (triggerName != null)
        {
            criteria.add(Restrictions.eq("triggerName", triggerName));
        }
        if (types != null)
        {
            criteria.add(Restrictions.in("eventType", types));
        }
        return criteria;
    }

    @Transactional
    public void purgeEvents(Date before)
    {
        getSessionFactory().getCurrentSession().getNamedQuery("purgeEventsBefore")
                .setTimestamp("before", before)
                .executeUpdate();
    }
}
