package org.sakaiproject.poll.repository.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.repository.PollRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class PollRepositoryImpl extends SpringCrudRepositoryImpl<Poll, Long> implements PollRepository {

    @Override
    @SuppressWarnings("unchecked")
    public List<Poll> findBySiteIdOrderByCreationDateDesc(String siteId) {
        if (siteId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("siteId", siteId));
        criteria.addOrder(Order.desc("creationDate"));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Poll> findBySiteIdsOrderByCreationDate(List<String> siteIds) {
        if (siteIds == null || siteIds.isEmpty()) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.in("siteId", siteIds));
        criteria.addOrder(Order.asc("creationDate"));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Poll> findOpenPollsBySiteIds(List<String> siteIds, Date now) {
        if (siteIds == null || siteIds.isEmpty() || now == null) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.in("siteId", siteIds));
        criteria.add(Restrictions.lt("voteOpen", now));
        criteria.add(Restrictions.gt("voteClose", now));
        criteria.addOrder(Order.asc("creationDate"));
        return criteria.list();
    }

    @Override
    public Optional<Poll> findByUuid(String uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("uuid", uuid));
        return Optional.ofNullable((Poll) criteria.uniqueResult());
    }
}
