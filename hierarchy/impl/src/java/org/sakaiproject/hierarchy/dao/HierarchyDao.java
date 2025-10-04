/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.hierarchy.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodePermission;
import org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode;

/**
 * DAO for hierarchy entities using standard Hibernate APIs.
 */
public interface HierarchyDao {

    // Maintenance (was used for legacy data cleanup; no longer invoked at runtime)

    // Persistence helpers
    void save(Object entity);
    void saveSet(Collection<?> entities);
    void delete(Object entity);
    void deleteSet(Collection<?> entities);
    void deleteMixedSet(Set<?>[] entitySets);
    void saveMixedSet(Set<?>[] entitySets);
    <T> T findById(Class<T> type, Long id);

    // Node meta data
    long countNodeMetaByHierarchyId(String hierarchyId);
    List<HierarchyNodeMetaData> findNodeMetaByHierarchyId(String hierarchyId);
    HierarchyNodeMetaData findRootNodeMetaByHierarchy(String hierarchyId);
    HierarchyNodeMetaData findNodeMetaByNodeId(Long nodeId);
    List<HierarchyNodeMetaData> findNodeMetaByNodeIds(List<Long> nodeIds);
    List<HierarchyNodeMetaData> findNodeMetaByHierarchyAndPermTokenOrdered(String hierarchyId, String permToken);

    // Nodes
    List<HierarchyPersistentNode> findNodesByIds(List<Long> nodeIds);

    // Permissions
    HierarchyNodePermission findNodePerm(String userId, String nodeId, String permission);
    List<HierarchyNodePermission> findNodePerms(String userId, String permission);
    List<HierarchyNodePermission> findNodePerms(String userId, String permission, List<String> nodeIds);
    List<HierarchyNodePermission> findNodePermsByNodeIds(List<String> nodeIds);
    List<HierarchyNodePermission> findNodePermsByUserIds(List<String> userIds);
}
