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

import org.sakaiproject.hierarchy.model.HierarchyNodePermission;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface HierarchyNodePermissionRepository extends SpringCrudRepository<HierarchyNodePermission, Long> {

    /**
     * Finds the single permission grant identified by its natural key (user, node, permission).
     *
     * @param userId the user the grant belongs to
     * @param nodeId the node the permission is held on
     * @param permission the permission name
     * @return the matching grant, or {@link Optional#empty()} if the user does not hold that
     *         permission on that node
     */
    Optional<HierarchyNodePermission> findByUserIdAndNodeIdAndPermission(String userId, String nodeId, String permission);

    /**
     * Finds every node on which a user holds the given permission.
     *
     * @param userId the user whose grants to return
     * @param permission the permission name to match
     * @return the matching grants, or an empty list if the user holds that permission on no node
     */
    List<HierarchyNodePermission> findByUserIdAndPermission(String userId, String permission);

    /**
     * Finds all permission grants on any of the given nodes, across all users and permissions.
     *
     * @param nodeIds the nodes whose grants to return; an empty collection yields an empty result
     * @return the matching grants, or an empty list if none match
     */
    List<HierarchyNodePermission> findByNodeIdIn(Collection<String> nodeIds);

    /**
     * Finds all permission grants held by any of the given users, across all nodes and permissions.
     *
     * @param userIds the users whose grants to return; an empty collection yields an empty result
     * @return the matching grants, or an empty list if none match
     */
    List<HierarchyNodePermission> findByUserIdIn(Collection<String> userIds);

    /**
     * Finds all grants of any of the given permissions, across all users and nodes.
     *
     * @param permissions the permission names to match; an empty collection yields an empty result
     * @return the matching grants, or an empty list if none match
     */
    List<HierarchyNodePermission> findByPermissionIn(Collection<String> permissions);

    /**
     * Finds the grants where a user holds the given permission on any of the given nodes.
     *
     * @param userId the user whose grants to return
     * @param permission the permission name to match
     * @param nodeIds the nodes to restrict to; an empty collection yields an empty result
     * @return the matching grants, or an empty list if none match
     */
    List<HierarchyNodePermission> findByUserIdAndPermissionAndNodeIdIn(String userId, String permission, Collection<String> nodeIds);

    /**
     * Finds the grants of the given permission on any of the given nodes, across all users.
     *
     * @param permission the permission name to match
     * @param nodeIds the nodes to restrict to; an empty collection yields an empty result
     * @return the matching grants, or an empty list if none match
     */
    List<HierarchyNodePermission> findByPermissionAndNodeIdIn(String permission, Collection<String> nodeIds);

    /**
     * Finds all permission grants a user holds on any of the given nodes, across all permissions.
     *
     * @param userId the user whose grants to return
     * @param nodeIds the nodes to restrict to; an empty collection yields an empty result
     * @return the matching grants, or an empty list if none match
     */
    List<HierarchyNodePermission> findByUserIdAndNodeIdIn(String userId, Collection<String> nodeIds);
}
