package org.sakaiproject.grading.api.repository;

import java.util.Optional;

import org.sakaiproject.grading.api.model.GradebookProperty;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradebookPropertyRepository extends SpringCrudRepository<GradebookProperty, Long> {

    Optional<GradebookProperty> findByName(String name);
}
