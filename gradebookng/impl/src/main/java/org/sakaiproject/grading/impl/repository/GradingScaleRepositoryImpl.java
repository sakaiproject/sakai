package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.GradingScale;
import org.sakaiproject.grading.api.repository.GradingScaleRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradingScaleRepositoryImpl extends SpringCrudRepositoryImpl<GradingScale, Long>  implements GradingScaleRepository {

    @Transactional(readOnly = true)
    public List<GradingScale> findByUnavailable(Boolean unavailable) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingScale> query = cb.createQuery(GradingScale.class);
        Root<GradingScale> gs = query.from(GradingScale.class);
        query.where(cb.equal(gs.get("unavailable"), unavailable));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradingScale> findByUnavailableAndUidNotIn(Boolean unavailable, Set<String> notTheseUids) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingScale> query = cb.createQuery(GradingScale.class);
        Root<GradingScale> gs = query.from(GradingScale.class);
        query.where(cb.and(cb.equal(gs.get("unavailable"), unavailable),
                            cb.not(gs.get("uid").in(notTheseUids))));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradingScale> findByUidIn(Set<String> theseUids) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingScale> query = cb.createQuery(GradingScale.class);
        Root<GradingScale> gs = query.from(GradingScale.class);
        query.where(gs.get("uid").in(theseUids));
        return session.createQuery(query).list();
    }
}
