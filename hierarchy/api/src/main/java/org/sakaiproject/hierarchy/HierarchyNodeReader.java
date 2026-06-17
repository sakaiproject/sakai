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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * Allows user to get hierarchical node data from the hierarchy
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyNodeReader {

   /**
    * Get the hierarchy root node for a specific hierarchy if it exists
    * @param hierarchyId a unique string which identifies this hierarchy
    * @return the {@link HierarchyNode} representing the root of the hierarchy
    * if found, otherwise returns a null
    */
   public HierarchyNode getRootNode(String hierarchyId);

   /**
    * Get a node based on the unique id,
    * convenience method for {@link #getNodesByIds(String[])}
    * @param nodeId a unique id for a hierarchy node
    * @return the {@link HierarchyNode} representing this node or null if it does not exist
    */
   public HierarchyNode getNodeById(String nodeId);

   /**
    * Get a set of nodes based on their unique ids,
    * <b>NOTE:</b> this method is here for efficiency
    * @param nodeIds unique ids for hierarchy nodes
    * @return a map of nodeId -> {@link HierarchyNode}
    */
   public Map<String, HierarchyNode> getNodesByIds(String[] nodeIds);

   /**
    * Get all the parent nodes for a specific node all the way to the root node, 
    * returns empty set if this is the root node
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param directOnly if true then only include the nodes which are directly connected to this node, 
    * else return every node that is a parent of this node
    * @return a Set of {@link HierarchyNode} objects representing all parent nodes for the specified child,
    * empty set if no parents found
    */
   public Set<HierarchyNode> getParentNodes(String nodeId, boolean directOnly);

   /**
    * Get all children nodes for this node in the hierarchy all the way to the leaves, 
    * will return no nodes if this node has no children
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param directOnly if true then only include the nodes which are directly connected to this node, 
    * else return every node that is a child of this node
    * @return a Set of {@link HierarchyNode} objects representing all children nodes for the specified parent,
    * empty set if no children found
    */
   public Set<HierarchyNode> getChildNodes(String nodeId, boolean directOnly);

   /**
    * Batch-resolves the transitive parent (ancestor) node ids for each of the given nodes in a
    * single query. Preferred over calling {@link #getParentNodes(String, boolean)} per node when
    * resolving many nodes.
    *
    * @param nodeIds the nodes to resolve ancestors for
    * @return a map keyed by node id whose value is that node's set of ancestor node ids; a node
    * with no ancestors maps to an empty set
    */
   public Map<String, Set<String>> getParentNodeIds(String[] nodeIds);

   /**
    * Batch-resolves the transitive child (descendant) node ids for each of the given nodes in a
    * single query. Preferred over calling {@link #getChildNodes(String, boolean)} per node when
    * resolving many nodes.
    *
    * @param nodeIds the nodes to resolve descendants for
    * @return a map keyed by node id whose value is that node's set of descendant node ids; a node
    * with no descendants maps to an empty set
    */
   public Map<String, Set<String>> getChildNodeIds(String[] nodeIds);

   /**
    * Batch-resolves the direct parent node ids for each of the given nodes in a single query.
    *
    * @param nodeIds the nodes to resolve direct parents for
    * @return a map keyed by node id whose value is that node's set of direct parent node ids; a
    * node with no parents maps to an empty set
    */
   public Map<String, Set<String>> getDirectParentNodeIds(String[] nodeIds);

   /**
    * Batch-resolves the direct child node ids for each of the given nodes in a single query.
    *
    * @param nodeIds the nodes to resolve direct children for
    * @return a map keyed by node id whose value is that node's set of direct child node ids; a node
    * with no children maps to an empty set
    */
   public Map<String, Set<String>> getDirectChildNodeIds(String[] nodeIds);

   /**
    * Finds the enabled nodes in a hierarchy whose title matches one of the given titles, grouped by
    * title. A node's title typically holds an entity reference (e.g. a site reference), so this is
    * the lookup used to resolve references to the nodes that represent them.
    *
    * @param hierarchyId a unique string which identifies this hierarchy
    * @param titles the node titles to look up
    * @return a map of title -&gt; list of node ids that carry that title; titles with no matching
    * (enabled) node are absent from the map
    */
   public Map<String, List<String>> getNodesByTitles(String hierarchyId, String[] titles);

   /**
    * Finds the structural leaf nodes of a hierarchy that should be pruned: enabled, non-root nodes
    * that have no children and do not represent a site. Used to clean up empty branches left behind
    * after their site nodes are removed.
    *
    * @param hierarchyId a unique string which identifies this hierarchy
    * @return the ids of the empty (childless) non-site nodes, or an empty list if none
    */
   public List<String> getEmptyNonSiteNodes(String hierarchyId);

}
