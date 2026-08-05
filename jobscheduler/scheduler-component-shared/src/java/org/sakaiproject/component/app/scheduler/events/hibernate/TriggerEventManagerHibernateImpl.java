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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent.TRIGGER_EVENT_TYPE;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;
import org.sakaiproject.scheduler.events.hibernate.TriggerEventHibernateImpl;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 5:24:51 PM
 * To change this template use File | Settings | File Templates.
 */
@Transactional
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
        final Session session = getSessionFactory().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TriggerEventHibernateImpl> root = cq.from(TriggerEventHibernateImpl.class);
        cq.select(cb.count(root));
        List<Predicate> predicates = buildPredicates(cb, root, after, before, jobs, triggerName, types);
        if (!predicates.isEmpty())
            cq.where(predicates.toArray(new Predicate[0]));
        return session.createQuery(cq).uniqueResult().intValue();
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
    	 final Session session = getSessionFactory().getCurrentSession();
         CriteriaBuilder cb = session.getCriteriaBuilder();
         CriteriaQuery<TriggerEventHibernateImpl> cq = cb.createQuery(TriggerEventHibernateImpl.class);
         Root<TriggerEventHibernateImpl> root = cq.from(TriggerEventHibernateImpl.class);
         List<Predicate> predicates = buildPredicates(cb, root, after, before, jobs, triggerName, types);
         if (!predicates.isEmpty())
             cq.where(predicates.toArray(new Predicate[0]));
         cq.orderBy(cb.desc(root.get("time")), cb.asc(root.get("eventType")));

         var query = session.createQuery(cq);
         if (first != null && size != null) {
             query.setFirstResult(first).setMaxResults(size);
         }
         return (List<TriggerEvent>)(List<?>) query.list();
    }

    /**
	 * Build the criteria for searching for events.
	 */
    protected List<Predicate> buildPredicates(CriteriaBuilder cb, Root<TriggerEventHibernateImpl> root,
            Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types) {
        List<Predicate> predicates = new ArrayList<>();
        if (after != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("time"), after));
        if (before != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("time"), before));
        if (jobs != null && !jobs.isEmpty())
            predicates.add(root.get("jobName").in(jobs));
        if (triggerName != null)
            predicates.add(cb.equal(root.get("triggerName"), triggerName));
        if (types != null)
            predicates.add(root.get("eventType").in((Object[]) types));
        return predicates;
    }

    @Transactional
    public void purgeEvents(Date before)
    {
        getSessionFactory().getCurrentSession().getNamedQuery("purgeEventsBefore")
                .setParameter("before", before)
                .executeUpdate();
    }
}
