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

package org.sakaiproject.rubrics.repository;

import org.sakaiproject.rubrics.model.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "evaluations", path = "evaluations")
public interface EvaluationRepository extends BaseResourceRepository<Evaluation, Long> {

    static final String EVALUATOR_CONSTRAINT = "(1 = ?#{principal.isEvaluator() ? 1 : 0} and " +
            QUERY_CONTEXT_CONSTRAINT + ")";

    static final String EVALUEE_CONSTRAINT = "(1 = ?#{principal.isEvalueeOnly() ? 1 : 0} and " +
            "resource.evaluatedItemOwnerId = ?#{principal.userId})";

    @Override
    @PreAuthorize("canRead(#id, 'Evaluation')")
    Evaluation findOne(Long id);

    @Override
    @PreAuthorize("hasRole('ROLE_EVALUATOR')")
    @Query("select resource from Evaluation resource where " + QUERY_CONTEXT_CONSTRAINT)
    Page<Evaluation> findAll(Pageable pageable);

    @Override
    @PreAuthorize("canWrite(#id, 'Evaluation')")
    void delete(Long id);

    @RestResource(path = "by-association-id", rel = "by-association-id")
    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource from Evaluation resource where resource.toolItemRubricAssociation.id = :toolItemRubricAssociationId " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    List<Evaluation> findByToolItemRubricAssociationId(@Param("toolItemRubricAssociationId") Long toolItemRubricAssociationId);

    @RestResource(path = "by-tool-item-and-associated-item-ids", rel = "by-tool-item-and-associated-item-ids")
    @PreAuthorize("hasRole('ROLE_EVALUATOR')")
    @Query("select resource from Evaluation resource where resource.toolItemRubricAssociation.toolId = :toolId " +
            "and resource.toolItemRubricAssociation.itemId = :itemId and " + QUERY_CONTEXT_CONSTRAINT)
    List<Evaluation> findByToolIdAndAssociationItemId(@Param("toolId") String toolId, @Param("itemId") String itemId);

    @RestResource(path = "by-tool-item-and-associated-item-and-evaluated-item-ids", rel = "by-tool-item-and-associated-item-and-evaluated-item-ids")
    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource from Evaluation resource where " +
            "resource.evaluatedItemId = :evaluatedItemId " +
            "and resource.toolItemRubricAssociation.toolId = :toolId " +
            "and resource.toolItemRubricAssociation.itemId = :itemId " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    List<Evaluation> findByToolIdAndAssociationItemIdAndEvaluatedItemId(@Param("toolId") String toolId,
            @Param("itemId") String itemId, @Param("evaluatedItemId") String evaluatedItemId);
}
