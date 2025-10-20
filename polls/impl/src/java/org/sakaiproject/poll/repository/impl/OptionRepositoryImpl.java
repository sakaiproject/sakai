package org.sakaiproject.poll.repository.impl;

import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.repository.OptionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class OptionRepositoryImpl extends SpringCrudRepositoryImpl<Option, Long> implements OptionRepository {

    @Override
    public List<Option> findByPollIdOrderByOptionOrder(Long pollId) {
        if (pollId == null) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Option> query = cb.createQuery(Option.class);
        Root<Option> root = query.from(Option.class);

        query.select(root)
                .where(cb.equal(root.get("pollId"), pollId))
                .orderBy(cb.asc(root.get("optionOrder")));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }
}
