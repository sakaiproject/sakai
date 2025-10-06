package org.sakaiproject.poll.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.poll.api.repository.VoteRepository;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

public class VoteRepositoryImpl extends SpringCrudRepositoryImpl<Vote, Long> implements VoteRepository {

    private Session session() { return sessionFactory.getCurrentSession(); }

    @Override
    @Transactional(readOnly = true)
    public List<Vote> findByPollId(Long pollId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Vote> cq = cb.createQuery(Vote.class);
        Root<Vote> root = cq.from(Vote.class);
        cq.where(cb.equal(root.get("pollId"), pollId));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vote> findByPollIdAndOptionId(Long pollId, Long optionId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Vote> cq = cb.createQuery(Vote.class);
        Root<Vote> root = cq.from(Vote.class);
        cq.where(cb.and(cb.equal(root.get("pollId"), pollId), cb.equal(root.get("pollOption"), optionId)));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vote> findByUserIdAndPollIds(String userId, List<Long> pollIds) {
        if (pollIds == null || pollIds.isEmpty()) return List.of();
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Vote> cq = cb.createQuery(Vote.class);
        Root<Vote> root = cq.from(Vote.class);
        cq.where(cb.and(cb.equal(root.get("userId"), userId), root.get("pollId").in(pollIds)));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vote> findByUserIdAndPollId(String userId, Long pollId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Vote> cq = cb.createQuery(Vote.class);
        Root<Vote> root = cq.from(Vote.class);
        cq.where(cb.and(cb.equal(root.get("userId"), userId), cb.equal(root.get("pollId"), pollId)));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vote> findByVoteId(Long voteId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Vote> cq = cb.createQuery(Vote.class);
        Root<Vote> root = cq.from(Vote.class);
        cq.where(cb.equal(root.get("id"), voteId));
        List<Vote> rs = session().createQuery(cq).getResultList();
        return rs.isEmpty() ? Optional.empty() : Optional.of(rs.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public int countDistinctSubmissionIdByPollId(Long pollId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Vote> root = cq.from(Vote.class);
        cq.select(cb.countDistinct(root.get("submissionId")))
          .where(cb.equal(root.get("pollId"), pollId));
        Long n = session().createQuery(cq).getSingleResult();
        return n == null ? 0 : n.intValue();
    }
}
