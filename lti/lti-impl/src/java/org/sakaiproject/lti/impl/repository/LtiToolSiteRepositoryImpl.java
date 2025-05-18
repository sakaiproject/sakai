package org.sakaiproject.lti.impl.repository;

import org.sakaiproject.lti.api.repository.LtiToolSiteRepository;
import org.sakaiproject.lti.impl.model.LtiToolSite;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiToolSiteRepositoryImpl extends SpringCrudRepositoryImpl<LtiToolSite, Long> implements LtiToolSiteRepository {
}
