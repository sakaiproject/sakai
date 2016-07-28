package org.sakaiproject.component.app.scheduler;

import org.hibernate.SessionFactory;
import org.sakaiproject.scheduler.events.hibernate.ContextMapping;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is used to allow faster lookups from a UUID to a context ID.
 */
public class ContextMapingDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String find(String uuid) {
        ContextMapping o = (ContextMapping) sessionFactory.getCurrentSession().get(ContextMapping.class, uuid);
        if (o != null) {
            return o.getContextId();
        }
        return null;
    }

    public void add(String uuid, String contextId) {
        ContextMapping contextMapping = new ContextMapping();
        contextMapping.setUuid(uuid);
        contextMapping.setContextId(contextId);
        sessionFactory.getCurrentSession().save(contextMapping);
    }

}
