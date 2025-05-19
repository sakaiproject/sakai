package org.sakaiproject.user.impl.repository;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.user.api.model.Preference;
import org.sakaiproject.user.api.repository.PreferenceRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
public class PreferenceRepositoryImpl extends SpringCrudRepositoryImpl<Preference, String> implements PreferenceRepository {
}
