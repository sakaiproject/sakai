package org.sakaiproject.grading.api.repository;

import java.util.List;

import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Gradebook;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface CategoryRepository extends SpringCrudRepository<Category, Long> {

    List<Category> findByGradebook_IdAndRemoved(Long gradebookId, Boolean removed);
    boolean existsByNameAndGradebookAndRemoved(String name, Gradebook gradebook, Boolean removed);
    boolean existsByNameAndGradebookAndNotIdAndRemoved(String name, Gradebook gradebook, Long id, Boolean removed);
    List<Category> findByGradebook_Uid(String gradebookUid);
}
