/**
 * Copyright (c) 2007-2024 The Apereo Foundation
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
package org.sakaiproject.hierarchy.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface HierarchyNodeRepository extends SpringCrudRepository<HierarchyNode, Long> {

    List<HierarchyNode> findByIdIn(Collection<Long> ids);

    long countByHierarchyId(String hierarchyId);

    List<HierarchyNode> findByHierarchyId(String hierarchyId);

    Optional<HierarchyNode> findByHierarchyIdAndIsRootNode(String hierarchyId, Boolean isRootNode);

    List<HierarchyNode> findByHierarchyIdAndPermToken(String hierarchyId, String permToken);

    long countByIsDisabledIsNull();

    void fixupDatabase();

    Set<HierarchyNode> findAllAncestors(Long nodeId);

    Set<HierarchyNode> findAllDescendants(Long nodeId);

    void deleteRelationsByNodeIds(Collection<Long> nodeIds);
}
