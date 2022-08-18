package org.sakaiproject.grading.api.repository;

import java.util.Date;
import java.util.List;

import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradingEventRepository extends SpringCrudRepository<GradingEvent, Long> {

    List<GradingEvent> findByGradableObject_Gradebook_Uid(String gradebookUid);
    List<GradingEvent> findByGradableObject_IdAndStudentIdOrderByDateGraded(Long assignmentId, String studentId);
    List<GradingEvent> findByDateGreaterThanEqualAndGradableObject_IdIn(Date since, List<Long> assignmentIds);
    int deleteByGradableObject(GradebookAssignment assignment);
}
