package org.sakaiproject.conversations.api.repository;

import java.util.Optional;

import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface SettingsRepository extends SpringCrudRepository<Settings, Long> {

    Optional<Settings> findBySiteId(String siteId);
}
