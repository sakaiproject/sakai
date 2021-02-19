package org.sakaiproject.conversations.impl.repository;

import java.util.Optional;

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.repository.SettingsRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class SettingsRepositoryImpl extends SpringCrudRepositoryImpl<Settings, Long>  implements SettingsRepository {

    @Transactional
    public Optional<Settings> findBySiteId(String siteId) {

        return Optional.ofNullable((Settings) sessionFactory.getCurrentSession().createCriteria(Settings.class)
            .add(Restrictions.eq("siteId", siteId))
            .uniqueResult());
    }
}
