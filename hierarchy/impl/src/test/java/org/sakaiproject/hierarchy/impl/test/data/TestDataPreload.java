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

package org.sakaiproject.hierarchy.impl.test.data;

import javax.annotation.PostConstruct;

import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.model.HierarchyNodePermission;
import org.sakaiproject.hierarchy.repository.HierarchyNodePermissionRepository;
import org.sakaiproject.hierarchy.repository.HierarchyNodeRepository;

import lombok.Setter;

/**
 * Contains test data for preloading and test constants
 */
public class TestDataPreload {

   @Setter public HierarchyNodeRepository nodeRepository;
   @Setter public HierarchyNodePermissionRepository permissionRepository;

   @PostConstruct
   public void init() {
      preloadTestData();
   }

   public final static String USER_ID = "user-11111111";
   public final static String USER_DISPLAY = "Aaron Zeckoski";
   public final static String ACCESS_USER_ID = "access-2222222";
   public final static String ACCESS_USER_DISPLAY = "Regular User";
   public final static String MAINT_USER_ID = "maint-33333333";
   public final static String MAINT_USER_DISPLAY = "Maint User";
   public final static String ADMIN_USER_ID = "admin";
   public final static String ADMIN_USER_DISPLAY = "Administrator";
   public final static String INVALID_USER_ID = "invalid-UUUUUU";

   public final static String PERM_TOKEN_1 = "tokenKey1";
   public final static String PERM_TOKEN_2 = "tokenKey2";
   public final static String INVALID_PERM_TOKEN = "invalid-permtoken";

   public final static String LOCATION1_ID = "/site/ref-1111111";
   public final static String LOCATION1_TITLE = "Location 1 title";
   public final static String LOCATION2_ID = "/site/ref-22222222";
   public final static String LOCATION2_TITLE = "Location 2 title";
   public final static String INVALID_LOCATION_ID = "invalid-LLLLLLLL";

   public final static String HIERARCHYA = "hierarchyA";
   public final static String HIERARCHYB = "hierarchyB";
   public final static String INVALID_HIERARCHY = "hierarchy-invalid";
   public final static String INVALID_NODE_ID = "invalid-nodeID";

   public final static String PERM_ONE = "permission.one";
   public final static String PERM_TWO = "permission.two";
   public final static String PERM_THREE = "permission.three";

   // Hierarchy A nodes (root=1, children=2,3,4, grandchildren=5,6,7,8)
   public HierarchyNode pNode1;
   public HierarchyNode pNode2;
   public HierarchyNode pNode3;
   public HierarchyNode pNode4;
   public HierarchyNode pNode5;
   public HierarchyNode pNode6;
   public HierarchyNode pNode7;
   public HierarchyNode pNode8;

   // Hierarchy B nodes (root=9, orphan=11 with no parents, child=10 with 2 parents)
   public HierarchyNode pNode9;
   public HierarchyNode pNode10;
   public HierarchyNode pNode11;

   public HierarchyNode node1;
   public HierarchyNode node2;
   public HierarchyNode node3;
   public HierarchyNode node4;
   public HierarchyNode node5;
   public HierarchyNode node6;
   public HierarchyNode node7;
   public HierarchyNode node8;
   public HierarchyNode node9;
   public HierarchyNode node10;
   public HierarchyNode node11;

   public HierarchyNodePermission nodePerm1;
   public HierarchyNodePermission nodePerm2;
   public HierarchyNodePermission nodePerm3;
   public HierarchyNodePermission nodePerm4;
   public HierarchyNodePermission nodePerm5;
   public HierarchyNodePermission nodePerm6;
   public HierarchyNodePermission nodePerm7;
   public HierarchyNodePermission nodePerm8;
   public HierarchyNodePermission nodePerm9;
   public HierarchyNodePermission nodePerm10;
   public HierarchyNodePermission nodePerm11;
   public HierarchyNodePermission nodePerm12;
   public HierarchyNodePermission nodePerm13;

   public boolean preloaded = false;

   public void preloadTestData() {
      // Pass 1: save all nodes with scalar fields (no relationships)
      pNode1  = newNode(HIERARCHYA, Boolean.TRUE,  USER_ID, "Univ of AZ",              null, null,         Boolean.FALSE);
      pNode2  = newNode(HIERARCHYA, Boolean.FALSE, USER_ID, "College of Engineering",  null, PERM_TOKEN_1, Boolean.FALSE);
      pNode3  = newNode(HIERARCHYA, Boolean.FALSE, USER_ID, "College of Arts",         null, PERM_TOKEN_1, Boolean.FALSE);
      pNode4  = newNode(HIERARCHYA, Boolean.FALSE, USER_ID, "College of Science",      null, PERM_TOKEN_2, Boolean.FALSE);
      pNode5  = newNode(HIERARCHYA, Boolean.FALSE, USER_ID, "Dept of Art",             null, PERM_TOKEN_1, Boolean.FALSE);
      pNode6  = newNode(HIERARCHYA, Boolean.FALSE, USER_ID, "Dept of Math",            null, null,         Boolean.FALSE);
      pNode7  = newNode(HIERARCHYA, Boolean.FALSE, USER_ID, "Dept of Physics",         null, null,         Boolean.FALSE);
      pNode8  = newNode(HIERARCHYA, Boolean.FALSE, USER_ID, "Dept of Biology",         null, null,         Boolean.FALSE);
      pNode9  = newNode(HIERARCHYB, Boolean.TRUE,  USER_ID, "Univ of BZ",              null, null,         Boolean.FALSE);
      pNode10 = newNode(HIERARCHYB, Boolean.FALSE, USER_ID, "College of BZ",           null, PERM_TOKEN_1, Boolean.FALSE);
      pNode11 = newNode(HIERARCHYB, Boolean.FALSE, USER_ID, "Provost of BZ",           null, null,         Boolean.FALSE);

      nodeRepository.save(pNode1);
      nodeRepository.save(pNode2);
      nodeRepository.save(pNode3);
      nodeRepository.save(pNode4);
      nodeRepository.save(pNode5);
      nodeRepository.save(pNode6);
      nodeRepository.save(pNode7);
      nodeRepository.save(pNode8);
      nodeRepository.save(pNode9);
      nodeRepository.save(pNode10);
      nodeRepository.save(pNode11);

      // Pass 2: wire up parent/child relationships using actual entity references
      // HIERARCHYA: pNode2,3,4 are direct children of pNode1
      pNode2.getParents().add(pNode1);
      nodeRepository.save(pNode2);
      pNode3.getParents().add(pNode1);
      nodeRepository.save(pNode3);
      pNode4.getParents().add(pNode1);
      nodeRepository.save(pNode4);
      // pNode5 is a direct child of pNode3
      pNode5.getParents().add(pNode3);
      nodeRepository.save(pNode5);
      // pNode6,7,8 are direct children of pNode4
      pNode6.getParents().add(pNode4);
      nodeRepository.save(pNode6);
      pNode7.getParents().add(pNode4);
      nodeRepository.save(pNode7);
      pNode8.getParents().add(pNode4);
      nodeRepository.save(pNode8);
      // HIERARCHYB: pNode10 has two parents: pNode9 and pNode11
      pNode10.getParents().add(pNode9);
      pNode10.getParents().add(pNode11);
      nodeRepository.save(pNode10);

      // node references are the same entities (no DTO conversion needed)
      node1  = pNode1;
      node2  = pNode2;
      node3  = pNode3;
      node4  = pNode4;
      node5  = pNode5;
      node6  = pNode6;
      node7  = pNode7;
      node8  = pNode8;
      node9  = pNode9;
      node10 = pNode10;
      node11 = pNode11;

      nodePerm1  = new HierarchyNodePermission(MAINT_USER_ID,  pNode2.getId().toString(),  PERM_ONE);
      nodePerm2  = new HierarchyNodePermission(USER_ID,        pNode3.getId().toString(),  PERM_TWO);
      nodePerm3  = new HierarchyNodePermission(MAINT_USER_ID,  pNode3.getId().toString(),  PERM_TWO);
      nodePerm4  = new HierarchyNodePermission(MAINT_USER_ID,  pNode4.getId().toString(),  PERM_ONE);
      nodePerm5  = new HierarchyNodePermission(MAINT_USER_ID,  pNode5.getId().toString(),  PERM_TWO);
      nodePerm6  = new HierarchyNodePermission(ACCESS_USER_ID, pNode5.getId().toString(),  PERM_ONE);
      nodePerm7  = new HierarchyNodePermission(MAINT_USER_ID,  pNode6.getId().toString(),  PERM_ONE);
      nodePerm8  = new HierarchyNodePermission(USER_ID,        pNode6.getId().toString(),  PERM_TWO);
      nodePerm9  = new HierarchyNodePermission(MAINT_USER_ID,  pNode7.getId().toString(),  PERM_ONE);
      nodePerm10 = new HierarchyNodePermission(ACCESS_USER_ID, pNode7.getId().toString(),  PERM_ONE);
      nodePerm11 = new HierarchyNodePermission(MAINT_USER_ID,  pNode8.getId().toString(),  PERM_ONE);
      nodePerm12 = new HierarchyNodePermission(ACCESS_USER_ID, pNode8.getId().toString(),  PERM_TWO);
      nodePerm13 = new HierarchyNodePermission(MAINT_USER_ID,  pNode2.getId().toString(),  PERM_TWO);

      permissionRepository.save(nodePerm1);
      permissionRepository.save(nodePerm2);
      permissionRepository.save(nodePerm3);
      permissionRepository.save(nodePerm4);
      permissionRepository.save(nodePerm5);
      permissionRepository.save(nodePerm6);
      permissionRepository.save(nodePerm7);
      permissionRepository.save(nodePerm8);
      permissionRepository.save(nodePerm9);
      permissionRepository.save(nodePerm10);
      permissionRepository.save(nodePerm11);
      permissionRepository.save(nodePerm12);
      permissionRepository.save(nodePerm13);

      preloaded = true;
   }

   private static HierarchyNode newNode(String hierarchyId, Boolean isRootNode, String ownerId,
           String title, String description, String permToken, Boolean isDisabled) {
      HierarchyNode node = new HierarchyNode();
      node.setHierarchyId(hierarchyId);
      node.setIsRootNode(isRootNode != null ? isRootNode : Boolean.FALSE);
      node.setOwnerId(ownerId);
      node.setTitle(title);
      node.setDescription(description);
      node.setPermToken(permToken);
      node.setIsDisabled(isDisabled != null ? isDisabled : Boolean.FALSE);
      return node;
   }
}
