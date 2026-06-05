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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface HierarchyNodeRepository extends SpringCrudRepository<HierarchyNode, Long> {

    /**
     * Finds the nodes whose id is one of the given ids.
     *
     * @param ids the node ids to look up; an empty collection yields an empty result
     * @return the matching nodes, or an empty list if none match
     */
    List<HierarchyNode> findByIdIn(Collection<Long> ids);

    /**
     * Counts the nodes belonging to a hierarchy.
     *
     * @param hierarchyId the hierarchy to count nodes in
     * @return the number of nodes in the hierarchy, or {@code 0} if it has none
     */
    long countByHierarchyId(String hierarchyId);

    /**
     * Finds all nodes belonging to a hierarchy.
     *
     * @param hierarchyId the hierarchy whose nodes to return
     * @return the nodes in the hierarchy, or an empty list if it has none
     */
    List<HierarchyNode> findByHierarchyId(String hierarchyId);

    /**
     * Finds the single node in a hierarchy matching the given root-node flag. A hierarchy has at
     * most one root node, so this is typically called with {@code Boolean.TRUE} to fetch the root.
     *
     * @param hierarchyId the hierarchy to search within
     * @param isRootNode the root-node flag the returned node must have
     * @return the matching node, or {@link Optional#empty()} if none matches
     */
    Optional<HierarchyNode> findByHierarchyIdAndIsRootNode(String hierarchyId, Boolean isRootNode);

    /**
     * Finds the nodes in a hierarchy that share the given permission token, ordered by id. Nodes
     * grouped under a common token share a permission boundary.
     *
     * @param hierarchyId the hierarchy to search within
     * @param permToken the permission token to match
     * @return the matching nodes ordered by id, or an empty list if none match
     */
    List<HierarchyNode> findByHierarchyIdAndPermToken(String hierarchyId, String permToken);

    /**
     * Finds the nodes in a hierarchy whose title is one of the given titles and whose disabled flag
     * matches {@code isDisabled}.
     *
     * @param hierarchyId the hierarchy to search within
     * @param titles the titles to match; an empty collection yields an empty result
     * @param isDisabled the disabled flag the returned nodes must have
     * @return the matching nodes, or an empty list if none match
     */
    List<HierarchyNode> findByHierarchyIdAndTitleInAndIsDisabled(String hierarchyId, Collection<String> titles, Boolean isDisabled);

    /**
     * Finds the childless (leaf) nodes in a hierarchy that are neither the root nor disabled and
     * whose title does not match the given SQL LIKE pattern.
     *
     * @param hierarchyId the hierarchy to search within
     * @param titlePattern a SQL LIKE pattern; nodes whose title matches it are excluded
     * @return the matching leaf nodes, or an empty list if none match
     */
    List<HierarchyNode> findChildlessByHierarchyIdAndTitleNotLike(String hierarchyId, String titlePattern);

    /**
     * Resolves the transitive ancestors (every parent up to the roots) of a node.
     *
     * @param nodeId the node to resolve ancestors for
     * @return the ancestor nodes, or an empty set if the node has no parents
     */
    Set<HierarchyNode> findAllAncestors(Long nodeId);

    /**
     * Resolves the transitive descendants (every child down to the leaves) of a node.
     *
     * @param nodeId the node to resolve descendants for
     * @return the descendant nodes, or an empty set if the node has no children
     */
    Set<HierarchyNode> findAllDescendants(Long nodeId);

    /**
     * Batch-resolves the transitive ancestor ids for each of the given nodes in a single query.
     *
     * @param nodeIds the nodes to resolve ancestors for
     * @return a map keyed by node id whose values are that node's transitive ancestor ids; nodes
     *         with no ancestors are absent from the map
     */
    Map<Long, Set<Long>> findAncestorIdsByNodeIds(Collection<Long> nodeIds);

    /**
     * Batch-resolves the transitive descendant ids for each of the given nodes in a single query.
     *
     * @param nodeIds the nodes to resolve descendants for
     * @return a map keyed by node id whose values are that node's transitive descendant ids; nodes
     *         with no descendants are absent from the map
     */
    Map<Long, Set<Long>> findDescendantIdsByNodeIds(Collection<Long> nodeIds);

    /**
     * Batch-resolves the direct parent ids for each of the given nodes in a single query.
     *
     * @param nodeIds the nodes to resolve direct parents for
     * @return a map keyed by node id whose values are that node's direct parent ids; nodes with no
     *         parents are absent from the map
     */
    Map<Long, Set<Long>> findDirectParentIdsByNodeIds(Collection<Long> nodeIds);

    /**
     * Batch-resolves the direct child ids for each of the given nodes in a single query.
     *
     * @param nodeIds the nodes to resolve direct children for
     * @return a map keyed by node id whose values are that node's direct child ids; nodes with no
     *         children are absent from the map
     */
    Map<Long, Set<Long>> findDirectChildIdsByNodeIds(Collection<Long> nodeIds);
}
