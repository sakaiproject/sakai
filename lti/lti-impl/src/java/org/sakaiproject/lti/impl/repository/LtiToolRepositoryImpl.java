package org.sakaiproject.lti.impl.repository;

import org.sakaiproject.lti.api.repository.LtiToolRepository;
import org.sakaiproject.lti.impl.model.LtiTool;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiToolRepositoryImpl extends SpringCrudRepositoryImpl<LtiTool, Long> implements LtiToolRepository {
}
