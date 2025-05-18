package org.sakaiproject.user.api.repository;

import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.user.api.model.Preference;

public interface PreferenceRepository extends SpringCrudRepository<Preference, String> {
}
