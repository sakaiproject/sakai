package org.sakaiproject.lti.impl.repository;

import org.sakaiproject.lti.api.repository.LtiMembershipJobRepository;
import org.sakaiproject.lti.api.model.LtiMembershipJob;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiMembershipJobRepositoryImpl extends SpringCrudRepositoryImpl<LtiMembershipJob, String> implements LtiMembershipJobRepository {
}
