package org.sakaiproject.grading.api.repository;

import org.sakaiproject.grading.api.model.GradebookManager;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradebookManagerRepository extends SpringCrudRepository<GradebookManager, String> {
    /**
     * Persist a transient instance of a GradebookManager, it must not already exist in the database
     * @param gradebookManager
     */
    void createGradebookManager(GradebookManager gradebookManager);
}
