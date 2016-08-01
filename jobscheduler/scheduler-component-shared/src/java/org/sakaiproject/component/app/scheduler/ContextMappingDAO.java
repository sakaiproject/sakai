package org.sakaiproject.component.app.scheduler;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.scheduler.events.hibernate.ContextMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is used to allow faster lookups from a UUID to a context ID.
 * This is because the quartz API doesn't allow for lookups based on data that's stored in the job map.
 * This is only needed when there are a large number of scheduled jobs.
 */
@Transactional
public class ContextMappingDAO {

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public String find(String componentId, String contextId) {
        ContextMapping o = getContextMapping(componentId, contextId);
        if (o != null) {
            return o.getUuid();
        }
        return null;
    }

    public void add(String uuid, String componentId, String contextId) {
        ContextMapping contextMapping = new ContextMapping();
        contextMapping.setUuid(uuid);
        contextMapping.setComponentId(componentId);
        contextMapping.setContextId(contextId);
        sessionFactory.getCurrentSession().save(contextMapping);
    }

    public void remove(String componentId, String contextId) {
        ContextMapping o = getContextMapping(componentId, contextId);
        if (o != null) {
            sessionFactory.getCurrentSession().delete(o);
        }
    }

    private ContextMapping getContextMapping(String componentId, String contextId) {
        return (ContextMapping) sessionFactory.getCurrentSession().createCriteria(ContextMapping.class)
                .add(Restrictions.eq("contextId", contextId))
                .add(Restrictions.eq("componentId", componentId))
                .uniqueResult();
    }
}
