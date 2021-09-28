package org.sakaiproject.conversations.impl.repository;

import java.util.Optional;

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.conversations.api.repository.ConvStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ConvStatusRepositoryImpl extends SpringCrudRepositoryImpl<ConvStatus, Long>  implements ConvStatusRepository {

    @Transactional
    public Optional<ConvStatus> findBySiteIdAndUserId(String siteId, String userId) {

        return Optional.ofNullable((ConvStatus) sessionFactory.getCurrentSession().createCriteria(ConvStatus.class)
            .add(Restrictions.eq("siteId", siteId))
            .add(Restrictions.eq("userId", userId))
            .uniqueResult());
    }
}
