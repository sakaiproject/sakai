package org.sakaiproject.grading.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradebookAssignmentRepository extends SpringCrudRepository<GradebookAssignment, Long> {

    Optional<GradebookAssignment> findByNameAndGradebook_UidAndRemoved(String name, String gradebookUid, Boolean removed);
    Optional<GradebookAssignment> findByIdAndGradebook_UidAndRemoved(Long id, String gradebookUid, Boolean removed);
    List<GradebookAssignment> findByGradebook_IdAndRemoved(Long gradebookId, Boolean removed);
    List<GradebookAssignment> findByCategory_IdAndRemoved(Long categoryId, Boolean removed);
    List<GradebookAssignment> findByGradebook_IdAndRemovedAndNotCounted(Long gradebookId, Boolean removed, Boolean notCounted);
    List<GradebookAssignment> findByGradebook_IdAndRemovedAndNotCountedAndUngraded(Long gradebookId, Boolean removed, Boolean notCounted, Boolean ungraded);
    Optional<GradebookAssignment> findByGradebook_UidAndExternalId(String gradebookUid, String externalId);
    Long countByGradebook_UidAndExternalId(String gradebookUid, String externalId);
    Long countByNameAndGradebook_UidAndRemoved(String name, String gradebookUid, Boolean removed);
    Long countByNameAndGradebookAndNotIdAndRemoved(String name, Gradebook gradebook, Long id, Boolean removed);
    boolean existsByIdAndRemoved(Long id, Boolean removed);
    List<GradebookAssignment> findByGradebook_Uid(String gradebookUid);
}
