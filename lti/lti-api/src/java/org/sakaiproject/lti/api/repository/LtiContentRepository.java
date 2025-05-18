package org.sakaiproject.lti.api.repository;

import org.sakaiproject.lti.impl.model.LtiContent;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface LtiContentRepository extends SpringCrudRepository<LtiContent, Long> {
}
