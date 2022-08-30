package org.sakaiproject.grading.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.grading.api.model.CourseGradeRecord;
import org.sakaiproject.grading.api.model.Gradebook;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface CourseGradeRecordRepository extends SpringCrudRepository<CourseGradeRecord, Long> {

    List<CourseGradeRecord> findByGradableObject_Id(Long id);
    List<CourseGradeRecord> findByGradableObject_GradebookAndEnteredGradeNotNull(Gradebook gradebook);
    Optional<CourseGradeRecord> findByGradableObject_GradebookAndStudentId(Gradebook gradebook, String studentId);
    Long countByGradableObject_Gradebook_IdAndEnteredGradeNotNullAndStudentIdIn(Long gradebookId, Set<String> studentIds);
    List<CourseGradeRecord> findByGradableObject_Gradebook_Uid(String gradebookUid);
}
