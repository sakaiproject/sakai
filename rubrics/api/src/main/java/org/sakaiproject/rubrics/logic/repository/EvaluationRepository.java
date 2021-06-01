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

import java.util.List;
import java.util.Optional;

import javax.persistence.QueryHint;

import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "evaluations", path = "evaluations")
public interface EvaluationRepository extends MetadataRepository<Evaluation, Long> {

    static final String EVALUATOR_CONSTRAINT = "(1 = ?#{principal.isEvaluator() ? 1 : 0} and " +
        QUERY_CONTEXT_CONSTRAINT + ")";

    static final String EVALUEE_CONSTRAINT = "(1 = ?#{principal.isEvaluee() ? 1 : 0} and " +
        "(resource.evaluatedItemOwnerId = ?#{principal.userId} or " +
            "(resource.evaluatedItemOwnerType = ?#{ T(org.sakaiproject.rubrics.logic.model.EvaluatedItemOwnerType).GROUP } and " +
                "resource.evaluatedItemOwnerId in (?#{ principal.groups }))) and "  + 
        "status = ?#{ T(org.sakaiproject.rubrics.logic.model.EvaluationStatus).RETURNED })";

    @Override
    @PreAuthorize("canRead(#id, 'Evaluation')")
    Optional<Evaluation> findById(Long id);

    @Override
    @PreAuthorize("hasRole('ROLE_EVALUATOR')")
    @Query("select resource from Evaluation resource where " + QUERY_CONTEXT_CONSTRAINT)
    Page<Evaluation> findAll(Pageable pageable);

    @Override
    @PreAuthorize("canWrite(#id, 'Evaluation')")
    void deleteById(Long id);

    @RestResource(path = "by-association", rel = "by-association")
    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource from Evaluation resource where " +
            "resource.toolItemRubricAssociation.id = :toolItemRubricAssociationId " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    @QueryHints(@QueryHint(name="org.hibernate.cacheable", value = "true"))
    List<Evaluation> findByToolItemRubricAssociationId(@Param("toolItemRubricAssociationId") Long toolItemRubricAssociationId);

    @RestResource(path = "by-tool-and-assignment-and-submission", rel = "by-tool-and-assignment-and-submission")
    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource from Evaluation resource where " +
            "resource.evaluatedItemId = :evaluatedItemId " +
            "and resource.toolItemRubricAssociation.toolId = :toolId " +
            "and resource.toolItemRubricAssociation.itemId = :itemId " +
            "and resource.toolItemRubricAssociation.active = 1 " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    @QueryHints(@QueryHint(name="org.hibernate.cacheable", value = "true"))
    List<Evaluation> findByToolIdAndAssociationItemIdAndEvaluatedItemId(@Param("toolId") String toolId,
            @Param("itemId") String itemId, @Param("evaluatedItemId") String evaluatedItemId);

    @RestResource(path = "by-association-and-user", rel = "by-association-and-user")
    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource.evaluatedItemId from Evaluation resource where " +
            " resource.toolItemRubricAssociation.itemId = :associationId " +
            "and resource.evaluatedItemOwnerId = :userId " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    @QueryHints(@QueryHint(name="org.hibernate.cacheable", value = "true"))
    String findByAssociationIdAndUserId(@Param("associationId") String associationId, @Param("userId") String userId);
}
