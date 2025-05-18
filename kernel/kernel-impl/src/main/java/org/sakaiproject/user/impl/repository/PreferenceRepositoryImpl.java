package org.sakaiproject.user.impl.repository;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.user.api.model.Preference;
import org.sakaiproject.user.api.repository.PreferenceRepository;

public class PreferenceRepositoryImpl extends SpringCrudRepositoryImpl<Preference, String> implements PreferenceRepository {
}
