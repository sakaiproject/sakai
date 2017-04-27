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

import org.sakaiproject.rubrics.model.Criterion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.Serializable;

@RepositoryRestResource(collectionResourceRel = "criterions", path = "criterions")
public interface CriterionRepository extends BaseResourceRepository<Criterion, Long> {

    @Override
    @PreAuthorize("canRead(#id, 'Criterion')")
    Criterion findOne(Long id);

    @Override
    @Query("select resource from Criterion resource where " + QUERY_CONTEXT_CONSTRAINT)
    Page<Criterion> findAll(Pageable pageable);

    @Override
    @PreAuthorize("canWrite(#id, 'Criterion')")
    void delete(Long id);
}
