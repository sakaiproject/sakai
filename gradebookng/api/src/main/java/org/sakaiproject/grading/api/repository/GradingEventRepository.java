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
