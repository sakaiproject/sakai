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
package org.sakaiproject.hierarchy.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * Simple utils to assist with working with the hierarchy
 */
public class HierarchyUtils {

   /**
    * Create a sorted list of nodes based on a set of input nodes,
    * list goes from root (or highest parent) down to the bottom most node
    */
   public static List<HierarchyNode> getSortedNodes(Collection<HierarchyNode> nodes) {
      List<HierarchyNode> sortedNodes = new ArrayList<>();
      for (HierarchyNode hierarchyNode : nodes) {
         if (sortedNodes.isEmpty()) {
            sortedNodes.add(hierarchyNode);
         } else {
            int i;
            for (i = 0; i < sortedNodes.size(); i++) {
               HierarchyNode sortedNode = sortedNodes.get(i);
               if (isAncestor(hierarchyNode, sortedNode)) {
                  break;
               }
            }
            sortedNodes.add(i, hierarchyNode);
         }
      }
      return sortedNodes;
   }

   /**
    * Create a set of all the unique child node ids based on the set of supplied nodes
    */
   public static Set<String> getUniqueChildNodes(Collection<HierarchyNode> nodes, boolean includeSuppliedNodeIds, boolean directOnly) {
      Set<String> s = new HashSet<>();
      for (HierarchyNode hierarchyNode : nodes) {
         if (includeSuppliedNodeIds) {
            s.add(hierarchyNode.getId().toString());
         }
         if (directOnly) {
            hierarchyNode.getChildren().forEach(n -> s.add(n.getId().toString()));
         } else {
            collectDescendantIds(hierarchyNode, s);
         }
      }
      return s;
   }

   /**
    * Create a set of all the unique parent node ids based on the set of supplied nodes
    */
   public static Set<String> getUniqueParentNodes(Collection<HierarchyNode> nodes, boolean includeSuppliedNodeIds, boolean directOnly) {
      Set<String> s = new HashSet<>();
      for (HierarchyNode hierarchyNode : nodes) {
         if (includeSuppliedNodeIds) {
            s.add(hierarchyNode.getId().toString());
         }
         if (directOnly) {
            hierarchyNode.getParents().forEach(n -> s.add(n.getId().toString()));
         } else {
            collectAncestorIds(hierarchyNode, s);
         }
      }
      return s;
   }

   private static boolean isAncestor(HierarchyNode potentialAncestor, HierarchyNode node) {
      for (HierarchyNode parent : node.getParents()) {
         if (parent.getId().equals(potentialAncestor.getId())) return true;
         if (isAncestor(potentialAncestor, parent)) return true;
      }
      return false;
   }

   private static void collectDescendantIds(HierarchyNode node, Set<String> ids) {
      for (HierarchyNode child : node.getChildren()) {
         ids.add(child.getId().toString());
         collectDescendantIds(child, ids);
      }
   }

   private static void collectAncestorIds(HierarchyNode node, Set<String> ids) {
      for (HierarchyNode parent : node.getParents()) {
         ids.add(parent.getId().toString());
         collectAncestorIds(parent, ids);
      }
   }
}
