package org.sakaiproject.poll.repository.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.repository.VoteRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class VoteRepositoryImpl extends SpringCrudRepositoryImpl<Vote, Long> implements VoteRepository {

    @Override
    @SuppressWarnings("unchecked")
    public List<Vote> findByPollId(Long pollId) {
        if (pollId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("pollId", pollId));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Vote> findByPollIdAndPollOption(Long pollId, Long pollOption) {
        if (pollId == null || pollOption == null) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("pollId", pollId));
        criteria.add(Restrictions.eq("pollOption", pollOption));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Vote> findByUserId(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("userId", userId));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Vote> findByUserIdAndPollIds(String userId, List<Long> pollIds) {
        if (userId == null || pollIds == null || pollIds.isEmpty()) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("userId", userId));
        criteria.add(Restrictions.in("pollId", pollIds));
        return criteria.list();
    }

    @Override
    public boolean existsByPollIdAndUserId(Long pollId, String userId) {
        if (pollId == null || userId == null) {
            return false;
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("pollId", pollId));
        criteria.add(Restrictions.eq("userId", userId));
        criteria.setProjection(Projections.rowCount());
        Number count = (Number) criteria.uniqueResult();
        return count != null && count.longValue() > 0;
    }

    @Override
    public int countDistinctSubmissionIds(Long pollId) {
        if (pollId == null) {
            return 0;
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("pollId", pollId));
        criteria.setProjection(Projections.countDistinct("submissionId"));
        Number count = (Number) criteria.uniqueResult();
        return count == null ? 0 : count.intValue();
    }
}
