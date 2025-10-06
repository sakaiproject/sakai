/*
 * Copyright (c) 2025 The Apereo Foundation
 * Licensed under the ECL-2.0 license.
 */
package org.sakaiproject.hierarchy.api.repository;

import java.util.List;

import org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface HierarchyPersistentNodeRepository extends SpringCrudRepository<HierarchyPersistentNode, Long> {
    List<HierarchyPersistentNode> findByIdIn(List<Long> ids);
}

