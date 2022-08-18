package org.sakaiproject.grading.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.grading.api.model.Comment;
import org.sakaiproject.grading.api.model.GradebookAssignment;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface CommentRepository extends SpringCrudRepository<Comment, Long> {

    Optional<Comment> findByStudentIdAndGradableObject_Gradebook_UidAndGradableObject_IdAndGradableObject_Removed(
            String studentUid, String gradebookUid, Long assignmentId, Boolean removed);

    List<Comment> findByGradableObjectAndStudentIdIn(GradebookAssignment assignment, Collection<String> studentIds);

    List<Comment> findByGradableObject_Gradebook_Uid(String gradebookUid);

    int deleteByGradableObject(GradebookAssignment assignment);
}
