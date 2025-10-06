package org.sakaiproject.poll.impl.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.poll.api.repository.PollRepository;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

public class PollRepositoryImpl extends SpringCrudRepositoryImpl<Poll, Long> implements PollRepository {

    private Session session() { return sessionFactory.getCurrentSession(); }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> findAllOrderByCreationDateAsc() {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Poll> cq = cb.createQuery(Poll.class);
        Root<Poll> root = cq.from(Poll.class);
        cq.select(root).orderBy(cb.asc(root.get("creationDate")));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> findBySiteIdOrderByCreationDate(String siteId, boolean asc) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Poll> cq = cb.createQuery(Poll.class);
        Root<Poll> root = cq.from(Poll.class);
        cq.where(cb.equal(root.get("siteId"), siteId));
        cq.select(root).orderBy(asc ? cb.asc(root.get("creationDate")) : cb.desc(root.get("creationDate")));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> findBySiteIdsOrderByCreationDate(List<String> siteIds, boolean asc) {
        if (siteIds == null || siteIds.isEmpty()) return List.of();
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Poll> cq = cb.createQuery(Poll.class);
        Root<Poll> root = cq.from(Poll.class);
        cq.where(root.get("siteId").in(siteIds));
        cq.select(root).orderBy(asc ? cb.asc(root.get("creationDate")) : cb.desc(root.get("creationDate")));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> findOpenPollsForSites(List<String> siteIds, Date now, boolean asc) {
        if (siteIds == null || siteIds.isEmpty()) return List.of();
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Poll> cq = cb.createQuery(Poll.class);
        Root<Poll> root = cq.from(Poll.class);
        Predicate inSites = root.get("siteId").in(siteIds);
        Predicate openOk = cb.or(cb.isNull(root.get("voteOpen")), cb.lessThanOrEqualTo(root.get("voteOpen"), now));
        Predicate closeOk = cb.or(cb.isNull(root.get("voteClose")), cb.greaterThanOrEqualTo(root.get("voteClose"), now));
        cq.where(cb.and(inSites, openOk, closeOk));
        cq.select(root).orderBy(asc ? cb.asc(root.get("creationDate")) : cb.desc(root.get("creationDate")));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Poll> findByPollId(Long pollId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Poll> cq = cb.createQuery(Poll.class);
        Root<Poll> root = cq.from(Poll.class);
        cq.where(cb.equal(root.get("pollId"), pollId));
        List<Poll> rs = session().createQuery(cq).getResultList();
        return rs.isEmpty() ? Optional.empty() : Optional.of(rs.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Poll> findByUuid(String uuid) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Poll> cq = cb.createQuery(Poll.class);
        Root<Poll> root = cq.from(Poll.class);
        cq.where(cb.equal(root.get("uuid"), uuid));
        List<Poll> rs = session().createQuery(cq).getResultList();
        return rs.isEmpty() ? Optional.empty() : Optional.of(rs.get(0));
    }
}
