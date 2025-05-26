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

import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradebookAssignmentRepository extends SpringCrudRepository<GradebookAssignment, Long> {

    Optional<GradebookAssignment> findByNameAndGradebook_UidAndRemoved(String name, String gradebookUid, Boolean removed);
    Optional<GradebookAssignment> findByIdAndGradebook_UidAndRemoved(Long id, String gradebookUid, Boolean removed);
    List<GradebookAssignment> findByGradebook_IdAndRemoved(Long gradebookId, Boolean removed);
    List<GradebookAssignment> findByGradebook_IdAndCategory_IdAndRemoved(Long gradebookId, Long categoryId, Boolean removed);
    List<GradebookAssignment> findByCategory_IdAndRemoved(Long categoryId, Boolean removed);
    List<GradebookAssignment> findByGradebook_IdAndRemovedAndNotCounted(Long gradebookId, Boolean removed, Boolean notCounted);
    List<GradebookAssignment> findByGradebook_IdAndRemovedAndNotCountedAndUngraded(Long gradebookId, Boolean removed, Boolean notCounted, Boolean ungraded);
    Optional<GradebookAssignment> findByGradebook_UidAndExternalId(String gradebookUid, String externalId);
    List<GradebookAssignment> findByExternalId(String externalId);
    Long countByGradebook_UidAndExternalId(String gradebookUid, String externalId);
    Long countByNameAndGradebook_UidAndRemoved(String name, String gradebookUid, Boolean removed);
    Long countByNameAndGradebookAndNotIdAndRemoved(String name, Gradebook gradebook, Long id, Boolean removed);
    boolean existsByIdAndRemoved(Long id, Boolean removed);
    List<GradebookAssignment> findByGradebook_Uid(String gradebookUid);
}
