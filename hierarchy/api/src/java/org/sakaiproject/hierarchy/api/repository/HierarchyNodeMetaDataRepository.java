/*
 * Copyright (c) 2025 The Apereo Foundation
 * Licensed under the ECL-2.0 license.
 */
package org.sakaiproject.hierarchy.api.repository;

import java.util.List;

import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface HierarchyNodeMetaDataRepository extends SpringCrudRepository<HierarchyNodeMetaData, Long> {

    long countByHierarchyId(String hierarchyId);
    List<HierarchyNodeMetaData> findByHierarchyId(String hierarchyId);
    HierarchyNodeMetaData findRootByHierarchyId(String hierarchyId);
    HierarchyNodeMetaData findByNodeId(Long nodeId);
    List<HierarchyNodeMetaData> findByNodeIds(List<Long> nodeIds);
    List<HierarchyNodeMetaData> findByHierarchyIdAndPermTokenOrdered(String hierarchyId, String permToken);
}

