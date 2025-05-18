package org.sakaiproject.lti.api.repository;

import org.sakaiproject.lti.impl.model.LtiMembershipJob;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface LtiMembershipJobRepository extends SpringCrudRepository<LtiMembershipJob, String> {
}
