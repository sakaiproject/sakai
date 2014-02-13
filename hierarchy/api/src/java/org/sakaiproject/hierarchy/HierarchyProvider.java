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

/**
 * This interface provides methods to get hierarchical node data into sakai
 * for use in determining the structure above sites/groups related to
 * adminstration and access to data and control of permissions
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyProvider extends HierarchyNodeReader {

   public static final String HIERARCHY_PERM_NODE_UPDATE = "perm_node_update";
   public static final String HIERARCHY_PERM_NODE_REMOVE = "perm_node_remove";

   /**
    * Determine if a user has a specific hierarchy permission at a specific hierarchy node
    * <br/>The actual permissions this should handle are shown at the top of this class
    * 
    * @param userId the internal user id (not username)
    * @param nodeId a unique id for a hierarchy node
    * @param hierarchyPermConstant a HIERARCHY_PERM_NODE constant
    * @return true if the user has this permission, false otherwise
    */
   public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant);

}
