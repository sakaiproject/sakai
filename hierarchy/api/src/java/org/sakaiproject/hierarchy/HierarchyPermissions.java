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

package org.sakaiproject.hierarchy;

import java.util.Map;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * This interface contains the methods for assigning and checking permissions in the hierarchy
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface HierarchyPermissions {

    /**
     * Determine if a user has a specific hierarchy permission at a specific hierarchy node,
     * a permission key can be any string though it will most likely be from a relatively small set
     * 
     * @param userId the internal user id (not username)
     * @param nodeId a unique id for a hierarchy node
     * @param hierarchyPermission a string which indicates a permission key (e.g. delete.item)
     * @return true if the user has this permission, false otherwise
     */
    public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermission);

    // ASSIGN

    /**
     * Assign the given permission to a user for the given hierarchy node,
     * can cascade the permission downward if desired
     * 
     * @param userId the internal user id (not username)
     * @param nodeId a unique id for a hierarchy node
     * @param hierarchyPermission a string which indicates a permission key (e.g. delete.item)
     * @param cascade if true then the permission is assigned to all nodes below this one as well,
     * if false it is only assigned to this node
     */
    public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermission, boolean cascade);

    /**
     * Remove a permission for a user from the given hierarchy node,
     * can cascade the permission downward if desired
     * 
     * @param userId the internal user id (not username)
     * @param nodeId a unique id for a hierarchy node
     * @param hierarchyPermission a string which indicates a permission key (e.g. delete.item)
     * @param cascade if true then the permission is removed from all nodes below this one as well,
     * if false it is only removed from this node
     */
    public void removeUserNodePerm(String userId, String nodeId, String hierarchyPermission, boolean cascade);

    // NODES

    /**
     * Get all the userIds for users which have a specific permission in a set of
     * hierarchy nodes, this can be used to check one node or many nodes as needed
     * 
     * @param nodeIds an array of unique ids for hierarchy nodes
     * @param hierarchyPermission a string which indicates a permission key (e.g. delete.item)
     * @return a set of userIds (not username/eid)
     */
    public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermission);

    /**
     * Get the hierarchy nodes which a user has a specific permission in,
     * this is used to find a set of nodes which a user should be able to see and to build
     * the list of hierarchy nodes a user has a given permission in
     * 
     * @param userId the internal user id (not username)
     * @param hierarchyPermission a string which indicates a permission key (e.g. delete.item)
     * @return a Set of {@link HierarchyNode} objects
     */
    public Set<HierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermission);

    /**
     * Get the set of all permissions which a user has on a node or group of nodes,
     * NOTE: this will get the set of ALL permissions inclusively for the given nodeIds
     * so nodes in the set which a user has no permissions on will not cause this to return no permissions,
     * example: for given user: nodeA(perm1, perm2), nodeB(perm1), nodeC(perm2), nodeD() : returns: (perm1, perm2)
     * 
     * @param userId the internal user id (not username)
     * @param nodeIds an array of unique ids for hierarchy nodes
     * @return the set of permission keys which exist on any of the given nodes
     */
    public Set<String> getPermsForUserNodes(String userId, String[] nodeIds);

    /**
     * Get all the users and permissions currently assigned to nodes,
     * the returned map will always contain every passed in nodeId as a key
     * <br/>
     * This is not super efficient by itself so it should not used when other methods are sufficient,
     * however, it is actually much better than calling the other methods repeatedly so this is primarily
     * for use in administrative interfaces
     * 
     * @param nodeIds an array of unique ids for hierarchy nodes
     * @return the map of nodeId -> (map of userId -> Set(permission))
     */
    public Map<String, Map<String, Set<String>>> getUsersAndPermsForNodes(String... nodeIds);

    /**
     * Get all the nodeIds and permissions for the given userIds,
     * the returned map will always contain every userId that was passed in as a key
     * 
     * @param userIds an array of unique ids for users (internal id, not eid)
     * @return the map of userId -> (map of nodeId -> Set(permission))
     */
    public Map<String, Map<String, Set<String>>> getNodesAndPermsForUser(String... userIds);

}
