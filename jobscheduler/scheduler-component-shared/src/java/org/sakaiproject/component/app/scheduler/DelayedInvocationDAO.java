package org.sakaiproject.component.app.scheduler;

import org.hibernate.SessionFactory;
import org.sakaiproject.scheduler.events.hibernate.DelayedInvocation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Just used for migration to the quartz scheduler.
 */
@Transactional
public class DelayedInvocationDAO {

    private SessionFactory sessionFactory;

    @Inject
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<DelayedInvocation> all() {
        return sessionFactory.getCurrentSession().createCriteria(DelayedInvocation.class).list();
    }

    public void remove(DelayedInvocation invocation) {
        sessionFactory.getCurrentSession().delete(invocation);
    }
}
