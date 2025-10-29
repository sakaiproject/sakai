package org.sakaiproject.poll.repository.impl;

import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.repository.VoteRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class VoteRepositoryImpl extends SpringCrudRepositoryImpl<Vote, Long> implements VoteRepository {

    @Override
    public List<Vote> findByPollId(Long pollId) {
        if (pollId == null) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Poll> pollJoin = root.join("poll");

        query.select(root)
                .where(cb.equal(pollJoin.get("pollId"), pollId));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Vote> findByPollIdAndPollOption(Long pollId, Long optionId) {
        if (pollId == null || optionId == null) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Poll> pollJoin = root.join("poll");
        Join<Vote, Option> optionJoin = root.join("option");

        Predicate pollPredicate = cb.equal(pollJoin.get("pollId"), pollId);
        Predicate optionPredicate = cb.equal(optionJoin.get("optionId"), optionId);

        query.select(root)
                .where(cb.and(pollPredicate, optionPredicate));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Vote> findByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);

        query.select(root)
                .where(cb.equal(root.get("userId"), userId));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Vote> findByUserIdAndPollIds(String userId, List<Long> pollIds) {
        if (userId == null || pollIds == null || pollIds.isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Poll> pollJoin = root.join("poll");

        Predicate userPredicate = cb.equal(root.get("userId"), userId);
        Predicate pollsPredicate = pollJoin.get("pollId").in(pollIds);

        query.select(root)
                .where(cb.and(userPredicate, pollsPredicate));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public boolean existsByPollIdAndUserId(Long pollId, String userId) {
        if (pollId == null || userId == null || userId.trim().isEmpty()) {
            return false;
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Poll> pollJoin = root.join("poll");

        Predicate pollPredicate = cb.equal(pollJoin.get("pollId"), pollId);
        Predicate userPredicate = cb.equal(root.get("userId"), userId);

        query.select(cb.count(root))
                .where(cb.and(pollPredicate, userPredicate));

        Long count = sessionFactory.getCurrentSession()
                .createQuery(query)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public int countDistinctSubmissionIds(Long pollId) {
        if (pollId == null) {
            return 0;
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Poll> pollJoin = root.join("poll");

        query.select(cb.countDistinct(root.get("submissionId")))
                .where(cb.equal(pollJoin.get("pollId"), pollId));

        Long count = sessionFactory.getCurrentSession()
                .createQuery(query)
                .getSingleResult();

        return Math.toIntExact(count);
    }
}
