package org.sakaiproject.lti.impl.repository;

import org.sakaiproject.lti.api.repository.LtiContentRepository;
import org.sakaiproject.lti.impl.model.LtiContent;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiContentRepositoryImpl extends SpringCrudRepositoryImpl<LtiContent, Long> implements LtiContentRepository {
}
