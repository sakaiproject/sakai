package org.sakaiproject.grading.impl.repository;

import org.sakaiproject.grading.api.model.GradebookManager;
import org.sakaiproject.grading.api.repository.GradebookManagerRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class GradebookManagerRepositoryImpl extends SpringCrudRepositoryImpl<GradebookManager, String> implements GradebookManagerRepository {

    @Override
    public void createGradebookManager(GradebookManager gradebookManager) {
        if (gradebookManager.getId() != null) {
            // the GradebookManagers id is the site id
            sessionFactory.getCurrentSession().persist(gradebookManager);
        }
    }
}
