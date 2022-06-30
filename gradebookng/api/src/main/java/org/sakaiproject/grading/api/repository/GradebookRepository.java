package org.sakaiproject.grading.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.grading.api.model.Gradebook;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradebookRepository extends SpringCrudRepository<Gradebook, Long> {

    Optional<Gradebook> findByUid(String uid);
    int deleteByUid(String uid);
}
