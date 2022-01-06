/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.logic.repository;

import java.util.Optional;

import org.sakaiproject.rubrics.logic.model.ReturnedEvaluation;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "returned-evaluations", path = "returned-evaluations")
public interface ReturnedEvaluationRepository extends CrudRepository<ReturnedEvaluation, Long> {

    @PreAuthorize("hasRole('ROLE_EVALUATOR')")
    @RestResource(path = "by-original-evaluation-id", rel = "by-original-evaluation-id")
    Optional<ReturnedEvaluation> findByOriginalEvaluationId(@Param("id") Long id);

    @PreAuthorize("hasRole('ROLE_EVALUATOR')")
    void deleteById(Long id);
}
