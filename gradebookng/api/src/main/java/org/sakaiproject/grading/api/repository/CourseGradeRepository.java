package org.sakaiproject.grading.api.repository;

import java.util.List;

import org.sakaiproject.grading.api.model.CourseGrade;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface CourseGradeRepository extends SpringCrudRepository<CourseGrade, Long> {

    List<CourseGrade> findByGradebook_Id(Long gradebookId);
    List<CourseGrade> findByGradebook_Uid(String gradebookUid);
}
