package org.sakaiproject.lti.api.repository;

import org.sakaiproject.lti.api.model.LtiContent;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface LtiContentRepository extends SpringCrudRepository<LtiContent, Long> {
}
