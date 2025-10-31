/*
 * Copyright (c) 2025 The Apereo Foundation
 * Licensed under the ECL-2.0 license.
 */
package org.sakaiproject.hierarchy.api.repository;

import java.util.List;

import org.sakaiproject.hierarchy.dao.model.HierarchyNodePermission;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface HierarchyNodePermissionRepository extends SpringCrudRepository<HierarchyNodePermission, Long> {

    HierarchyNodePermission findByUserIdAndNodeIdAndPermission(String userId, String nodeId, String permission);
    List<HierarchyNodePermission> findByUserIdAndPermission(String userId, String permission);
    List<HierarchyNodePermission> findByUserIdAndPermissionAndNodeIds(String userId, String permission, List<String> nodeIds);
    List<HierarchyNodePermission> findByPermissionAndNodeIds(String permission, List<String> nodeIds);
    List<HierarchyNodePermission> findByUserIdAndNodeIds(String userId, List<String> nodeIds);
    List<HierarchyNodePermission> findByNodeIdIn(List<String> nodeIds);
    List<HierarchyNodePermission> findByUserIdIn(List<String> userIds);
}
