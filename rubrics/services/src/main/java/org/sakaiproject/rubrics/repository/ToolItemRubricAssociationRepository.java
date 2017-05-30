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

import org.sakaiproject.rubrics.model.ToolItemRubricAssociation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "rubric-associations", path = "rubric-associations")
public interface ToolItemRubricAssociationRepository extends BaseResourceRepository<ToolItemRubricAssociation, Long> {

    @Override
    @PreAuthorize("canRead(#id, 'ToolItemRubricAssociation')")
    ToolItemRubricAssociation findOne(Long id);

    @Override
    @Query("select resource from ToolItemRubricAssociation resource where " + QUERY_CONTEXT_CONSTRAINT)
    Page<ToolItemRubricAssociation> findAll(Pageable pageable);

    @Override
    @PreAuthorize("canWrite(#id, 'ToolItemRubricAssociation')")
    void delete(Long id);

    @RestResource(path = "by-tool-item-ids", rel = "by-tool-item-ids")
    @Query("select resource from ToolItemRubricAssociation resource where resource.toolId = :toolId " +
            "and resource.itemId = :itemId and " + QUERY_CONTEXT_CONSTRAINT)
    List<ToolItemRubricAssociation> findByToolIdAndItemId(@Param("toolId") String toolId, @Param("itemId") String itemId);

    @RestResource(path = "by-rubric-id", rel = "by-rubric-id")
    @Query("select resource from ToolItemRubricAssociation resource where resource.rubricId = :rubricId " +
            "and " + QUERY_CONTEXT_CONSTRAINT)
    List<ToolItemRubricAssociation> findByRubricId(@Param("rubricId") String rubricId);
}
