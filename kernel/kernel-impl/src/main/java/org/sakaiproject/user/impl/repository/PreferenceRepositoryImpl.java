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
    // Explicitly set the domain class for this repository
    private final Class<Preference> domainClass = Preference.class;
    
    @Override
    public Class<Preference> getDomainClass() {
        return domainClass;
    }
    
    // Override key methods to ensure domain class is used
    @Override
    @Transactional
    public <S extends Preference> S save(S entity) {
        SessionFactory sf = getSessionFactory();
        if (sf == null) {
            log.error("SessionFactory is null, cannot save entity");
            return entity;
        }
        
        Session session = sf.getCurrentSession();
        boolean isNew = entity.getId() == null;
        
        try {
            if (isNew) {
                session.persist(entity);
                return entity;
            } else {
                return (S) session.merge(entity);
            }
        } catch (Exception e) {
            log.error("Error saving preference entity: {}", e.getMessage(), e);
            return entity;
        }
    }
    
    @Override
    public Optional<Preference> findById(String id) {
        try {
            if (id == null) {
                return Optional.empty();
            }
            
            SessionFactory sf = getSessionFactory();
            if (sf == null) {
                log.error("SessionFactory is null, cannot find entity");
                return Optional.empty();
            }
            
            Session session = sf.getCurrentSession();
            return Optional.ofNullable(session.get(domainClass, id));
        } catch (Exception e) {
            log.error("Error finding preference by id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public boolean existsById(String id) {
        try {
            if (id == null) {
                return false;
            }
            return findById(id).isPresent();
        } catch (Exception e) {
            log.error("Error checking if preference exists by id {}: {}", id, e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public void deleteById(String id) {
        try {
            if (id == null) {
                log.warn("Cannot delete preference with null id");
                return;
            }
            
            SessionFactory sf = getSessionFactory();
            if (sf == null) {
                log.error("SessionFactory is null, cannot delete entity");
                return;
            }
            
            Session session = sf.getCurrentSession();
            findById(id).ifPresent(session::delete);
        } catch (Exception e) {
            log.error("Error deleting preference by id {}: {}", id, e.getMessage());
        }
    }
}
