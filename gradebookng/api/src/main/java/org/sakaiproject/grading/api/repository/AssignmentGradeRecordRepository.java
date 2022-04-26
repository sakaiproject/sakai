package org.sakaiproject.grading.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.grading.api.model.AssignmentGradeRecord;
import org.sakaiproject.grading.api.model.GradebookAssignment;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface AssignmentGradeRecordRepository extends SpringCrudRepository<AssignmentGradeRecord, Long> {

    List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradebookId, Boolean removed);
    List<AssignmentGradeRecord> findByGradableObject_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradableObjectId, Boolean removed);
    Optional<AssignmentGradeRecord> findByGradableObject_IdAndStudentId(Long assignmentId, String studentId);
    List<AssignmentGradeRecord> findByGradableObject_Gradebook_Id(Long gradebookId);
    List<AssignmentGradeRecord> findByGradableObject_Gradebook_Uid(String gradebookUid);
    List<AssignmentGradeRecord> findByGradableObject_RemovedAndGradableObject_IdInAndStudentIdIn(Boolean removed, List<Long> gradableObjectIds, List<String> studentIds);
    List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedAndStudentIdIn(Long gradebookId, Boolean removed, Collection<String> studentIds);
    List<AssignmentGradeRecord> findByGradableObjectAndStudentIdIn(GradebookAssignment assignment, Collection<String> studentIds);
    int deleteByGradableObject(GradebookAssignment assignment);
}
