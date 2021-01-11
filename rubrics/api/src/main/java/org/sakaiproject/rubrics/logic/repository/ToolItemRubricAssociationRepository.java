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

import org.sakaiproject.rubrics.logic.model.ToolItemRubricAssociation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "rubric-associations", path = "rubric-associations")
public interface ToolItemRubricAssociationRepository extends MetadataRepository<ToolItemRubricAssociation, Long> {

    @Override
    @PreAuthorize("canRead(#id, 'ToolItemRubricAssociation')")
    Optional<ToolItemRubricAssociation> findById(Long id);

    @Override
    @Query("select resource from ToolItemRubricAssociation resource where " + QUERY_CONTEXT_CONSTRAINT)
    Page<ToolItemRubricAssociation> findAll(Pageable pageable);

    @Override
    @PreAuthorize("canWrite(#id, 'ToolItemRubricAssociation')")
    void deleteById(Long id);

    @RestResource(path = "by-tool-and-assignment", rel = "by-tool-and-assignment")
    @Query("select resource from ToolItemRubricAssociation resource where " +
                "resource.toolId = :toolId and " +
                "resource.itemId = :itemId and " +
                "active = 1 and " + QUERY_CONTEXT_CONSTRAINT)
    @QueryHints(@QueryHint(name="org.hibernate.cacheable", value = "true"))
    List<ToolItemRubricAssociation> findByToolIdAndItemId(@Param("toolId") String toolId, @Param("itemId") String itemId);

    @RestResource(path = "by-assignment-and-rubric", rel = "by-assignment-and-rubric")
    @Query("select resource from ToolItemRubricAssociation resource where " +
                "resource.itemId = :itemId and " +
                "resource.rubricId = :rubricId and " + QUERY_CONTEXT_CONSTRAINT)
    @QueryHints(@QueryHint(name="org.hibernate.cacheable", value = "true"))
    List<ToolItemRubricAssociation> findByToolIdAndItemId(@Param("itemId") String itemId, @Param("rubricId") Long rubricId);

    @RestResource(path = "by-rubric", rel = "by-rubric")
    @Query("select resource from ToolItemRubricAssociation resource where " +
                "resource.rubricId = :rubricId")
    @QueryHints(@QueryHint(name="org.hibernate.cacheable", value = "true"))
    List<ToolItemRubricAssociation> findByRubricId(@Param("rubricId") Long rubricId);
	
    @RestResource(path = "by-item-id-prefix", rel = "by-item-id-prefix")
    @Query("select resource from ToolItemRubricAssociation resource where " +
                "resource.toolId = :toolId and " +
                "resource.itemId like CONCAT(:itemId, '%') and " + QUERY_CONTEXT_CONSTRAINT)
    @QueryHints(@QueryHint(name="org.hibernate.cacheable", value = "true"))
    List<ToolItemRubricAssociation> findByItemIdPrefix(@Param("toolId") String toolId, @Param("itemId") String itemId);
}
