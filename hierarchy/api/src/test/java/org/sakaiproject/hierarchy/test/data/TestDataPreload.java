/******************************************************************************
 * TestDataPreload.java - created by Sakai App Builder -AZ
 *
 * Copyright (c) 2006 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 *
 * A copy of the Educational Community License has been included in this
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 *****************************************************************************/

package org.sakaiproject.hierarchy.test.data;

import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * Contains test data for preloading and test constants
 */
public class TestDataPreload {

   public final static String PERM_TOKEN_1 = "tokenKey1";
   public final static String PERM_TOKEN_2 = "tokenKey2";
   public final static String INVALID_PERM_TOKEN = "invalid-permtoken";

   public final static String HIERARCHYA = "hierarchyA";
   public final static String HIERARCHYB = "hierarchyB";
   public final static String INVALID_HIERARCHY = "hierarchy-invalid";
   public final static String INVALID_NODE_ID = "invalid-nodeID";

   // Hierarchy A: root=1, children=2,3,4, grandchildren=5 (under 3), 6,7,8 (under 4)
   public HierarchyNode node1;
   public HierarchyNode node2;
   public HierarchyNode node3;
   public HierarchyNode node4;
   public HierarchyNode node5;
   public HierarchyNode node6;
   public HierarchyNode node7;
   public HierarchyNode node8;
   // Hierarchy B: root=9, orphan=11, child=10 (parents: 9 and 11)
   public HierarchyNode node9;
   public HierarchyNode node10;
   public HierarchyNode node11;

   public TestDataPreload() {
      node1  = node(1L,  HIERARCHYA, true,  "Univ of AZ",             null);
      node2  = node(2L,  HIERARCHYA, false, "College of Engineering",  PERM_TOKEN_1);
      node3  = node(3L,  HIERARCHYA, false, "College of Arts",         PERM_TOKEN_1);
      node4  = node(4L,  HIERARCHYA, false, "College of Science",      PERM_TOKEN_2);
      node5  = node(5L,  HIERARCHYA, false, "Dept of Art",             PERM_TOKEN_1);
      node6  = node(6L,  HIERARCHYA, false, "Dept of Math",            null);
      node7  = node(7L,  HIERARCHYA, false, "Dept of Physics",         null);
      node8  = node(8L,  HIERARCHYA, false, "Dept of Biology",         null);
      node9  = node(9L,  HIERARCHYB, true,  "Univ of BZ",              null);
      node10 = node(10L, HIERARCHYB, false, "College of BZ",           PERM_TOKEN_1);
      node11 = node(11L, HIERARCHYB, false, "Provost of BZ",           null);

      // Wire parent/child relationships (both sides for in-memory use)
      addChild(node1, node2);
      addChild(node1, node3);
      addChild(node1, node4);
      addChild(node3, node5);
      addChild(node4, node6);
      addChild(node4, node7);
      addChild(node4, node8);
      addChild(node9, node10);
      addChild(node11, node10);
   }

   private static HierarchyNode node(Long id, String hierarchyId, boolean isRoot, String title, String permToken) {
      HierarchyNode n = new HierarchyNode();
      n.setId(id);
      n.setHierarchyId(hierarchyId);
      n.setIsRootNode(isRoot);
      n.setTitle(title);
      n.setPermToken(permToken);
      n.setIsDisabled(false);
      return n;
   }

   private static void addChild(HierarchyNode parent, HierarchyNode child) {
      parent.getChildren().add(child);
      child.getParents().add(parent);
   }
}
