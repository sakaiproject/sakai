/*
 * Copyright (c) 2003-2022 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.grading.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;

import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface GradebookAssignmentRepository extends SpringCrudRepository<GradebookAssignment, Long> {

    Optional<GradebookAssignment> findByNameAndGradebookIdAndRemoved(String name, String gradebookId, Boolean removed);
    Optional<GradebookAssignment> findByIdAndGradebookIdAndRemoved(Long id, String gradebookId, Boolean removed);

    @Transactional(readOnly = true)
    Optional<GradableObject> findByGradableIdAndGradebookIdAndRemoved(Long id, String gradebookId, Boolean removed);

    List<GradebookAssignment> findByGradebookIdAndRemoved(String gradebookId, Boolean removed);
    List<GradebookAssignment> findByCategoryIdAndRemoved(Long categoryId, Boolean removed);
    List<GradebookAssignment> findByGradebookIdAndRemovedAndNotCounted(String gradebookId, Boolean removed, Boolean notCounted);
    List<GradebookAssignment> findByGradebookIdAndRemovedAndNotCountedAndUngraded(String gradebookId, Boolean removed, Boolean notCounted, Boolean ungraded);
    Optional<GradebookAssignment> findByGradebookIdAndExternalId(String gradebookId, String externalId);
    Long countByGradebookIdAndExternalId(String gradebookId, String externalId);
    Long countByNameAndGradebookIdAndRemoved(String name, String gradebookId, Boolean removed);
    Long countByNameAndGradebookAndNotIdAndRemoved(String name, Gradebook gradebook, Long id, Boolean removed);
    boolean existsByIdAndRemoved(Long id, Boolean removed);
    List<GradebookAssignment> findByGradebookId(String gradebookId);
}
