package org.sakaiproject.grading.api.repository;

import java.util.List;

import org.sakaiproject.grading.api.model.GradeMapping;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradeMappingRepository extends SpringCrudRepository<GradeMapping, Long> {

    public List<GradeMapping> findByGradebook_Uid(String gradebookUid);
}
