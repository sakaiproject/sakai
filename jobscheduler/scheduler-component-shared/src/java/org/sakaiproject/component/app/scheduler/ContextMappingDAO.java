package org.sakaiproject.component.app.scheduler;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.scheduler.events.hibernate.ContextMapping;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * This is used to allow faster lookups from a UUID to a context ID.
 * This is because the quartz API doesn't allow for lookups based on data that's stored in the job map.
 * This is only needed when there are a large number of scheduled jobs.
 */
@Transactional
public class ContextMappingDAO {

    private SessionFactory sessionFactory;

    @Inject
    @Named("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public String get(String componentId, String contextId) {
        ContextMapping o = getContextMapping(componentId, contextId);
        if (o != null) {
            return o.getUuid();
        }
        return null;
    }

    public Collection<String> find(String componentId, String contextId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ContextMapping.class);
        if (contextId != null) {
                criteria.add(Restrictions.eq("contextId", contextId));
        }
        if (componentId != null) {
            criteria.add(Restrictions.eq("componentId", componentId));
        }
        // We only want the IDs
        criteria.setProjection(Projections.property("id"));
        return criteria.list();

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

    public void remove(String uuid) {
        Session session = sessionFactory.getCurrentSession();
        ContextMapping o = (ContextMapping) session.get(ContextMapping.class, uuid);
        if (o != null) {
            session.delete(o);
        }
    }

    private ContextMapping getContextMapping(String componentId, String contextId) {
        return (ContextMapping) sessionFactory.getCurrentSession().createCriteria(ContextMapping.class)
                .add(Restrictions.eq("contextId", contextId))
                .add(Restrictions.eq("componentId", componentId))
                .uniqueResult();
    }
}
