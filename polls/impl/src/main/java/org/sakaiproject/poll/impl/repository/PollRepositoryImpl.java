package org.sakaiproject.poll.impl.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.repository.PollRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PollRepositoryImpl extends SpringCrudRepositoryImpl<Poll, String> implements PollRepository {

    @Override
    public List<Poll> findBySiteIdOrderByCreationDateDesc(String siteId) {
        if (siteId == null) return Collections.emptyList();

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
    public List<Poll> findOpenPollsBySiteIds(List<String> siteIds, Instant now) {
        if (siteIds == null || siteIds.isEmpty() || now == null) return Collections.emptyList();

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
    public List<Option> findOptionsByPollId(String pollId) {
        return findById(pollId).map(Poll::getOptions).orElse(Collections.emptyList());
    }

    @Override
    public Optional<Option> findOptionByOptionId(Long optionId) {
        if (optionId == null) return Optional.empty();
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Option.class, optionId));
    }
}
