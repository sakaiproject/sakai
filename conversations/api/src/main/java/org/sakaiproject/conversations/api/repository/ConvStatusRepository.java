package org.sakaiproject.conversations.api.repository;

import java.util.Optional;

import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface ConvStatusRepository extends SpringCrudRepository<ConvStatus, Long> {

    Optional<ConvStatus> findBySiteIdAndUserId(String siteId, String userId);
}
