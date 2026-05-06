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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.grading.api.model.AssignmentGradeRecord;
import org.sakaiproject.grading.api.model.GradebookAssignment;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface AssignmentGradeRecordRepository extends SpringCrudRepository<AssignmentGradeRecord, Long> {

    List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradebookId, Boolean removed);
    List<AssignmentGradeRecord> findByGradableObject_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradableObjectId, Boolean removed);
    List<AssignmentGradeRecord> findByGradableObject_IdAndGradableObject_Removed(Long gradableObjectId, Boolean removed);
    Optional<AssignmentGradeRecord> findByGradableObject_IdAndStudentId(Long assignmentId, String studentId);
    List<AssignmentGradeRecord> findByGradableObject_Gradebook_Id(Long gradebookId);
    List<AssignmentGradeRecord> findByGradableObject_Gradebook_Uid(String gradebookUid);
    List<AssignmentGradeRecord> findByGradableObject_RemovedAndGradableObject_IdInAndStudentIdIn(Boolean removed, List<Long> gradableObjectIds, List<String> studentIds);
    List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedAndStudentIdIn(Long gradebookId, Boolean removed, Collection<String> studentIds);
    List<AssignmentGradeRecord> findByGradableObjectAndStudentIdIn(GradebookAssignment assignment, Collection<String> studentIds);
    int deleteByGradableObject(GradebookAssignment assignment);
}
