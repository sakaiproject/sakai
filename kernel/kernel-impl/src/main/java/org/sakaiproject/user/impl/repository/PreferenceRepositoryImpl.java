package org.sakaiproject.user.impl.repository;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.user.api.model.Preference;
import org.sakaiproject.user.api.repository.PreferenceRepository;

public class PreferenceRepositoryImpl extends SpringCrudRepositoryImpl<Preference, String> implements PreferenceRepository {
    // Default constructor
    public PreferenceRepositoryImpl() {
        super();
    }
    
    // Override to explicitly provide the domain class
    @Override
    public Class<Preference> getDomainClass() {
        return Preference.class;
    }
}
