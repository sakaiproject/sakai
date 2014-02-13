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

import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * Contains test data for preloading and test constants
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TestDataPreload {

   /**
    * this permission does not translate
    */
   public final static String PERM_TOKEN_1 = "tokenKey1";
   /**
    * this permission should translate downward
    */
   public final static String PERM_TOKEN_2 = "tokenKey2";
   public final static String INVALID_PERM_TOKEN = "invalid-permtoken";

   // testing constants
   public final static String HIERARCHYA = "hierarchyA";
   public final static String HIERARCHYB = "hierarchyB";
   public final static String INVALID_HIERARCHY = "hierarchy-invalid";
   public final static String INVALID_NODE_ID = "invalid-nodeID";

   // testing data objects here
   public HierarchyNode node1 = new HierarchyNode("1", HIERARCHYA, "Univ of AZ", null, toSet(""), toSet(""), toSet("2,3,4"), toSet("2,3,4,5,6,7,8"), Boolean.FALSE );
   public HierarchyNode node2 = new HierarchyNode("2", HIERARCHYA, "College of Engineering", PERM_TOKEN_1, toSet("1"), toSet("1"), toSet(""), toSet(""), Boolean.FALSE );
   public HierarchyNode node3 = new HierarchyNode("3", HIERARCHYA, "College of Arts", PERM_TOKEN_1, toSet("1"), toSet("1"), toSet("5"), toSet("5"), Boolean.FALSE );
   public HierarchyNode node4 = new HierarchyNode("4", HIERARCHYA, "College of Science", PERM_TOKEN_2, toSet("1"), toSet("1"), toSet("6,7,8"), toSet("6,7,8"), Boolean.FALSE );
   public HierarchyNode node5 = new HierarchyNode("5", HIERARCHYA, "Dept of Art", PERM_TOKEN_1, toSet("3"), toSet("1,3"), toSet(""), toSet(""), Boolean.FALSE );
   public HierarchyNode node6 = new HierarchyNode("6", HIERARCHYA, "Dept of Math", null, toSet("4"), toSet("1,4"), toSet(""), toSet(""), Boolean.FALSE );
   public HierarchyNode node7 = new HierarchyNode("7", HIERARCHYA, "Dept of Physics", null, toSet("4"), toSet("1,4"), toSet(""), toSet(""), Boolean.FALSE );
   public HierarchyNode node8 = new HierarchyNode("8", HIERARCHYA, "Dept of Biology", null, toSet("4"), toSet("1,4"), toSet(""), toSet(""), Boolean.FALSE );
   public HierarchyNode node9 = new HierarchyNode("9", HIERARCHYB, "Univ of BZ", null, toSet(""), toSet(""), toSet("10"), toSet("10"), Boolean.FALSE );
   public HierarchyNode node10 = new HierarchyNode("10", HIERARCHYB, "College of BZ", PERM_TOKEN_1, toSet("9,11"), toSet("9,11"), toSet(""), toSet(""), Boolean.FALSE );
   public HierarchyNode node11 = new HierarchyNode("11", HIERARCHYB, "Provost of BZ", null, toSet(""), toSet(""), toSet("10"), toSet("10"), Boolean.FALSE );


   /**
    * Encode the comma delimited list of nodes to a set
    * @param commaDelimitedNums
    * @return sorted set
    */
   private Set<String> toSet(String commaDelimitedNums) {
      Set<String> s = new TreeSet<String>();
      if (commaDelimitedNums != null && !commaDelimitedNums.equals("")) {
         String[] split = commaDelimitedNums.split(",");
         for (int i = 0; i < split.length; i++) {
            s.add( split[i] );
         }
      }
      return s;
   }

}
