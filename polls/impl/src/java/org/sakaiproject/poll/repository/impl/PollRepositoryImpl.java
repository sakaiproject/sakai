package org.sakaiproject.poll.repository.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.repository.PollRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class PollRepositoryImpl extends SpringCrudRepositoryImpl<Poll, Long> implements PollRepository {

    @Override
    public List<Poll> findBySiteIdOrderByCreationDateDesc(String siteId) {
        if (siteId == null) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Poll> query = cb.createQuery(Poll.class);
        Root<Poll> root = query.from(Poll.class);

        query.select(root)
                .where(cb.equal(root.get("siteId"), siteId))
                .orderBy(cb.desc(root.get("creationDate")));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Poll> findBySiteIdsOrderByCreationDate(List<String> siteIds) {
        if (siteIds == null || siteIds.isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Poll> query = cb.createQuery(Poll.class);
        Root<Poll> root = query.from(Poll.class);

        query.select(root)
                .where(root.get("siteId").in(siteIds))
                .orderBy(cb.asc(root.get("creationDate")));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Poll> findOpenPollsBySiteIds(List<String> siteIds, Date now) {
        if (siteIds == null || siteIds.isEmpty() || now == null) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Poll> query = cb.createQuery(Poll.class);
        Root<Poll> root = query.from(Poll.class);

        Predicate sitePredicate = root.get("siteId").in(siteIds);
        Predicate openPredicate = cb.lessThanOrEqualTo(root.get("voteOpen"), now);
        Predicate closePredicate = cb.greaterThanOrEqualTo(root.get("voteClose"), now);

        query.select(root)
                .where(cb.and(sitePredicate, openPredicate, closePredicate))
                .orderBy(cb.asc(root.get("creationDate")));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public Optional<Poll> findByUuid(String uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Poll> query = cb.createQuery(Poll.class);
        Root<Poll> root = query.from(Poll.class);

        query.select(root)
                .where(cb.equal(root.get("uuid"), uuid));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResultOptional();
    }
}
