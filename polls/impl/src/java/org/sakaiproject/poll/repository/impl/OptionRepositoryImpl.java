package org.sakaiproject.poll.repository.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.repository.OptionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class OptionRepositoryImpl extends SpringCrudRepositoryImpl<Option, Long> implements OptionRepository {

    @Override
    @SuppressWarnings("unchecked")
    public List<Option> findByPollIdOrderByOptionOrder(Long pollId) {
        if (pollId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("pollId", pollId));
        criteria.addOrder(Order.asc("optionOrder"));
        return criteria.list();
    }
}
