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

import org.sakaiproject.hierarchy.model.HierarchyNode;


/**
 * This service interface defines the capabilities of the hierarchy system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyService extends HierarchyNodeReader, HierarchyNodeWriter, HierarchyTokens, HierarchyPermissions {

   /**
    * Creates a new hierarchy with the unique id specified, exception if this id is already used
    * @param hierarchyId a unique id which defines the hierarchy
    * @return the object representing the root node of the new hierarchy
    */
   public HierarchyNode createHierarchy(String hierarchyId);

   /**
    * Sets the root node of this hierarchy, note that although a hierarchy might have multiple
    * nodes at the top of the hierarchy, it always has a primary node which is considering the
    * "entry point" into the hierarchy<br/>
    * A node must have no parents to be set to the root node<br/>
    * The first node added to a hierarchy becomes the root node by default
    * @param hierarchyId a unique id which defines the hierarchy
    * @param nodeId a unique id for a hierarchy node
    * @return the object representing the node which is now the root node
    */
   public HierarchyNode setHierarchyRootNode(String hierarchyId, String nodeId);

   /**
    * Completely and permanantly destroy a hierarchy and all related nodes
    * @param hierarchyId a unique id which defines the hierarchy
    */
   public void destroyHierarchy(String hierarchyId);

}
