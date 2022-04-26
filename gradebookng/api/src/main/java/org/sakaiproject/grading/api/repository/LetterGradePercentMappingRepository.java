package org.sakaiproject.grading.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.grading.api.model.LetterGradePercentMapping;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface LetterGradePercentMappingRepository extends SpringCrudRepository<LetterGradePercentMapping, Long> {

    List<LetterGradePercentMapping> findByMappingType(Integer mappingType);
    Optional<LetterGradePercentMapping> findByGradebookIdAndMappingType(Long gradebookId, Integer mappingType);
}
