package org.sakaiproject.widget.impl.persistence;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.hibernate.HibernateCrudRepository;
import org.sakaiproject.widget.api.persistence.WidgetRepository;
import org.sakaiproject.widget.model.Widget;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This is the widget repository (DAO) it contains all the methods for persisting widgets to the database.
 * We primarily are using Hibernate's Criteria api however this is being replaced by JPA's Criteria api.
 */
@Transactional
public class WidgetRepositoryImpl extends HibernateCrudRepository<Widget, String> implements WidgetRepository {

    @Override
    public List<Widget> getWidgetsForSiteWithStatus(String context, Widget.STATUS... statuses) {
        Criteria criteria = startCriteriaQuery().add(Restrictions.eq("context", context));
        if (statuses.length > 0) {
            criteria.add(Restrictions.in("status", statuses));
        } else {
            criteria.add(Restrictions.ne("status", Widget.STATUS.DELETED));
        }
        return criteria.list();
    }

    @Override
    public long countWidgetsForSite(String context) {
        Object count = startCriteriaQuery()
                .setProjection(Projections.rowCount())
                .add(Restrictions.eq("context", context))
                .add(Restrictions.ne("status", Widget.STATUS.DELETED))
                .uniqueResult();
        return ((Number) count).longValue();
    }
}
